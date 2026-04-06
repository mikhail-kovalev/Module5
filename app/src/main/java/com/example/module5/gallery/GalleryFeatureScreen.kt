package com.example.module5.gallery

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GalleryFeatureScreen(
    modifier: Modifier = Modifier,
    galleryViewModel: PhotoGalleryViewModel = viewModel()
) {
    val photos by galleryViewModel.photos.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        val capturedFile = pendingPhotoFile
        pendingPhotoFile = null

        if (isSuccess && capturedFile != null) {
            galleryViewModel.refreshPhotos()
        } else {
            capturedFile?.delete()
        }
    }

    val launchCameraCapture: () -> Unit = {
        val targetFile = galleryViewModel.createNewPhotoFile()
        if (targetFile == null) {
            scope.launch {
                snackbarHostState.showSnackbar("Не удалось подготовить файл для фото")
            }
        } else {
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetFile
            )
            pendingPhotoFile = targetFile
            takePictureLauncher.launch(photoUri)
        }
    }

    val requiredPermissions = remember {
        buildList {
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val hasAllPermissions = requiredPermissions.all { permission ->
            context.hasPermission(permission)
        }
        if (hasAllPermissions) {
            launchCameraCapture()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Разрешение на камеру не выдано")
            }
        }
    }

    val onTakePhotoClick: () -> Unit = {
        val hasAllPermissions = requiredPermissions.all { permission ->
            context.hasPermission(permission)
        }
        if (hasAllPermissions) {
            launchCameraCapture()
        } else {
            requestPermissionsLauncher.launch(requiredPermissions.toTypedArray())
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Задания 2-3: Галерея") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onTakePhotoClick) {
                Text("+")
            }
        }
    ) { innerPadding ->
        if (photos.isEmpty()) {
            EmptyGalleryState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onTakePhotoClick = onTakePhotoClick
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = photos,
                    key = { photo -> photo.file.absolutePath }
                ) { photo ->
                    PhotoGridItem(
                        photo = photo,
                        onExport = { item ->
                            scope.launch {
                                val exported = galleryViewModel.exportToGallery(item)
                                if (exported) {
                                    snackbarHostState.showSnackbar("Фото добавлено в галерею")
                                } else {
                                    snackbarHostState.showSnackbar("Не удалось экспортировать фото")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGalleryState(
    modifier: Modifier = Modifier,
    onTakePhotoClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "У вас пока нет фото",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onTakePhotoClick) {
                Text("Сделать первое фото")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoGridItem(
    photo: GalleryPhoto,
    onExport: (GalleryPhoto) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val thumbnail by rememberPhotoThumbnail(file = photo.file)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = { },
                onLongClick = { isMenuExpanded = true }
            )
    ) {
        if (thumbnail != null) {
            Image(
                bitmap = thumbnail!!,
                contentDescription = photo.file.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
        ) {
            TextButton(onClick = { isMenuExpanded = true }) {
                Text("⋮")
            }
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Экспорт в галерею") },
                    onClick = {
                        isMenuExpanded = false
                        onExport(photo)
                    }
                )
            }
        }
    }
}

@Composable
private fun rememberPhotoThumbnail(file: File): State<ImageBitmap?> {
    return produceState<ImageBitmap?>(
        initialValue = null,
        key1 = file.absolutePath,
        key2 = file.lastModified()
    ) {
        value = withContext(Dispatchers.IO) {
            decodeSampledBitmap(
                file = file,
                requestedWidth = 400,
                requestedHeight = 400
            )?.asImageBitmap()
        }
    }
}

private fun decodeSampledBitmap(
    file: File,
    requestedWidth: Int,
    requestedHeight: Int
): Bitmap? {
    if (!file.exists()) return null

    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(file.absolutePath, options)
    options.inSampleSize = calculateInSampleSize(options, requestedWidth, requestedHeight)
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeFile(file.absolutePath, options)
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    requestedWidth: Int,
    requestedHeight: Int
): Int {
    var inSampleSize = 1
    val (height, width) = options.outHeight to options.outWidth
    if (height > requestedHeight || width > requestedWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= requestedHeight &&
            halfWidth / inSampleSize >= requestedWidth
        ) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

private fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
