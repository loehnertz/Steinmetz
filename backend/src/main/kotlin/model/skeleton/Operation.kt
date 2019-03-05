package model.skeleton

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName


@JsonRootName("operation")
data class Operation(
        @JsonProperty("type")
        val type: String = "",

        @JsonProperty("target")
        val target: String = ""
)
