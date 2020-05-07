package com.keylimetie.dottys

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val mapper = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}
@JsonIgnoreProperties
data class DottysLoginResponseModel(
    @get:JsonProperty("_id") @field:JsonProperty("_id")
    val id: String? = null,

    val updatedAt: String? = null,
    val createdAt: String? = null,
    val address1: String? = null,
    val address2: String? = null,
    val anniversaryDate: Any? = null,
    val cell: String? = null,
    val dob: String? = null,
    var email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val state: String? = null,
    val zip: String? = null,
    val lastKnownLocationId: String? = null,
    val lastLoginAt: String? = null,

    @get:JsonProperty("homeLocationId") @field:JsonProperty("homeLocationId")
    val homeLocationID: String? = null,

    val updatedBy: String? = null,
    val city: String? = null,
    val timezone: String? = null,
    val totalMonthlyPlayDuration: Long? = null,
    val phoneNumberVerified: Boolean? = null,
    val profilePicture: String? = null,
    val weeklyVisits: Long? = null,
    val weeklyPlayDuration: Long? = null,
    var points: Long? = null,
    val monthlyPlayDuration: Long? = null,
    val lastPointsForCashRedeemed: Any? = null,
    val lastPointsForCashBought: Any? = null,
    val termsAccepted: Boolean? = null,

    @get:JsonProperty("isOptOutFromMailingList") @field:JsonProperty("isOptOutFromMailingList")
    val isOptOutFromMailingList: Boolean? = null,

    @get:JsonProperty("isDeleted") @field:JsonProperty("isDeleted")
    val isDeleted: Boolean? = null,

    val emailVerified: Boolean? = null,
    val deviceToken: String? = null,
    val cellVerified: Boolean? = null,
    val averageMonthlyPlayDuration: Long? = null,
    val averageMonthlyPlayDays: Long? = null,
    val averageDailyPlayDuration: Long? = null,
    val acl: List<DottysACL>? = null,
    val fullName: String? = null,
    val token: String? = null
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysLoginResponseModel>(json)
    }
}

data class DottysACL(
    @get:JsonProperty("_id") @field:JsonProperty("_id")
    val id: String? = null,

    val role: String? = null
)

data class DottysRegisterModel(
    var email: String? = null,
    var password: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var phoneNumber: String? = null,
    var birthday: String? = null
)


//val mapperBeacon = jacksonObjectMapper().apply {
//    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
//    setSerializationInclusion(JsonInclude.Include.NON_NULL)
//}

data class DottysRegisterResponseModel(
    val lastLoginAt: String? = null,
    val updatedAt: String? = null,
    val createdAt: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,

    @get:JsonProperty("_id") @field:JsonProperty("_id")
    val id: String? = null,

    val weeklyVisits: Long? = null,
    val weeklyPlayDuration: Long? = null,
    val points: Long? = null,
    val monthlyPlayDuration: Long? = null,
    val lastPointsForCashRedeemed: Any? = null,
    val lastPointsForCashBought: Any? = null,
    val termsAccepted: Boolean? = null,

    @get:JsonProperty("isOptOutFromMailingList") @field:JsonProperty("isOptOutFromMailingList")
    val isOptOutFromMailingList: Boolean? = null,

    @get:JsonProperty("isDeleted") @field:JsonProperty("isDeleted")
    val isDeleted: Boolean? = null,

    val deviceToken: Any? = null,
    val averageMonthlyPlayDuration: Long? = null,
    val averageMonthlyPlayDays: Long? = null,
    val averageDailyPlayDuration: Long? = null,
    val acl: List<DottysACL>? = null,
    val fullName: String? = null,
    val token: String? = null
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysRegisterResponseModel>(json)
    }
}

data class DottysRegisterRequestModel(

    var address1: String? = null,
    var address2: String? = null,
    var anniversaryDate: Any? = null,
    var cell: String? = null,
    var email: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var zip: String? = null,
    var password: String? = null,
    var city: String? = null


) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysLoginResponseModel>(json)
    }
}


data class DottysErrorModel (
    @get:JsonProperty("error") @field:JsonProperty("error")
     val error: ErrorDottys? = null
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysErrorModel>(json)
    }
}

data class ErrorDottys (
    val messages: List<String>? = null,
    val stack: String? = null
)
