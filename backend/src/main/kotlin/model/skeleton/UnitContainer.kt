package model.skeleton

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName


@JsonRootName("units")
data class UnitContainer(
        @JsonProperty("unit")
        val units: ArrayList<Unit>
)
