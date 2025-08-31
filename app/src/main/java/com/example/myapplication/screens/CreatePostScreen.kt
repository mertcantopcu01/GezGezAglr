package com.example.myapplication.screens

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.firebase.FirebaseStorageService
import com.example.myapplication.firebase.FirestoreService
import com.example.myapplication.ui.AppBackground
import com.example.myapplication.ui.TextFieldStyles
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onPostCreated: () -> Unit,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val tfColors = TextFieldStyles.defaultTextFieldColors()
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("") }   // 👈 otomatik konum sonucu burada
    var rating by remember { mutableStateOf(5f) }         // 0..10
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Foto seçiciler
    val openDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> selectedImageUri = uri }

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    // Konum izin başlatıcısı
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val fine = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            scope.launch {
                val pd = getProvinceDistrictFromDevice(context)
                val formatted = pd?.let { it.province?.let { p ->
                    val d = it.district?.takeIf { s -> s.isNotBlank() }
                    if (d != null) "$p / $d" else p
                }}
                if (formatted != null) {
                    locationText = formatted
                }
            }
        } else {
            errorMsg = context.getString(R.string.title_and_desc_required) // istersen özel "izin gerekli" metni ekle
        }
    }

    AppBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.create_post),
                            color = cs.onPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = cs.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cs.primary,
                        titleContentColor = cs.onPrimary,
                        navigationIconContentColor = cs.onPrimary,
                        actionIconContentColor = cs.onPrimary
                    )
                )
            },
            containerColor = cs.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cs.surface),
                    elevation = CardDefaults.cardElevation(6.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(stringResource(R.string.place_name), fontFamily = FontFamily.Monospace, color = cs.onSurface.copy(alpha = 0.7f)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = tfColors
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(stringResource(R.string.description), fontFamily = FontFamily.Monospace, color = cs.onSurface.copy(alpha = 0.7f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            colors = tfColors
                        )

                        // 👇 KONUM (otomatik – readOnly alan + butonlar)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = locationText,
                                onValueChange = { /* readOnly */ },
                                label = { Text("Konum", fontFamily = FontFamily.Monospace, color = cs.onSurface.copy(alpha = 0.7f)) },
                                readOnly = true,
                                modifier = Modifier.weight(1f),
                                colors = tfColors
                            )
                            OutlinedButton(
                                onClick = {
                                    permissionsLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                shape = MaterialTheme.shapes.medium
                            ) { Text("Konumumu Al", fontFamily = FontFamily.Monospace) }

                            if (locationText.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { locationText = "" },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.error)
                                ) { Text("Temizle", fontFamily = FontFamily.Monospace) }
                            }
                        }

                        Text("Puan: ${rating.toInt()}/10", fontFamily = FontFamily.Monospace, color = cs.onSurface)
                        Slider(
                            value = rating,
                            onValueChange = { rating = it },
                            valueRange = 0f..10f,
                            steps = 9
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        pickMedia.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    } else {
                                        openDocument.launch(arrayOf("image/*"))
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium
                            ) { Text(stringResource(R.string.choose_photo), fontFamily = FontFamily.Monospace) }

                            if (selectedImageUri != null) {
                                OutlinedButton(
                                    onClick = { selectedImageUri = null },
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.error)
                                ) { Text(stringResource(R.string.remove), fontFamily = FontFamily.Monospace) }
                            }
                        }

                        selectedImageUri?.let { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = stringResource(R.string.chosen_photo),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(MaterialTheme.shapes.large)
                            )
                        }

                        // Hata
                        errorMsg?.let {
                            Surface(
                                color = cs.errorContainer,
                                contentColor = cs.onErrorContainer,
                                shape = MaterialTheme.shapes.medium
                            ) { Text(it, Modifier.padding(12.dp), fontFamily = FontFamily.Monospace) }
                        }

                        // Paylaş
                        Button(
                            onClick = {
                                if (title.isBlank() || description.isBlank()) {
                                    errorMsg = context.getString(R.string.title_and_desc_required)
                                    return@Button
                                }
                                errorMsg = null
                                isLoading = true

                                fun save(imageUrl: String?) {
                                    FirestoreService.createPost(
                                        title = title,
                                        description = description,
                                        rating = rating.toInt(),
                                        photoUrl = imageUrl,
                                        location = locationText.ifBlank { null }   // 👈 konumu kaydet
                                    ) { ok ->
                                        isLoading = false
                                        if (ok) onPostCreated() else errorMsg = context.getString(R.string.post_save_failed)
                                    }
                                }

                                selectedImageUri?.let { uri ->
                                    FirebaseStorageService.uploadImage(context, uri) { url ->
                                        save(url)
                                    }
                                } ?: save(null)
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = cs.primary,
                                contentColor = cs.onPrimary,
                                disabledContainerColor = cs.primary.copy(alpha = 0.4f),
                                disabledContentColor = cs.onPrimary.copy(alpha = 0.7f)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp),
                                    color = cs.onPrimary
                                )
                            } else {
                                Text(stringResource(R.string.share), fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

/* ==== Yardımcı: cihazdan İl / İlçe elde et ==== */

data class ProvinceDistrict(val province: String?, val district: String?)

@SuppressLint("MissingPermission")
private suspend fun getProvinceDistrictFromDevice(context: android.content.Context): ProvinceDistrict? =
    withContext(Dispatchers.IO) {
        val fused: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        // 1) hızlı lastLocation, 2) yoksa getCurrentLocation
        val last: Location? = try { fused.lastLocation.await() } catch (_: Exception) { null }
        val loc = last ?: try {
            val cts = CancellationTokenSource()
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
        } catch (_: Exception) { null }

        if (loc == null) return@withContext null

        val geo = Geocoder(context, Locale("tr", "TR"))
        val list = try {
            if (Build.VERSION.SDK_INT >= 33) {
                geo.getFromLocation(loc.latitude, loc.longitude, 1)
            } else {
                @Suppress("DEPRECATION")
                geo.getFromLocation(loc.latitude, loc.longitude, 1)
            }
        } catch (_: Exception) { null }

        val addr = list?.firstOrNull() ?: return@withContext null

        // Türkiye için pratik alanlar
        val province = addr.adminArea ?: addr.subAdminArea ?: addr.locality
        val district = addr.subAdminArea ?: addr.locality ?: addr.subLocality

        ProvinceDistrict(province, district)
    }
