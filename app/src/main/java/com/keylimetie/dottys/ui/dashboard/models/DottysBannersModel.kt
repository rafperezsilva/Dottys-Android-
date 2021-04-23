package com.keylimetie.dottys.ui.dashboard.models

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.module.kotlin.*
@JsonIgnoreProperties(ignoreUnknown = true)
data class DottysBannerModel (
    @get:JsonProperty("docs")@field:JsonProperty("docs")
    val bannerList: ArrayList<DottysBanners>? = null,
    val total: Long? = null,
    val limit: Long? = null,
    val page: Long? = null,
    val pages: Long? = null
) {
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysBannerModel>(json)
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
data class DottysBanners (
    @get:JsonProperty("isDeleted")@field:JsonProperty("isDeleted")
    val isDeleted: Boolean? = null,

    @get:JsonProperty("_id")@field:JsonProperty("_id")
    val id: String? = null,
    val displayDuration: Long? = null,

    val title: String? = null,
    val description: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val priority: Long? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null,
    val image: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
){
    fun toJson() = mapper.writeValueAsString(this)

    companion object {
        fun fromJson(json: String) = mapper.readValue<DottysBanners>(json)
    }
}
