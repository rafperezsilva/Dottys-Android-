package com.keylimetie.dottys

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val mapper = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}

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
    val email: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    val state: String? = null,
    val zip: String? = null,
    val lastLoginAt: String? = null,

    @get:JsonProperty("homeLocationId") @field:JsonProperty("homeLocationId")
    val homeLocationID: String? = null,

    val updatedBy: String? = null,
    val city: String? = null,
    val timezone: String? = null,
    val totalMonthlyPlayDuration: Long? = null,
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

    val deviceToken: String? = null,
    val averageMonthlyPlayDuration: Long? = null,
    val averageMonthlyPlayDays: Long? = null,
    val averageDailyPlayDuration: Long? = null,
    val acl: List<ACL>? = null,
    val fullName: String? = null,
    val token: String? = null
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysLoginResponseModel>(json)
    }
}

data class ACL(
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
    val acl: List<ACL>? = null,
    val fullName: String? = null,
    val token: String? = null
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysRegisterResponseModel>(json)
    }
}

//    fun toJson() = mapperBeacon.writeValueAsString(this)
//
//    companion object {
//        fun fromJson(json: String) = mapperBeacon.readValue<DottysRegisterResponseModel>(json)
//    }
//}
//
//data class ACL (
//    @get:JsonProperty("_id")@field:JsonProperty("_id")
//    val id: String? = null,
//
//    val role: String? = null
//)