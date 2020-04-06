package com.keylimetie.dottys.ui.locations


import com.fasterxml.jackson.annotation.JsonIgnore
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

@Suppress("UNCHECKED_CAST")
private fun <T> ObjectMapper.convert(
    k: kotlin.reflect.KClass<*>,
    fromJson: (JsonNode) -> T,
    toJson: (T) -> String,
    isUnion: Boolean = false
) = registerModule(SimpleModule().apply {
    addSerializer(k.java as Class<T>, object : StdSerializer<T>(k.java as Class<T>) {
        override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) =
            gen.writeRawValue(toJson(value))
    })
    addDeserializer(k.java as Class<T>, object : StdDeserializer<T>(k.java as Class<T>) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
            fromJson(p.readValueAsTree())
    })
})

val mapper = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
    convert(StoreType::class, { StoreType.fromValue(it.asText()) }, { "\"${it.value}\"" })
}

data class DottysLocationsStoresModel(
    val total: Long? = null,
    val limit: Long? = null,
    val page: Long? = null,
    val pages: Long? = null,
    val locations: List<DottysStoresLocation>? = null
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromLocationJson(json: String) = mapper.readValue<DottysLocationsStoresModel>(json)
    }
}

data class DottysStoresLocation(
    @get:JsonProperty("_id") @field:JsonProperty("_id")
    val id: String? = null,

    val updatedAt: String? = null,
    val createdAt: String? = null,

    @get:JsonProperty("regionId") @field:JsonProperty("regionId")
    val regionID: String? = null,

    val name: String? = null,
    val address1: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zip: String? = null,
    val phone: String? = null,
    @JsonIgnore(true)
    val storeType: StoreType? = StoreType.DottyS,
    val storeNumber: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val seq: Long? = null,
    val address2: String? = null,

    @get:JsonProperty("isDeleted") @field:JsonProperty("isDeleted")
    val isDeleted: Boolean? = null,

    val hours: List<String>? = null,
    val distance: Double? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null


)



enum class StoreType(val value: String) {
    Anchor("Anchor"),
    BradleyS("Bradley's"),
    DelToro("Del Toro "),
    DottyS("Dotty's"),
    Empty(""),
    PaddyS("Paddy's"),
    StoreTypePaddyS("Paddy's ");

    companion object {
        fun fromValue(value: String): StoreType = when (value) {
            "Anchor" -> Anchor
            "Bradley's" -> BradleyS
            "Del Toro " -> DelToro
            "Dotty's" -> DottyS
            "" -> Empty
            "Paddy's" -> PaddyS
            "Paddy's " -> StoreTypePaddyS
            else -> throw IllegalArgumentException()
        }
    }
}