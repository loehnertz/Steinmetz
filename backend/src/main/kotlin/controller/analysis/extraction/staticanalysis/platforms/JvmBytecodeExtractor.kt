package controller.analysis.extraction.staticanalysis.platforms

import controller.analysis.extraction.graph.GraphInserter
import controller.analysis.extraction.graph.UnitContainerExtractor
import controller.analysis.extraction.staticanalysis.StaticAnalysisExtractor
import model.graph.Graph
import utility.ArchiveExtractor
import java.io.File
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource


// TODO: Refactor this into something less hacky
class JvmBytecodeExtractor(private val projectName: String, private val basePackageIdentifier: String, private val archive: File) : StaticAnalysisExtractor() {
    private val basePath = buildBasePath(platformIdentifier, projectName)
    private val staticAnalysisBasePath = "$basePath/$StaticAnalysisDirectory"
    private val unarchiver = ArchiveExtractor(".class", "$staticAnalysisBasePath/$unarchiverPath")

    override fun extract(): Graph {
        unarchiver.unpackAnalysisArchive(archive)

        Runtime.getRuntime().exec("java -jar backend/src/main/resources/jpeek.jar --sources $staticAnalysisBasePath/$unarchiverPath/${buildBasePackagePath()} --target $staticAnalysisBasePath/$SkeletonDirectoryName/jpeek --overwrite").waitFor()

        val convertedSkeletonXml = convertSkeletonXml(File("$staticAnalysisBasePath/$SkeletonDirectoryName/jpeek/skeleton.xml")).readText()
        cleanup(staticAnalysisBasePath)
        val unitContainer = UnitContainerExtractor.extract(convertedSkeletonXml)

        return convertUnitContainerToGraph(unitContainer, basePackageIdentifier)
    }

    private fun convertSkeletonXml(skeletonFile: File): File {
        val xmlInput = StreamSource(skeletonFile)
        val xsl = StreamSource(File("backend/src/main/resources/jpeek-skeleton.xsl"))
        val outputFile = File("$staticAnalysisBasePath/$SkeletonDirectoryName/$projectName-processed.xml")
        outputFile.parentFile.mkdirs()
        outputFile.createNewFile()
        val xmlOutput = StreamResult(outputFile)

        val transformer = TransformerFactory.newInstance().newTransformer(xsl)
        transformer.transform(xmlInput, xmlOutput)

        return outputFile
    }

    private fun buildBasePackagePath(): String {
        return basePackageIdentifier.replace('.', '/')
    }

    companion object {
        private const val platformIdentifier = GraphInserter.JvmProjectKey
        private const val unarchiverPath = "jar"
    }
}
