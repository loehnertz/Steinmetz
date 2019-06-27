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

    fun convertBinaryToDecimal(binary: String): Int {
        var num: Long = binary.toLong()
        var decimalNumber = 0
        var i = 0
        var remainder: Long
        while (num.toInt() != 0) {
            remainder = num % 10
            num /= 10
            decimalNumber += (remainder * Math.pow(2.0, i.toDouble())).toInt()
            ++i
        }
        return decimalNumber
    }
}
