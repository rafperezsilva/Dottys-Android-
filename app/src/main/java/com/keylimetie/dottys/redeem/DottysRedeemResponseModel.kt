package com.keylimetie.dottys.redeem


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
data class DottysRedeemResponseModel(
    var barcode: String? = null,
    var validationCode: String? = null,

    @get:JsonProperty("_id") @field:JsonProperty("_id")
    var id: String? = null,

    var redeemedValidationCode: String? = null,
    var productCode: String? = null,
    var user: List<Any?>? = null
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysRedeemResponseModel>(json)
    }
}

//data class DottysErrorModel (
//    val error: Error? = null
//) {
//    fun toJson() = mapper.writeValueAsString(this)
//
//    companion object {
//        fun fromJson(json: String) = mapper.readValue<DottysErrorModel>(json)
//    }
//}
//
//data class Error (
//    val messages: List<String>? = null
//)
