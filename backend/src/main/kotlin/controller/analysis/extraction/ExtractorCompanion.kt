package controller.analysis.extraction


interface ExtractorCompanion {
    fun getWorkingDirectory(): String

    companion object {
        const val ExtractionBasePath = "/tmp/steinmetz"
    }
}
