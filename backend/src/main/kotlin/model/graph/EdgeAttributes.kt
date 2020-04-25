package model.graph


data class EdgeAttributes(
    var couplingScore: Int = 1,
    var staticCouplingScore: Int = 0,
    var dynamicCouplingScore: Int = 0,
    var semanticCouplingScore: Int = 0,
    var evolutionaryCouplingScore: Int = 0
) {
    companion object {
        fun mergeEdgeAttributes(vararg edgeAttributes: EdgeAttributes): EdgeAttributes {
            return EdgeAttributes(
                staticCouplingScore = edgeAttributes.map { it.staticCouplingScore }.filter { it != 0 }.average().toInt(),
                dynamicCouplingScore = edgeAttributes.map { it.dynamicCouplingScore }.filter { it != 0 }.average().toInt(),
                semanticCouplingScore = edgeAttributes.map { it.semanticCouplingScore }.filter { it != 0 }.average().toInt(),
                evolutionaryCouplingScore = edgeAttributes.map { it.evolutionaryCouplingScore }.filter { it != 0 }.average().toInt()
            ).also { check(scoresAreLegal(it)) { "The edge attributes $it are not legal" } }
        }

        private fun scoresAreLegal(edgeAttributes: EdgeAttributes): Boolean {
            if (edgeAttributes.couplingScore < 0 || edgeAttributes.couplingScore > 100) return false
            if (edgeAttributes.dynamicCouplingScore < 0 || edgeAttributes.dynamicCouplingScore > 100) return false
            if (edgeAttributes.semanticCouplingScore < 0 || edgeAttributes.semanticCouplingScore > 100) return false
            if (edgeAttributes.evolutionaryCouplingScore < 0 || edgeAttributes.evolutionaryCouplingScore > 100) return false
            return true
        }
    }
}
