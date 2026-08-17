package com.example.yzuwifilocationresearch.device

import android.os.Build
import com.example.yzuwifilocationresearch.model.DeviceInfo
import java.util.Locale

object DeviceInfoProvider {
    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceBrand = formatBrand(Build.MANUFACTURER),
            deviceModel = cleanSystemValue(Build.MODEL),
            androidVersion = cleanSystemValue(Build.VERSION.RELEASE),
            apiLevel = Build.VERSION.SDK_INT
        )
    }

    fun formatBrand(rawBrand: String?): String {
        val value = cleanSystemValue(rawBrand)
        if (value.isBlank()) return ""

        return when (value.lowercase(Locale.US)) {
            "htc" -> "HTC"
            else -> value.lowercase(Locale.US).replaceFirstChar { firstChar ->
                if (firstChar.isLowerCase()) firstChar.titlecase(Locale.US) else firstChar.toString()
            }
        }
    }

    private fun cleanSystemValue(value: String?): String {
        return value?.trim().orEmpty()
    }
}
