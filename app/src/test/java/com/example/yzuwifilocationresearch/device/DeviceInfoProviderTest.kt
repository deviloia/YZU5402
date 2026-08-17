package com.example.yzuwifilocationresearch.device

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceInfoProviderTest {
    @Test
    fun formatBrand_formatsCommonAndroidManufacturers() {
        assertEquals("Samsung", DeviceInfoProvider.formatBrand("samsung"))
        assertEquals("Xiaomi", DeviceInfoProvider.formatBrand("xiaomi"))
        assertEquals("Oppo", DeviceInfoProvider.formatBrand("oppo"))
        assertEquals("HTC", DeviceInfoProvider.formatBrand("HTC"))
    }

    @Test
    fun formatBrand_handlesBlankValuesWithoutFakeBrand() {
        assertEquals("", DeviceInfoProvider.formatBrand(""))
        assertEquals("", DeviceInfoProvider.formatBrand("   "))
        assertEquals("", DeviceInfoProvider.formatBrand(null))
    }
}
