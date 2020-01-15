package controller.analysis.extraction.coupling.evolutionary.platforms.jvm

import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.Platform
import controller.analysis.extraction.coupling.evolutionary.EvolutionaryCouplingExtractor
import controller.analysis.extraction.coupling.evolutionary.VcsSystem
import model.graph.Edge
import model.graph.EdgeAttributes
import model.graph.Graph
import model.graph.Unit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utility.Utilities
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8


class JvmEvolutionaryCouplingExtractor(private val vcsSystem: VcsSystem, private val basePackageIdentifier: String, private val vcsLogFile: File) : EvolutionaryCouplingExtractor() {
    private val logger: Logger = LoggerFactory.getLogger(JvmEvolutionaryCouplingExtractor::class.java)

    private val pathBasedBasePackageIdentifier: String = basePackageIdentifier.replace('.', '/')

    override fun extract(): Graph {
        val processBuilder: ProcessBuilder = ProcessBuilder("java", "-jar", Utilities.getExternalExecutableAsFile(ExecutableName).absolutePath, "-l", vcsLogFile.absolutePath, "-c", vcsSystem.toString().toLowerCase(), "-a", "coupling", "-n", MinRevisions.toString(), "-m", MinSharedRevisions.toString(), "-i", MinCouplingScore.toString(), "-t", DaysToCombineCommits.toString()).also { it.redirectErrorStream(true) }
        val process: Process = processBuilder.start()
        val output: List<String> = String(process.inputStream.readAllBytes(), UTF_8).split("\n").drop(1).also { logger.info("Calculated evolutionary coupling") }

        cleanup(vcsLogFile.absolutePath)

        return convertOutputToGraph(output).also { logger.info("Constructed evolutionary coupling graph") }
    }

    override fun normalizeUnit(unit: Unit): Unit = AbstractExtractor.normalizeUnit(unit)

    private fun convertOutputToGraph(output: List<String>): Graph {
        val edges: MutableSet<Edge> = output.mapNotNull { parseOutputLine(it) }.toMutableSet().also { logger.info("Extracted ${it.size} evolutionary coupling pairs") }
        return Graph(edges = edges)
    }

    private fun parseOutputLine(line: String): Edge? {
        return try {
            val (start: String, end: String, degree: String, _: String) = line.split(',')

            if (!start.endsWith(FileExtension) || !end.endsWith(FileExtension)) return null
            if (basePackageIdentifier != WildcardBasePackageIdentifer && (!start.contains(pathBasedBasePackageIdentifier) || !end.contains(pathBasedBasePackageIdentifier))) return null

            val startUnit: Unit = convertPathToUnit(stripAwayBasePath(start))
            val endUnit: Unit = convertPathToUnit(stripAwayBasePath(end))

            if (!isLegalUnit(startUnit.packageIdentifier, basePackageIdentifier) || !isLegalUnit(endUnit.packageIdentifier, basePackageIdentifier)) return null

            Edge(start = startUnit, end = endUnit, attributes = EdgeAttributes(evolutionaryCouplingScore = degree.toInt()))
        } catch (exception: Exception) {
            null
        }
    }

    private fun convertPathToUnit(path: String): Unit {
        val identifier: String = path.replace('/', '.').removeSuffix(FileExtension).substringAfterLast("$PlatformIdentifier.")
        return Unit(identifier = identifier.substringAfterLast('.'), packageIdentifier = identifier.substringBeforeLast('.'))
    }

    private fun stripAwayBasePath(path: String): String {
        if (basePackageIdentifier == WildcardBasePackageIdentifer) return path
        val basePath: String = path.substringBefore(pathBasedBasePackageIdentifier)
        return path.substringAfter(basePath)
    }

    companion object {
        private val PlatformIdentifier: String = Platform.JAVA.toString().toLowerCase()
        private const val ExecutableName = "code-maat.jar"
        private const val FileExtension = ".java"
        private const val MinRevisions = 1
        private const val MinSharedRevisions = 1
        private const val MinCouplingScore = 1
        private const val DaysToCombineCommits = 1
    }
}
