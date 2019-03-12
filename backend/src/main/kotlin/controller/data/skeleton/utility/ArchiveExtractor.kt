package controller.data.skeleton.utility

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream


class ArchiveExtractor(private val sourceCodeFileExtension: String, private val destinationDirectory: String) {
    fun unpackAnalysisArchive(archive: File) {
        ZipInputStream(FileInputStream(archive)).use { input ->
            val buffer = ByteArray(1024)

            var entry = input.nextEntry
            while (entry != null) {
                if (entry.name.contains(sourceCodeFileExtension)) {
                    val newFile = File(destinationDirectory, entry.name)
                    File(newFile.parent).mkdirs()
                    val output = FileOutputStream(newFile)

                    var len = input.read(buffer)
                    while (len > 0) {
                        output.write(buffer, 0, len)
                        len = input.read(buffer)
                    }
                    output.close()
                }

                entry = input.nextEntry
            }
        }
    }
}
