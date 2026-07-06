package com.example.musicapp.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.example.musicapp.data.local.dao.TrackDao
import com.example.musicapp.data.local.entity.Track
import com.example.musicapp.data.local.entity.TrackLyrics
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.data.remote.dto.AudioFeaturesResponse
import com.example.musicapp.data.remote.dto.LRCLibResponse
import com.example.musicapp.data.remote.service.EssentiaApiService
import com.example.musicapp.data.remote.service.LRCLibApiService
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.viewmodels.SelectSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resumeWithException

class TrackRepositoryImpl(
    private val trackDao: TrackDao,
    private val audioFeaturesApi: EssentiaApiService,
    private val lyricsApi: LRCLibApiService,
    ) : TrackRepository {

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
            else -> trackDao.getAllTracksByName()
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

    override suspend fun updateInstrumentalAndVoice(
        newInstrumental: Boolean?,
        newVoice: String?,
        tracks: List<Int>
    ) {
        trackDao.updateInstrumentalVoice(newInstrumental, newVoice, tracks)
    }

    override suspend fun getAllUnEnriched(): List<Track> {
        return trackDao.getAllUnenriched()
    }

    override suspend fun getAudioFeatures(context: Context, track: Track): AudioFeaturesResponse? {
        var tempFile: File? = null
        var response: AudioFeaturesResponse? = null
        try {
            tempFile = trimAudio(context, track.fileUri.toUri(), track.duration)

            val requestFile = tempFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
            val file = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

            response = audioFeaturesApi.getAudioFeatures(file)


        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            tempFile?.delete()
        }

        return response
    }


    @OptIn(UnstableApi::class)
    private suspend fun trimAudio(context: Context, inputUri: Uri, duration: Long): File {
        val outputCacheFile = File(context.cacheDir, "temp_trim_${System.currentTimeMillis()}.mp3")


        return suspendCancellableCoroutine { continuation ->

            val start = if (duration < 60000) 0L else if (duration < 180000) 15000 else 30000
            val end = start + 45000

            val mediaItem = MediaItem.Builder()
                .setUri(inputUri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(start)
                        .setEndPositionMs(end)
                        .build()
                )
                .build()

            val editedMediaItem = EditedMediaItem.Builder(mediaItem)
                .setRemoveVideo(true)
                .build()

            val mainExecutor = context.mainExecutor
            mainExecutor.execute {
                try {
                    val transformer = Transformer.Builder(context).build()

                    transformer.addListener(object : Transformer.Listener {
                        override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                            if (continuation.isActive) {
                                continuation.resume(outputCacheFile) { cause, _, _ -> if (outputCacheFile.exists()) {
                                    outputCacheFile.delete()
                                } }
                            }
                        }

                        override fun onError(
                            composition: Composition,
                            exportResult: ExportResult,
                            exportException: ExportException
                        ) {
                            if (continuation.isActive) {
                                continuation.resumeWithException(exportException)
                            }
                        }
                    })

                    transformer.start(editedMediaItem, outputCacheFile.absolutePath)

                    continuation.invokeOnCancellation {
                        transformer.cancel()
                    }

                } catch (e: Exception) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }
            }
        }    }


    override suspend fun getAlbumTracks(albumId: Int): List<TrackInfo> {
        return trackDao.getAlbumTracks(albumId)
    }

    override suspend fun getTrackByUri(uri: String): Track? {
        return trackDao.getTrackByUri(uri)
    }

    override suspend fun getTracksByIds(trackIds: List<Int>, source: SelectSource): List<TrackInfo> {
        when (source) {
            SelectSource.ALBUM -> return trackDao.getTracksByIds(trackIds)
            SelectSource.PLAYLIST -> return trackDao.getPlaylistTracksByIds(trackIds).map { it.trackInfo }
            SelectSource.QUEUE -> {
                val tracks = trackDao.getTracksByIds(trackIds)
                val trackMap = tracks.associateBy { it.trackId }
                return trackIds.mapNotNull { id -> trackMap[id] }
            }
        }
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

    override suspend fun updateAll(tracks: List<Track>) {
        trackDao.updateAll(tracks)
    }

    override suspend fun getTrackWithLyrics(): List<Int> {
        return trackDao.getAllTracksWithLyrics()
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

    override suspend fun getAll(): List<Track> {
        return trackDao.getAllGrouped()
    }

    override suspend fun getLyrics(trackInfo: TrackInfo): LRCLibResponse? {
        return try {
            lyricsApi.getLyrics(
                trackName = trackInfo.title,
                albumName = trackInfo.albumTitle,
                artistName = trackInfo.artistName,
                durationSec = trackInfo.duration/1000
            )
        } catch (e: Exception) {
            Log.e("lyrics search", e.message.toString())
            null
        }

    }

    override suspend fun getLyricsLRCLibCached(trackInfo: TrackInfo): LRCLibResponse? {
        return try {
            lyricsApi.getLyricsCached(
                trackName = trackInfo.title,
                albumName = trackInfo.albumTitle,
                artistName = trackInfo.artistName,
                durationSec = trackInfo.duration/1000
            )
        } catch (e: Exception) {
            Log.e("lyrics search", e.message.toString())
            null
        }
    }

    override suspend fun insertAllLyrics(lyrics: List<TrackLyrics>) {
        trackDao.insertAllLyrics(lyrics)
    }

    override suspend fun insertLyrics(lyrics: TrackLyrics) {
        trackDao.insertLyrics(lyrics)
    }

    override suspend fun updateInstrumental(instrumental: Boolean, trackId: Int) {
        trackDao.updateInstrumental(instrumental, trackId)
    }

    override suspend fun getCachedLyrics(trackId: Int): TrackLyrics? {
        return trackDao.getTrackLyrics(trackId)
    }
}