package controller.analysis.clustering.mcl

import utility.Utilities
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8


class MclExecutor(private val inputFile: File, private val inflationValue: Double) {
    fun execute(): String {
        val process: Process = Runtime.getRuntime().exec(buildCommand(inflationValue))
        return String(process.inputStream.readAllBytes(), UTF_8)
    }

    private fun buildCommand(clusteringInflationValue: Double): String {
        return "${retrieveExecutablePath()} ${inputFile.absolutePath} $MclBaseCommand -I $clusteringInflationValue"
    }

    private fun retrieveExecutablePath(): String {
        return Utilities.getExternalExecutableAsFile(ExecutableName).absolutePath
    }

    companion object {
        private const val ExecutableName = "mcl"
        private const val MclBaseCommand = "--abc -o -"
    }
}
