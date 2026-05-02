package com.example.musicapp.data.repository.impl

import com.example.musicapp.data.dao.PlaylistDao
import com.example.musicapp.data.dao.PlaylistTracksDao
import com.example.musicapp.data.dto.PlaylistTrack
import com.example.musicapp.data.entity.Playlist
import com.example.musicapp.data.entity.PlaylistTracks
import com.example.musicapp.data.repository.PlaylistRepository
import com.example.musicapp.data.repository.PlaylistStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

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
//        val top4 = mutableListOf<String>()
//        var i = 0
//        while (top4.size < 4 && i < tracks.size){
//            val current = tracks[i].trackInfo.albumArt
//            if (current != null && current != "" && !top4.contains(current)){
//                top4.add(current)
//            }
//        }
//
//        return top4
//    }
}