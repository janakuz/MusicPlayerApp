package com.example.musicapp.service

import android.content.Context
import android.net.Uri
import com.example.musicapp.data.local.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class DatabaseBackupManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val dbName = "music_app_db"
    private val dataStoreName = "preference_file"

    suspend fun exportDatabase(destinationUri: Uri): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching<Unit> {
                database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

                val dbFile = context.getDatabasePath(dbName)
                if (!dbFile.exists()) throw FileNotFoundException("Database file not found")

                val dataStoreFile = File(context.filesDir, "datastore/$dataStoreName.preferences_pb")
                if (!dataStoreFile.exists()) throw FileNotFoundException("DataStore file not found")

                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                        if (dbFile.exists()) {
                            zipOut.putNextEntry(ZipEntry(dbName))
                            dbFile.inputStream().copyTo(zipOut)
                            zipOut.closeEntry()
                        }
                        if (dataStoreFile.exists()) {
                            zipOut.putNextEntry(ZipEntry("$dataStoreName.preferences_pb"))
                            dataStoreFile.inputStream().copyTo(zipOut)
                            zipOut.closeEntry()
                        }
                    }
                }
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
                val dataStoreDir = File(context.filesDir, "datastore").apply { mkdirs() }
                val dataStoreFile = File(dataStoreDir, "$dataStoreName.preferences_pb")

                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                        var entry = zipIn.nextEntry
                        while (entry != null) {
                            when (entry.name) {
                                dbName -> {
                                    FileOutputStream(currentDbFile).use { out -> zipIn.copyTo(out) }
                                }

                                "$dataStoreName.preferences_pb" -> {
                                    FileOutputStream(dataStoreFile).use { out -> zipIn.copyTo(out) }
                                }
                            }
                            zipIn.closeEntry()
                            entry = zipIn.nextEntry
                        }
                    }
                }

                context.getDatabasePath("$dbName-wal").delete()
                context.getDatabasePath("$dbName-shm").delete()
            }
        }
    }

}