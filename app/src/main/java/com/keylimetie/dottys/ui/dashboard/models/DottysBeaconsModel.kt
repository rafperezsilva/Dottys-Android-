package com.keylimetie.dottys.ui.dashboard.models

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.*
import com.keylimetie.dottys.beacon_service.DottysBeaconServiceDelegate
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
}

data class DottysBeaconsModel (
    val total: Long? = null,
    val limit: Long? = null,
    val page: Long? = null,
    val pages: Long? = null,
    val beacons: List<DottysBeacon>? = null
) {
    fun toJson() = mapperBeacons.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapperBeacons.readValue<DottysBeaconsModel>(json)
    }
}
data class DottysBeaconArray (

    var beaconArray: ArrayList<DottysBeacon>? = null
){
    fun toJson() = mapperBeacons.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapperBeacons.readValue<DottysBeaconArray>(json)
    }
}
data class DottysBeacon (
    @get:JsonProperty("_id")@field:JsonProperty("_id")
    val id: String? = null,

    val updatedAt: String? = null,
    val createdAt: String? = null,
    val uuid: String? = null,

    @get:JsonProperty("locationId")@field:JsonProperty("locationId")
    val locationID: String? = null,

    val major: Long? = null,
    val minor: Long? = null,
    val beaconType: BeaconType? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null,
    var isConected: Boolean? = null,

    @get:JsonProperty("isDeleted")@field:JsonProperty("isDeleted")
    val isDeleted: Boolean? = null,

    val location: DottysStoresLocation? = null


){
    fun toJson() = mapperBeacons.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapperBeacons.readValue<DottysBeacon>(json)
    }
}

enum class BeaconType(val value: String) {
    Gaming("GAMING"),
    Location("LOCATION");

    companion object {
        fun fromValue(value: String): BeaconType = when (value) {
            "GAMING"   -> Gaming
            "LOCATION" -> Location
            else       -> throw IllegalArgumentException()
        }
    }
}