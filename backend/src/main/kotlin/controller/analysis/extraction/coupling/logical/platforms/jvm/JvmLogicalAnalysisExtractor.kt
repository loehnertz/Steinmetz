package controller.analysis.extraction.coupling.logical.platforms.jvm

import controller.analysis.extraction.coupling.logical.LogicalAnalysisExtractor
import controller.analysis.extraction.coupling.logical.VcsSystem
import model.graph.Edge
import model.graph.EdgeAttributes
import model.graph.Graph
import model.graph.Unit
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*


class JvmLogicalAnalysisExtractor(private val vcsSystem: VcsSystem, private val basePackageIdentifier: String, private val vcsLogFile: File) : LogicalAnalysisExtractor() {
    private val pathBasedBasePackageIdentifier: String = basePackageIdentifier.replace('.', '/')

    override fun extract(): Graph {
        val output: ArrayList<String> = arrayListOf()
        val processBuilder: ProcessBuilder = ProcessBuilder("java", "-jar", "backend/src/main/resources/code-maat.jar", "-l", vcsLogFile.absolutePath, "-c", vcsSystem.toString().toLowerCase(), "-a", "coupling", "-n", MinRevisions.toString(), "-m", MinSharedRevisions.toString(), "-i", MinCouplingScore.toString(), "-t", DaysToCombineCommits.toString()).also { it.redirectErrorStream(true) }
        val process: Process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String = reader.readLine()
        while (reader.ready()) {
            output.add(line)
            line = reader.readLine()
        }
        process.waitFor()
        output.removeAt(0)

        cleanup(vcsLogFile.absolutePath)

        return convertOutputToGraph(output)
    }

    private fun convertOutputToGraph(output: ArrayList<String>): Graph {
        val edges: List<Edge> = output.mapNotNull { parseOutputLine(it) }
        return Graph(edges = edges.toMutableSet())
    }

    private fun parseOutputLine(line: String): Edge? {
        try {
            val (start: String, end: String, degree: String, _: String) = line.split(',')

            if (!start.endsWith(FileExtension) || !end.endsWith(FileExtension)) return null
            if (!start.contains(pathBasedBasePackageIdentifier) || !end.contains(pathBasedBasePackageIdentifier)) return null

            val startUnit: Unit = convertPathToUnit(stripAwayBasePath(start))
            val endUnit: Unit = convertPathToUnit(stripAwayBasePath(end))

            if (!startUnit.packageIdentifier.startsWith(basePackageIdentifier) || !endUnit.packageIdentifier.startsWith(basePackageIdentifier)) return null

            return Edge(start = startUnit, end = endUnit, attributes = EdgeAttributes(logicalCouplingScore = degree.toInt(), dynamicCouplingScore = 0))
        } catch (exception: Exception) {
            return null
        }
    }

    private fun convertPathToUnit(path: String): Unit {
        val identifier: String = path.replace('/', '.').removeSuffix(FileExtension)
        return Unit(identifier = identifier.substringAfterLast('.'), packageIdentifier = "$basePackageIdentifier.${identifier.substringBeforeLast('.')}")
    }

    private fun stripAwayBasePath(path: String): String {
        return path.substringAfter("$pathBasedBasePackageIdentifier/")
    }

    companion object {
        private const val FileExtension = ".java"
        private const val MinRevisions = 2
        private const val MinSharedRevisions = 2
        private const val MinCouplingScore = 1
        private const val DaysToCombineCommits = 1
    }
}
