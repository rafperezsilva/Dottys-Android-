package com.keylimetie.dottys.ui.drawing

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val mapper = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}

data class DottysDrawingRewardsModel (
    @get:JsonProperty("_id")@field:JsonProperty("_id")
    val id: String? = null,

    val updatedAt: String? = null,
    val createdAt: String? = null,

    @get:JsonProperty("regionId")@field:JsonProperty("regionId")
    val regionID: String? = null,

    val name: String? = null,
    val address1: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zip: String? = null,
    val phone: String? = null,
    val storeType: String? = null,
    val storeNumber: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val seq: Long? = null,
    val address2: Any? = null,

    @get:JsonProperty("isDeleted")@field:JsonProperty("isDeleted")
    val isDeleted: Boolean? = null,

    val hours: List<String>? = null
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysDrawingRewardsModel>(json)
    }
}