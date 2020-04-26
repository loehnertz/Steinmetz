package controller.analysis.extraction.coupling.statically


data class ResponseForAClassPair(
    val classIdentifiers: ResponseForAClassIdentifiers,
    private var classAMetrics: ResponseForAClassMetrics = ResponseForAClassMetrics(),
    private var classBMetrics: ResponseForAClassMetrics = ResponseForAClassMetrics()
) {
    fun calculateCouplingPercentage(): Int {
        val totallyInvokedMethods: Double = (classAMetrics.invokedMethods + classBMetrics.invokedMethods).toDouble()
        val totallyAccessibleMethods: Double = (classAMetrics.accessibleMethods + classBMetrics.accessibleMethods).toDouble()
        val couplingScore: Int = ((totallyInvokedMethods / totallyAccessibleMethods) * 100).toInt()
        return if (couplingScore <= 0) {
            1
        } else {
            couplingScore
        }
    }

    fun updateClassMetrics(classIdentifier: String, metrics: ResponseForAClassMetrics): Boolean {
        when (classIdentifier) {
            classIdentifiers.classAIdentifier -> classAMetrics = metrics
            classIdentifiers.classBIdentifier -> classBMetrics = metrics
            else                              -> return false
        }
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResponseForAClassPair

        if (classIdentifiers != other.classIdentifiers) return false

        return true
    }

    override fun hashCode(): Int {
        return classIdentifiers.hashCode()
    }
}

data class ResponseForAClassIdentifiers(
    val classAIdentifier: String,
    val classBIdentifier: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResponseForAClassIdentifiers

        if (classAIdentifier != other.classAIdentifier && classAIdentifier != other.classBIdentifier) return false
        if (classBIdentifier != other.classBIdentifier && classBIdentifier != other.classAIdentifier) return false

        return true
    }

    override fun hashCode(): Int {
        return 31 * (classAIdentifier.hashCode() + classBIdentifier.hashCode())
    }
}

data class ResponseForAClassMetrics(
    val invokedMethods: Int = 0,
    val accessibleMethods: Int = 0
)
