package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraCaptureScreen(
    title: String = "Capture Form / Document",
    onPhotoCaptured: (File) -> Unit,
    onUploadSelected: (Uri) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required to capture photos", Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onUploadSelected(uri)
        }
    }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
    var capturedFile by remember { mutableStateOf<File?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .build()
    }

    // Update flash mode on imageCapture safely
    LaunchedEffect(flashMode) {
        try {
            imageCapture.flashMode = flashMode
        } catch (_: Exception) {}
    }

    BackHandler {
        if (capturedFile != null) {
            capturedFile = null
        } else {
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!hasCameraPermission) {
            // Permission request fallback screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )

                        Text(
                            text = "Camera Permission Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Camera access allows you to take profile portrait pictures directly within the app.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("request_camera_permission_button")
                        ) {
                            Text("Grant Camera Permission")
                        }

                        OutlinedButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upload_image_fallback_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload Image From Gallery")
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        } else if (capturedFile != null) {
            // Captured Photo Review Screen
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x99000000))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Review Captured Photo",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    IconButton(onClick = { capturedFile = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Retake", tint = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = capturedFile,
                        contentDescription = "Captured Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Surface(
                    color = Color(0xCC000000),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { capturedFile = null },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("retake_photo_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retake")
                        }

                        Button(
                            onClick = {
                                capturedFile?.let { onPhotoCaptured(it) }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("use_photo_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Use This Photo")
                        }
                    }
                }
            }
        } else {
            // Live CameraX Preview using COMPATIBLE mode (TextureView) to prevent SurfaceView abandoned buffer queues
            var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
            var isCameraAvailable by remember { mutableStateOf(true) }
            val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

            // Initialize cameraProvider once
            LaunchedEffect(Unit) {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        cameraProvider = cameraProviderFuture.get()
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(context))
            }

            // Cleanup when leaving screen
            DisposableEffect(lifecycleOwner) {
                onDispose {
                    try {
                        cameraProvider?.unbindAll()
                        cameraExecutor.shutdown()
                    } catch (_: Exception) {}
                }
            }

            // PreviewView instance with TextureView mode
            val previewView = remember(context) {
                PreviewView(context).apply {
                    // Use COMPATIBLE implementation mode (TextureView) to prevent BufferQueue abandoned issues
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }

            // Bind camera safely only when cameraProvider or lensFacing changes
            LaunchedEffect(cameraProvider, lensFacing, lifecycleOwner) {
                val provider = cameraProvider ?: return@LaunchedEffect
                try {
                    provider.unbindAll()

                    val targetLens = lensFacing
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(targetLens)
                        .build()

                    // Check if device actually has requested camera
                    val safeSelector = if (provider.hasCamera(cameraSelector)) {
                        cameraSelector
                    } else if (provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else if (provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        null
                    }

                    if (safeSelector != null) {
                        isCameraAvailable = true
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        provider.bindToLifecycle(
                            lifecycleOwner,
                            safeSelector,
                            preview,
                            imageCapture
                        )
                    } else {
                        isCameraAvailable = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    isCameraAvailable = false
                }
            }

            if (!isCameraAvailable) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "Camera Hardware Unavailable",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "No camera was detected on this device. You can upload an image or photo directly from your device storage.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload Image from Device")
                            }
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Close")
                            }
                        }
                    }
                }
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { previewView }
                )

            // Document Framing Guide Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.dp, Color(0x99FFFFFF), RoundedCornerShape(12.dp))
                )
            }

            // Top Camera Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color(0x88000000))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("camera_close_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Close Camera", tint = Color.White)
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                // Flash toggle
                IconButton(
                    onClick = {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                            else -> ImageCapture.FLASH_MODE_AUTO
                        }
                    },
                    modifier = Modifier.testTag("camera_flash_toggle")
                ) {
                    Icon(
                        imageVector = when (flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                            ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
                            else -> Icons.Default.FlashAuto
                        },
                        contentDescription = "Flash Toggle",
                        tint = Color.White
                    )
                }
            }

            // Bottom Shutter & Upload Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0x99000000))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Upload from gallery icon button
                IconButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("camera_upload_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Upload from Gallery", tint = Color.White)
                        Text("Gallery", color = Color.White, fontSize = 9.sp)
                    }
                }

                // Shutter Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(enabled = !isCapturing) {
                            isCapturing = true
                            val photosDir = File(context.filesDir, "captured_forms")
                            if (!photosDir.exists()) photosDir.mkdirs()
                            val photoFile = File(photosDir, "capture_${System.currentTimeMillis()}.jpg")

                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                            imageCapture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        isCapturing = false
                                        capturedFile = photoFile
                                    }

                                    override fun onError(exc: ImageCaptureException) {
                                        isCapturing = false
                                        Toast.makeText(context, "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                        .testTag("shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp), color = MaterialTheme.colorScheme.primary)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.Black, CircleShape)
                                .background(Color.White)
                        )
                    }
                }

                // Switch Camera Lens Facing
                IconButton(
                    onClick = {
                        val newFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                        cameraProvider?.let { provider ->
                            val selector = CameraSelector.Builder().requireLensFacing(newFacing).build()
                            if (provider.hasCamera(selector)) {
                                lensFacing = newFacing
                            } else {
                                Toast.makeText(context, "Lens not available on this device", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("camera_switch_lens_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Switch Camera", tint = Color.White)
                        Text("Flip", color = Color.White, fontSize = 9.sp)
                    }
                }
            }
        }
        }
    }
}
