package controller.data.skeleton.jvm

import controller.data.graph.GraphInserter
import controller.data.skeleton.SkeletonExtractor
import controller.data.skeleton.utility.ArchiveExtractor
import java.io.File
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource


// TODO: Refactor this into something less hacky
class JvmBytecodeExtractor(private val projectName: String, private val basePackageIdentifier: String, private val archive: File) : SkeletonExtractor() {
    private val basePath = buildBasePath(platformIdentifier, projectName)
    private val staticAnalysisBasePath = "$basePath/$staticAnalysisDirectory"
    private val unarchiver = ArchiveExtractor(".class", staticAnalysisBasePath)

    override fun extract(): String {
        unarchiver.unpackAnalysisArchive(archive)

        Runtime.getRuntime().exec("java -jar backend/src/main/resources/jpeek.jar --sources $staticAnalysisBasePath/${buildBasePackagePath()} --target $staticAnalysisBasePath/$skeletonDirectoryName/jpeek --overwrite").waitFor()

        val convertedSkeletonXml = convertSkeletonXml(File("$staticAnalysisBasePath/$skeletonDirectoryName/jpeek/skeleton.xml")).readText()
        cleanup(staticAnalysisBasePath)
        return convertedSkeletonXml
    }

    private fun convertSkeletonXml(skeletonFile: File): File {
        val xmlInput = StreamSource(skeletonFile)
        val xsl = StreamSource(File("backend/src/main/resources/jpeek-skeleton.xsl"))
        val outputFile = File("$staticAnalysisBasePath/$skeletonDirectoryName/$projectName-processed.xml")
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
