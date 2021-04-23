package com.keylimetie.dottys.splash

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
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
data class DottysPagerModel (
    @get:JsonProperty(required=false)@field:JsonProperty(required=false)
    val title: String,

    @get:JsonProperty(required=false)@field:JsonProperty(required=false)
    val image: Int,

    @get:JsonProperty(required=false)@field:JsonProperty(required=false)
    val subtitle: String
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysPagerModel>(json)
    }
}