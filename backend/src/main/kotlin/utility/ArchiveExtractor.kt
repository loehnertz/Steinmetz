package utility

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class ArchiveExtractor(private val fileExtension: String, private val destinationDirectory: String) {
    fun unpackAnalysisArchive(archive: File) {
        ZipInputStream(FileInputStream(archive)).use { input ->
            val buffer = ByteArray(1024)

            var entry: ZipEntry? = input.nextEntry
            while (entry != null) {
                if (entry.name.contains(fileExtension)) {
                    val newFile = File(destinationDirectory, entry.name)
                    File(newFile.parent).mkdirs()
                    val output = FileOutputStream(newFile)

                    var length: Int = input.read(buffer)
                    while (length > 0) {
                        output.write(buffer, 0, length)
                        length = input.read(buffer)
                    }
                    output.close()
                }

                entry = input.nextEntry
            }
        }
    }
}
