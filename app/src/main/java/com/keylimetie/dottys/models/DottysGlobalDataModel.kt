package com.keylimetie.dottys.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val mapperGlobalData = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}
@JsonIgnoreProperties(ignoreUnknown = true)
data class DottysGlobalDataModel (
    @get:JsonProperty("_id") @field:JsonProperty("_id")
    val id: String? = null,

    val terms: String? = null,
    val updatedBy: String? = null,
    val updatedAt: String? = null,

    @get:JsonProperty("privacy_url") @field:JsonProperty("privacy_url")
    val privacyURL: String? = null,

    val statusText: String? = null,
    val drawingTemplates: Map<String,Monthly>?= null,
   //val drawingTemplates: DrawingTemplates? = null,
    var drawingDetail: ArrayList<Monthly?>? = null
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysGlobalDataModel>(json)
    }


}

@JsonIgnoreProperties(ignoreUnknown = true)
@kotlinx.serialization.Serializable
data class DrawingTemplates(
    val weekly: Monthly? = null,
    val monthly: Monthly? = null,
    val quarterly: Monthly? = null
)

@kotlinx.serialization.Serializable
data class Monthly(
    val description: String? = null,
    val priceInPoints: Long? = null,
    val subtitle: String? = null,
    val title: String? = null,

    @get:JsonProperty("_id") @field:JsonProperty("_id")
    val id: String? = null,

    val quantity: Long? = null
)