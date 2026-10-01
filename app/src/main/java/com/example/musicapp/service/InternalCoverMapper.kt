package com.example.musicapp.service

import android.content.Context
import coil.map.Mapper
import coil.request.Options
import java.io.File

class InternalCoverMapper(
    private val context: Context
) : Mapper<String, File> {

    private val supportedDirs = setOf("covers", "album_art", "artist_art", "playlist_art")

    override fun map(data: String, options: Options): File? {
        val folder = data.substringBefore("/", "")

        return when {
            folder in supportedDirs -> File(context.filesDir, data)

            supportedDirs.any { data.contains("/files/$it/") } -> {
                val dirName = supportedDirs.first { data.contains("/files/$it/") }
                val fileName = data.substringAfterLast("/")
                File(context.filesDir, "$dirName/$fileName")
            }

            else -> null
        }
    }
}