package com.example.musicapp.data.repository.impl

import android.content.Context
import android.net.Uri
import com.example.musicapp.data.dao.PlaylistDao
import com.example.musicapp.data.dao.PlaylistTracksDao
import com.example.musicapp.data.dto.PlaylistTrack
import com.example.musicapp.data.entity.Playlist
import com.example.musicapp.data.entity.PlaylistTracks
import com.example.musicapp.data.repository.PlaylistRepository
import com.example.musicapp.data.repository.PlaylistStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.io.File
import java.util.UUID

class OfflinePlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val playlistTracksDao: PlaylistTracksDao
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
}