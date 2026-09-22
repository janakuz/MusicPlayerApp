package com.example.musicapp.service

import android.content.Context
import android.net.Uri
import com.example.musicapp.data.local.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException

class DatabaseBackupManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val dbName = "music_app_db"

    suspend fun exportDatabase(destinationUri: Uri): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching<Unit> {
                database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

                val dbFile = context.getDatabasePath(dbName)
                if (!dbFile.exists()) throw FileNotFoundException("Database file not found")

                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    dbFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: throw IOException("Could not open output stream")
            }
        }
    }

    suspend fun importDatabase(sourceUri: Uri): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching<Unit> {
                if (database.isOpen) {
                    database.close()
                }

                val currentDbFile = context.getDatabasePath(dbName)
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    currentDbFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: throw IOException("Could not open input stream")

                context.getDatabasePath("$dbName-wal").delete()
                context.getDatabasePath("$dbName-shm").delete()
            }
        }
    }

}