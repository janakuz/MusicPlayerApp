package com.example.musicapp.data.repository

import com.example.musicapp.data.dao.TrackDao
import com.example.musicapp.data.entity.Track
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.components.SortField
import kotlinx.coroutines.flow.Flow

class OfflineTrackRepository(private val trackDao: TrackDao) : TrackRepository {

    override fun getAllTracksByName(): Flow<List<TrackInfo>> =
        trackDao.getAllTracksByName()

    override fun getAllTracksByNameDesc(): Flow<List<TrackInfo>> =
        trackDao.getAllTracksByNameDesc()

    override fun getAllTracksByDuration(): Flow<List<TrackInfo>> =
        trackDao.getAllTracksByDuration()

    override fun getAllTracksByDurationDesc(): Flow<List<TrackInfo>> =
        trackDao.getAllTracksByDurationDesc()

    override fun getAllTracks(orderBy: SortOption): Flow<List<TrackInfo>> {
        return when (orderBy.field) {
            SortField.NAME -> if (orderBy.ascending) trackDao.getAllTracksByName() else trackDao.getAllTracksByNameDesc()
            SortField.DURATION -> if (orderBy.ascending) trackDao.getAllTracksByDuration() else trackDao.getAllTracksByDurationDesc()
            SortField.RELEASE_DATE -> TODO() //shouldn't happen
        }
    }

    override fun getAllTracksFull(): Flow<List<Track>> {
        return trackDao.getAllTracksFull()
    }

    override fun getTrackById(id: Int): Flow<Track> =
        trackDao.getTrack(id)

    override fun getTrackInfo(id: Int): Flow<TrackInfo> =
        trackDao.getTrackInfo(id)

    override fun getTracksByArtist(artistId: Int): Flow<List<TrackInfo>> =
        trackDao.getAllTracksByArtist(artistId)

    override fun getTracksInAlbum(albumId: Int): Flow<List<TrackInfo>> =
        trackDao.getAllTracksInAlbum(albumId)

    override suspend fun getAlbumTracks(albumId: Int): List<TrackInfo> {
        return trackDao.getAlbumTracks(albumId)
    }

    override suspend fun getTrackByUri(uri: String): Track? {
        return trackDao.getTrackByUri(uri)
    }

    override suspend fun getTracksByIds(trackIds: Set<Int>): List<TrackInfo> {
        return trackDao.getTracksByIds(trackIds)
    }


    override suspend fun insertAll(tracks: List<Track>) {
        trackDao.insertAll(tracks)
    }

    override suspend fun insert(track: Track) {
        trackDao.insert(track)
    }

    override suspend fun update(track: Track) {
        trackDao.update(track)
    }

    override suspend fun delete(track: Track) {
        trackDao.delete(track)
    }

    override suspend fun getAllUris(): List<String> {
        return trackDao.getAllUris()
    }

    override suspend fun deleteByUri(uris: List<String>) {
        trackDao.deleteByUri(uris)
    }
}
