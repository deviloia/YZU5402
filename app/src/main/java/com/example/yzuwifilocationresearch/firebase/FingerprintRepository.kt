package com.example.yzuwifilocationresearch.firebase

import com.example.yzuwifilocationresearch.model.FingerprintSample
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FingerprintRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val fingerprints = firestore.collection(FirestoreCollections.FINGERPRINT_SAMPLES)

    suspend fun addFingerprint(sample: FingerprintSample): String {
        if (sample.documentId.isNotBlank()) {
            fingerprints
                .document(sample.documentId)
                .set(FirestoreMappers.fingerprintToMap(sample))
                .await()
            return sample.documentId
        }

        val document = fingerprints.document()
        val sampleWithDocumentId = sample.copy(documentId = document.id)
        document
            .set(FirestoreMappers.fingerprintToMap(sampleWithDocumentId))
            .await()
        return document.id
    }

    suspend fun getFingerprint(documentId: String): FingerprintSample? {
        require(documentId.isNotBlank()) { "documentId must not be blank." }

        val snapshot = fingerprints.document(documentId).get().await()
        val data = snapshot.data ?: return null
        return FirestoreMappers.mapToFingerprint(data)
    }

    suspend fun getAllFingerprints(): List<FingerprintSample> {
        return fingerprints
            .get()
            .await()
            .documents
            .mapNotNull { snapshot ->
                snapshot.data?.let(FirestoreMappers::mapToFingerprint)
            }
    }
}
