package model.skeleton

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper


@JsonRootName("unit")
data class Unit(
        @JsonProperty("name")
        val identifier: String = "",

        @JsonProperty("package")
        val packageIdentifier: String = "",

        @JacksonXmlElementWrapper(localName = "methods")
        val methods: ArrayList<Method>? = arrayListOf()  // TODO: Remove the nullable
)
