package controller.analysis.extraction.coupling.evolutionary.platforms.jvm

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.coupling.evolutionary.EvolutionaryCouplingExtractor
import controller.analysis.extraction.coupling.evolutionary.VcsSystem
import model.graph.Edge
import model.graph.EdgeAttributes
import model.graph.Graph
import model.graph.Unit
import utility.Utilities
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*


class JvmEvolutionaryCouplingExtractor(private val vcsSystem: VcsSystem, private val basePackageIdentifier: String, private val vcsLogFile: File) : EvolutionaryCouplingExtractor() {
    private val pathBasedBasePackageIdentifier: String = basePackageIdentifier.replace('.', '/')

    override fun extract(): Graph {
        val output: ArrayList<String> = arrayListOf()
        val processBuilder: ProcessBuilder = ProcessBuilder("java", "-jar", Utilities.getExternalExecutableAsFile(ExecutableName).absolutePath, "-l", vcsLogFile.absolutePath, "-c", vcsSystem.toString().toLowerCase(), "-a", "coupling", "-n", MinRevisions.toString(), "-m", MinSharedRevisions.toString(), "-i", MinCouplingScore.toString(), "-t", DaysToCombineCommits.toString()).also { it.redirectErrorStream(true) }
        val process: Process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String = reader.readLine()
        while (reader.ready()) {
            output.add(line)
            line = reader.readLine()
        }
        process.destroy().also { println("\tCalculated evolutionary coupling") }
        output.removeAt(0)  // Just contains the column names

        cleanup(vcsLogFile.absolutePath)

        return convertOutputToGraph(output).also { println("\tConstructed evolutionary coupling graph") }
    }

    override fun normalizeUnit(unit: Unit): Unit = AbstractExtractor.normalizeUnit(unit)

    private fun convertOutputToGraph(output: List<String>): Graph {
        val edges: List<Edge> = output.mapNotNull { parseOutputLine(it) }.also { println("\tExtracted ${it.size} evolutionary coupling pairs") }
        return Graph(edges = edges.toMutableSet())
    }

    private fun parseOutputLine(line: String): Edge? {
        return try {
            val (start: String, end: String, degree: String, _: String) = line.split(',')

            if (!start.endsWith(FileExtension) || !end.endsWith(FileExtension)) return null
            if (!start.contains(pathBasedBasePackageIdentifier) || !end.contains(pathBasedBasePackageIdentifier)) return null

            val startUnit: Unit = convertPathToUnit(stripAwayBasePath(start))
            val endUnit: Unit = convertPathToUnit(stripAwayBasePath(end))

            if (!startUnit.packageIdentifier.startsWith(basePackageIdentifier) || !endUnit.packageIdentifier.startsWith(basePackageIdentifier)) return null

            Edge(start = startUnit, end = endUnit, attributes = EdgeAttributes(evolutionaryCouplingScore = degree.toInt()))
        } catch (exception: Exception) {
            null
        }
    }

    private fun convertPathToUnit(path: String): Unit {
        val identifier: String = path.replace('/', '.').removeSuffix(FileExtension)
        return Unit(identifier = identifier.substringAfterLast('.'), packageIdentifier = identifier.substringBeforeLast('.'))
    }

    private fun stripAwayBasePath(path: String): String {
        val basePath: String = path.substringBefore(pathBasedBasePackageIdentifier)
        return path.substringAfter(basePath)
    }

    companion object {
        private const val ExecutableName = "code-maat.jar"
        private const val FileExtension = ".java"
        private const val MinRevisions = 1
        private const val MinSharedRevisions = 1
        private const val MinCouplingScore = 1
        private const val DaysToCombineCommits = 1
    }
}
