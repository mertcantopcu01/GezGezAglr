@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.myapplication.screens

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.firebase.FirebaseStorageService
import com.example.myapplication.firebase.FirestoreService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.util.Locale

// ---- Basit koordinat ----
data class LL(val latitude: Double, val longitude: Double)

@Composable
fun CreatePostScreen(
    onPostCreated: () -> Unit,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()
    var didInitialAutoScroll by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(scroll.maxValue) {
        if (!didInitialAutoScroll && scroll.maxValue > 0) {
            didInitialAutoScroll = true
            // (İsteğe bağlı) ufak bir bekleme: layout tamamlanmış olsun
            delay(5000)
            val bottom = scroll.maxValue
            // alta in
            scroll.animateScrollTo(
                bottom,
                animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
            )
            // kısa bekle
            delay(250)
            // tekrar üste
            scroll.animateScrollTo(
                0,
                animationSpec = tween(durationMillis = 900, easing = LinearOutSlowInEasing)
            )
        }
    }

    var placeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5) }                 // 1..10
    val images = remember { mutableStateListOf<Uri>() }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var selectedAddress by remember { mutableStateOf("") }
    var selectedLatLng by remember { mutableStateOf<LL?>(null) }
    var rawLocation by remember { mutableStateOf("") }

    // Foto seçiciler
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

    val fieldBg =
        if (cs.isLight()) Color(0xFFF1F2F4) else cs.surfaceVariant.copy(alpha = 0.25f)

    // Fotoğraf zorunlu!
    val formValid = placeName.isNotBlank() && description.isNotBlank() && images.isNotEmpty()

    // ---------- DSA: scroll hint + otomatik altına inip üste çıkma DEMOSU ----------
    val prefs = remember { context.getSharedPreferences("hints", Context.MODE_PRIVATE) }
    var everShown by remember { mutableStateOf(prefs.getBoolean("hint_createpost_scroll", false)) }
    var showHint by rememberSaveable { mutableStateOf(false) }
    var autoDemoRunning by remember { mutableStateOf(false) }

    // İçerik taşınca ve daha önce hiç gösterilmediyse ipucunu aç
    LaunchedEffect(scroll.maxValue, everShown) {
        if (!everShown && scroll.maxValue > 0) {
            showHint = true
        }
    }

    // Kullanıcı kaydırırsa ipucunu kapat (ama otomatik demo çalışırken kapatma)
    LaunchedEffect(scroll.isScrollInProgress) {
        if (scroll.isScrollInProgress && showHint && !autoDemoRunning) {
            showHint = false
            prefs.edit().putBoolean("hint_createpost_scroll", true).apply()
            everShown = true
        }
    }

    // İpucu açıldığında BİR KEZ: en alta animasyonla in -> kısa bekle -> tekrar üste çık
    LaunchedEffect(showHint, scroll.maxValue) {
        if (showHint && !everShown && scroll.maxValue > 0 && !autoDemoRunning) {
            autoDemoRunning = true
            // biraz bekle, sonra demo
            delay(250)
            val bottom = scroll.maxValue
            // alta in
            scroll.animateScrollTo(
                bottom,
                animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
            )
            delay(250)
            // tekrar en üste
            scroll.animateScrollTo(
                0,
                animationSpec = tween(durationMillis = 900, easing = LinearOutSlowInEasing)
            )
            autoDemoRunning = false

            // ipucunu kapat ve bir daha gösterme
            showHint = false
            prefs.edit().putBoolean("hint_createpost_scroll", true).apply()
            everShown = true
        }
    }
    // -------------------------------------------------------------------------------

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.Close, null) } },
                title = { Text("Yeni Gönderi", fontWeight = FontWeight.SemiBold) }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 12.dp,
                color = cs.surface
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Divider(color = cs.outlineVariant.copy(alpha = 0.4f))
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (!formValid || isSaving) return@Button
                            error = null
                            isSaving = true

                            scope.launch {
                                val urls = buildList {
                                    for (uri in images) {
                                        FirebaseStorageService.uploadImageSuspend(context, uri)
                                            ?.let { add(it) }
                                    }
                                }
                                FirestoreService.createPost(
                                    title = placeName,
                                    description = description,
                                    rating = rating,                 // 1..10
                                    photos = urls,
                                    location = selectedAddress.ifBlank { null }
                                ) { ok ->
                                    isSaving = false
                                    if (ok) onPostCreated() else error = "Kaydedilemedi."
                                }
                            }
                        },
                        enabled = formValid && !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Paylaşılıyor…")
                        } else {
                            Icon(Icons.Filled.Send, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Paylaş")
                        }
                    }
                }
            }
        },
        containerColor = cs.background
    ) { inner ->
        Box(Modifier.fillMaxSize()) {
            // ---- İÇERİK ----
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(inner)                 // bottomBar/topBar boşluğu
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(scroll)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                SectionLabel("Mekan Adı")
                TextField(
                    value = placeName, onValueChange = { placeName = it },
                    placeholder = { Text("Mekan ismi yazın...") }, singleLine = true,
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                )

                SectionLabel("Konum (Maps linki ya da adres/yer adı)")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = rawLocation,
                        onValueChange = { rawLocation = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Örn: https://maps.app.goo.gl/...  veya  Akaretler Gloria") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = fieldBg, unfocusedContainerColor = fieldBg,
                            disabledContainerColor = fieldBg, focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        leadingIcon = { Icon(Icons.Filled.LocationOn, null) }
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                val result = resolveUserLocationInput(context, rawLocation)
                                if (result == null) {
                                    error = "Konum çözümlenemedi. Geçerli bir Maps linki veya net bir adres/isim deneyin."
                                } else {
                                    selectedLatLng = result.first
                                    selectedAddress = result.second
                                    if (placeName.isBlank()) placeName = rawLocation.trim()
                                    error = null
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Dönüştür") }
                }

                TextField(
                    value = selectedAddress,
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Adres / İl / İlçe (dönüştür ile doldurulur)") },
                    readOnly = true, singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = fieldBg, unfocusedContainerColor = fieldBg,
                        disabledContainerColor = fieldBg, focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // ---- Puan: 1..10 Slider ----
                SectionLabel("Puan (1–10)")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Seçilen puanı gösteren rozet
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(cs.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$rating", style = MaterialTheme.typography.titleMedium, color = cs.primary)
                    }

                    // 1..10 aralığında, tam sayı adımlar
                    Slider(
                        value = rating.toFloat(),
                        onValueChange = { rating = it.toInt().coerceIn(1, 10) },
                        valueRange = 1f..10f,
                        steps = 8, // endpoints hariç 8 nokta -> toplam 10 değer
                        modifier = Modifier.weight(1f)
                    )
                }

                SectionLabel("Fotoğraflar")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    images.forEachIndexed { index, uri ->
                        PhotoThumb(uri = uri, onRemove = { images.removeAt(index) })
                    }
                    // Boşken kırmızı vurgulu "Görsel Ekle"
                    AddPhotoTile(
                        onClick = { requestImages() },
                        highlightEmpty = images.isEmpty()
                    )
                }

                if (images.isEmpty()) {
                    Text(
                        "En az 1 görsel eklemelisin.",
                        color = cs.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                error?.let {
                    Text(it, color = cs.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // --- DSA overlay: “Aşağı kaydırabilirsiniz” ---
            ScrollHintOverlay(
                visible = showHint,
                onDismiss = {
                    showHint = false
                    prefs.edit().putBoolean("hint_createpost_scroll", true).apply()
                    everShown = true
                }
            )
        }
    }
}

/* ---------------- helpers ---------------- */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun PhotoThumb(uri: Uri, onRemove: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    Box(Modifier.size(92.dp).clip(RoundedCornerShape(18.dp))) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(ctx).data(uri).crossfade(true).build(),
            contentDescription = "Seçili görsel",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
            loading = { Box(Modifier.matchParentSize().background(cs.surfaceVariant.copy(alpha = 0.4f))) },
            error   = { Box(Modifier.matchParentSize().background(cs.surfaceVariant.copy(alpha = 0.4f))) }
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, "Kaldır", tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun AddPhotoTile(onClick: () -> Unit, highlightEmpty: Boolean = false) {
    val cs = MaterialTheme.colorScheme
    val bg = if (cs.isLight()) Color.Transparent else cs.surface.copy(alpha = 0.1f)
    val strokeColor = if (highlightEmpty) cs.error else cs.outline.copy(alpha = 0.6f)
    val labelColor  = if (highlightEmpty) cs.error else cs.onSurface.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
            )
            drawRoundRect(
                color = strokeColor,
                style = stroke,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx())
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Image, null, tint = labelColor)
            Spacer(Modifier.height(4.dp))
            Text("Görsel Ekle", color = labelColor, style = MaterialTheme.typography.labelMedium)
        }
    }
}

private fun ColorScheme.isLight() = this.background.luminance() > 0.5f

/* ---------- Konum çözümleme (link veya adres/yer adı) ---------- */
private suspend fun resolveUserLocationInput(
    context: Context,
    input: String
): Pair<LL, String>? = withContext(Dispatchers.Default) {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return@withContext null

    extractLatLngFromUrlOrQuery(trimmed)?.let { ll ->
        val addr = reverseGeocode(context, ll) ?: "${ll.latitude.format6()}, ${ll.longitude.format6()}"
        return@withContext ll to addr
    }

    forwardGeocode(context, trimmed)?.let { ll ->
        val addr = reverseGeocode(context, ll) ?: "${ll.latitude.format6()}, ${ll.longitude.format6()}"
        return@withContext ll to addr
    }

    null
}

private fun decode(s: String): String = try { URLDecoder.decode(s, "UTF-8") } catch (_: Exception) { s }

private fun extractLatLngFromUrlOrQuery(text: String): LL? {
    val lower = text.lowercase()

    Regex("""geo:([-+]?\d+(\.\d+)?),\s*([-+]?\d+(\.\d+)?)""").find(lower)?.let {
        val lat = it.groupValues[1].toDoubleOrNull()
        val lng = it.groupValues[3].toDoubleOrNull()
        if (lat != null && lng != null) return LL(lat, lng)
    }

    Regex("""@([-+]?\d+(\.\d+)?),\s*([-+]?\d+(\.\d+)?)\,""").find(lower)?.let {
        val lat = it.groupValues[1].toDoubleOrNull()
        val lng = it.groupValues[3].toDoubleOrNull()
        if (lat != null && lng != null) return LL(lat, lng)
    }

    Regex("""[?&]query=([-+]?\d+(\.\d+)?),\s*([-+]?\d+(\.\d+)?)""").find(lower)?.let {
        val lat = it.groupValues[1].toDoubleOrNull()
        val lng = it.groupValues[3].toDoubleOrNull()
        if (lat != null && lng != null) return LL(lat, lng)
    }

    Regex("""[?&]query=([^&]+)""").find(lower)?.let { m ->
        val q = decode(m.groupValues[1])
        if (q.matches(Regex("""[-+]?\d+(\.\d+)?\s*,\s*[-+]?\d+(\.\d+)?"""))) {
            val parts = q.split(',').map { it.trim() }
            val lat = parts[0].toDoubleOrNull()
            val lng = parts[1].toDoubleOrNull()
            if (lat != null && lng != null) return LL(lat, lng)
        }
    }

    Regex("""!3d([-+]?\d+(\.\d+)?)!4d([-+]?\d+(\.\d+)?)""").find(lower)?.let {
        val lat = it.groupValues[1].toDoubleOrNull()
        val lng = it.groupValues[3].toDoubleOrNull()
        if (lat != null && lng != null) return LL(lat, lng)
    }

    return null
}

private suspend fun forwardGeocode(context: Context, text: String): LL? =
    withContext(Dispatchers.IO) {
        try {
            val geo = Geocoder(context, Locale("tr","TR"))
            val list = geo.getFromLocationName(text, 1)
            val a = list?.firstOrNull() ?: return@withContext null
            LL(a.latitude, a.longitude)
        } catch (_: Exception) { null }
    }

private suspend fun reverseGeocode(context: Context, ll: LL): String? =
    withContext(Dispatchers.IO) {
        try {
            val geo = Geocoder(context, Locale("tr","TR"))
            val list = geo.getFromLocation(ll.latitude, ll.longitude, 1)
            val a = list?.firstOrNull() ?: return@withContext null
            val province = a.adminArea ?: a.subAdminArea ?: a.locality
            val district = a.subAdminArea ?: a.locality ?: a.subLocality
            when {
                province != null && district != null -> "$province / $district"
                province != null -> province
                else -> a.getAddressLine(0)
            }
        } catch (_: Exception) { null }
    }

private fun Double.format6(): String = String.format(Locale.US, "%.6f", this)

/* ------------------------------ */
/* DSA Overlay: aşağı kaydır ipucu */
/* ------------------------------ */
@Composable
private fun ScrollHintOverlay(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val infinite = rememberInfiniteTransition(label = "scrollHint")
    val offsetY by infinite.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    val alpha by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // 2.5 sn sonra kendiliğinden kapan (demo sonrası yine de güvence)
    LaunchedEffect(Unit) {
        delay(2500)
        onDismiss()
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(visible = visible) {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .offset(y = offsetY.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onDismiss() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Aşağı kaydırabilirsiniz",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                    )
                }
            }
        }
    }
}
