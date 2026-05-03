package com.example.musicapp.data.repository.impl

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.text.toLowerCase
import androidx.room.Transaction
import androidx.room.withTransaction
import com.example.musicapp.data.dao.PlaylistDao
import com.example.musicapp.data.dao.PlaylistTracksDao
import com.example.musicapp.data.database.AppDatabase
import com.example.musicapp.data.dto.PlaylistTrack
import com.example.musicapp.data.entity.Playlist
import com.example.musicapp.data.entity.PlaylistTracks
import com.example.musicapp.data.repository.PlaylistRepository
import com.example.musicapp.data.repository.PlaylistStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.io.File
import java.util.UUID

class OfflinePlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val playlistTracksDao: PlaylistTracksDao,
    private val db: AppDatabase,
    @ApplicationContext private val context: Context
) : PlaylistRepository {

    override fun getAllPlaylists(
        sortBy: String,
        ascending: Boolean
    ): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists(sortBy, ascending)
    }

    override fun getPlaylist(id: Int): Flow<Playlist> {
        return playlistDao.getPlaylist(id)
    }

    override suspend fun getPlaylistById(id: Int): Playlist {
        return playlistDao.getPlaylistById(id)
    }

    override suspend fun insert(playlist: Playlist): Long {
        return playlistDao.insertPlaylist(playlist)
    }

    override suspend fun update(playlist: Playlist) {
        val withUpdateTimestamp = playlist.copy(lastUpdated = System.currentTimeMillis())
        playlistDao.updatePlaylist(withUpdateTimestamp)
    }

    override suspend fun delete(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }

    override suspend fun deleteById(playlistId: Int) {
        playlistDao.deleteById(playlistId)
    }

    override fun getArtForCollage(playlistId: Int) : Flow<List<String>> {
        return playlistTracksDao.getTop4ImagesForPlaylist(playlistId)
    }

    override fun getPlaylistStats(playlistId: Int): Flow<PlaylistStats> {
        return combine (
            playlistTracksDao.getTop4ImagesForPlaylist(playlistId),
            playlistTracksDao.getTrackCount(playlistId),
            playlistTracksDao.getDuration(playlistId)
        ) {images, tracks, duration ->
            PlaylistStats(
                images = images,
                trackCount = tracks,
                duration = duration
            )
        }
    }

    override fun savePlaylistImage(
        context: Context,
        uri: Uri
    ): String? {
        return try {
            val directory = File(context.filesDir, "playlist_covers")
            if (!directory.exists()) directory.mkdirs()

            val file = File(directory, "playlist_${UUID.randomUUID()}.jpg")

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun readFile(context: Context, file: Uri) : List<String> {
        val trackTails = mutableListOf<String>()

        context.contentResolver.openInputStream(file)?.use { inputStream ->
            inputStream.bufferedReader().forEachLine { line ->
                if (line.isNotBlank() && !line.startsWith("#")) {
                    val tail = line.split("/").takeLast(2).joinToString("/")
                    trackTails.add(tail.lowercase())
                }
            }
        }

        return trackTails
    }

    private fun getPlaylistName(context: Context, file: Uri) : String? {
        var result: String? = null
        if (file.scheme == "content") {
            val cursor = context.contentResolver.query(file, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = it.getString(nameIndex).split(".")[0]
                    }
                }
            }
        }
        return result
    }



    override suspend fun importPlaylist(file: Uri) {
        val trackPaths = readFile(context, file)
        val playlistName = getPlaylistName(context, file)

        if (playlistName == null) return

        db.withTransaction {
            val playlistId = playlistDao.insertPlaylist(Playlist(name = playlistName, lastUpdated = System.currentTimeMillis())).toInt()
            val playlistTracks = mutableListOf<PlaylistTracks>()

            trackPaths.forEachIndexed { index, pathTail ->
                val track = db.trackDao().findTrackByPath("%$pathTail")
                if (track != null){
                    playlistTracks.add(PlaylistTracks(
                        playlistId = playlistId,
                        trackId = track.id,
                        position = index,
                        addedAt = System.currentTimeMillis()
                    ))
                }

            }

            playlistTracksDao.insertAll(playlistTracks)
        }
    }

    override fun exportPlaylist(
        uri: Uri,
        tracks: List<PlaylistTrack>
    ) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.bufferedWriter().use { writer ->
                writer.write("#EXTM3U\n")
                tracks.forEach { track ->
                    val durationInSec = track.trackInfo.duration / 1000
                    writer.write("#EXTINF:$durationInSec,${track.trackInfo.artistName} - ${track.trackInfo.title}\n")
                    writer.write("${track.trackInfo.filePath}\n")
                }
            }
        }
    }
}