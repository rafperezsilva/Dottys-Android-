package com.playspinwin.dottys.ui.dashboard.models//package com.keylimetie.dottys.ui.dashboard
//
//import com.fasterxml.jackson.annotation.JsonProperty
//
//import com.fasterxml.jackson.annotation.*
//import com.fasterxml.jackson.core.*
//import com.fasterxml.jackson.databind.*
//import com.fasterxml.jackson.databind.deser.std.StdDeserializer
//import com.fasterxml.jackson.databind.module.SimpleModule
//import com.fasterxml.jackson.databind.node.*
//import com.fasterxml.jackson.databind.ser.std.StdSerializer
//import com.fasterxml.jackson.module.kotlin.*
//
//
//@Suppress("UNCHECKED_CAST")
//private fun <T> ObjectMapper.convert(k: kotlin.reflect.KClass<*>, fromJson: (JsonNode) -> T, toJson: (T) -> String, isUnion: Boolean = false) = registerModule(SimpleModule().apply {
//    addSerializer(k.java as Class<T>, object : StdSerializer<T>(k.java as Class<T>) {
//        override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) = gen.writeRawValue(toJson(value))
//    })
//    addDeserializer(k.java as Class<T>, object : StdDeserializer<T>(k.java as Class<T>) {
//        override fun deserialize(p: JsonParser, ctxt: DeserializationContext) = fromJson(p.readValueAsTree())
//    })
//})
//
//val locationMapper = jacksonObjectMapper().apply {
//    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
//    setSerializationInclusion(JsonInclude.Include.NON_NULL)
//    convert(RegionID::class,  { RegionID.fromValue(it.asText()) },  { "\"${it.value}\"" })
//    convert(StoreType::class, { StoreType.fromValue(it.asText()) }, { "\"${it.value}\"" })
//}
//
//data class DottysStoreLocationsModel (
//    val locations: List<DottysLocations>? = null
//) {
//    fun toJson() = locationMapper.writeValueAsString(this)
//
//    companion object {
//        fun fromJson(json: String) = locationMapper.readValue<DottysStoreLocationsModel>(json)
//    }
//}
//
//data class DottysLocations (
//    @get:JsonProperty("_id")@field:JsonProperty("_id")
//    val id: String? = null,
//
//    val updatedAt: String? = null,
//    val createdAt: String? = null,
//
//    @get:JsonProperty("regionId")@field:JsonProperty("regionId")
//    val regionID: RegionID? = null,
//
//    val name: String? = null,
//    val address1: String? = null,
//    val city: String? = null,
//    val state: String? = null,
//    val zip: String? = null,
//    val phone: String? = null,
//    val storeType: StoreType? = null,
//    val storeNumber: Long? = null,
//    val latitude: Double? = null,
//    val longitude: Double? = null,
//    val seq: Long? = null,
//    val address2: String? = null,
//
//    @get:JsonProperty("isDeleted")@field:JsonProperty("isDeleted")
//    val isDeleted: Boolean? = null,
//
//    val hours: List<String>? = null,
//    val distance: Double? = null,
//    val createdBy: String? = null,
//    val updatedBy: String? = null
//)
//
//enum class RegionID(val value: String) {
//    The5900E037D03Da51D479C051A("5900e037d03da51d479c051a");
//
//    companion object {
//        fun fromValue(value: String): RegionID = when (value) {
//            "5900e037d03da51d479c051a" -> The5900E037D03Da51D479C051A
//            else                       -> throw IllegalArgumentException()
//        }
//    }
//}
//
//enum class StoreType(val value: String) {
//    DottyS("Dotty's"),
//    Empty("");
//
//    companion object {
//        fun fromValue(value: String): StoreType = when (value) {
//            "Dotty's" -> DottyS
//            ""        -> Empty
//            else      -> throw IllegalArgumentException()
//        }
//    }
//}