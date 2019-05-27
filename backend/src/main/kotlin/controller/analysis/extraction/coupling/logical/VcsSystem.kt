package controller.analysis.extraction.coupling.logical


enum class VcsSystem {
    GIT2,
    HG,
    SVN,
    P4,
    TFS;

    companion object {
        fun getVcsSystemByName(name: String): VcsSystem = valueOf(name.toUpperCase())
    }
}
