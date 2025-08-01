package com.example.musicapp.data.repository

import com.example.musicapp.data.dao.TrackDao
import com.example.musicapp.data.entity.Track
import com.example.musicapp.data.dto.TrackInfo
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

    override fun getAllTracks(orderBy: SortField, descending: Boolean): Flow<List<TrackInfo>> {
        return when (orderBy) {
            SortField.TITLE -> if (descending) trackDao.getAllTracksByNameDesc() else trackDao.getAllTracksByName()
            SortField.DURATION -> if (descending) trackDao.getAllTracksByDurationDesc() else trackDao.getAllTracksByDuration()
        }
    }

    override fun getTrackById(id: Int): Flow<Track> =
        trackDao.getTrack(id)

    override fun getTrackInfo(id: Int): Flow<TrackInfo> =
        trackDao.getTrackInfo(id)

    override fun getTracksByArtist(artistId: Int): Flow<List<TrackInfo>> =
        trackDao.getAllTracksByArtist(artistId)

    override fun getTracksInAlbum(albumId: Int): Flow<List<TrackInfo>> =
        trackDao.getAllTracksInAlbum(albumId)


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
}
