@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.myapplication.screens

import android.Manifest

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.firebase.FirebaseStorageService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.ui.AppThemeColors
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale
import org.osmdroid.events.MapEventsReceiver
// Basit koordinat
data class LL(val latitude: Double, val longitude: Double)

/* ====================== Screen ====================== */
@Composable
fun CreatePostScreen(
    onPostCreated: () -> Unit,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()

    var placeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var stars by remember { mutableStateOf(3) } // 0..5
    val images = remember { mutableStateListOf<Uri>() }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var selectedAddress by remember { mutableStateOf("") }
    var selectedLatLng by remember { mutableStateOf<LL?>(null) }

    // İzinler
    val locationPerms = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* sonrası onClick içinde kontrol */ }

    // Foto seçici
    val pickMultiple = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris -> if (uris != null) images.addAll(uris) }
    val openMultiple = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> images.addAll(uris) }

    fun requestImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickMultiple.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else openMultiple.launch(arrayOf("image/*"))
    }

    val fieldBg = if (isDark) cs.surfaceVariant.copy(alpha = 0.25f) else Color(0xFFF1F2F4)
    var showMap by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.Close, null) }
                },
                title = { Text("Yeni Gönderi", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppThemeColors.extra.topBar,
                    titleContentColor = AppThemeColors.extra.onTopBar,
                    navigationIconContentColor = AppThemeColors.extra.onTopBar
                )
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp, shadowElevation = 8.dp, color = cs.surface) {
                Column(
                    Modifier.navigationBarsPadding().padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (placeName.isBlank() || description.isBlank()) {
                                error = "Lütfen mekan adı ve açıklama girin."
                                return@Button
                            }
                            error = null
                            isSaving = true
                            val first = images.firstOrNull()

                            fun save(url: String?) {
                                FirestoreService.createPost(
                                    title = placeName,
                                    description = description,
                                    rating = stars * 2,                         // 0..10
                                    photoUrl = url,
                                    location = selectedAddress.ifBlank { null }
                                ) { ok ->
                                    isSaving = false
                                    if (ok) onPostCreated() else error = "Kaydedilemedi."
                                }
                            }

                            if (first != null) {
                                FirebaseStorageService.uploadImage(context, first) { url -> save(url) }
                            } else save(null)
                        },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth().height(52.dp).imePadding(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = cs.onPrimary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp)
                            )
                        } else Text("Paylaş")
                    }
                }
            }
        },
        containerColor = cs.background
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp).verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionLabel("Mekan Adı")
            TextField(
                value = placeName, onValueChange = { placeName = it },
                placeholder = { Text("Mekan ismi ya da konum yazın...") }, singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldBg, unfocusedContainerColor = fieldBg,
                    disabledContainerColor = fieldBg, focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            SectionLabel("Açıklama")
            TextField(
                value = description, onValueChange = { description = it },
                placeholder = { Text("Deneyimlerini paylaş…") },
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldBg, unfocusedContainerColor = fieldBg,
                    disabledContainerColor = fieldBg, focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
            )

            SectionLabel("Konum")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = selectedAddress,
                    onValueChange = { /* readOnly */ },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Adres / İl / İlçe") },
                    readOnly = true, singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = fieldBg, unfocusedContainerColor = fieldBg,
                        disabledContainerColor = fieldBg, focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    leadingIcon = { Icon(Icons.Filled.LocationOn, null) }
                )
                FilledIconButton(onClick = {
                    if (!hasLocationPermission(context)) {
                        permissionLauncher.launch(locationPerms)
                    } else {
                        scope.launch {
                            val pair = getCurrentLocationAndAddress(context)
                            if (pair == null) {
                                error = "Konum alınamadı"
                            } else {
                                selectedLatLng = pair.first
                                selectedAddress = pair.second
                            }
                        }
                    }
                }) { Icon(Icons.Filled.MyLocation, contentDescription = "Konumumu al") }

                FilledIconButton(onClick = { showMap = true }) {
                    Icon(Icons.Filled.Map, contentDescription = "Haritadan seç")
                }
            }

            SectionLabel("Puan")
            StarRow(
                value = stars, onChange = { stars = it },
                starColor = Color(0xFFFFC107),
                emptyColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                size = 22.dp, space = 6.dp
            )

            SectionLabel("Fotoğraflar")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                images.forEachIndexed { index, uri ->
                    PhotoThumb(uri = uri, onRemove = { images.removeAt(index) })
                }
                AddPhotoTile(onClick = { requestImages() })
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    if (showMap) {
        MapPickerBottomSheet(
            initial = selectedLatLng,
            onDismiss = { showMap = false },
            onPicked = { ll, addr ->
                selectedLatLng = ll
                selectedAddress = addr
                showMap = false
            }
        )
    }
}

/* ---------------- helpers ---------------- */
@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onBackground)
}

@Composable
private fun StarRow(
    value: Int, onChange: (Int) -> Unit,
    starColor: Color, emptyColor: Color,
    size: Dp, space: Dp
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { i ->
            val filled = i < value
            Icon(
                imageVector = Icons.Filled.Star, contentDescription = null,
                tint = if (filled) starColor else emptyColor,
                modifier = Modifier.size(size).clickable { onChange(i + 1) }
            )
            if (i != 4) Spacer(Modifier.width(space))
        }
    }
}

@Composable
private fun PhotoThumb(uri: Uri, onRemove: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    Box(Modifier.size(92.dp).clip(RoundedCornerShape(18.dp))) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(ctx).data(uri).crossfade(true).build(),
            contentDescription = "Seçili görsel", contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
            loading = { Box(Modifier.matchParentSize().background(cs.surfaceVariant.copy(alpha = 0.4f))) },
            error = { Box(Modifier.matchParentSize().background(cs.surfaceVariant.copy(alpha = 0.4f))) }
        )
        Box(
            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(22.dp)
                .clip(CircleShape).background(Color.Black.copy(alpha = 0.55f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.Close, "Kaldır", tint = Color.White, modifier = Modifier.size(14.dp)) }
    }
}

@Composable
private fun AddPhotoTile(onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val bg = if (MaterialTheme.colorScheme.isLight()) Color.Transparent else cs.surface.copy(alpha = 0.1f)
    Box(
        modifier = Modifier.size(92.dp).clip(RoundedCornerShape(18.dp)).background(bg).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
            drawRoundRect(color = cs.outline.copy(alpha = 0.6f), style = stroke,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Image, null, tint = cs.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
            Text("Görsel Ekle", color = cs.onSurface.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
        }
    }
}

private fun ColorScheme.isLight() = this.background.luminance() > 0.5f

/* ---------- Konum: cihazdan alma + reverse geocode ---------- */
private fun hasLocationPermission(context: android.content.Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

@SuppressLint("MissingPermission")
private suspend fun getCurrentLocationAndAddress(context: android.content.Context): Pair<LL, String>? {
    if (!hasLocationPermission(context)) return null
    val fused: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val loc: Location = try {
        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await() ?: return null
    } catch (_: Exception) { return null }

    val ll = LL(loc.latitude, loc.longitude)
    val addr = reverseGeocode(context, ll)
    return ll to (addr ?: "${ll.latitude.format6()}, ${ll.longitude.format6()}")
}

private suspend fun reverseGeocode(context: android.content.Context, ll: LL): String? =
    withContext(Dispatchers.IO) {
        try {
            val geo = Geocoder(context, Locale("tr","TR"))
            val list = if (Build.VERSION.SDK_INT >= 33) {
                geo.getFromLocation(ll.latitude, ll.longitude, 1)
            } else {
                @Suppress("DEPRECATION")
                geo.getFromLocation(ll.latitude, ll.longitude, 1)
            }
            val a = list?.firstOrNull() ?: return@withContext null
            val province = a.adminArea ?: a.subAdminArea ?: a.locality
            val district = a.subAdminArea ?: a.locality ?: a.subLocality
            return@withContext when {
                province != null && district != null -> "$province / $district"
                province != null -> province
                else -> a.getAddressLine(0)
            }
        } catch (_: Exception) {
            return@withContext null
        }
    }

/* =================== Map Picker (osmdroid) =================== */
@Composable
private fun MapPickerBottomSheet(
    initial: LL?,
    onDismiss: () -> Unit,
    onPicked: (LL, String) -> Unit
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    var picked by remember { mutableStateOf<LL?>(initial) }
    var pickedAddress by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(picked) {
        pickedAddress = picked?.let { reverseGeocode(context, it) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = cs.surface, tonalElevation = 2.dp) {
        Column(Modifier.fillMaxWidth()) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(360.dp),
                factory = { ctx ->
                    Configuration.getInstance().userAgentValue = ctx.packageName
                    val map = MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                    }
                    val start = picked ?: LL(41.015137, 28.979530) // İstanbul
                    map.controller.setZoom(12.0)
                    map.controller.setCenter(GeoPoint(start.latitude, start.longitude))

                    var marker: Marker? = null
                    fun placeMarker(ll: LL) {
                        val gp = GeoPoint(ll.latitude, ll.longitude)
                        if (marker == null) {
                            marker = Marker(map).apply {
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            map.overlays.add(marker)
                        }
                        marker!!.position = gp
                        map.controller.setCenter(gp)
                        map.invalidate()
                    }
                    picked?.let { placeMarker(it) }

                    val receiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?) = false
                        override fun longPressHelper(p: GeoPoint?): Boolean {
                            p ?: return false
                            val ll = LL(p.latitude, p.longitude)
                            picked = ll
                            placeMarker(ll)
                            true
                            return TODO("Provide the return value")
                        }
                    }
                    map.overlays.add(MapEventsOverlay(receiver))
                    map
                }
            )

            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = cs.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when {
                            picked == null -> "Haritada uzun basarak bir konum seçin"
                            pickedAddress != null -> pickedAddress!!
                            else -> "${picked!!.latitude.format6()}, ${picked!!.longitude.format6()}"
                        },
                        color = cs.onSurface
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text("İptal")
                    }
                    Button(
                        onClick = {
                            val ll = picked ?: return@Button
                            val addr = pickedAddress ?: "${ll.latitude.format6()}, ${ll.longitude.format6()}"
                            onPicked(ll, addr)
                        },
                        enabled = picked != null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Seç") }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/* tiny util */
private fun Double.format6(): String = String.format(Locale.US, "%.6f", this)
