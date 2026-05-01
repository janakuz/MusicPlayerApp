package com.example.musicapp.data.repository.impl

import com.example.musicapp.data.dao.PlaylistDao
import com.example.musicapp.data.entity.Playlist
import com.example.musicapp.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class OfflinePlaylistRepository(
    private val playlistDao: PlaylistDao
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
}