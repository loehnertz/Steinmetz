package utility

import java.io.File


object Utilities {
    private val externalExecutablePath: String = System.getenv("EXTERNAL_EXECUTABLE_PATH") ?: "backend/lib/executables/"

    fun getExternalExecutableAsFile(name: String): File {
        return File(externalExecutablePath + name).absoluteFile
    }

    fun extractTsv(tsvFile: File): List<List<String>> {
        return tsvFile.readLines().map { it.split("\t") }
    }
}
