package model.skeleton

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper


@JsonRootName("method")
data class Method(
        @JsonProperty("name")
        val name: String = "",

        @JacksonXmlElementWrapper(localName = "operations")
        val operations: ArrayList<Operation>? = arrayListOf()  // TODO: Remove the nullable
)
