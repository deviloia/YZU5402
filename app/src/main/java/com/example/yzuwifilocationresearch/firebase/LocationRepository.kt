package com.example.yzuwifilocationresearch.firebase

import com.example.yzuwifilocationresearch.model.LocationPoint
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LocationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val locations = firestore.collection(FirestoreCollections.LOCATIONS)

    suspend fun saveLocation(location: LocationPoint) {
        require(location.locationId.isNotBlank()) { "locationId must not be blank." }

        locations
            .document(location.locationId)
            .set(FirestoreMappers.locationToMap(location))
            .await()
    }

    suspend fun getLocation(locationId: String): LocationPoint? {
        require(locationId.isNotBlank()) { "locationId must not be blank." }

        val snapshot = locations.document(locationId).get().await()
        val data = snapshot.data ?: return null
        return FirestoreMappers.mapToLocation(data)
    }

    suspend fun updateLocation(location: LocationPoint) {
        require(location.locationId.isNotBlank()) { "locationId must not be blank." }

        locations
            .document(location.locationId)
            .set(FirestoreMappers.locationToMap(location))
            .await()
    }

    suspend fun getAllLocations(): List<LocationPoint> {
        return locations
            .get()
            .await()
            .documents
            .mapNotNull { snapshot ->
                snapshot.data?.let(FirestoreMappers::mapToLocation)
            }
    }
}
