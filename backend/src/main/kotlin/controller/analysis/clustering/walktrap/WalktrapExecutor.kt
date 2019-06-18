package controller.analysis.clustering.walktrap

import utility.Utilities
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*


class WalktrapExecutor(private val inputFile: File, private val iterations: Int) {
    fun execute(): String {
        val process: Process = Runtime.getRuntime().exec(buildCommand(inputFile, iterations)).also { it.waitFor() }
        return Scanner(process.inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next()
    }

    private fun buildCommand(inputFile: File, amountOfClusters: Int): String {
        return "${retrieveExecutablePath()} ${inputFile.absolutePath} $WalktrapBaseCommand -p$amountOfClusters"
    }

    private fun retrieveExecutablePath(): String {
        return Utilities.getResourceAsText(ExecutableName).absolutePath
    }

    companion object {
        private const val ExecutableName = "executables/walktrap"
        private const val WalktrapBaseCommand = "-s"
    }
}