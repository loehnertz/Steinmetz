package controller.analysis.extraction


enum class Platform {
    JAVA;

    companion object {
        fun getPlatformByName(name: String): Platform = valueOf(name.toUpperCase())
    }
}
