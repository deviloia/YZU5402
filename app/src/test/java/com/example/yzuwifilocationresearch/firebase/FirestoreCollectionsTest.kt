package com.example.yzuwifilocationresearch.firebase

import org.junit.Assert.assertEquals
import org.junit.Test

class FirestoreCollectionsTest {
    @Test
    fun collectionNames_matchProjectSchema() {
        assertEquals("locations", FirestoreCollections.LOCATIONS)
        assertEquals("fingerprintSamples", FirestoreCollections.FINGERPRINT_SAMPLES)
        assertEquals("testResults", FirestoreCollections.TEST_RESULTS)
    }
}
