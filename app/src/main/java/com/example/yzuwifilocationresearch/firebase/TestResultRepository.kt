package com.example.yzuwifilocationresearch.firebase

import com.example.yzuwifilocationresearch.model.TestResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TestResultRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val testResults = firestore.collection(FirestoreCollections.TEST_RESULTS)

    suspend fun addTestResult(result: TestResult): String {
        if (result.documentId.isNotBlank()) {
            testResults
                .document(result.documentId)
                .set(FirestoreMappers.testResultToMap(result))
                .await()
            return result.documentId
        }

        val document = testResults.document()
        val resultWithDocumentId = result.copy(documentId = document.id)
        document
            .set(FirestoreMappers.testResultToMap(resultWithDocumentId))
            .await()
        return document.id
    }

    suspend fun getTestResult(documentId: String): TestResult? {
        require(documentId.isNotBlank()) { "documentId must not be blank." }

        val snapshot = testResults.document(documentId).get().await()
        val data = snapshot.data ?: return null
        return FirestoreMappers.mapToTestResult(data)
    }

    suspend fun getAllTestResults(): List<TestResult> {
        return testResults
            .get()
            .await()
            .documents
            .mapNotNull { snapshot ->
                snapshot.data?.let(FirestoreMappers::mapToTestResult)
            }
    }
}
