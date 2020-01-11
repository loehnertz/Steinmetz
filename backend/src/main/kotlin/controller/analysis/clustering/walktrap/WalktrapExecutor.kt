package controller.analysis.clustering.walktrap

import utility.Utilities
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8


class WalktrapExecutor(private val inputFile: File, private val iterations: Int) {
    fun execute(): String {
        val process: Process = Runtime.getRuntime().exec(buildCommand())
        return String(process.inputStream.readAllBytes(), UTF_8)
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
