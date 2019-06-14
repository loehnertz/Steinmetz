package controller.analysis.clustering.mcl

import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.*


class MclExecutor(private val input: String, private val inflationValue: Double) {
    fun execute(): String {
        val process: Process = Runtime.getRuntime().exec(buildCommand(inflationValue))
        BufferedWriter(OutputStreamWriter(process.outputStream)).use { writer -> writer.write(input); writer.flush() }
        process.waitFor()
        return Scanner(process.inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next()
    }

    private fun buildCommand(clusteringInflationValue: Double): String {
        return "${retrieveExecutablePath()} $MclBaseCommand -I $clusteringInflationValue"
    }

    private fun retrieveExecutablePath(): String {
        return "backend/src/main/resources/$ExecutableName"
    }

    companion object {
        private const val ExecutableName = "mcl"
        private const val MclBaseCommand = "- --abc -o -"
    }
}
