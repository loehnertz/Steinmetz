package controller.analysis.extraction


interface ExtractorCompanion {
    fun getArchiveUploadPath(): String

    companion object {
        const val ExtractionBasePath = "/tmp/steinmetz"
    }
}
