package controller.data.extraction.staticanalysis.jvm

import controller.data.extraction.graph.GraphInserter
import controller.data.extraction.staticanalysis.StaticAnalysisExtractor
import controller.data.extraction.staticanalysis.utility.ArchiveExtractor
import java.io.File
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource


// TODO: Refactor this into something less hacky
class JvmBytecodeExtractor(private val projectName: String, private val basePackageIdentifier: String, private val archive: File) : StaticAnalysisExtractor() {
    private val basePath = buildBasePath(platformIdentifier, projectName)
    private val staticAnalysisBasePath = "$basePath/$StaticAnalysisDirectory"
    private val unarchiver = ArchiveExtractor(".class", staticAnalysisBasePath)

    override fun extract(): String {
        unarchiver.unpackAnalysisArchive(archive)

        Runtime.getRuntime().exec("java -jar backend/src/main/resources/jpeek.jar --sources $staticAnalysisBasePath/${buildBasePackagePath()} --target $staticAnalysisBasePath/$SkeletonDirectoryName/jpeek --overwrite").waitFor()

        val convertedSkeletonXml = convertSkeletonXml(File("$staticAnalysisBasePath/$SkeletonDirectoryName/jpeek/skeleton.xml")).readText()
        cleanup(staticAnalysisBasePath)
        return convertedSkeletonXml
    }

    private fun convertSkeletonXml(skeletonFile: File): File {
        val xmlInput = StreamSource(skeletonFile)
        val xsl = StreamSource(File("backend/src/main/resources/jpeek-skeleton.xsl"))
        val outputFile = File("$staticAnalysisBasePath/$SkeletonDirectoryName/$projectName-processed.xml")
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
    }
}
