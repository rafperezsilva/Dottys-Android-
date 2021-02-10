package com.keylimetie.dottys.ui.dashboard.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val mapper = jacksonObjectMapper().apply {
    this.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}
@JsonIgnoreProperties(ignoreUnknown = true)
class DottysDrawingSumaryModel(elements: ArrayList<DottysDrawingSumaryModelElement>) :
    ArrayList<DottysDrawingSumaryModelElement>(elements) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysDrawingSumaryModel>(json)
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
data class DottysDrawingSumaryModelElement(
    val drawingType: String? = "",
    val numberOfEntries: Int? = 0,
    val title: String? = "",
    val endDate: String? = ""
)
