package com.keylimetie.dottys.ui.dashboard.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.keylimetie.dottys.beacon_service.BeaconEventType
import com.keylimetie.dottys.beacon_service.DottysBeaconResponseModel
import com.keylimetie.dottys.ui.locations.CompanyType
import com.keylimetie.dottys.ui.locations.DottysStoresLocation


@Suppress("UNCHECKED_CAST")
private fun <T> ObjectMapper.convert(k: kotlin.reflect.KClass<*>, fromJson: (JsonNode) -> T, toJson: (T) -> String, isUnion: Boolean = false) = registerModule(SimpleModule().apply {
    addSerializer(k.java as Class<T>, object : StdSerializer<T>(k.java as Class<T>) {
        override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) = gen.writeRawValue(toJson(value))
    })
    addDeserializer(k.java as Class<T>, object : StdDeserializer<T>(k.java as Class<T>) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext) = fromJson(p.readValueAsTree())
    })
})

val mapperBeacons = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
    convert(BeaconType::class, {
        BeaconType.fromValue(
            it.asText()
        )
    }, { "\"${it.value}\"" })
    convert(CompanyType::class,   { CompanyType.fromValue(it.asText()) },   { "\"${it.value}\"" })

}
@JsonIgnoreProperties(ignoreUnknown = true)
data class DottysBeaconsModel (
    val total: Long? = null,
    val limit: Long? = null,
    val page: Long? = null,
    val pages: Long? = null,
    var beacons: List<DottysBeacon>? = null
) {
    fun toJson() = mapperBeacons.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapperBeacons.readValue<DottysBeaconsModel>(json)
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
data class DottysBeaconArray (

    var beaconArray: ArrayList<DottysBeacon>? = null
){
    fun toJson() = mapperBeacons.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapperBeacons.readValue<DottysBeaconArray>(json)
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
data class  DottysBeacon (
    @get:JsonProperty("_id")@field:JsonProperty("_id")
    var id: String? = null,

    var updatedAt: String? = null,
    var createdAt: String? = null,
    var uuid: String? = null,

    @get:JsonProperty("locationId")@field:JsonProperty("locationId")
    var locationID: String? = null,

    var major: Long? = null,
    var minor: Long? = null,
    var beaconType: BeaconType? = null,
    var createdBy: String? = null,
    var updatedBy: String? = null,
    var isConected: Boolean? = null,
    var isRegistered: Boolean = false,
    var expiration: Int = 0,

    @get:JsonProperty("isDeleted")@field:JsonProperty("isDeleted")
    val isDeleted: Boolean? = null,

    var location: DottysStoresLocation? = null,
    var beaconIdentifier: String? = id,
    var eventType: BeaconEventType? = null,

    @get:JsonProperty("userId")@field:JsonProperty("userId")
    var userID: String? = null,

    var locationSequence: Long? = null




){
    fun toJson() = mapperBeacons.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapperBeacons.readValue<DottysBeacon>(json)

    }
}


data class BeaconConnectionsUpdatesModel(
    var beacon: DottysBeaconResponseModel? = null,
    var updates: ArrayList<String>? = null
) {
    fun toJson(): String = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<BeaconConnectionsUpdatesModel>(json)
    }
}


enum class BeaconType(val value: String) {
    GAMING("GAMING"),
    LOCATION("LOCATION");

    companion object {
        fun fromValue(value: String): BeaconType = when (value) {
            "GAMING"   -> GAMING
            "LOCATION" -> LOCATION
            else       -> throw IllegalArgumentException()
        }
    }
    }
