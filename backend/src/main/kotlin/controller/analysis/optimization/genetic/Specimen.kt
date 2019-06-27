package controller.analysis.optimization.genetic


data class Specimen(
        var chromosome: IntArray,
        var fitness: Double? = null
) {
    fun clone(): Specimen {
        return Specimen(chromosome = this.chromosome.copyOf(), fitness = fitness)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Specimen

        if (!chromosome.contentEquals(other.chromosome)) return false
        if (fitness != other.fitness) return false

        return true
    }

    override fun hashCode(): Int {
        var result: Int = chromosome.contentHashCode()
        result = 31 * result + (fitness?.hashCode() ?: 0)
        return result
    }
}
