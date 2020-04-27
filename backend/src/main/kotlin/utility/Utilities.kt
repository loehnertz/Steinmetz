package utility

import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.pow


object Utilities {
    private val externalExecutablePath: String = System.getenv("EXTERNAL_EXECUTABLE_PATH") ?: "backend/lib/executables/"

    fun getExternalExecutableAsFile(name: String): File {
        return File(externalExecutablePath + name).absoluteFile
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
            decimalNumber += (remainder * 2.0.pow(i.toDouble())).toInt()
            ++i
        }
        return decimalNumber
    }

    fun createCoroutineDispatcher(): ExecutorCoroutineDispatcher = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).asCoroutineDispatcher()

    fun heapSizeInMb(): Int = (Runtime.getRuntime().totalMemory() / 1e+6).toInt()

    fun freeMemoryPercentage(): Int = ((Runtime.getRuntime().freeMemory().toDouble() / Runtime.getRuntime().maxMemory().toDouble()) * 100).toInt()

    fun calculateRuntimeDuration(startTime: Long): Int = ((System.currentTimeMillis() - startTime) / 1000).toInt()
}
