package model.resource

import java.io.File


data class ProjectRequest(
        val projectName: String,
        val projectPlatform: String,
        val basePackageIdentifier: String,
        val staticAnalysisFile: File,
        val dynamicAnalysisFile: File
)
