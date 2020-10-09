package com.keylimetie.dottys.ui.drawing

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val drawingMapper = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}

data class DottysDrawingUserModel(
    val drawings: List<DottysDrawing>? = null,
) {
    fun toJson() = drawingMapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = drawingMapper.readValue<DottysDrawingUserModel>(json)
    }
}

@JsonIgnoreProperties
data class DottysDrawing(
    @get:JsonProperty("_id") @field:JsonProperty("_id")
    val id: String? = null,

    val drawingType: String? = null,
    val endDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    var title: String? = null,
    var subtitle: String? = null,
    val startDate: String? = null,
    val regionName: String? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null,

    @get:JsonProperty("regionId") @field:JsonProperty("regionId")
    val regionID: String? = null,

    val quantity: Long? = null,
    val priceInPoints: Long? = null,
    val locationName: String? = null,
    val dummy: Boolean? = false,

    @get:JsonProperty("locationId") @field:JsonProperty("locationId")
    val locationID: String? = null,

    @get:JsonProperty("isDeleted") @field:JsonProperty("isDeleted")
    val isDeleted: Boolean? = null,
) {
    fun toJson() = drawingMapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = drawingMapper.readValue<DottysDrawing>(json)
    }
}
