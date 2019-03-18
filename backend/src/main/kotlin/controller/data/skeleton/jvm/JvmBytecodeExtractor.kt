package controller.data.skeleton.jvm

import controller.data.skeleton.utility.ArchiveExtractor
import java.io.File
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource


// TODO: Refactor this into something less hacky
class JvmBytecodeExtractor(private val projectName: String, private val archive: File) {
    private val unarchiver = ArchiveExtractor(".class", "$unarchiverBasePath/$projectName")

    fun extract(): File {
        unarchiver.unpackAnalysisArchive(archive)

        Runtime.getRuntime().exec("java -jar backend/src/main/resources/jpeek.jar --sources $unarchiverBasePath/$projectName --target $unarchiverBasePath/$projectName/$SkeletonDirectoryName/jpeek --overwrite").waitFor()

        val skeletonFile = File("$unarchiverBasePath/$projectName/$SkeletonDirectoryName/jpeek/skeleton.xml")
        return convertSkeletonXml(skeletonFile)
    }

    private fun convertSkeletonXml(skeletonFile: File): File {
        val xmlInput = StreamSource(skeletonFile)
        val xsl = StreamSource(File("backend/src/main/resources/jpeek-skeleton.xsl"))
        val outputFile = File("$unarchiverBasePath/$projectName/$SkeletonDirectoryName/$projectName-processed.xml")
        outputFile.createNewFile()
        val xmlOutput = StreamResult(outputFile)

        val transformer = TransformerFactory.newInstance().newTransformer(xsl)
        transformer.transform(xmlInput, xmlOutput)

        return outputFile
    }

    companion object {
        private const val SkeletonDirectoryName = "skeleton"
        private const val unarchiverBasePath = "/tmp/steinmetz/jvm"
    }
}
