package controller.analysis.extraction.coupling.semantically.platforms.java

import codes.jakob.semanticcoupling.SemanticCouplingCalculator
import codes.jakob.semanticcoupling.model.NaturalLanguage
import codes.jakob.semanticcoupling.model.ProgrammingLanguage
import codes.jakob.semanticcoupling.model.SemanticCoupling
import controller.analysis.extraction.AbstractExtractor
import controller.analysis.extraction.coupling.semantically.SemanticCouplingExtractor
import model.graph.Edge
import model.graph.EdgeAttributes
import model.graph.Graph
import model.graph.Unit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utility.ArchiveExtractor
import java.io.File


class JavaSemanticCouplingExtractor(private val projectName: String, private val basePackageIdentifier: String, private val sourceCodeFilesArchive: File, private val edgesToConsider: Set<Edge>) : SemanticCouplingExtractor() {
    private val logger: Logger = LoggerFactory.getLogger(JavaSemanticCouplingExtractor::class.java)

    private val unarchiverPath = "${getWorkingDirectory()}/sources/$projectName/"
    private val unarchiver = ArchiveExtractor(".$JavaFileExtension", unarchiverPath)

    override fun extract(): Graph {
        unarchiver.unpackAnalysisArchive(sourceCodeFilesArchive)

        val files: List<File> = retrieveSourceCodeFiles()
        val semanticCouplingCalculator: SemanticCouplingCalculator = setupSemanticCouplingCalculator(files)
        val semanticCouplings: List<SemanticCoupling> = semanticCouplingCalculator.calculate().also { logger.info("Calculated ${it.size} semantic coupling pairs") }
        val similarities: List<Triple<String, String, Double>> = semanticCouplings.map { Triple(it.documents.first().name, it.documents.last().name, it.score!!) }

        cleanup(unarchiverPath, sourceCodeFilesArchive.absolutePath)

        return Graph(edges = buildEdgesOutOfSimilarities(similarities).toMutableSet()).also { logger.info("Constructed semantic coupling graph") }
    }

    override fun normalizeUnit(unit: Unit): Unit = AbstractExtractor.normalizeUnit(unit)

    private fun buildEdgesOutOfSimilarities(similarities: List<Triple<String, String, Double>>): List<Edge> {
        val edges: ArrayList<Edge> = arrayListOf()

        for (similarity: Triple<String, String, Double> in similarities) {
            val start: String = similarity.first
            val end: String = similarity.second
            val semanticCouplingScore: Int = ((if (similarity.third < 0.0) 0.0 else similarity.third) * 100).toInt()

            val startUnit = Unit(identifier = start.substringAfterLast('.'), packageIdentifier = start.substringBeforeLast('.'))
            val endUnit = Unit(identifier = end.substringAfterLast('.'), packageIdentifier = end.substringBeforeLast('.'))
            val edgeAttributes = EdgeAttributes(semanticCouplingScore = semanticCouplingScore)

            edges.add(Edge(start = startUnit, end = endUnit, attributes = edgeAttributes))
        }

        return edges
    }

    private fun retrieveSourceCodeFiles(): List<File> {
        return File(unarchiverPath)
            .walk()
            .filter { it.isFile }
            .filter { it.extension == JavaFileExtension }
            .filter { it.name != PackageInfoFileName }
            .filter { isLegalUnit(convertFileNameToIdentifier(it.absolutePath), basePackageIdentifier) }
            .toList()
    }

    private fun setupSemanticCouplingCalculator(files: List<File>): SemanticCouplingCalculator {
        return SemanticCouplingCalculator(
            files = buildFileListMap(files),
            programmingLanguage = JavaEnumKey,
            naturalLanguage = EnglishLanguageEnumKey,
            fileSimilaritiesToCalculate = buildFilePairsToCalculate()
        ).also { it.doNotUseLsi() }
    }

    private fun buildFileListMap(files: List<File>): Map<String, String> {
        return files.map { convertFileNameToIdentifier(it.absolutePath) to it.readText() }.toMap()
    }

    private fun buildFilePairsToCalculate(): List<Set<String>> {
        return edgesToConsider.map { setOf(it.start.toString(), it.end.toString()) }
    }

    private fun convertFileNameToIdentifier(filePath: String): String {
        var identifer: String = filePath.replace('/', '.').substringAfterLast("$projectName.").substringAfterLast(basePackageIdentifier).replace(".$JavaFileExtension", "")
        if (basePackageIdentifier != WildcardBasePackageIdentifer) identifer = basePackageIdentifier + identifer
        return identifer
    }

    companion object {
        private const val JavaFileExtension = "java"
        private const val PackageInfoFileName = "package-info.java"
        private val JavaEnumKey = ProgrammingLanguage.JAVA
        private val EnglishLanguageEnumKey = NaturalLanguage.EN
    }
}
