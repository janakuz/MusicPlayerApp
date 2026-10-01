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

    val coversDir = File(context.filesDir, "covers")
    val artistDir = File(context.filesDir, "artist_art")
    val albumsDir = File(context.filesDir, "album_art")
    val playlistDir = File(context.filesDir, "playlist_art")

    suspend fun exportDatabase(destinationUri: Uri): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val db = database.openHelper.writableDatabase
            val cursor = db.query("PRAGMA wal_checkpoint(PASSIVE)")
            cursor.close()
            runCatching<Unit> {
                db.query("PRAGMA shrink_memory").close()

                val dbFile = context.getDatabasePath(dbName)
                val dbWalFile = File(dbFile.path + "-wal")
                val dbShmFile = File(dbFile.path + "-shm")
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
                        if (dbWalFile.exists()) {
                            zipOut.putNextEntry(ZipEntry("$dbName-wal"))
                            dbWalFile.inputStream().buffered().use { it.copyTo(zipOut) }
                            zipOut.closeEntry()
                        }
                        if (dbShmFile.exists()) {
                            zipOut.putNextEntry(ZipEntry("$dbName-shm"))
                            dbShmFile.inputStream().buffered().use { it.copyTo(zipOut) }
                            zipOut.closeEntry()
                        }
                        if (dataStoreFile.exists()) {
                            zipOut.putNextEntry(ZipEntry("$dataStoreName.preferences_pb"))
                            dataStoreFile.inputStream().copyTo(zipOut)
                            zipOut.closeEntry()
                        }

                        for (dir in listOf(coversDir, albumsDir, artistDir, playlistDir)) {
                            if (dir.exists()) {
                                dir.walkTopDown().filter { it.isFile }.forEach { coverFile ->
                                    val entryName = "${dir.name}/${coverFile.name}"
                                    zipOut.putNextEntry(ZipEntry(entryName))
                                    coverFile.inputStream().buffered().use { it.copyTo(zipOut) }
                                    zipOut.closeEntry()
                                }
                            }
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
                val dbWalFile = File(currentDbFile.path + "-wal")
                val dbShmFile = File(currentDbFile.path + "-shm")

                val dataStoreDir = File(context.filesDir, "datastore").apply { mkdirs() }
                val dataStoreFile = File(dataStoreDir, "$dataStoreName.preferences_pb")

                if (dbWalFile.exists()) dbWalFile.delete()
                if (dbShmFile.exists()) dbShmFile.delete()
                if (currentDbFile.exists()) currentDbFile.delete()

                val mediaFolders = setOf("covers", "album_art", "artist_art", "playlist_art")

                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                        var entry = zipIn.nextEntry
                        while (entry != null) {
                            val entryName = entry.name.removePrefix("/")

                            when {
                                entryName == dbName -> {
                                    FileOutputStream(currentDbFile).use { out -> zipIn.copyTo(out) }
                                }

                                entryName == "$dbName-wal" -> {
                                    FileOutputStream(dbWalFile).use { out -> zipIn.copyTo(out) }
                                }

                                entryName == "$dbName-shm" -> {
                                    FileOutputStream(dbShmFile).use { out -> zipIn.copyTo(out) }
                                }

                                entryName == "$dataStoreName.preferences_pb" -> {
                                    FileOutputStream(dataStoreFile).use { out -> zipIn.copyTo(out) }
                                }

                                mediaFolders.any { entryName.startsWith("$it/") } -> {
                                    val targetFile = File(context.filesDir, entryName)
                                    targetFile.parentFile?.mkdirs()
                                    FileOutputStream(targetFile).use { out -> zipIn.copyTo(out) }
                                }
                            }
                            zipIn.closeEntry()
                            entry = zipIn.nextEntry
                        }
                    }
                }
            }
        }
    }

}