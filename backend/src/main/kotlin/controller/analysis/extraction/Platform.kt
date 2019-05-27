package controller.analysis.extraction


enum class Platform {
    JVM;

    companion object {
        fun getPlatformByName(name: String): Platform = valueOf(name.toUpperCase())
    }
}
