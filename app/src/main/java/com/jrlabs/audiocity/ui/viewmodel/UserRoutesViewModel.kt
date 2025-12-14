package com.jrlabs.audiocity.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.jrlabs.audiocity.domain.model.UserRoute
import com.jrlabs.audiocity.domain.model.UserStop
import com.jrlabs.audiocity.domain.service.UserRoutesService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class UserRoutesViewModel @Inject constructor(
    private val userRoutesService: UserRoutesService
) : ViewModel() {

    val userRoutes: StateFlow<List<UserRoute>> = userRoutesService.userRoutes

    private val _selectedRoute = MutableStateFlow<UserRoute?>(null)
    val selectedRoute: StateFlow<UserRoute?> = _selectedRoute.asStateFlow()

    private val _isCreatingRoute = MutableStateFlow(false)
    val isCreatingRoute: StateFlow<Boolean> = _isCreatingRoute.asStateFlow()

    private val _isEditingRoute = MutableStateFlow(false)
    val isEditingRoute: StateFlow<Boolean> = _isEditingRoute.asStateFlow()

    fun createRoute(
        name: String,
        city: String,
        description: String = "",
        neighborhood: String = ""
    ): UserRoute {
        val route = UserRoute(
            name = name,
            city = city,
            description = description,
            neighborhood = neighborhood
        )
        return userRoutesService.createRoute(route)
    }

    fun updateRoute(route: UserRoute) {
        userRoutesService.updateRoute(route)
    }

    fun deleteRoute(routeId: String) {
        userRoutesService.deleteRoute(routeId)
        if (_selectedRoute.value?.id == routeId) {
            _selectedRoute.value = null
        }
    }

    fun selectRoute(route: UserRoute?) {
        _selectedRoute.value = route
    }

    fun getRoute(routeId: String): UserRoute? {
        return userRoutesService.getRouteById(routeId)
    }

    fun togglePublish(routeId: String) {
        val route = userRoutesService.getRouteById(routeId)
        if (route != null) {
            if (route.isPublished) {
                userRoutesService.unpublishRoute(routeId)
            } else {
                userRoutesService.publishRoute(routeId)
            }
        }
    }

    fun addStop(routeId: String, stop: UserStop) {
        userRoutesService.addStopToRoute(routeId, stop)
    }

    fun removeStop(routeId: String, stopId: String) {
        userRoutesService.removeStopFromRoute(routeId, stopId)
    }

    fun reorderStops(routeId: String, fromIndex: Int, toIndex: Int) {
        val route = userRoutesService.getRouteById(routeId) ?: return
        val stops = route.stops.toMutableList()
        if (fromIndex in stops.indices && toIndex in stops.indices) {
            val item = stops.removeAt(fromIndex)
            stops.add(toIndex, item)
            val newOrder = stops.map { it.id }
            userRoutesService.reorderStops(routeId, newOrder)
        }
    }

    fun setCreatingRoute(value: Boolean) {
        _isCreatingRoute.value = value
    }

    fun setEditingRoute(value: Boolean) {
        _isEditingRoute.value = value
    }
}
