package com.keylimetie.dottys.models


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
    convert(IconType::class, { IconType.fromValue(it.asText()) }, { "\"${it.value}\"" })
}

data class DottysRewardsModel(
    val pointsForCashCount: Long? = null,
    val rewards: List<DottysReward>? = null
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysRewardsModel>(json)
    }
}

data class DottysReward(
    @get:JsonProperty("_id") @field:JsonProperty("_id")
    val id: String? = null,

    val description: String? = null,
    val endDate: String? = null,

    @get:JsonProperty("isDeleted") @field:JsonProperty("isDeleted")
    val isDeleted: Boolean? = null,

    val iconType: IconType? = null,
    val graphicType: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    var redeemed: Boolean? = null,
    val offerType: String? = null,

    @get:JsonProperty("userId") @field:JsonProperty("userId")
    val userID: String? = null,

    val fullName: String? = null,

    @get:JsonProperty("homeLocationId") @field:JsonProperty("homeLocationId")
    val homeLocationID: String? = null,

    val startDate: String? = null,
    val value: Long? = null,

    @get:JsonProperty("isSpun") @field:JsonProperty("isSpun")
    val isSpun: Boolean? = null,

    val pointsForCash: Boolean? = null,
    val redeemedDate: String? = null,
    val hostCode: String? = null,

    @get:JsonProperty("locationId") @field:JsonProperty("locationId")
    val locationID: String? = null,

    val location: String? = null,

    @get:JsonProperty("regionId") @field:JsonProperty("regionId")
    val regionID: String? = null,

    val region: String? = null,
    val validationCode: String? = null,
    val redeemedValidationCode: String? = null
)

enum class IconType(val value: String) {
    DailyCheckin("dailyCheckin"),
    Extra15XPoints("extra15xPoints"),
    IconFreeGift("iconFreeGift"),
    PointsToDollars("pointsToDollars"),
    TacoTuesday("tacoTuesday");

    companion object {
        fun fromValue(value: String): IconType = when (value) {
            "dailyCheckin" -> DailyCheckin
            "extra15xPoints" -> Extra15XPoints
            "iconFreeGift" -> IconFreeGift
            "pointsToDollars" -> PointsToDollars
            "tacoTuesday" -> TacoTuesday
            else -> throw IllegalArgumentException()
        }
    }
}


