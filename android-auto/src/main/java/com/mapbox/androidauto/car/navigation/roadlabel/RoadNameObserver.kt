package com.mapbox.androidauto.car.navigation.roadlabel

import com.mapbox.navigation.base.trip.model.eh.EHorizonPosition
import com.mapbox.navigation.base.trip.model.eh.RoadName
import com.mapbox.navigation.base.trip.model.roadobject.RoadObjectEnterExitInfo
import com.mapbox.navigation.base.trip.model.roadobject.RoadObjectPassInfo
import com.mapbox.navigation.base.trip.model.roadobject.distanceinfo.RoadObjectDistanceInfo
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.eh.EHorizonObserver

abstract class RoadNameObserver(
    val mapboxNavigation: MapboxNavigation
) : EHorizonObserver {

    var currentRoadName: RoadName? = null

    abstract fun onRoadUpdate(currentRoadName: RoadName?)

    override fun onPositionUpdated(
        position: EHorizonPosition,
        distances: List<RoadObjectDistanceInfo>
    ) {
        val edgeId = position.eHorizonGraphPosition.edgeId
        val edgeMetadata = mapboxNavigation.graphAccessor.getEdgeMetadata(edgeId)
        val currentRoadName = edgeMetadata?.names?.firstOrNull()
        if (this.currentRoadName != currentRoadName) {
            this.currentRoadName = currentRoadName
            onRoadUpdate(currentRoadName)
        }
    }

    override fun onRoadObjectAdded(roadObjectId: String) {
        // Do nothing
    }

    override fun onRoadObjectEnter(objectEnterExitInfo: RoadObjectEnterExitInfo) {
        // Do nothing
    }

    override fun onRoadObjectExit(objectEnterExitInfo: RoadObjectEnterExitInfo) {
        // Do nothing
    }

    override fun onRoadObjectPassed(objectPassInfo: RoadObjectPassInfo) {
        // Do nothing
    }

    override fun onRoadObjectRemoved(roadObjectId: String) {
        // Do nothing
    }

    override fun onRoadObjectUpdated(roadObjectId: String) {
        // Do nothing
    }
}
