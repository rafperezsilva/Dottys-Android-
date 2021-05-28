package com.keylimetie.dottys.beacon_service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.keylimetie.dottys.ui.dashboard.models.BeaconType
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon


val mapper = jacksonObjectMapper().apply {
    this.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
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
    val eventType: BeaconEventType? = null,
    val beaconType: BeaconType? = null,
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

    fun  castToBeaconEvent(): DottysBeacon {
          val beaconCasted = DottysBeacon()
           beaconCasted.userID = userID
           beaconCasted.locationSequence = locationSequence
           beaconCasted.id = beaconIdentifier
           beaconCasted.uuid = uuid
           beaconCasted.eventType = eventType
           beaconCasted.updatedAt = updatedAt
           beaconCasted.createdAt = createdAt
           beaconCasted.beaconIdentifier = beaconIdentifier
           beaconCasted.major = major
           beaconCasted.minor = minor
           beaconCasted.beaconType = beaconType
           beaconCasted.createdBy = createdBy
           beaconCasted.updatedBy = updatedBy
        return beaconCasted
    }
}