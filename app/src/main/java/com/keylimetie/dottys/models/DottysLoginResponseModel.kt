package com.keylimetie.dottys

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
data class DottysLoginResponseModel (
    var averageDailyPlayDuration: Long? = null,
    var averageMonthlyPlayDays: Long? = null,
    var averageMonthlyPlayDuration: Long? = null,
    var cellVerified: Boolean? = null,
    var deviceToken: Any? = null,
    var emailVerified: Boolean? = null,

    @get:JsonProperty("isDeleted")@field:JsonProperty("isDeleted")
    var isDeleted: Boolean? = null,

    @get:JsonProperty("isOptOutFromMailingList")@field:JsonProperty("isOptOutFromMailingList")
    var isOptOutFromMailingList: Boolean? = null,

    var termsAccepted: Boolean? = null,
    var lastPointsForCashBought: Any? = null,
    var lastPointsForCashRedeemed: Any? = null,
    var monthlyPlayDuration: Long? = null,
    var points: Long? = null,
    var weeklyPlayDuration: Long? = null,
    var weeklyVisits: Long? = null,
    var tags: List<Any?>? = null,
    var trackLocation: String? = null,// NEVER / WHILE_USING / ALWAYS
    var bluetooth: Boolean? = null,
    var notifications: Boolean? = null,
    var backgroundAppRefresh: Boolean? = null,

    @get:JsonProperty("_id")@field:JsonProperty("_id")
    var id: String? = null,

    var updatedAt: String? = null,
    var createdAt: String? = null,
    var email: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var acl: List<ACL>? = null,
    var lastLoginAt: String? = null,
    var cell: String? = null,
    var updatedBy: String? = null,
    var address1: String? = null,
    var appVersion: String? = null,
    var city: String? = null,
    var deviceId: String? = null,
    var state: String? = null,
    var zip: String? = null,

    @get:JsonProperty("homeLocationId")@field:JsonProperty("homeLocationId")
    var homeLocationID: String? = null,

    @get:JsonProperty("lastKnownLocationId")@field:JsonProperty("lastKnownLocationId")
    var lastKnownLocationID: String? = null,

    var timezone: String? = null,
    var profilePicture: String? = null,
    var fullName: String? = null,
    var token: String? = null
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysLoginResponseModel>(json)
    }
}

data class ACL (
    var role: DottysRoleUser? = null,
    @get:JsonProperty("_id")@field:JsonProperty("_id")
    var id: String? = null
)


@JsonIgnoreProperties(ignoreUnknown = true)
data class DottysRegisterModel(
    var email: String? = "",
    var password: String? = "",
    var firstName: String? = "",
    var lastName: String? = "",
    var phoneNumber: String? = "",
    var birthday: String? = ""
)

enum class DottysRoleUser(var varue: String) {
    EMPLOYEE("EMPLOYEE"),
    SUPER_ADMIN("SUPER_ADMIN"),
    ADMIN("ADMIN"),
    USER("USER"),
    REGION_ADMIN("REGION_ADMIN");

    companion object {
        fun fromvarue(varue: String): DottysRoleUser = when (varue) {
            "EMPLOYEE"     -> DottysRoleUser.EMPLOYEE
            "SUPER_ADMIN"     -> DottysRoleUser.SUPER_ADMIN
            "ADMIN"     -> DottysRoleUser.ADMIN
            "USER" -> DottysRoleUser.USER
            "REGION_ADMIN" -> DottysRoleUser.REGION_ADMIN


            else -> throw IllegalArgumentException()
        }
    }
}
//region
//var mapperBeacon = jacksonObjectMapper().apply {
//    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
//    setSerializationInclusion(JsonInclude.Include.NON_NULL)
//}

//data class DottysRegisterResponseModel(
//    var lastLoginAt: String? = "",
//    var updatedAt: String? = "",
//    var createdAt: String? = "",
//    var email: String? = "",
//    var firstName: String? = "",
//    var lastName: String? = "",
//
//    @get:JsonProperty("_id") @field:JsonProperty("_id")
//    var id: String? = null,
//
//    var weeklyVisits: Long? = null,
//    var weeklyPlayDuration: Long? = null,
//    var points: Long? = null,
//    var monthlyPlayDuration: Long? = null,
//    var lastPointsForCashRedeemed: Any? = null,
//    var lastPointsForCashBought: Any? = null,
//    var termsAccepted: Boolean? = null,
//
//    @get:JsonProperty("isOptOutFromMailingList") @field:JsonProperty("isOptOutFromMailingList")
//    var isOptOutFromMailingList: Boolean? = null,
//
//    @get:JsonProperty("isDeleted") @field:JsonProperty("isDeleted")
//    var isDeleted: Boolean? = null,
//
//    var deviceToken: Any? = null,
//    var averageMonthlyPlayDuration: Long? = null,
//    var averageMonthlyPlayDays: Long? = null,
//    var averageDailyPlayDuration: Long? = null,
//    var acl: List<DottysACL>? = null,
//    var fullName: String? = null,
//    var token: String? = null
//) {
//    fun toJson() = mapper.writeValueAsString(this)
//
//    companion object {
//        fun fromJson(json: String) = mapper.readValue<DottysRegisterResponseModel>(json)
//    }
//}
//endregion
@JsonIgnoreProperties(ignoreUnknown = true)
data class DottysRegisterRequestModel(

    var address1: String? = null,
    var address2: String? = null,
    var anniversaryDate: String? = null,
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class DottysErrorModel (
    val error: Error? = null
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysErrorModel>(json)
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
data class Error (
    val messages: List<String>? = null
)

//
//@JsonIgnoreProperties(ignoreUnknown = true)
//data class DottysErrorModel (
//    @get:JsonProperty("error") @field:JsonProperty("error")
//     var error: ErrorDottys? = null
//) {
//    fun toJson() = mapper.writeValueAsString(this)
//
//    companion object {
//        fun fromJson(json: String) = mapper.readValue<DottysErrorModel>(json)
//    }
//}
//
//data class ErrorDottys (
//    var messages: List<String>? = null,
//    var stack: String? = null
//)
//@JsonIgnoreProperties(ignoreUnknown = true)