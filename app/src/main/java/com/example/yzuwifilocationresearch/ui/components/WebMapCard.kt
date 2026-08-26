package com.example.yzuwifilocationresearch.ui.components

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private val AppTileSource = XYTileSource(
    "CartoDB Positron",
    0,
    20,
    256,
    ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/light_all/",
        "https://b.basemaps.cartocdn.com/light_all/",
        "https://c.basemaps.cartocdn.com/light_all/",
        "https://d.basemaps.cartocdn.com/light_all/"
    ),
    "Map data OpenStreetMap contributors, CARTO"
)

@Composable
fun WebMapCard(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    height: Int = 220,
    zoom: Int = 18,
    groundTruthLatitude: Double? = null,
    groundTruthLongitude: Double? = null
) {
    val context = LocalContext.current
    val gpsPoint = GeoPoint(latitude, longitude)
    val groundTruthPoint = if (groundTruthLatitude != null && groundTruthLongitude != null) {
        GeoPoint(groundTruthLatitude, groundTruthLongitude)
    } else {
        null
    }
    val centerPoint = if (groundTruthPoint != null) {
        GeoPoint(
            (gpsPoint.latitude + groundTruthPoint.latitude) / 2.0,
            (gpsPoint.longitude + groundTruthPoint.longitude) / 2.0
        )
    } else {
        gpsPoint
    }
    val gpsIcon = createPinDrawable(context.resources, 0xFFE5484D.toInt(), "G")
    val groundTruthIcon = createPinDrawable(context.resources, 0xFF2D6CDF.toInt(), "T")

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE8EEF5)),
        factory = { appContext ->
            Configuration.getInstance().userAgentValue =
                "YZUWifiLocationResearch/${appContext.packageName}"
            MapView(appContext).apply {
                setTileSource(AppTileSource)
                setMultiTouchControls(true)
                minZoomLevel = 3.0
                maxZoomLevel = 20.0
                controller.setZoom(zoom.toDouble())
                controller.setCenter(centerPoint)
                setMapMarkers(this, gpsPoint, groundTruthPoint, gpsIcon, groundTruthIcon)
            }
        },
        update = { mapView ->
            mapView.controller.setZoom(zoom.toDouble())
            mapView.controller.setCenter(centerPoint)
            setMapMarkers(mapView, gpsPoint, groundTruthPoint, gpsIcon, groundTruthIcon)
            mapView.invalidate()
        }
    )
}

private fun setMapMarkers(
    mapView: MapView,
    gpsPoint: GeoPoint,
    groundTruthPoint: GeoPoint?,
    gpsIcon: BitmapDrawable,
    groundTruthIcon: BitmapDrawable
) {
    mapView.overlays.clear()
    mapView.overlays.add(
        Marker(mapView).apply {
            position = gpsPoint
            icon = gpsIcon
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "GPS 原始位置"
        }
    )

    if (groundTruthPoint != null) {
        mapView.overlays.add(
            Marker(mapView).apply {
                position = groundTruthPoint
                icon = groundTruthIcon
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Ground Truth 人工校正位置"
            }
        )
    }
}

private fun createPinDrawable(
    resources: Resources,
    color: Int,
    label: String
): BitmapDrawable {
    val width = 64
    val height = 80
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.color = color
    canvas.drawCircle(width / 2f, 28f, 24f, paint)
    val path = Path().apply {
        moveTo(width / 2f - 13f, 45f)
        lineTo(width / 2f + 13f, 45f)
        lineTo(width / 2f, 76f)
        close()
    }
    canvas.drawPath(path, paint)

    paint.color = android.graphics.Color.WHITE
    paint.textSize = 24f
    paint.typeface = Typeface.DEFAULT_BOLD
    paint.textAlign = Paint.Align.CENTER
    val textY = 36f - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(label, width / 2f, textY, paint)

    return BitmapDrawable(resources, bitmap)
}
