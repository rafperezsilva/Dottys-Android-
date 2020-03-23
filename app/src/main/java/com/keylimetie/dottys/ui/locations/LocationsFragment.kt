package com.keylimetie.dottys.ui.locations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R


class LocationsFragment : Fragment(), DottysLocationDelegates {

    private lateinit var locationViewModel: LocationsViewModel
    var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        locationViewModel =
            ViewModelProviders.of(this).get(LocationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_locations, container, false)
        rootView = root
        var activity: DottysMainNavigationActivity? = activity as DottysMainNavigationActivity?
        activity?.let { locationViewModel.initLocationView(this, it, root) }
        return root
    }


    override fun getStoresLocation(locations: DottysLocationsStoresModel) {
        locations.locations?.let { locationViewModel.initMapWHitMarker(it) }
        locationViewModel.screenDimensionManager(LocationViewType.COLLAPSE_TYPE)
    }

    override fun allItemsCollapse(isColappse: Boolean) {
        if (isColappse) this.locationViewModel.screenDimensionManager(LocationViewType.COLLAPSE_TYPE) else {
            this.locationViewModel.screenDimensionManager(LocationViewType.EXPANDED_TYPE)
        }
    }
}
