package controller.analysis.extraction.coupling.statically.platforms.java

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.TypeDeclaration
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.resolution.Resolvable
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.javaparser.utils.ProjectRoot
import controller.analysis.extraction.Platform
import controller.analysis.extraction.coupling.statically.StaticAnalysisExtractor
import kotlinx.coroutines.runBlocking
import model.graph.*
import model.graph.Unit
import utility.ArchiveExtractor
import utility.mapConcurrently
import utility.toNullable
import java.io.File
import java.util.*
import com.github.javaparser.ast.Node as AstNode


class JavaStaticCouplingExtractor(projectName: String, private val basePackageIdentifier: String, private val archive: File) : StaticAnalysisExtractor() {
    private val basePath: String = buildBasePath(PlatformIdentifier, projectName)
    private val staticAnalysisBasePath = "$basePath/$StaticAnalysisDirectory"
    private val unarchiver = ArchiveExtractor(".$JavaFileExtension", staticAnalysisBasePath)

    override fun extract(): Graph {
        val unarchivedDirectory: File = unarchiver.unpackAnalysisArchive(archive)

        val invocations: Set<Pair<String, String>> = extractInvocations(unarchivedDirectory)

        val graph: Graph = convertInvocationPairsToGraph(invocations)

        cleanup(staticAnalysisBasePath)

        return graph
    }

    override fun normalizeUnit(unit: Unit): Unit = Unit(identifier = unit.identifier, packageIdentifier = unit.packageIdentifier)

    private fun extractInvocations(unarchivedDirectory: File): Set<Pair<String, String>> {
        val projectRoot: ProjectRoot = SymbolSolverCollectionStrategy().collect(unarchivedDirectory.toPath())
        return projectRoot.sourceRoots
            .flatMap { it.tryToParseParallelized() }
            .mapNotNull { it.result.toNullable() }
            .flatMap { it.types }
            .mapNotNull { it as? ClassOrInterfaceDeclaration }  // TODO: Should Enum's also be included?
            .map { Pair(it, retrieveCallClasses(it)) }
            .flatMap { retrieveCallPairs(it) }
            .toSet()
    }

    private fun retrieveCallClasses(declaration: ClassOrInterfaceDeclaration): List<Resolvable<ResolvedType>> {
        return (retrieveSuperTypes(declaration) + retrieveReferencedTypes(declaration)).mapNotNull { it as? Resolvable<ResolvedType> }
    }

    private fun retrieveCallPairs(classReferences: Pair<TypeDeclaration<*>, List<Resolvable<ResolvedType>>>): List<Pair<String, String>> {
        val classIdentifier: String = classReferences.first.fullyQualifiedName.get()
        val referencedTypes: List<String> = runBlocking { classReferences.second.mapConcurrently { resolveType(it) }.filterNotNull().map { it.describe() } }
        return referencedTypes.filter { it.startsWith(basePackageIdentifier) }.filter { it != classIdentifier }.map { Pair(classIdentifier, it) }
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

    private fun convertInvocationPairsToGraph(invokations: Set<Pair<String, String>>): Graph {
        val graph = Graph()

        for (invocation in invokations) {
            val startUnit = Unit(identifier = invocation.first.substringAfterLast('.'), packageIdentifier = invocation.first.substringBeforeLast('.'))
            val endUnit = Unit(identifier = invocation.second.substringAfterLast('.'), packageIdentifier = invocation.second.substringBeforeLast('.'))
            val edge = Edge(start = startUnit, end = endUnit, attributes = EdgeAttributes())

            if (startUnit == endUnit) continue

            graph.addOrUpdateEdge(edge)
        }

        graph.nodes.map { attachUnitFootprint(it) }.forEach { graph.addOrUpdateNode(it) }

        return graph
    }

    private fun attachUnitFootprint(node: Node): Node {
        node.attributes.footprint = UnitFootprint(
            byteSize = calculateUnitFootprint(node.unit)
        )
        return node
    }

    private fun calculateUnitFootprint(unit: Unit): Long {
        return File(staticAnalysisBasePath).walk()
            .filter { it.isFile }
            .filter { it.extension == JavaFileExtension }
            .find { it.path.replace("/", ".").substringAfterLast(basePackageIdentifier) == "${unit.packageIdentifier.substringAfterLast(basePackageIdentifier)}.${unit.identifier}.$JavaFileExtension" }
            ?.length()
            ?: 0
    }

    companion object {
        private val PlatformIdentifier: String = Platform.JAVA.toString().toLowerCase()
        private const val JavaFileExtension = "java"
    }
}
