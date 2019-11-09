package controller.analysis.extraction.coupling.statically.platforms.jvm

import controller.analysis.extraction.Platform
import controller.analysis.extraction.coupling.statically.StaticAnalysisExtractor
import controller.analysis.extraction.coupling.statically.StaticAnalysisExtractorCompanion
import gr.gousiosg.javacg.stat.ClassVisitor
import gr.gousiosg.javacg.stat.JCallGraph
import model.graph.*
import model.graph.Unit
import utility.ArchiveExtractor
import java.io.File
import java.util.regex.Pattern


class JvmBytecodeExtractor(projectName: String, private val basePackageIdentifier: String, private val archive: File) : StaticAnalysisExtractor() {
    private val basePath: String = buildBasePath(platformIdentifier, projectName)
    private val staticAnalysisBasePath = "$basePath/$StaticAnalysisDirectory"
    private val unarchiver = ArchiveExtractor(".$classFileExtension", "$staticAnalysisBasePath/$unarchiverPath")

    override fun extract(): Graph {
        unarchiver.unpackAnalysisArchive(archive)

        val jcallGraph: JCallGraph = JCallGraph(listOf(archive), listOf(Pattern.compile("$basePackageIdentifier.*"))).also { it.analyze() }
        val invokations: List<Pair<String, String>> = jcallGraph.classVisitors.flatMap { it.classInvokations }.map { it.split(' ') }.map { Pair(it.first().replace(ClassVisitor.PREFIX, ""), it.last()) }

        val graph: Graph = convertInvokationPairsToGraph(invokations)
        graph.nodes.map { attachUnitFootprint(it) }.forEach { graph.addOrUpdateNode(it) }

        cleanup(staticAnalysisBasePath)

        return mergeInnerUnitNodesWithParentNodes(graph)
    }

    override fun normalizeUnit(unit: Unit): Unit = JvmBytecodeExtractor.normalizeUnit(unit)

    private fun convertInvokationPairsToGraph(invokations: List<Pair<String, String>>): Graph {
        val graph = Graph()

        for (invokation: Pair<String, String> in invokations) {
            val startUnit = Unit(identifier = invokation.first.substringAfterLast('.'), packageIdentifier = invokation.first.substringBeforeLast('.'))
            val endUnit = Unit(identifier = invokation.second.substringAfterLast('.'), packageIdentifier = invokation.second.substringBeforeLast('.'))
            val edge = Edge(start = startUnit, end = endUnit, attributes = EdgeAttributes())

            if (startUnit == endUnit) continue

            graph.addOrUpdateEdge(edge)
        }

        return graph
    }

    private fun attachUnitFootprint(node: Node): Node {
        node.attributes.footprint = UnitFootprint(
            byteSize = calculateUnitFootprint(node.unit)
        )
        return node
    }

    private fun calculateUnitFootprint(unit: Unit): Long {
        return File("$staticAnalysisBasePath/$unarchiverPath/${unit.packageIdentifier.replace('.', '/')}/${unit.identifier}.$classFileExtension").length()
    }

    companion object : StaticAnalysisExtractorCompanion {
        private val platformIdentifier: String = Platform.JAVA.toString().toLowerCase()
        private const val unarchiverPath = "jar"
        private const val classFileExtension = "class"
        private const val innerUnitDelimiter = '$'

        override fun normalizeUnit(unit: Unit): Unit = Unit(identifier = unit.identifier.substringBeforeLast(innerUnitDelimiter), packageIdentifier = unit.packageIdentifier)
    }
}
