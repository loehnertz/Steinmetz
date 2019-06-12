package model.graph


data class Unit(
        val identifier: String,
        val packageIdentifier: String
) {
    override fun toString(): String {
        return "$packageIdentifier.$identifier"
    }
}
