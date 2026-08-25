package com.example.yzuwifilocationresearch.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * 用 WebView 載入 Google Maps 的免 API Key 嵌入網址（output=embed），
 * 效果跟網頁版的 <iframe> 嵌入地圖一樣，不需要申請 Google Maps API Key。
 * 座標為 null 時不顯示（呼叫端應該改顯示 MapPlaceholderCard）。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebMapCard(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    height: Int = 220,
    zoom: Int = 18
) {
    val url = "https://maps.google.com/maps?q=$latitude,$longitude&z=$zoom&output=embed"

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(14.dp)),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                loadUrl(url)
            }
        },
        update = { webView ->
            webView.loadUrl(url)
        }
    )
}
