package controller.analysis.extraction.coupling.statically.platforms.java

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.resolution.Resolvable
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.javaparser.utils.ProjectRoot
import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.Platform
import controller.analysis.extraction.coupling.statically.StaticAnalysisExtractor
import model.graph.*
import model.graph.Unit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utility.ArchiveExtractor
import utility.Utilities
import utility.toNullable
import java.io.File
import java.util.*
import com.github.javaparser.ast.Node as AstNode


class JavaStaticCouplingExtractor(projectName: String, private val basePackageIdentifier: String, private val archive: File) : StaticAnalysisExtractor() {
    private val logger: Logger = LoggerFactory.getLogger(JavaStaticCouplingExtractor::class.java)

    private val basePath: String = buildBasePath(PlatformIdentifier, projectName)
    private val staticAnalysisBasePath = "$basePath/$StaticAnalysisDirectory"
    private val unarchiver = ArchiveExtractor(".$JavaFileExtension", staticAnalysisBasePath)
    private val potentialUnitFiles: List<File> = File(staticAnalysisBasePath).walk().filter { it.isFile }.filter { it.extension == JavaFileExtension }.toList()

    override fun extract(): Graph {
        val unarchivedDirectory: File = unarchiver.unpackAnalysisArchive(archive)

        val referencePairs: Set<Pair<String, String>> = extractReferencePairs(unarchivedDirectory).also { logger.info("Extracted ${it.size} static coupling pairs") }

        val graph: Graph = convertReferencePairsToGraph(referencePairs)

        cleanup(staticAnalysisBasePath)

        return graph.also { logger.info("Constructed static coupling graph") }
    }

    override fun normalizeUnit(unit: Unit): Unit = AbstractExtractor.normalizeUnit(unit)

    private fun extractReferencePairs(unarchivedDirectory: File): Set<Pair<String, String>> {
        val projectRoot: ProjectRoot = SymbolSolverCollectionStrategy().collect(unarchivedDirectory.toPath())
        projectRoot.sourceRoots.forEach { sourceRoot ->
            sourceRoot.parserConfiguration.isStoreTokens = false
            sourceRoot.parserConfiguration.isAttributeComments = false
            sourceRoot.parserConfiguration.isIgnoreAnnotationsWhenAttributingComments = false
        }

        return projectRoot.sourceRoots
            .filter { !it.root.toString().contains(JavaTestDirectory) }
            .flatMap { it.tryToParse() }
            .mapNotNull { it.result.toNullable() }
            .also { logger.info("Parsed ASTs") }
            .flatMap { it.types }
            .mapNotNull { it as? ClassOrInterfaceDeclaration }  // TODO: Should Enum's also be included?
            .filter { isLegalUnit(it.fullyQualifiedName.get()) }
            .map { it to retrieveCallClasses(it) }
            .flatMap { retrieveCallPairs(it) }
            .also { logger.info("Retrieved call pairs") }
            .toSet()
            .also { JavaParserFacade.clearInstances() }
            .also { System.gc() }
            .also { logger.info("Cleared parsing caches") }
    }

    private fun convertReferencePairsToGraph(referencePairs: Set<Pair<String, String>>): Graph {
        val graph = Graph()

        for (referencePair in referencePairs) {
            val startUnit = Unit(identifier = referencePair.first.substringAfterLast('.'), packageIdentifier = referencePair.first.substringBeforeLast('.'))
            val endUnit = Unit(identifier = referencePair.second.substringAfterLast('.'), packageIdentifier = referencePair.second.substringBeforeLast('.'))
            val edge = Edge(start = startUnit, end = endUnit, attributes = EdgeAttributes())

            if (startUnit == endUnit) continue

            graph.addOrUpdateEdge(edge)
        }

        graph.nodes.map { attachUnitFootprint(it) }.forEach { graph.addOrUpdateNode(it) }

        return graph
    }

    private fun retrieveCallClasses(declaration: ClassOrInterfaceDeclaration): List<Resolvable<ResolvedType>> {
        return (retrieveSuperTypes(declaration) + retrieveReferencedTypes(declaration)).mapNotNull { it as? Resolvable<ResolvedType> }
    }

    private fun retrieveCallPairs(classReferences: Pair<TypeDeclaration<*>, List<Resolvable<ResolvedType>>>): List<Pair<String, String>> {
        val classIdentifier: String = classReferences.first.fullyQualifiedName.get()
        val referencedTypes: List<String> = classReferences.second.mapNotNull { resolveType(it) }.map { it.describe() }
        return referencedTypes.filter { isLegalUnit(it) }.filter { it != classIdentifier }.map { classIdentifier to it }.also { checkToFreeMemory() }
    }

    private fun retrieveSuperTypes(declaration: ClassOrInterfaceDeclaration): Set<ClassOrInterfaceType> {
        return (declaration.extendedTypes + declaration.implementedTypes).toSet()
    }

    private fun retrieveReferencedTypes(node: AstNode): Set<ClassOrInterfaceType> {
        val types: HashSet<ClassOrInterfaceType> = hashSetOf()
        val queue: ArrayDeque<AstNode> = ArrayDeque<AstNode>().also { it.add(node) }

        while (queue.isNotEmpty()) {
            val element: AstNode = queue.pop()
            if (element is ClassOrInterfaceType) types.add(element)
            element.childNodes.forEach { queue.add(it) }
        }

        return types
    }

    private fun resolveType(type: Resolvable<ResolvedType>): ResolvedType? {
        return try {
            type.resolve()
        } catch (e: Exception) {
            null
        }
    }

    private fun checkToFreeMemory() {
        if (Utilities.freeMemoryPercentage() <= LowMemoryPercentageThreshold) {
            JavaParserFacade.clearInstances()
            System.gc()
        }
    }

    private fun attachUnitFootprint(node: Node): Node {
        node.attributes.footprint = UnitFootprint(
            byteSize = calculateUnitByteFootprint(node.unit)
        )
        return node
    }

    private fun calculateUnitByteFootprint(unit: Unit): Long {
        return potentialUnitFiles.find { it.path.replace("/", ".").substringAfterLast(basePackageIdentifier) == "${unit.packageIdentifier.substringAfterLast(basePackageIdentifier)}.${unit.identifier}.$JavaFileExtension" }?.length()
               ?: potentialUnitFiles.find { "${unit.packageIdentifier.substringAfterLast(basePackageIdentifier)}.${unit.identifier}".contains(it.path.replace("/", ".").substringAfterLast(basePackageIdentifier).removeSuffix(".$JavaFileExtension")) }?.length()
               ?: DefaultUnitFootprintByteSize
    }

    private fun isLegalUnit(identifier: String): Boolean {
        return isLegalUnit(identifier, basePackageIdentifier) && !identifier.substringAfterLast('.').contains(JavaGenericsStatement)
    }

    companion object {
        private val PlatformIdentifier: String = Platform.JAVA.toString().toLowerCase()
        private const val JavaFileExtension = "java"
        private const val JavaTestDirectory = "/test/"
        private val JavaGenericsStatement = Regex("[<>]")
        private const val LowMemoryPercentageThreshold = 15
    }
}
