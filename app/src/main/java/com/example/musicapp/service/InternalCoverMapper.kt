package com.example.musicapp.service

import android.content.Context
import coil.map.Mapper
import coil.request.Options
import java.io.File

class InternalCoverMapper(
    private val context: Context
) : Mapper<String, File> {

    override fun map(data: String, options: Options): File? {
        return when {
            data.startsWith("covers/") -> File(context.filesDir, data)

            data.contains("/files/covers/") -> {
                val fileName = data.substringAfterLast("/")
                File(context.filesDir, "covers/$fileName")
            }

            data.startsWith("album_art/") -> File(context.filesDir, data)
            data.startsWith("artist_art/") -> File(context.filesDir, data)
            data.startsWith("playlist_art/") -> File(context.filesDir, data)

            else -> null
        }
    }
}