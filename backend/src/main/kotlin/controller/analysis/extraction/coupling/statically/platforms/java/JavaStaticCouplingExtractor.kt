package controller.analysis.extraction.coupling.statically.platforms.java

import com.github.javaparser.ast.AccessSpecifier.*
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.resolution.Resolvable
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedMethodLikeDeclaration
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.javaparser.utils.ProjectRoot
import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.Platform
import controller.analysis.extraction.coupling.statically.AbstractStaticAnalysisExtractor
import controller.analysis.extraction.coupling.statically.ResponseForAClassIdentifiers
import controller.analysis.extraction.coupling.statically.ResponseForAClassMetrics
import controller.analysis.extraction.coupling.statically.ResponseForAClassPair
import model.graph.*
import model.graph.Unit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utility.*
import java.io.File
import java.util.*
import com.github.javaparser.ast.Node as AstNode


class JavaStaticCouplingExtractor(projectName: String, private val basePackageIdentifier: String, private val archive: File) : AbstractStaticAnalysisExtractor() {
    private val logger: Logger = LoggerFactory.getLogger(JavaStaticCouplingExtractor::class.java)

    private val basePath: String = buildBasePath(PlatformIdentifier, projectName)
    private val staticAnalysisBasePath = "$basePath/$StaticAnalysisDirectory"
    private val unarchiver = ArchiveExtractor(".$JavaFileExtension", staticAnalysisBasePath)

    override fun extract(): Graph {
        val unarchivedDirectory: File = unarchiver.unpackAnalysisArchive(archive)

        val referencePairs: Set<Pair<ClassDeclaration, ClassDeclaration>> = extractReferencePairs(unarchivedDirectory).also { logger.info("Extracted ${it.size} static coupling pairs") }

        val graph: Graph = convertReferencePairsToGraph(referencePairs)

        cleanup(staticAnalysisBasePath).also { checkToFreeMemory() }

        return graph.also { logger.info("Constructed static coupling graph") }
    }

    override fun normalizeUnit(unit: Unit): Unit = AbstractExtractor.normalizeUnit(unit)

    private fun extractReferencePairs(unarchivedDirectory: File): Set<Pair<ClassDeclaration, ClassDeclaration>> {
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
            .also { checkToFreeMemory() }
            .flatMap { it.types }
            .mapNotNull { it as? ClassOrInterfaceDeclaration }
            .filter { isLegalUnit(it.fullyQualifiedName.get()) }
            .map { ClassDeclaration(it) to retrieveReferencedTypes(it) }
            .also { logger.info("Retrieved referenced types") }
            .map { retrieveCallPairs(it) }
            .also { logger.info("Retrieved call pairs") }
            .flatten()
            .toSet()
    }

    private fun convertReferencePairsToGraph(referencePairs: Set<Pair<ClassDeclaration, ClassDeclaration>>): Graph {
        val graph = Graph()

        for (referencePair: Pair<ClassDeclaration, ClassDeclaration> in referencePairs) {
            val responseForAClassIdentifiers = ResponseForAClassIdentifiers(referencePair.first.identifier, referencePair.second.identifier)
            val responseForAClassCouplingScore: Int = responseForAClassPairMap[responseForAClassIdentifiers]?.calculateCouplingPercentage() ?: 0

            val startUnit = Unit(identifier = referencePair.first.identifier.substringAfterLast('.'), packageIdentifier = referencePair.first.identifier.substringBeforeLast('.'))
            val endUnit = Unit(identifier = referencePair.second.identifier.substringAfterLast('.'), packageIdentifier = referencePair.second.identifier.substringBeforeLast('.'))

            if (!isLegalPair(startUnit, endUnit)) continue

            try {
                val edge = Edge(start = startUnit, end = endUnit, attributes = EdgeAttributes(staticCouplingScore = responseForAClassCouplingScore))
                graph.addOrUpdateEdge(edge)
            } catch (e: IllegalStateException) {
                logger.info("Edge from '$startUnit' to '$endUnit' has illegal static coupling score '$responseForAClassCouplingScore' and will be excluded")
                continue
            }
        }

        val classDeclarations: Set<ClassDeclaration> = referencePairs.flatMap { listOf(it.first, it.second) }.filter { it.characters != null }.toSet()
        graph.nodes.map { attachUnitFootprint(it, classDeclarations) }.forEach { graph.addOrUpdateNode(it) }

        return graph
    }

    private fun retrieveCallPairs(classReferences: Pair<ClassDeclaration, Map<ResolvedReferenceTypeDeclaration, ResponseForAClassMetrics>>): List<Pair<ClassDeclaration, ClassDeclaration>> {
        classReferences.second.forEach { updateResponseForClassMetrics(classReferences.first, it) }
        return classReferences.second
            .map { it.key }
            .filter { it.qualifiedName != classReferences.first.identifier }
            .map { classReferences.first to ClassDeclaration(it) }
            .also { logger.debug("Retrieved the call pairs of class ${classReferences.first.identifier}") }
    }

    private fun updateResponseForClassMetrics(caller: ClassDeclaration, callee: Map.Entry<ResolvedReferenceTypeDeclaration, ResponseForAClassMetrics>) {
        val referencedTypeIdentifier: String = callee.key.qualifiedName

        if (caller.identifier == referencedTypeIdentifier) return

        val responseForAClassIdentifiers = ResponseForAClassIdentifiers(classAIdentifier = caller.identifier, classBIdentifier = referencedTypeIdentifier)
        val responseForAClassPair: ResponseForAClassPair? = responseForAClassPairMap[responseForAClassIdentifiers]
        if (responseForAClassPair == null) {
            val classAMetrics = ResponseForAClassMetrics(accessibleMethods = countAccessibleMethodLikeDeclarations(callee.key, caller.declaration!!))
            val classBMetrics: ResponseForAClassMetrics = callee.value
            responseForAClassPairMap[responseForAClassIdentifiers] = ResponseForAClassPair(classIdentifiers = responseForAClassIdentifiers, classAMetrics = classAMetrics, classBMetrics = classBMetrics)
        } else {
            responseForAClassPair.updateClassMetrics(referencedTypeIdentifier, callee.value)
        }
    }

    private fun retrieveReferencedTypes(caller: ClassOrInterfaceDeclaration): Map<ResolvedReferenceTypeDeclaration, ResponseForAClassMetrics> {
        val callerType: ResolvedReferenceTypeDeclaration = caller.resolve()!!
        val objectCreations: MutableSet<ObjectCreationExpr> = mutableSetOf()
        val methodCalls: MutableSet<MethodCallExpr> = mutableSetOf()
        val queue: ArrayDeque<AstNode> = ArrayDeque<AstNode>().also { it.add(caller) }

        while (queue.isNotEmpty()) {
            val node: AstNode = queue.pop()
            when (node) {
                is ObjectCreationExpr -> objectCreations.add(node);
                is MethodCallExpr     -> methodCalls.add(node);
            }
            node.childNodes.forEach { queue.add(it) }
        }

        return (objectCreations.map { resolve(it to it.scope.toNullable()) } + methodCalls.map { resolve(it to it.scope.toNullable()) })
            .asSequence()
            .filterNotNull()
            .distinctBy { it.second.signature }
            .filter { isLegalUnit(it.first.qualifiedName) }
            .groupBy({ it.first }, { it.second })
            .map { it.key to ResponseForAClassMetrics(invokedMethods = it.value.count(), accessibleMethods = countAccessibleMethodLikeDeclarations(callerType, it.key)) }
            .toMap()
            .also { logger.debug("Resolved the referenced types of class ${caller.nameAsString}") }
    }

    private fun <T : ResolvedMethodLikeDeclaration> resolve(resolvable: Pair<Resolvable<T>, Expression?>): Pair<ResolvedReferenceTypeDeclaration, ResolvedMethodLikeDeclaration>? {
        val declaration: ResolvedMethodLikeDeclaration? = resolveDeclaration(resolvable.first)
        val declaringType: ResolvedReferenceTypeDeclaration? = if (resolvable.second != null) {
            resolveType(resolvable.second!!)
        } else {
            declaration?.declaringType()
        }

        if (declaration == null || declaringType == null) return null
        return declaringType to declaration
    }

    private fun <T : ResolvedMethodLikeDeclaration> resolveDeclaration(resolvable: Resolvable<T>): ResolvedMethodLikeDeclaration? {
        return try {
            resolvable.resolve()
        } catch (e: Throwable) {
            null
        }
    }

    private fun resolveType(resolvable: Expression): ResolvedReferenceTypeDeclaration? {
        return try {
            resolvable.calculateResolvedType()?.asReferenceType()?.typeDeclaration
        } catch (e: Throwable) {
            null
        }
    }

    private fun countAccessibleMethodLikeDeclarations(caller: ResolvedReferenceTypeDeclaration, callee: ResolvedReferenceTypeDeclaration): Int {
        return callee.allMethods
            .map { it.declaration as ResolvedMethodLikeDeclaration }
            .plus(callee.constructors.map { it as ResolvedMethodLikeDeclaration })
            .filter { !JavaObjectClassMethodsSignatures.contains(it.signature) }
            .count { methodIsAccessibleByCaller(it, caller, callee) }
    }

    private fun methodIsAccessibleByCaller(method: ResolvedMethodLikeDeclaration, caller: ResolvedReferenceTypeDeclaration, callee: ResolvedReferenceTypeDeclaration): Boolean {
        return when (method.accessSpecifier()!!) {
            PUBLIC          -> true
            PACKAGE_PRIVATE -> packagePrivateMethodIsAccessibleByCaller(method, caller, callee)
            PROTECTED       -> protectedMethodIsAccessibleByCaller(method, caller, callee)
            PRIVATE         -> false
        }
    }

    private fun packagePrivateMethodIsAccessibleByCaller(method: ResolvedMethodLikeDeclaration, caller: ResolvedReferenceTypeDeclaration, callee: ResolvedReferenceTypeDeclaration): Boolean {
        if (callee.isInterface) return true
        return method.packageName == caller.packageName
    }

    private fun protectedMethodIsAccessibleByCaller(method: ResolvedMethodLikeDeclaration, caller: ResolvedReferenceTypeDeclaration, callee: ResolvedReferenceTypeDeclaration): Boolean {
        if (packagePrivateMethodIsAccessibleByCaller(method, caller, callee)) return true
        return try {
            method.declaringType().isAssignableBy(caller)
        } catch (e: UnsolvedSymbolException) {
            false
        }
    }

    private fun checkToFreeMemory() {
        val freeMemoryPercentage: Int = Utilities.freeMemoryPercentage()
        if (freeMemoryPercentage <= LowMemoryPercentageThreshold) {
            logger.info("Low free heap space percentage ($freeMemoryPercentage%) triggered forced garbage collection")
            JavaParserFacade.clearInstances()
            System.gc()
        }
    }

    private fun attachUnitFootprint(node: Node, classDeclarations: Set<ClassDeclaration>): Node {
        val size: Long? = classDeclarations.find { it.identifier == node.unit.toString() }?.characters?.toLong()
        if (size != null) node.attributes.footprint = UnitFootprint(characters = size)
        return node
    }

    private fun isLegalUnit(identifier: String): Boolean {
        return isLegalUnit(identifier, basePackageIdentifier) && !identifier.substringAfterLast('.').contains(JavaGenericsStatement) && !identifier.contains('-')
    }

    private fun isLegalPair(unitA: Unit, unitB: Unit): Boolean {
        if (unitA == unitB) return false
        if (unitA.toString().substringBeforeLast('.') == unitB.toString()) return false
        if (unitB.toString().substringBeforeLast('.') == unitA.toString()) return false
        return true
    }

    companion object {
        private val PlatformIdentifier: String = Platform.JAVA.toString().toLowerCase()
        private const val JavaFileExtension = "java"
        private const val JavaTestDirectory = "/test/"
        private val JavaGenericsStatement = Regex("[<>]")
        private val JavaObjectClassMethodsSignatures: Set<String> = setOf("clone()", "equals(java.lang.Object)", "finalize()", "getClass()", "hashCode()", "notify()", "notifyAll()", "toString()", "wait()", "wait(long)", "wait(long, int)")
        private const val LowMemoryPercentageThreshold = 15
    }

    internal data class ClassDeclaration(
        val identifier: String,
        val characters: Int? = null,
        val declaration: ResolvedReferenceTypeDeclaration? = null
    ) {
        constructor(declaration: ClassOrInterfaceDeclaration) : this(identifier = declaration.fullyQualifiedName.get(), characters = declaration.toString().countJavaSourceCharacters(), declaration = declaration.resolve())
        constructor(declaration: ResolvedReferenceTypeDeclaration) : this(identifier = declaration.qualifiedName, characters = declaration.retrieveAstNode()?.toString()?.countJavaSourceCharacters(), declaration = declaration)
    }
}
