package controller.analysis.extraction.coupling.statically.platforms.jvm

import controller.analysis.extraction.Platform
import controller.analysis.extraction.coupling.statically.StaticAnalysisExtractor
import controller.analysis.extraction.coupling.statically.StaticAnalysisExtractorCompanion
import controller.analysis.extraction.graph.UnitContainerExtractor
import model.graph.Graph
import model.graph.Node
import model.graph.Unit
import model.graph.UnitFootprint
import model.skeleton.UnitContainer
import utility.ArchiveExtractor
import java.io.File
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource


// TODO: Refactor this into something less hacky
class JvmBytecodeExtractor(private val projectName: String, private val basePackageIdentifier: String, private val archive: File) : StaticAnalysisExtractor() {
    private val basePath: String = buildBasePath(platformIdentifier, projectName)
    private val staticAnalysisBasePath = "$basePath/$StaticAnalysisDirectory"
    private val unarchiver = ArchiveExtractor(".$classFileExtension", "$staticAnalysisBasePath/$unarchiverPath")

    override fun extract(): Graph {
        unarchiver.unpackAnalysisArchive(archive)

        Runtime.getRuntime().exec("java -jar backend/src/main/resources/jpeek.jar --sources $staticAnalysisBasePath/$unarchiverPath/${buildBasePackagePath()} --target $staticAnalysisBasePath/$SkeletonDirectoryName/jpeek --overwrite").waitFor()

        val convertedSkeletonXml: String = convertSkeletonXml(File("$staticAnalysisBasePath/$SkeletonDirectoryName/jpeek/skeleton.xml")).readText()
        val unitContainer: UnitContainer = UnitContainerExtractor.extract(convertedSkeletonXml)
        val graph: Graph = convertUnitContainerToGraph(unitContainer, basePackageIdentifier)
        graph.nodes.map { attachUnitFootprint(it) }.forEach { graph.addOrUpdateNode(it) }
        cleanup(staticAnalysisBasePath)

        return graph
    }

    private fun convertSkeletonXml(skeletonFile: File): File {
        val xmlInput = StreamSource(skeletonFile)
        val xsl = StreamSource(File("backend/src/main/resources/jpeek-skeleton.xsl"))
        val outputFile = File("$staticAnalysisBasePath/$SkeletonDirectoryName/$projectName-processed.xml")
        outputFile.parentFile.mkdirs()
        outputFile.createNewFile()
        val xmlOutput = StreamResult(outputFile)

        val transformer: Transformer = TransformerFactory.newInstance().newTransformer(xsl)
        transformer.transform(xmlInput, xmlOutput)

        return outputFile
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

    private fun buildBasePackagePath(): String {
        return basePackageIdentifier.replace('.', '/')
    }

    companion object : StaticAnalysisExtractorCompanion {
        private val platformIdentifier: String = Platform.JAVA.toString().toLowerCase()
        private const val unarchiverPath = "jar"
        private const val classFileExtension = "class"
        private const val innerUnitDelimiter = '$'

        override fun normalizeUnit(unit: Unit): Unit = Unit(identifier = unit.identifier.substringBeforeLast(innerUnitDelimiter), packageIdentifier = unit.packageIdentifier)
    }
}
