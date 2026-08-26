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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebMapCard(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    height: Int = 220,
    zoom: Int = 18
) {
    val mapUrl = "https://www.google.com/maps?q=$latitude,$longitude&z=$zoom&output=embed"
    val html = """
        <!doctype html>
        <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    html, body, iframe {
                        width: 100%;
                        height: 100%;
                        margin: 0;
                        padding: 0;
                        border: 0;
                        overflow: hidden;
                    }
                </style>
            </head>
            <body>
                <iframe
                    src="$mapUrl"
                    width="100%"
                    height="100%"
                    loading="lazy"
                    referrerpolicy="no-referrer-when-downgrade">
                </iframe>
            </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(14.dp)),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                loadMapHtml(html)
            }
        },
        update = { webView ->
            webView.loadMapHtml(html)
        }
    )
}

private fun WebView.loadMapHtml(html: String) {
    loadDataWithBaseURL(
        "https://www.google.com",
        html,
        "text/html",
        "UTF-8",
        null
    )
}
