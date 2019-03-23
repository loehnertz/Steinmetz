package controller.analysis.extraction.graph

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import model.skeleton.UnitContainer


object UnitContainerExtractor {
    fun extract(processedSkeletonXml: String): UnitContainer = parseAs(processedSkeletonXml)

    private inline fun <reified T : Any> parseAs(xml: String): T = kotlinXmlMapper.readValue(xml)

    private val kotlinXmlMapper = XmlMapper(JacksonXmlModule()
            .apply { setDefaultUseWrapper(false) }).registerKotlinModule()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}
