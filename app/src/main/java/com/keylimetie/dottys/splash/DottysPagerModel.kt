package com.keylimetie.dottys.splash

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val mapper = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}

data class DottysPagerModel (
    @get:JsonProperty(required=true)@field:JsonProperty(required=true)
    val title: String,

    @get:JsonProperty(required=true)@field:JsonProperty(required=true)
    val image: Int,

    @get:JsonProperty(required=true)@field:JsonProperty(required=true)
    val subtitle: String
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysPagerModel>(json)
    }
}