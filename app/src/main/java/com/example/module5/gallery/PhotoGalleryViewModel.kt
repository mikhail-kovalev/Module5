package com.example.module5.gallery

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoGalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val _photos = MutableStateFlow<List<GalleryPhoto>>(emptyList())
    val photos: StateFlow<List<GalleryPhoto>> = _photos.asStateFlow()

    init {
        refreshPhotos()
    }

    fun refreshPhotos() {
        viewModelScope.launch(Dispatchers.IO) {
            val pictureDir = getPictureDirectory() ?: run {
                _photos.value = emptyList()
                return@launch
            }

            val scannedPhotos = pictureDir
                .listFiles()
                ?.asSequence()
                ?.filter { file ->
                    file.isFile && file.extension.equals("jpg", ignoreCase = true)
                }
                ?.map { file ->
                    GalleryPhoto(
                        file = file,
                        createdAt = file.lastModified()
                    )
                }
                ?.sortedByDescending { photo -> photo.createdAt }
                ?.toList()
                .orEmpty()

            _photos.value = scannedPhotos
        }
    }

    fun createNewPhotoFile(): File? {
        val picturesDirectory = getPictureDirectory() ?: return null
        val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val baseName = "IMG_${formatter.format(Date())}"

        var attempt = 0
        while (attempt < 1000) {
            val suffix = if (attempt == 0) "" else "_$attempt"
            val target = File(picturesDirectory, "$baseName$suffix.jpg")
            if (!target.exists()) {
                val created = runCatching { target.createNewFile() }.getOrDefault(false)
                if (created) return target
            }
            attempt += 1
        }
        return null
    }

    suspend fun exportToGallery(photo: GalleryPhoto): Boolean {
        return withContext(Dispatchers.IO) {
            exportJpegToMediaStore(sourceFile = photo.file)
        }
    }

    private fun getPictureDirectory(): File? {
        val app = getApplication<Application>()
        val directory = app.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: File(app.filesDir, "pictures_fallback")
        if (!directory.exists() && !directory.mkdirs()) return null
        return directory
    }

    private fun exportJpegToMediaStore(sourceFile: File): Boolean {
        if (!sourceFile.exists() || !sourceFile.isFile) return false

        val resolver = getApplication<Application>().contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, sourceFile.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/Module5Gallery"
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val destinationUri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ) ?: return false

        val copiedSuccessfully = runCatching {
            resolver.openOutputStream(destinationUri)?.use { output ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } != null
        }.getOrDefault(false)

        if (!copiedSuccessfully) {
            resolver.delete(destinationUri, null, null)
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val readyValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            resolver.update(destinationUri, readyValues, null, null)
        }

        return true
    }
}
