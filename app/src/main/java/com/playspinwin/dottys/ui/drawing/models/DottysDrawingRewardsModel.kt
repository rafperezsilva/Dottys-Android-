package com.playspinwin.dottys.ui.drawing.models

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
data class DottysMainHomeLocationModel(
    val total: Long? = (0).toLong(),
    val limit: Long? = (0).toLong(),
    val page: Long? = (0).toLong(),
    val pages: Long? = (0).toLong(),
    @get:JsonProperty("_id") @field:JsonProperty("_id")
    val id: String? = "",
    val locations: List<DottysDrawingRewardsModel>? = listOf(DottysDrawingRewardsModel()),
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysMainHomeLocationModel>(json)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class DottysDrawingRewardsModel(
    @get:JsonProperty("_id") @field:JsonProperty("_id")
    val id: String? = "",

    val updatedAt: String? = "",
    val createdAt: String? = "",
    val updatedBy: String? = "",
    val createdBy: String? = "",

    @get:JsonProperty("regionId") @field:JsonProperty("regionId")
    val regionID: String? = "",

    val name: String? = "",
    val address1: String? = "",
    val city: String? = "",
    val state: String? = "",
    val zip: String? = "",
    val phone: String? = "",
    val storeType: String? = "",
    val storeNumber: Long? = (0).toLong(),
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val seq: Long? = (0).toLong(),
    val company: String? = "",
    val address2: String? = "",
    val total: Long? = (0).toLong(),

    @get:JsonProperty("isDeleted") @field:JsonProperty("isDeleted")
    val isDeleted: Boolean? = false,

    val hours: List<String>? = listOf(""),
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysDrawingRewardsModel>(json)
    }
}