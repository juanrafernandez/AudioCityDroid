package com.jrlabs.audiocity.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jrlabs.audiocity.data.model.Route
import com.jrlabs.audiocity.data.model.Stop
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun fetchAllRoutes(): Result<List<Route>> {
        return try {
            val snapshot = firestore.collection("routes")
                .whereEqualTo("is_active", true)
                .get()
                .await()

            val routes = snapshot.documents.mapNotNull { Route.fromDocument(it) }
            Result.success(routes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchRoute(routeId: String): Result<Route> {
        return try {
            val document = firestore.collection("routes")
                .document(routeId)
                .get()
                .await()

            val route = Route.fromDocument(document)
            if (route != null) {
                Result.success(route)
            } else {
                Result.failure(Exception("Route not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchStops(routeId: String): Result<List<Stop>> {
        return try {
            val snapshot = firestore.collection("stops")
                .whereEqualTo("route_id", routeId)
                .get()
                .await()

            val stops = snapshot.documents
                .mapNotNull { Stop.fromDocument(it) }
                .sortedBy { it.order }

            Result.success(stops)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAllStops(): Result<List<Stop>> {
        return try {
            val snapshot = firestore.collection("stops")
                .get()
                .await()

            val stops = snapshot.documents.mapNotNull { Stop.fromDocument(it) }
            Result.success(stops)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCompleteRoute(routeId: String): Result<Pair<Route, List<Stop>>> {
        return try {
            val routeResult = fetchRoute(routeId)
            val stopsResult = fetchStops(routeId)

            if (routeResult.isSuccess && stopsResult.isSuccess) {
                Result.success(Pair(routeResult.getOrThrow(), stopsResult.getOrThrow()))
            } else {
                Result.failure(Exception("Failed to fetch complete route"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
