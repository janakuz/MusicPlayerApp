package com.example.musicapp.service

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ImageStorageManager(private val context: Context) {
    suspend fun saveImageFromUri(
        sourceUri: Uri,
        folderName: String,
        fileName: String
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.filesDir, folderName).apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(directory, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile.absolutePath
        }.getOrNull()
    }

    suspend fun saveCustomArtwork(
        sourceUri: Uri,
        target: ImageTarget,
        entityId: Int
    ): String? {
        return saveImageFromUri(
            sourceUri = sourceUri,
            folderName = target.folderName,
            fileName = target.createFileName(entityId)
        )
    }

    suspend fun getCustomImagesForEntity(
        target: ImageTarget,
        id: Int
    ): List<String> = withContext(Dispatchers.IO) {
        val directory = File(context.filesDir, target.folderName)
        if (!directory.exists() || !directory.isDirectory) {
            return@withContext emptyList()
        }

        val prefix = "${target.folderName}_${id}_"

        directory.listFiles { file ->
            file.isFile && file.name.startsWith(prefix) && file.name.endsWith(".jpg")
        }?.map { it.absolutePath } ?: emptyList()
    }

    suspend fun deleteImageFile(filePath: String?): Boolean = withContext(Dispatchers.IO) {
        if (filePath.isNullOrEmpty()) return@withContext false
        runCatching {
            val file = File(filePath)
            if (file.exists() && file.absolutePath.startsWith(context.filesDir.absolutePath)) {
                file.delete()
            } else false
        }.getOrDefault(false)
    }
}

enum class ImageTarget(val folderName: String) {
    ALBUM("album_art"),
    ARTIST("artist_art"),
    PLAYLIST("playlist_art");

    fun createFileName(id: Int): String = "${folderName}_${id}_${UUID.randomUUID()}.jpg"
}

