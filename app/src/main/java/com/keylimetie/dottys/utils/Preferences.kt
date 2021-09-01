package com.keylimetie.dottys.utils

import android.content.Context
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.models.DottysLoginResponseModel
import com.keylimetie.dottys.PreferenceTypeKey
import com.keylimetie.dottys.models.DottysGlobalDataModel
import com.keylimetie.dottys.models.DottysRewardsModel
import com.keylimetie.dottys.ui.dashboard.models.DottysBannerModel
import com.keylimetie.dottys.ui.dashboard.models.DottysBeacon
import com.keylimetie.dottys.ui.dashboard.models.DottysBeaconArray
import com.keylimetie.dottys.ui.drawing.models.DottysDrawingRewardsModel
import com.keylimetie.dottys.ui.locations.DottysLocationsStoresModel

class Preferences(private val context: DottysBaseActivity) {
    /**
     * GET BEACON FOR CURRENT STORE
     * */
    fun getDottysBeaconsList(): ArrayList<DottysBeacon>? {
        context.sharedPreferences = context.getSharedPreferences(
            PreferenceTypeKey.PREFS_DATA.name,
            Context.MODE_PRIVATE)
        val textoDate =  context.sharedPreferences!!.getString(PreferenceTypeKey.BEACONS_LIST.name, "")
        return try {
            var person: DottysBeaconArray =
                DottysBeaconArray.fromJson(
                    textoDate!!
                )
            try {
                var beaconAtStore =
                    person.beaconArray//.beacons?.filter { it.location?.storeNumber ?: 0 == getUserNearsLocations().locations?.first()?.storeNumber ?: 0 }
                            as ArrayList
                //saveDataPreference(PreferenceTypeKey.BEACON_AT_CONECTION,
                //    DottysBeaconArray(beaconAtStore).toJson())
                beaconAtStore
            } catch (e: Exception) {
                println(e)
                null
            }
        } catch (e: Exception) {
            println(e)
            null
        }
    }
    /**
     * GET CURRENT TOKEN
     * */
    fun getCurrentToken(): String? {
        context.sharedPreferences =  context.getSharedPreferences(
            PreferenceTypeKey.PREFS_DATA.name,
            Context.MODE_PRIVATE)
        return try {
            context.sharedPreferences!!.getString(PreferenceTypeKey.TOKEN.name, null)
        } catch (e: Exception) {
            println(e)
            null
        }
    }
    /**
     * GET BEAON STATUS
     * */
    fun getBeaconStatus(): DottysBeaconArray? {
        if (context.sharedPreferences == null) {
            context.sharedPreferences =  context.getSharedPreferences(
                PreferenceTypeKey.PREFS_DATA.name,
                0
            )
        }
        val textoDate =
            context.sharedPreferences!!.getString(PreferenceTypeKey.BEACON_AT_CONECTION.name, "")
        return try {
            var person: DottysBeaconArray? =
                textoDate?.let { DottysBeaconArray.fromJson(it) }
            person
        } catch (e: Exception) {
            println(e)
            null
        }
    }
    /**
     * GET CURRENT USER
     */
    fun getUserPreference(): DottysLoginResponseModel {
        if (context.sharedPreferences == null) {
            context.sharedPreferences =  context.getSharedPreferences(
                PreferenceTypeKey.PREFS_DATA.name,
                0
            )
        }
        val userData = context.sharedPreferences!!.getString(PreferenceTypeKey.USER_DATA.name, "")
        return try {
            var currentUserData: DottysLoginResponseModel =
                DottysLoginResponseModel.fromJson(
                    userData!!
                )
            currentUserData
        } catch (e: Exception) {
            println(e)
            DottysLoginResponseModel()
        }
    }
    /**
     * GET GLOBAL DATA
     */
    fun getGlobalData(): DottysGlobalDataModel {
        context.sharedPreferences =  context.getSharedPreferences(
            PreferenceTypeKey.PREFS_DATA.name,
            0
        )
        val globalInfo = context.sharedPreferences!!.getString(PreferenceTypeKey.GLOBAL_DATA.name, "")
        return try {
            var globalData: DottysGlobalDataModel =
                DottysGlobalDataModel.fromJson(
                    globalInfo!!
                )
            globalData
        } catch (e: Exception) {
            println(e)
            DottysGlobalDataModel()
        }
    }
    /**
     * GET DRAWINGS
     */
    fun getDrawings(): DottysDrawingRewardsModel {
        context.sharedPreferences =  context.getSharedPreferences(
            PreferenceTypeKey.PREFS_DATA.name,
            0
        )
        val drawingData = context.sharedPreferences!!.getString(PreferenceTypeKey.DRAWINGS.name, "")
        return try {
            var drawingLocations: DottysDrawingRewardsModel =
                DottysDrawingRewardsModel.fromJson(
                    drawingData!!
                )
            drawingLocations
        } catch (e: Exception) {
            println(e)
            DottysDrawingRewardsModel()
        }
    }
    /**
     * GET ALL DOTTYS LOCATIONS
     */
    fun getUserNearsLocations(): DottysLocationsStoresModel {
        context.sharedPreferences =  context.getSharedPreferences(
            PreferenceTypeKey.PREFS_DATA.name,
            0
        )
        val nearStoreData = context.sharedPreferences?.getString(PreferenceTypeKey.LOCATIONS.name, "")
        return try {
            var nearStoreLocation: DottysLocationsStoresModel =
                DottysLocationsStoresModel.fromLocationJson(
                    nearStoreData!!
                )
            nearStoreLocation
        } catch (e: Exception) {
            println(e)
            DottysLocationsStoresModel()
        }
    }
    /**
     * GET REWARDS AT SESSION
     */
    fun getRewardsAtSession(): DottysRewardsModel {

        context.sharedPreferences =  context.getSharedPreferences(
            PreferenceTypeKey.PREFS_DATA.name,
            0
        )

        val rewardsDAta = context.sharedPreferences!!.getString(PreferenceTypeKey.REWARDS.name, "")
        return try {
            var rewards: DottysRewardsModel =
                DottysRewardsModel.fromJson(
                    rewardsDAta!!
                )
            rewards
        } catch (e: Exception) {
            println(e)
            DottysRewardsModel()
        }
    }
    /**
     * GET STORED BANNERS
     */
    fun getBannersStored(): DottysBannerModel {
        if (context.sharedPreferences == null) {
            context.sharedPreferences =  context.getSharedPreferences(
                PreferenceTypeKey.PREFS_DATA.name,
                0
            )
        }
        val bannerData = context.sharedPreferences!!.getString(PreferenceTypeKey.BANNERS.name, "")
        return try {
            var banners: DottysBannerModel =
                DottysBannerModel.fromJson(
                    bannerData!!
                )
            banners
        } catch (e: Exception) {
            println(e)
            DottysBannerModel()
        }
    }
}