package com.example.musicapp.data.repository

import com.example.musicapp.data.local.dao.PlaylistTracksDao
import com.example.musicapp.data.local.model.PlaylistTrack
import com.example.musicapp.data.local.entity.PlaylistTracks
import kotlinx.coroutines.flow.Flow

class PlaylistTracksRepositoryImpl (
    private val playlistTracksDao: PlaylistTracksDao,
    private val playlistRepository: PlaylistRepository
) : PlaylistTracksRepository {
    override suspend fun removeTrackFromPlaylist(entryId: Int, playlistId: Int) {
        val position = playlistTracksDao.getTrackPosition(entryId)
        position?.let { pos ->
            playlistTracksDao.removeTrackFromPlaylistByIds(entryId)
            playlistTracksDao.shiftPositionsDown(playlistId, pos)
            val playlist = playlistRepository.getPlaylistById(playlistId)
            playlistRepository.update(playlist)
        }
    }

    override suspend fun removeTracksFromPlaylist(
        entryIds: List<Int>,
    ) {
        playlistTracksDao.removeTracksFromPlaylist(entryIds)
    }

    override suspend fun insertTrackToPlaylist(playlistId: Int, trackId: Int) {
        val position = playlistTracksDao.getMaxPosition(playlistId)
        val newEntryPosition = if (position != null) position + 1 else 0
        val entry = PlaylistTracks(
            playlistId = playlistId,
            trackId = trackId,
            position = newEntryPosition,
            addedAt = System.currentTimeMillis()
        )
        playlistTracksDao.insertTrackToPlaylist(entry)
        val playlist = playlistRepository.getPlaylistById(playlistId)
        playlistRepository.update(playlist)
    }

    override fun getAllTracksInPlaylist(
        playlistId: Int,
        sortBy: String,
        ascending: Boolean
    ) : Flow<List<PlaylistTrack>> {
        return playlistTracksDao.getTracksForPlaylistByPosition(playlistId)
    }

    override suspend fun getTracksInPlaylist(playlistId: Int): List<PlaylistTrack> {
        return playlistTracksDao.getTracksForPlaylist(playlistId)
    }

    override suspend fun addTracksToPlaylist(playlistId: Int, trackIds: List<Int>) {
        val startPos = (playlistTracksDao.getMaxPosition(playlistId) ?: -1) + 1
        val timestamp = System.currentTimeMillis()

        val entries = trackIds.mapIndexed { index, trackId ->
            PlaylistTracks(
                playlistId = playlistId,
                trackId = trackId,
                position = startPos + index,
                addedAt = timestamp
            )
        }

        playlistTracksDao.insertAll(entries)
        val playlist = playlistRepository.getPlaylistById(playlistId)
        playlistRepository.update(playlist)
    }

    override fun getAll(): Flow<List<PlaylistTrack>> {
        return playlistTracksDao.getALl()
    }

    override suspend fun reorder(reordered: List<PlaylistTrack>) {
        val entries = reordered.mapIndexed { ind, it ->
            PlaylistTracks(
                id = it.entryId,
                playlistId = it.playlistId,
                trackId = it.trackInfo.trackId,
                position = ind,
                addedAt = it.addedAt
            )
        }

        playlistTracksDao.reorder(entries)
    }
}