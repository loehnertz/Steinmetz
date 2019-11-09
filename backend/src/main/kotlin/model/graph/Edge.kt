package model.graph


data class Edge(
    val start: Unit,
    val end: Unit,
    val attributes: EdgeAttributes
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Edge

        if (start != other.start) return false
        if (end != other.end) return false

        return true
    }

    override fun hashCode(): Int {
        return 31 * start.hashCode() + end.hashCode()
    }
}
