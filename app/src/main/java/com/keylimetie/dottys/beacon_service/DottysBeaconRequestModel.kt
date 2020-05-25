package com.keylimetie.dottys.beacon_service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val mapper = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}

data class DottysBeaconRequestModel (
    val beaconIdentifier: String? = null,
    val uuid: String? = null,
    val major: Long? = null,
    val minor: Long? = null,
    val eventType: String? = null
) {
    fun toJson(): String = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysBeaconRequestModel>(json)
    }
}

data class DottysBeaconResponseModel (
    val updatedAt: String? = null,
    val createdAt: String? = null,
    val beaconIdentifier: String? = null,
    val uuid: String? = null,
    val major: Long? = null,
    val minor: Long? = null,
    val eventType: String? = null,
    val beaconType: String? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null,

    @get:JsonProperty("userId")@field:JsonProperty("userId")
    val userID: String? = null,

    val locationSequence: Long? = null,

    @get:JsonProperty("_id")@field:JsonProperty("_id")
    val id: String? = null
) {
    fun toJson():String = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysBeaconResponseModel>(json)
    }
}