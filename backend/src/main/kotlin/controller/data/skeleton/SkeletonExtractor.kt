package controller.data.skeleton

import java.io.File


abstract class SkeletonExtractor {
    fun buildBasePath(platformIdentifier: String, projectName: String) = "$unarchiverBasePath/$platformIdentifier/$projectName"

    fun cleanup(basePath: String) = File(basePath).deleteRecursively()

    abstract fun extract(): String

    companion object {
        const val staticAnalysisDirectory = "static"
        const val skeletonDirectoryName = "skeleton"
        const val unarchiverBasePath = "/tmp/steinmetz"
    }
}
