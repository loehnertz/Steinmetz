package controller.analysis.extraction.coupling.semantic.platforms.java

import codes.jakob.semanticcoupling.SemanticCouplingCalculator
import codes.jakob.semanticcoupling.model.NaturalLanguage
import codes.jakob.semanticcoupling.model.ProgrammingLanguage
import controller.analysis.extraction.coupling.semantic.SemanticCouplingExtractor
import model.graph.Edge
import model.graph.EdgeAttributes
import model.graph.Graph
import model.graph.Unit
import utility.ArchiveExtractor
import java.io.File


class JavaSemanticCouplingExtractor(projectName: String, private val basePackageIdentifier: String, private val sourceCodeFilesArchive: File, private val edgesToConsider: Set<Edge>) : SemanticCouplingExtractor() {
    private val unarchiverPath = "${getWorkingDirectory()}/sources/$projectName/"
    private val unarchiver = ArchiveExtractor(FileExtension, unarchiverPath)

    override fun extract(): Graph {
        unarchiver.unpackAnalysisArchive(sourceCodeFilesArchive)

        val files: List<File> = retrieveSourceCodeFiles()
        val semanticCouplingCalculator: SemanticCouplingCalculator = setupSemanticCouplingCalculator(files).also { it.calculate() }
        val similarities: List<Triple<String, String, Double>> = semanticCouplingCalculator.retrieveSimilaritiesAsListOfTriples()

        cleanup(unarchiverPath, sourceCodeFilesArchive.absolutePath)

        return Graph(edges = buildEdgesOutOfSimilarities(similarities).toMutableSet())
    }

    private fun buildEdgesOutOfSimilarities(similarities: List<Triple<String, String, Double>>): List<Edge> {
        val edges: ArrayList<Edge> = arrayListOf()

        for (similarity: Triple<String, String, Double> in similarities) {
            val start: String = similarity.first
            val end: String = similarity.second
            val semanticCouplingScore: Int = (similarity.third * 100).toInt()

            val startUnit = Unit(identifier = start.substringAfterLast('.'), packageIdentifier = start.substringBeforeLast('.'))
            val endUnit = Unit(identifier = end.substringAfterLast('.'), packageIdentifier = end.substringBeforeLast('.'))
            val edgeAttributes = EdgeAttributes(semanticCouplingScore = semanticCouplingScore)

            edges.add(Edge(start = startUnit, end = endUnit, attributes = edgeAttributes))
        }

        return edges
    }

    private fun retrieveSourceCodeFiles(): List<File> {
        return File(unarchiverPath)
                .walkTopDown()
                .filter { it.isFile }
                .filter { it.name.endsWith(FileExtension) }
                .filter { it.name != PackageInfoFileName }
                .toList()
    }

    private fun setupSemanticCouplingCalculator(files: List<File>): SemanticCouplingCalculator {
        return SemanticCouplingCalculator(
                files = buildFileListMap(files),
                programmingLanguage = JavaEnumKey,
                naturalLanguage = EnglishLanguageEnumKey,
                fileSimilaritiesToCalculate = buildFilePairsToCalculate()
        ).also { it.useStemming() }
    }

    private fun buildFilePairsToCalculate(): List<Pair<String, String>> {
        return edgesToConsider.map { Pair(it.start.toString(), it.end.toString()) }
    }

    private fun buildFileListMap(files: List<File>): List<Map<String, String>> {
        return files.map { mapOf(convertFileNameToIdentifier(it.absolutePath) to it.readText()) }
    }

    private fun convertFileNameToIdentifier(filePath: String): String {
        return "$basePackageIdentifier${filePath.replace('/', '.').substringAfter(basePackageIdentifier).replace(FileExtension, "")}"
    }

    companion object {
        private const val FileExtension = ".java"
        private const val PackageInfoFileName = "package-info.java"
        private val JavaEnumKey = ProgrammingLanguage.JAVA
        private val EnglishLanguageEnumKey = NaturalLanguage.EN
    }
}
