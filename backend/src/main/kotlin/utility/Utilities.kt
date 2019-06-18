package utility

import java.io.File


object Utilities {
    fun getResourceAsText(path: String): File {
        var fullPath: String = path
        if (!path.startsWith('/')) fullPath = "/$fullPath"
        return File(object {}.javaClass.getResource(fullPath).file)
    }

    fun extractTsv(tsvFile: File): List<List<String>> {
        return tsvFile.readLines().map { it.split("\t") }
    }
}
