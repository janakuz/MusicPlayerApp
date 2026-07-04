package com.example.musicapp.data.repository

import android.content.Context
import com.example.musicapp.data.local.entity.Track
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.data.remote.dto.AudioFeaturesResponse
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.viewmodels.SelectSource
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    fun getAllTracksByName(): Flow<List<TrackInfo>>

    fun getAllTracksByNameDesc(): Flow<List<TrackInfo>>

    fun getAllTracksByDuration(): Flow<List<TrackInfo>>

    fun getAllTracksByDurationDesc(): Flow<List<TrackInfo>>

    fun getAllTracks(orderBy: SortOption): Flow<List<TrackInfo>>

    fun getAllTracksFull(): Flow<List<Track>>

    fun getTrackById(id: Int): Flow<Track>

    fun getTrackInfo(id: Int): Flow<TrackInfo>

    fun getTracksByArtist(artistId: Int): Flow<List<TrackInfo>>

    fun getTracksInAlbum(albumId: Int): Flow<List<TrackInfo>>

    suspend fun updateInstrumentalAndVoice(newInstrumental: Boolean?, newVoice: String?, tracks: List<Int>)

    suspend fun getAllUnEnriched(): List<Track>

    suspend fun getAudioFeatures(context: Context, track: Track): AudioFeaturesResponse?

    suspend fun getAlbumTracks(albumId: Int): List<TrackInfo>

    suspend fun getTrackByUri(uri: String): Track?

    suspend fun getTracksByIds(trackIds: List<Int>, source: SelectSource = SelectSource.ALBUM): List<TrackInfo>

    suspend fun insertAll(tracks: List<Track>)

    suspend fun insert(track: Track)

    suspend fun update(track: Track)

    suspend fun delete(track: Track)

    suspend fun getAllUris(): List<String>

    suspend fun deleteByUri(uris: List<String>)

    suspend fun getAll(): List<Track>
}
