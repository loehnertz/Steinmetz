package model.resource

import java.io.File


data class NewProjectRequest(
        val projectIdentifier: String,
        val projectPlatform: String,
        val staticAnalysisArchive: File,
        val dynamicAnalysisArchive: File?
)
