package model.graph


data class UnitFootprint(
    val characters: Long
) {
    companion object {
        fun mergeUnitFootprints(vararg unitFootprints: UnitFootprint): UnitFootprint {
            var characterCountSum = 0L
            unitFootprints.forEach { characterCountSum += it.characters }

            return UnitFootprint(
                characters = characterCountSum
            )
        }
    }
}
