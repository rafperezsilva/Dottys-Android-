package com.keylimetie.dottys.redeem


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val mapper = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}

data class DottysRedeemResponseModel(
    val barcode: String? = null,
    val validationCode: String? = null,

    @get:JsonProperty("_id") @field:JsonProperty("_id")
    val id: String? = null,

    val redeemedValidationCode: String? = null,
    val productCode: String? = null,
    val user: List<Any?>? = null
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
