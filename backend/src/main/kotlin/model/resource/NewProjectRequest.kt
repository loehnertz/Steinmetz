package model.resource

import java.io.File


data class NewProjectRequest(
        val projectName: String,
        val projectPlatform: String,
        val basePackageIdentifier: String,
        val staticAnalysisArchive: File,
        val dynamicAnalysisArchive: File
)
