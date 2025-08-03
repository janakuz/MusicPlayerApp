package com.example.musicapp

import android.content.Context
import android.content.res.AssetManager
import android.media.MediaScannerConnection
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object DevLibraryBootstrap {

    private const val SAMPLE_DIR = "sample_tracks"
    private const val TARGET_DIR = "/sdcard/Music"
    private const val MARKER_FILENAME = ".sample_tracks_seeded"

    fun ensureSampleTracksAvailable(context: Context) {
        if (isSeeded()) return

        try {
            copyAssetFolderRecursively(context, context.assets, SAMPLE_DIR, TARGET_DIR)
            writeSeededMarker()
        } catch (e: Exception) {
            Log.e("DevLibraryBootstrap", "Failed to seed sample tracks", e)
        }
    }

    private fun isSeeded(): Boolean {
        val marker = File(TARGET_DIR, MARKER_FILENAME)
        return marker.exists()
    }

    private fun writeSeededMarker() {
        try {
            val marker = File(TARGET_DIR, MARKER_FILENAME)
            if (!marker.exists()) {
                marker.parentFile?.mkdirs()
                marker.writeText("seeded at ${System.currentTimeMillis()}")
            }
        } catch (e: Exception) {
            Log.w("DevLibraryBootstrap", "Could not write seeded marker", e)
        }
    }

    private fun copyAssetFolderRecursively(
        context: Context,
        assetManager: AssetManager,
        assetPath: String,
        targetRoot: String
    ) {
        val list = assetManager.list(assetPath) ?: return
        if (list.isEmpty()) {
            copyAssetFile(context, assetManager, assetPath, targetRoot)
        } else {
            for (child in list) {
                val childAssetPath = if (assetPath.isEmpty()) child else "$assetPath/$child"
                copyAssetFolderRecursively(context, assetManager, childAssetPath, targetRoot)
            }
        }
    }

    private fun copyAssetFile(
        context: Context,
        assetManager: AssetManager,
        assetPath: String,
        targetRoot: String
    ) {
        val relativePath = assetPath.removePrefix("$SAMPLE_DIR/").trimStart('/')
        val outFile = File(targetRoot, relativePath)
        outFile.parentFile?.mkdirs()
        if (outFile.exists()) return

        assetManager.open(assetPath).use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }

        MediaScannerConnection.scanFile(
            context,
            arrayOf(outFile.absolutePath),
            null,
            null
        )
    }
}