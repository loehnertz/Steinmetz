package model.resource

import controller.analysis.extraction.Platform
import controller.analysis.extraction.coupling.logical.VcsSystem
import java.io.File


data class ProjectRequest(
        val projectName: String,
        val projectPlatform: Platform,
        val vcsSystem: VcsSystem,
        val basePackageIdentifier: String,
        val staticAnalysisFile: File,
        val dynamicAnalysisFile: File,
        val logicalAnalysisFile: File
)
