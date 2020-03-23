package com.keylimetie.dottys.ui.drawing

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.*

val drawingMapper = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}

data class DottysDrawingUserModel (
    val drawings: List<Drawing>? = null
) {
    fun toJson() = drawingMapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = drawingMapper.readValue<DottysDrawingUserModel>(json)
    }
}

data class Drawing (
    @get:JsonProperty("_id")@field:JsonProperty("_id")
    val id: String? = null,

    val drawingType: String? = null,
    val endDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val startDate: String? = null,
    val regionName: String? = null,

    @get:JsonProperty("regionId")@field:JsonProperty("regionId")
    val regionID: String? = null,

    val quantity: Long? = null,
    val priceInPoints: Long? = null,
    val locationName: String? = null,

    @get:JsonProperty("locationId")@field:JsonProperty("locationId")
    val locationID: String? = null,

    @get:JsonProperty("isDeleted")@field:JsonProperty("isDeleted")
    val isDeleted: Boolean? = null
)
