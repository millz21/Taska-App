package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.data.SavedLocationEntity
import com.example.ui.theme.TaskaBorder
import com.example.ui.theme.TaskaDeepInk
import com.example.ui.theme.TaskaInk
import com.example.ui.theme.TaskaLime
import com.example.ui.theme.TaskaMutedText
import com.example.ui.theme.TaskaSurfaceAlt
import com.example.ui.theme.TaskaViolet
import com.example.ui.theme.TaskaVioletSoft
import com.example.ui.theme.TaskaWhite
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenBg
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.cos
import kotlin.math.sin

data class DetectedRealLocation(
    val areaName: String,
    val fullAddress: String,
    val latitude: Double,
    val longitude: Double,
    val source: String // "Fused GPS", "GPS", or "Live Location"
)

data class RealWorldPlaceSearchResult(
    val displayName: String,
    val shortAreaName: String,
    val latitude: Double,
    val longitude: Double
)

data class TaskLocationPin(
    val id: String,
    val title: String,
    val subtitle: String,
    val areaName: String,
    val badgeText: String,
    val latitude: Double,
    val longitude: Double,
    val isCompleted: Boolean = false
)

object RealWorldLocationAndMapHelper {

    private val knownAreaCoordinates: Map<String, Pair<Double, Double>> = mapOf(
        "kabulonga" to (-15.4192 to 28.3497),
        "woodlands" to (-15.4426 to 28.3358),
        "roma" to (-15.3742 to 28.3204),
        "olympia" to (-15.3885 to 28.3082),
        "rhodes park" to (-15.4089 to 28.3019),
        "longacres" to (-15.4165 to 28.3125),
        "northmead" to (-15.3998 to 28.2921),
        "chilenje" to (-15.4533 to 28.3248),
        "chelston" to (-15.3738 to 28.3822),
        "avondale" to (-15.3621 to 28.3954),
        "ibex hill" to (-15.4235 to 28.3842),
        "meanwood" to (-15.3412 to 28.3745),
        "handsworth" to (-15.3812 to 28.3341),
        "unza" to (-15.3924 to 28.3328),
        "east park" to (-15.3965 to 28.3361),
        "cairo road" to (-15.4167 to 28.2822),
        "lusaka" to (-15.3875 to 28.3228),
        "ndola" to (-12.9587 to 28.6366),
        "kitwe" to (-12.8024 to 28.2132),
        "livingstone" to (-17.8419 to 25.8544),
        "kabwe" to (-14.4469 to 28.4464),
        "solwezi" to (-12.1688 to 26.3894),
        "chipata" to (-13.6333 to 32.6500)
    )

    fun isGoogleMapsApiKeyConfigured(): Boolean {
        val key = runCatching { BuildConfig.MAPS_API_KEY }.getOrDefault("")
        return key.isNotBlank() &&
            key != "YOUR_GOOGLE_MAPS_API_KEY" &&
            key != "MY_MAPS_API_KEY" &&
            !key.startsWith("YOUR_")
    }

    fun hasFineOrCoarseLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Resolves a task or booking's area name into real-world coordinates using:
     * 1. Saved locations matching the area name
     * 2. Known neighbourhood/city coordinates
     * 3. Deterministic offset around the user's primary saved location so multiple tasks in the same area remain distinct on Google Maps
     */
    fun resolveTaskAreaCoordinates(
        areaName: String,
        savedLocations: List<SavedLocationEntity> = emptyList(),
        indexOffset: Int = 0
    ): Pair<Double, Double> {
        val clean = areaName.trim().lowercase(Locale.US)
        val matchedSaved = savedLocations.firstOrNull {
            clean.contains(it.areaName.lowercase(Locale.US)) ||
                clean.contains(it.label.lowercase(Locale.US))
        }
        val basePair = when {
            matchedSaved != null -> matchedSaved.latitude to matchedSaved.longitude
            else -> {
                val matchedKnown = knownAreaCoordinates.entries.firstOrNull { (key, _) ->
                    clean.contains(key)
                }?.value
                matchedKnown ?: run {
                    val primary = savedLocations.firstOrNull { it.isPrimary } ?: savedLocations.firstOrNull()
                    if (primary != null) primary.latitude to primary.longitude else (-15.3875 to 28.3228)
                }
            }
        }
        if (indexOffset == 0) return basePair
        val angleRad = (indexOffset * 1.15)
        val radiusDeg = 0.0038 * ((indexOffset % 4) + 1)
        return (basePair.first + sin(angleRad) * radiusDeg) to (basePair.second + cos(angleRad) * radiusDeg)
    }

    /**
     * Actively detects the user's real-world location:
     * 1. Uses Google Play Services FusedLocationProviderClient (`Priority.PRIORITY_HIGH_ACCURACY`) for precise location detection
     * 2. Falls back to Android LocationManager (GPS / Fused / Network)
     * 3. Falls back to real-world IP geolocation over HTTPS if device GPS hardware is unavailable
     * 4. Reverse-geocodes the exact coordinates into a real neighbourhood / street / city name
     */
    @SuppressLint("MissingPermission")
    suspend fun detectRealWorldLocation(
        context: Context,
        hasLocationPermission: Boolean
    ): DetectedRealLocation = withContext(Dispatchers.IO) {
        var detectedLat: Double? = null
        var detectedLng: Double? = null
        var source = "Fused GPS"

        val permissionGranted = hasLocationPermission || hasFineOrCoarseLocationPermission(context)

        if (permissionGranted) {
            // 1. Try Google Play Services FusedLocationProviderClient (play-services-location)
            val fusedLocation = requestFusedHighAccuracyLocation(context)
            if (fusedLocation != null && (fusedLocation.latitude != 0.0 || fusedLocation.longitude != 0.0)) {
                detectedLat = fusedLocation.latitude
                detectedLng = fusedLocation.longitude
                source = "Fused GPS"
            }

            // 2. Fallback to Android LocationManager if FusedLocationProviderClient returned null
            if (detectedLat == null || detectedLng == null) {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (locationManager != null) {
                    val activeLoc = requestFreshDeviceLocation(locationManager)
                        ?: getBestLastKnownLocation(locationManager)
                    if (activeLoc != null && (activeLoc.latitude != 0.0 || activeLoc.longitude != 0.0)) {
                        detectedLat = activeLoc.latitude
                        detectedLng = activeLoc.longitude
                        source = "GPS"
                    }
                }
            }
        }

        // 3. If hardware GPS returned no satellite lock (e.g., inside a cloud/desktop environment),
        // query real-world network geolocation so we still detect the user's real-world location.
        if (detectedLat == null || detectedLng == null) {
            val networkGeo = fetchNetworkGeolocation()
            if (networkGeo != null) {
                return@withContext networkGeo
            }
        }

        val finalLat = detectedLat ?: -15.3875
        val finalLng = detectedLng ?: 28.3228
        val (areaName, fullAddress) = reverseGeocodeCoordinates(context, finalLat, finalLng)

        DetectedRealLocation(
            areaName = areaName,
            fullAddress = fullAddress,
            latitude = finalLat,
            longitude = finalLng,
            source = source
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFusedHighAccuracyLocation(context: Context): Location? {
        val fusedClient = runCatching { LocationServices.getFusedLocationProviderClient(context) }.getOrNull()
            ?: return null

        // First request a fresh high-accuracy current location fix
        val currentFix = withTimeoutOrNull(4200L) {
            suspendCancellableCoroutine<Location?> { cont ->
                val cancellationTokenSource = CancellationTokenSource()
                cont.invokeOnCancellation {
                    runCatching { cancellationTokenSource.cancel() }
                }
                try {
                    fusedClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    ).addOnSuccessListener { location ->
                        if (cont.isActive) cont.resume(location)
                    }.addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
                } catch (_: Exception) {
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
        if (currentFix != null) return currentFix

        // Fallback to fusedClient.lastLocation
        return withTimeoutOrNull(2000L) {
            suspendCancellableCoroutine<Location?> { cont ->
                try {
                    fusedClient.lastLocation
                        .addOnSuccessListener { location ->
                            if (cont.isActive) cont.resume(location)
                        }
                        .addOnFailureListener {
                            if (cont.isActive) cont.resume(null)
                        }
                } catch (_: Exception) {
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFreshDeviceLocation(locationManager: LocationManager): Location? {
        val candidates = listOf(
            "fused",
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )
        val enabledProviders = candidates.filter { provider ->
            runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
        }
        if (enabledProviders.isEmpty()) return null

        for (provider in enabledProviders) {
            val loc = withTimeoutOrNull(3500L) {
                suspendCancellableCoroutine<Location?> { cont ->
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            locationManager.getCurrentLocation(
                                provider,
                                null,
                                { runnable -> runnable.run() },
                                { location ->
                                    if (cont.isActive) cont.resume(location)
                                }
                            )
                        } else {
                            val listener = object : LocationListener {
                                override fun onLocationChanged(location: Location) {
                                    runCatching { locationManager.removeUpdates(this) }
                                    if (cont.isActive) cont.resume(location)
                                }

                                @Deprecated("Deprecated in Java")
                                override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                                override fun onProviderEnabled(p: String) {}
                                override fun onProviderDisabled(p: String) {
                                    runCatching { locationManager.removeUpdates(this) }
                                    if (cont.isActive) cont.resume(null)
                                }
                            }
                            @Suppress("DEPRECATION")
                            locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                            cont.invokeOnCancellation {
                                runCatching { locationManager.removeUpdates(listener) }
                            }
                        }
                    } catch (_: Exception) {
                        if (cont.isActive) cont.resume(null)
                    }
                }
            }
            if (loc != null) return loc
        }
        return null
    }

    @SuppressLint("MissingPermission")
    private fun getBestLastKnownLocation(locationManager: LocationManager): Location? {
        val providers = listOf(
            "fused",
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        return providers.mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }.maxByOrNull { it.time }
    }

    private fun fetchNetworkGeolocation(): DetectedRealLocation? {
        try {
            val conn = (URL("https://ipwho.is/").openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "TaskaAndroidApp/1.0")
            }
            if (conn.responseCode in 200..299) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                if (json.optBoolean("success", true)) {
                    val lat = json.optDouble("latitude", Double.NaN)
                    val lng = json.optDouble("longitude", Double.NaN)
                    val city = json.optString("city").trim()
                    val region = json.optString("region").trim()
                    val country = json.optString("country").trim()
                    if (!lat.isNaN() && !lng.isNaN()) {
                        val nominatimPair = reverseGeocodeViaNominatim(lat, lng)
                        val areaName = nominatimPair?.first?.takeIf { it.isNotBlank() }
                            ?: listOf(city, region).filter { it.isNotBlank() }.joinToString(", ")
                                .ifBlank { "Detected Location" }
                        val fullAddr = nominatimPair?.second?.takeIf { it.isNotBlank() }
                            ?: listOf(city, region, country).filter { it.isNotBlank() }.joinToString(", ")
                        return DetectedRealLocation(
                            areaName = areaName,
                            fullAddress = fullAddr,
                            latitude = lat,
                            longitude = lng,
                            source = "Live Location"
                        )
                    }
                }
            }
        } catch (_: Exception) {
        }

        try {
            val conn = (URL("https://ipapi.co/json/").openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "TaskaAndroidApp/1.0")
            }
            if (conn.responseCode in 200..299) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val lat = json.optDouble("latitude", Double.NaN)
                val lng = json.optDouble("longitude", Double.NaN)
                val city = json.optString("city").trim()
                val region = json.optString("region").trim()
                val country = json.optString("country_name").trim()
                if (!lat.isNaN() && !lng.isNaN()) {
                    val areaName = listOf(city, region).filter { it.isNotBlank() }.joinToString(", ")
                        .ifBlank { "Detected Location" }
                    val fullAddr = listOf(city, region, country).filter { it.isNotBlank() }.joinToString(", ")
                    return DetectedRealLocation(
                        areaName = areaName,
                        fullAddress = fullAddr,
                        latitude = lat,
                        longitude = lng,
                        source = "Live Location"
                    )
                }
            }
        } catch (_: Exception) {
        }
        return null
    }

    /**
     * Reverse-geocodes (lat, lng) into (shortAreaName, fullDisplayAddress) using
     * OpenStreetMap Nominatim + Android Geocoder.
     */
    suspend fun reverseGeocodeCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val nominatimResult = reverseGeocodeViaNominatim(latitude, longitude)
        if (nominatimResult != null && nominatimResult.first.isNotBlank()) {
            return@withContext nominatimResult
        }

        try {
            @Suppress("DEPRECATION")
            val addresses = Geocoder(context, Locale.getDefault()).getFromLocation(latitude, longitude, 1)
            val addr = addresses?.firstOrNull()
            if (addr != null) {
                val parts = listOfNotNull(
                    addr.subLocality,
                    addr.thoroughfare,
                    addr.locality,
                    addr.subAdminArea,
                    addr.adminArea
                ).map { it.trim() }.filter { it.isNotBlank() }.distinct()

                val shortArea = parts.take(2).joinToString(", ")
                val fullLine = (0..addr.maxAddressLineIndex)
                    .mapNotNull { idx -> addr.getAddressLine(idx) }
                    .joinToString(", ")
                    .ifBlank { parts.joinToString(", ") }

                if (shortArea.isNotBlank()) {
                    return@withContext shortArea to fullLine
                }
            }
        } catch (_: Exception) {
        }

        val coordLabel = String.format(Locale.US, "Pin (%.4f, %.4f)", latitude, longitude)
        coordLabel to coordLabel
    }

    private fun reverseGeocodeViaNominatim(latitude: Double, longitude: Double): Pair<String, String>? {
        return try {
            val url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=$latitude&lon=$longitude&zoom=18&addressdetails=1"
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4500
                readTimeout = 4500
                requestMethod = "GET"
                setRequestProperty("User-Agent", "TaskaMarketplaceApp/1.0 (Android)")
                setRequestProperty("Accept-Language", Locale.getDefault().language.ifBlank { "en" })
            }
            if (conn.responseCode in 200..299) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val displayName = json.optString("display_name").trim()
                val address = json.optJSONObject("address")
                if (address != null) {
                    val primaryPart = listOf(
                        address.optString("suburb"),
                        address.optString("neighbourhood"),
                        address.optString("residential"),
                        address.optString("quarter"),
                        address.optString("road"),
                        address.optString("amenity"),
                        address.optString("village"),
                        address.optString("town")
                    ).map { it.trim() }.firstOrNull { it.isNotBlank() }.orEmpty()

                    val cityPart = listOf(
                        address.optString("city"),
                        address.optString("town"),
                        address.optString("municipality"),
                        address.optString("county"),
                        address.optString("state")
                    ).map { it.trim() }.firstOrNull { it.isNotBlank() && !it.equals(primaryPart, ignoreCase = true) }.orEmpty()

                    val shortArea = listOf(primaryPart, cityPart)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                        .ifBlank {
                            displayName.split(",").take(2).joinToString(",").trim()
                        }

                    if (shortArea.isNotBlank()) {
                        return shortArea to displayName.ifBlank { shortArea }
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Searches real-world places, streets, neighbourhoods, landmarks, or cities via OpenStreetMap Nominatim.
     */
    suspend fun searchRealWorldPlaces(query: String): List<RealWorldPlaceSearchResult> =
        withContext(Dispatchers.IO) {
            val clean = query.trim()
            if (clean.length < 2) return@withContext emptyList()
            try {
                val encoded = URLEncoder.encode(clean, "UTF-8")
                val url = "https://nominatim.openstreetmap.org/search?format=jsonv2&q=$encoded&limit=5&addressdetails=1"
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 4500
                    readTimeout = 4500
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "TaskaMarketplaceApp/1.0 (Android)")
                    setRequestProperty("Accept-Language", Locale.getDefault().language.ifBlank { "en" })
                }
                if (conn.responseCode in 200..299) {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val arr = JSONArray(body)
                    val results = mutableListOf<RealWorldPlaceSearchResult>()
                    for (i in 0 until arr.length()) {
                        val item = arr.optJSONObject(i) ?: continue
                        val lat = item.optString("lat").toDoubleOrNull() ?: continue
                        val lon = item.optString("lon").toDoubleOrNull() ?: continue
                        val displayName = item.optString("display_name").trim()
                        val name = item.optString("name").trim()
                        val shortArea = if (name.isNotBlank()) {
                            val secondToken = displayName.split(",")
                                .map { it.trim() }
                                .firstOrNull { it.isNotBlank() && !it.equals(name, ignoreCase = true) }
                            if (secondToken != null) "$name, $secondToken" else name
                        } else {
                            displayName.split(",").take(2).joinToString(", ").trim()
                        }
                        results.add(
                            RealWorldPlaceSearchResult(
                                displayName = displayName,
                                shortAreaName = shortArea.ifBlank { displayName },
                                latitude = lat,
                                longitude = lon
                            )
                        )
                    }
                    return@withContext results
                }
            } catch (_: Exception) {
            }
            emptyList()
        }

    /**
     * Opens the exact real-world coordinates in Google Maps or the user's external maps application,
     * with a seamless browser fallback to Google Maps if no native geo handler is installed.
     */
    fun openInExternalRealWorldMap(
        context: Context,
        latitude: Double,
        longitude: Double,
        label: String
    ) {
        val cleanLabel = label.trim().ifBlank { "Selected Location" }
        val encodedLabel = Uri.encode(cleanLabel)
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
        val geoIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(geoIntent)
        } catch (_: Exception) {
            try {
                val webMapUri = Uri.parse(
                    "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
                )
                val webIntent = Intent(Intent.ACTION_VIEW, webMapUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (_: Exception) {
            }
        }
    }
}

/**
 * Interactive Google Maps SDK for Android card for viewing Task & Booking locations across the app.
 * Renders a native `GoogleMap` composable with markers for each task location, interactive camera
 * panning/zooming, map type selector (Normal / Hybrid / Terrain), and a selected task callout bar.
 */
@Composable
fun TaskLocationsGoogleMapCard(
    sectionTitle: String,
    sectionSubtitle: String,
    selectedArea: String,
    taskPins: List<TaskLocationPin>,
    savedLocations: List<SavedLocationEntity> = emptyList(),
    onSelectTaskPin: (TaskLocationPin) -> Unit = {},
    onOpenFullLocationMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val defaultCenter = remember(selectedArea, savedLocations, taskPins) {
        val firstPin = taskPins.firstOrNull()
        if (firstPin != null) {
            LatLng(firstPin.latitude, firstPin.longitude)
        } else {
            val (lat, lng) = RealWorldLocationAndMapHelper.resolveTaskAreaCoordinates(
                areaName = selectedArea,
                savedLocations = savedLocations
            )
            LatLng(lat, lng)
        }
    }

    var selectedPin by remember(taskPins) { mutableStateOf(taskPins.firstOrNull()) }
    var googleMapType by remember { mutableStateOf(MapType.NORMAL) }
    var useGoogleMapsSdkLayer by remember { mutableStateOf(true) }

    val hasLocationPermission = remember(context) {
        RealWorldLocationAndMapHelper.hasFineOrCoarseLocationPermission(context)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 13.5f)
    }

    LaunchedEffect(defaultCenter.latitude, defaultCenter.longitude) {
        runCatching {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(defaultCenter, 13.5f),
                durationMs = 550
            )
        }
    }

    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_locations_google_map_card"),
        cornerRadius = 22.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = TaskaViolet,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = sectionTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TaskaInk
                        )
                    }
                    Text(
                        text = sectionSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TaskaMutedText
                    )
                }

                Surface(
                    color = TaskaVioletSoft,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { useGoogleMapsSdkLayer = !useGoogleMapsSdkLayer }
                        .testTag("toggle_map_engine_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Switch map view",
                            tint = TaskaViolet,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (useGoogleMapsSdkLayer) "Google Maps SDK" else "Street Map",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TaskaViolet
                        )
                    }
                }
            }

            // Interactive Map Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, TaskaBorder, RoundedCornerShape(18.dp))
                    .testTag("task_locations_google_map_view")
            ) {
                if (useGoogleMapsSdkLayer) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = hasLocationPermission,
                            mapType = googleMapType
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = hasLocationPermission,
                            compassEnabled = true,
                            mapToolbarEnabled = true
                        ),
                        onMapClick = { latLng ->
                            // Center camera smoothly on tap
                            coroutineScope.launch {
                                runCatching {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLng(latLng),
                                        400
                                    )
                                }
                            }
                        }
                    ) {
                        // Search radius circle around active area center
                        Circle(
                            center = defaultCenter,
                            radius = 2200.0,
                            fillColor = Color(0x1F5B3DF5),
                            strokeColor = Color(0xFF5B3DF5),
                            strokeWidth = 3f
                        )

                        // Render task location markers on Google Maps
                        taskPins.forEach { pin ->
                            val markerLatLng = LatLng(pin.latitude, pin.longitude)
                            val markerHue = if (pin.isCompleted) {
                                BitmapDescriptorFactory.HUE_GREEN
                            } else if (selectedPin?.id == pin.id) {
                                BitmapDescriptorFactory.HUE_VIOLET
                            } else {
                                BitmapDescriptorFactory.HUE_AZURE
                            }
                            Marker(
                                state = remember(pin.id, pin.latitude, pin.longitude) {
                                    MarkerState(position = markerLatLng)
                                },
                                title = pin.title,
                                snippet = "${pin.areaName} • ${pin.badgeText}",
                                icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                                onClick = {
                                    selectedPin = pin
                                    false
                                }
                            )
                        }

                        // Saved user locations markers
                        savedLocations.forEach { loc ->
                            Marker(
                                state = remember("saved_${loc.id}", loc.latitude, loc.longitude) {
                                    MarkerState(position = LatLng(loc.latitude, loc.longitude))
                                },
                                title = loc.label,
                                snippet = loc.areaName,
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                            )
                        }
                    }

                    // Map Type Cycle Pill overlay (Normal / Hybrid / Terrain)
                    Surface(
                        color = TaskaDeepInk.copy(alpha = 0.80f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                googleMapType = when (googleMapType) {
                                    MapType.NORMAL -> MapType.HYBRID
                                    MapType.HYBRID -> MapType.TERRAIN
                                    else -> MapType.NORMAL
                                }
                            }
                            .testTag("google_map_type_button")
                    ) {
                        Text(
                            text = when (googleMapType) {
                                MapType.HYBRID -> "Map: Hybrid"
                                MapType.TERRAIN -> "Map: Terrain"
                                else -> "Map: Standard"
                            },
                            color = TaskaWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                } else {
                    val activeLat = selectedPin?.latitude ?: defaultCenter.latitude
                    val activeLng = selectedPin?.longitude ?: defaultCenter.longitude
                    val activeLabel = selectedPin?.title ?: selectedArea.ifBlank { "Task Area" }
                    LeafletStreetMapWebView(
                        latitude = activeLat,
                        longitude = activeLng,
                        pinTitle = activeLabel,
                        taskPins = taskPins,
                        onPinMovedOnMap = { _, _ -> },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Task Marker Quick-Selector Chips
            if (taskPins.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    taskPins.take(3).forEach { pin ->
                        val isSelected = selectedPin?.id == pin.id
                        Surface(
                            color = if (isSelected) TaskaViolet else TaskaSurfaceAlt,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, if (isSelected) TaskaViolet else TaskaBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    selectedPin = pin
                                    coroutineScope.launch {
                                        runCatching {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(pin.latitude, pin.longitude),
                                                    15f
                                                ),
                                                450
                                            )
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isSelected) TaskaLime else TaskaViolet,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = pin.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TaskaWhite else TaskaInk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Selected Task Location Detail Bar + Actions
            val currentPin = selectedPin
            if (currentPin != null) {
                Surface(
                    color = TaskaSurfaceAlt,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, TaskaBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = currentPin.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TaskaInk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    color = if (currentPin.isCompleted) VerifiedGreenBg else TaskaVioletSoft,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = currentPin.badgeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentPin.isCompleted) VerifiedGreen else TaskaViolet,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${currentPin.areaName} · (${String.format(Locale.US, "%.4f, %.4f", currentPin.latitude, currentPin.longitude)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TaskaMutedText
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    RealWorldLocationAndMapHelper.openInExternalRealWorldMap(
                                        context = context,
                                        latitude = currentPin.latitude,
                                        longitude = currentPin.longitude,
                                        label = "${currentPin.title} (${currentPin.areaName})"
                                    )
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(TaskaVioletSoft)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open in Google Maps",
                                    tint = TaskaViolet,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Button(
                                onClick = { onSelectTaskPin(currentPin) },
                                colors = ButtonDefaults.buttonColors(containerColor = TaskaViolet),
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Select", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Centered on ${selectedArea.ifBlank { "Lusaka" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TaskaMutedText
                    )
                    OutlinedButton(
                        onClick = onOpenFullLocationMap,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, TaskaViolet),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Manage Map Pins",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TaskaViolet
                        )
                    }
                }
            }
        }
    }
}

private class TaskaMapJsBridge(
    private val onPinChanged: (Double, Double) -> Unit
) {
    @JavascriptInterface
    fun onMapPinMoved(lat: Double, lng: Double) {
        onPinChanged(lat, lng)
    }
}

/**
 * Interactive real-world map powered by the Google Maps SDK for Android (`GoogleMap` Compose)
 * with a built-in layer switcher for Leaflet/OpenStreetMap street tiles.
 * Users can pan, zoom, tap anywhere on the map, or drag the marker to drop a custom pin and view task locations.
 */
@Composable
fun InteractiveRealWorldMapView(
    latitude: Double,
    longitude: Double,
    pinTitle: String,
    onPinMovedOnMap: (Double, Double) -> Unit,
    taskPins: List<TaskLocationPin> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasLocationPermission = remember(context) {
        RealWorldLocationAndMapHelper.hasFineOrCoarseLocationPermission(context)
    }
    var useGoogleMapsSdk by remember { mutableStateOf(true) }
    var googleMapType by remember { mutableStateOf(MapType.NORMAL) }

    val pinLatLng = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pinLatLng, 15f)
    }

    LaunchedEffect(latitude, longitude) {
        runCatching {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 15f),
                durationMs = 500
            )
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, TaskaBorder, RoundedCornerShape(18.dp))
            .background(Color(0xFFE8EEF5))
    ) {
        if (useGoogleMapsSdk) {
            val markerState = remember(latitude, longitude) {
                MarkerState(position = LatLng(latitude, longitude))
            }

            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("google_maps_sdk_composable"),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapType = googleMapType
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = hasLocationPermission,
                    compassEnabled = true,
                    mapToolbarEnabled = true
                ),
                onMapClick = { clickedLatLng ->
                    onPinMovedOnMap(clickedLatLng.latitude, clickedLatLng.longitude)
                }
            ) {
                // Active dropped pin marker
                Marker(
                    state = markerState,
                    title = pinTitle.ifBlank { "Selected Pin Location" },
                    snippet = String.format(Locale.US, "%.4f, %.4f", latitude, longitude),
                    draggable = true,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
                )

                // Search radius circle around dropped pin
                Circle(
                    center = pinLatLng,
                    radius = 400.0,
                    fillColor = Color(0x225B3DF5),
                    strokeColor = Color(0xFF5B3DF5),
                    strokeWidth = 3f
                )

                // Task location pins on the map
                taskPins.forEach { taskPin ->
                    Marker(
                        state = remember(taskPin.id, taskPin.latitude, taskPin.longitude) {
                            MarkerState(position = LatLng(taskPin.latitude, taskPin.longitude))
                        },
                        title = taskPin.title,
                        snippet = "${taskPin.areaName} • ${taskPin.badgeText}",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (taskPin.isCompleted) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_AZURE
                        )
                    )
                }
            }

            // Map Type Toggle Pill (Standard / Hybrid / Terrain)
            Surface(
                color = TaskaDeepInk.copy(alpha = 0.82f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        googleMapType = when (googleMapType) {
                            MapType.NORMAL -> MapType.HYBRID
                            MapType.HYBRID -> MapType.TERRAIN
                            else -> MapType.NORMAL
                        }
                    }
            ) {
                Text(
                    text = when (googleMapType) {
                        MapType.HYBRID -> "Google Maps: Hybrid"
                        MapType.TERRAIN -> "Google Maps: Terrain"
                        else -> "Google Maps: Standard"
                    },
                    color = TaskaWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        } else {
            LeafletStreetMapWebView(
                latitude = latitude,
                longitude = longitude,
                pinTitle = pinTitle,
                taskPins = taskPins,
                onPinMovedOnMap = onPinMovedOnMap,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Layer Switcher Button (Google Maps SDK <-> Street Map Tiles)
        Surface(
            color = TaskaWhite.copy(alpha = 0.94f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, TaskaBorder),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(50))
                .clickable { useGoogleMapsSdk = !useGoogleMapsSdk }
                .testTag("switch_map_layer_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Switch map layer",
                    tint = TaskaViolet,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (useGoogleMapsSdk) "Switch to Street Tiles" else "Switch to Google Maps SDK",
                    color = TaskaInk,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
private fun LeafletStreetMapWebView(
    latitude: Double,
    longitude: Double,
    pinTitle: String,
    taskPins: List<TaskLocationPin> = emptyList(),
    onPinMovedOnMap: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val latestOnPinMoved by rememberUpdatedState(onPinMovedOnMap)
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var pageLoaded by remember { mutableStateOf(false) }

    val taskMarkersJs = remember(taskPins) {
        taskPins.joinToString("\n") { pin ->
            val safeTitle = pin.title.replace("'", "\\'").replace("\n", " ")
            val safeSub = "${pin.areaName} • ${pin.badgeText}".replace("'", "\\'").replace("\n", " ")
            "L.marker([${pin.latitude}, ${pin.longitude}]).addTo(map).bindPopup('<b>$safeTitle</b><br/>$safeSub');"
        }
    }

    val mapHtml = remember(taskMarkersJs) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: #e8eef5; font-family: sans-serif; }
                #map { width: 100%; height: 100%; }
                .custom-pin {
                    width: 26px;
                    height: 26px;
                    border-radius: 50% 50% 50% 0;
                    background: #5B3DF5;
                    position: absolute;
                    transform: rotate(-45deg);
                    left: 50%;
                    top: 50%;
                    margin: -20px 0 0 -13px;
                    box-shadow: 0 4px 10px rgba(91, 61, 245, 0.45);
                    border: 3px solid #ffffff;
                }
                .custom-pin::after {
                    content: '';
                    width: 10px;
                    height: 10px;
                    margin: 8px 0 0 8px;
                    background: #D4F358;
                    position: absolute;
                    border-radius: 50%;
                }
                .leaflet-control-attribution { font-size: 9px !important; opacity: 0.75; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var initLat = $latitude;
                var initLng = $longitude;
                var map = L.map('map', { zoomControl: true }).setView([initLat, initLng], 14);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '&copy; OpenStreetMap'
                }).addTo(map);

                var pinIcon = L.divIcon({
                    className: 'custom-div-icon',
                    html: "<div class='custom-pin'></div>",
                    iconSize: [30, 42],
                    iconAnchor: [15, 42]
                });

                var marker = L.marker([initLat, initLng], { draggable: true, icon: pinIcon }).addTo(map);
                var accuracyCircle = L.circle([initLat, initLng], {
                    color: '#5B3DF5',
                    fillColor: '#5B3DF5',
                    fillOpacity: 0.10,
                    weight: 1.5,
                    radius: 350
                }).addTo(map);

                $taskMarkersJs

                function notifyAndroid(lat, lng) {
                    marker.setLatLng([lat, lng]);
                    accuracyCircle.setLatLng([lat, lng]);
                    if (window.AndroidMapBridge && window.AndroidMapBridge.onMapPinMoved) {
                        window.AndroidMapBridge.onMapPinMoved(lat, lng);
                    }
                }

                map.on('click', function(e) {
                    notifyAndroid(e.latlng.lat, e.latlng.lng);
                });

                marker.on('dragend', function(e) {
                    var pos = marker.getLatLng();
                    notifyAndroid(pos.lat, pos.lng);
                });

                function updatePinFromAndroid(lat, lng, label) {
                    var current = marker.getLatLng();
                    var dist = Math.abs(current.lat - lat) + Math.abs(current.lng - lng);
                    marker.setLatLng([lat, lng]);
                    accuracyCircle.setLatLng([lat, lng]);
                    if (dist > 0.0003) {
                        map.flyTo([lat, lng], Math.max(map.getZoom(), 14), { duration: 0.6 });
                    }
                    if (label && label.length > 0) {
                        marker.bindPopup("<b>" + label + "</b>").openPopup();
                    }
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    LaunchedEffect(latitude, longitude, pinTitle, pageLoaded) {
        if (pageLoaded) {
            val safeLabel = pinTitle.replace("'", "\\'").replace("\n", " ")
            webViewRef?.evaluateJavascript(
                "if (typeof updatePinFromAndroid === 'function') { updatePinFromAndroid($latitude, $longitude, '$safeLabel'); }",
                null
            )
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                settings.userAgentString = "TaskaMarketplaceApp/1.0 (Android)"
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        pageLoaded = true
                    }
                }
                addJavascriptInterface(
                    TaskaMapJsBridge { lat, lng ->
                        post {
                            latestOnPinMoved(lat, lng)
                        }
                    },
                    "AndroidMapBridge"
                )
                setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    false
                }
                loadDataWithBaseURL(
                    "https://openstreetmap.org/",
                    mapHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
                webViewRef = this
            }
        },
        modifier = modifier
    )
}
