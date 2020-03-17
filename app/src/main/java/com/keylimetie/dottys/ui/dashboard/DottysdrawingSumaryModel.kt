package com.keylimetie.dottys.ui.dashboard

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val mapper = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}

class DottysDrawingSumaryModel(elements: ArrayList<DottysDrawingSumaryModelElement>) :
    ArrayList<DottysDrawingSumaryModelElement>(elements) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysDrawingSumaryModel>(json)
    }
}

data class DottysDrawingSumaryModelElement(
    val drawingType: String? = null,
    val numberOfEntries: Int? = null,
    val title: String? = null,
    val endDate: String? = null
)
