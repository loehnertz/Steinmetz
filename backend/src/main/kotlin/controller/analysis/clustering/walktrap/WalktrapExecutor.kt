package controller.analysis.clustering.walktrap

import utility.Utilities
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*


class WalktrapExecutor(private val inputFile: File, private val iterations: Int) {
    fun execute(): String {
        val process: Process = Runtime.getRuntime().exec(buildCommand()).also { it.waitFor() }
        return Scanner(process.inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next()
    }

    private fun buildCommand(): String {
        return "${retrieveExecutablePath()} ${inputFile.absolutePath} $WalktrapBaseCommand -p$iterations"
    }

    private fun retrieveExecutablePath(): String {
        return Utilities.getExternalExecutableAsFile(ExecutableName).absolutePath
    }

    companion object {
        private const val ExecutableName = "walktrap"
        private const val WalktrapBaseCommand = "-s"
    }
}
