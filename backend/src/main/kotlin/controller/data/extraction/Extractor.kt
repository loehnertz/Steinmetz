package controller.data.extraction


interface Extractor {
    fun getArchiveUploadPath(): String

    companion object {
        const val ExtractionBasePath = "/tmp/steinmetz"
    }
}
