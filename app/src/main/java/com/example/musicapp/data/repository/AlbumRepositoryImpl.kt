package com.example.musicapp.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.musicapp.data.local.dao.AlbumDao
import com.example.musicapp.data.local.dao.TrackDao
import com.example.musicapp.data.local.entity.Album
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.data.local.model.LabelInfo
import com.example.musicapp.data.remote.dto.AlbumDiscogsResponse
import com.example.musicapp.data.remote.dto.DiscogsSearchResponse
import com.example.musicapp.data.remote.dto.ReleaseGroupMB
import com.example.musicapp.data.remote.dto.ReleaseSearchResponse
import com.example.musicapp.data.remote.service.CoverArtArchiveApiService
import com.example.musicapp.data.remote.service.DiscogsApiService
import com.example.musicapp.data.remote.service.MusicbrainzApiService
import com.example.musicapp.service.ImageStorageManager
import com.example.musicapp.service.ImageTarget
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.components.characteristics
import com.example.musicapp.util.normalizeForMatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AlbumRepositoryImpl(
    private val albumDao: AlbumDao,
    private val trackDao: TrackDao,
    private val musicbrainzApiService: MusicbrainzApiService,
    private val coverArtArchiveApiService: CoverArtArchiveApiService,
    private val discogsApiService: DiscogsApiService,
    private val imageStorageManager: ImageStorageManager
) : AlbumRepository {

    private fun characteristicSort(characteristic: String): String {
        return """
            (
                SELECT AVG(t.$characteristic) 
                FROM tracks t 
                WHERE t.albumId = a.id
            )
        """.trimIndent()
    }

    override fun getAllAlbums(orderBy: SortOption): Flow<List<Album>> {
        val titleSort = """
                         CASE 
                            WHEN title LIKE 'The %' THEN SUBSTR(title, 5)
                            WHEN title LIKE 'A %' THEN SUBSTR(title, 3)
                            WHEN title LIKE 'An %' THEN SUBSTR(title, 4)
                            WHEN title GLOB '[^a-zA-Z0-9]*' THEN SUBSTR(title, 2)
                            ELSE title 
                         END COLLATE NOCASE
                        """.trimIndent()

        val sqlOrderBy =
            when (orderBy.field) {
                SortField.NAME -> titleSort
                SortField.DURATION -> "duration"
                SortField.RELEASE_DATE -> "releaseDate"
                SortField.NUMBER_OF_TRACKS -> "numTracks"
                SortField.ENGAGEMENT -> characteristicSort("engagement")
                SortField.APPROACHABILITY -> characteristicSort("approachability")
                SortField.DANCEABILITY -> characteristicSort("danceability")
                SortField.MOOD_AGGRESSIVE -> characteristicSort("moodAggressive")
                SortField.MOOD_RELAXED -> characteristicSort("moodRelaxed")
                SortField.MOOD_SAD -> characteristicSort("moodSad")
                SortField.MOOD_HAPPY -> characteristicSort("moodHappy")
                SortField.MOOD_PARTY -> characteristicSort("moodParty")
                SortField.BPM -> characteristicSort("bpm")
                SortField.LOUDNESS -> characteristicSort("loudness")
                SortField.DYNAMIC_RANGE -> characteristicSort("dynamicComplexity")
                else -> titleSort
            }

        val direction = if (orderBy.ascending) "ASC" else "DESC"

        val sqlString = """
                        SELECT * 
                        FROM albums a
                        ORDER BY $sqlOrderBy $direction 
                        ${if (orderBy.field in characteristics) "NULLS LAST" else ""}
                       """.trimIndent()

        val query = SimpleSQLiteQuery(sqlString)

        return albumDao.getAlbumsSorted(query)
    }

    override fun getAlbum(id: Int): Flow<Album> =
        albumDao.getAlbum(id)

    override fun getTopLabels(orderBy: SortOption, limit: Int): Flow<List<LabelInfo>> {
        return when (orderBy.field){
            SortField.TOTAL_COUNT -> albumDao.getMostRepresentedLabels("total", limit)
            SortField.ARTIST_COUNT -> albumDao.getMostRepresentedLabels("artistCount", limit)
            SortField.ALBUM_COUNT -> albumDao.getMostRepresentedLabels("albumCount", limit)
            else -> albumDao.getMostRepresentedLabels("total", limit)
        }
    }

    override fun getLabelItems(label: String): Flow<SearchResult> {
        return combine(
            albumDao.getLabelArtists(label),
            albumDao.getLabelAlbums(label)
        ) {
                artists, albums ->
            SearchResult(artists, albums)
        }

    }

    override suspend fun getAll(): List<Album> {
        return albumDao.getAll()
    }

    override suspend fun getById(id: Int): Album {
        return albumDao.getById(id)
    }

    override suspend fun getByIdFull(id: Int): List<AlbumInfo> {
        return albumDao.getByIdFull(id)
    }

    override suspend fun getByTitle(title: String, year: String?): Album? {
        return if (year != null) {
            albumDao.getAlbumByTitleAndYear(title.normalizeForMatching(), year)
//                ?: albumDao.getAlbumByTitle(title.normalizeForMatching())
        } else {
            albumDao.getAlbumByTitle(title.normalizeForMatching())
        }

    }

    override suspend fun findAlbumMB(query: String): ReleaseSearchResponse? {
        return try {
            musicbrainzApiService.findAlbum(query)
        } catch (e: Exception) {
            Log.e("album search", e.message.toString())
            null
        }
    }

    override suspend fun findReleaseGroupMB(mbId: String): ReleaseGroupMB? {
        return try {
            musicbrainzApiService.findReleaseGroup(mbId, "artist-credits+tags")
        } catch (e: Exception) {
            Log.e("album search", e.message.toString())
            null
        }
    }


    override suspend fun findAlbumDiscogs(
        artist: String,
        album: String,
        year: String?
    ): DiscogsSearchResponse? {
        return try {
            val response = discogsApiService.searchAlbum(artist, album, year)
            if (response.results.isEmpty()) {
                try {
                    delay(1000)
                    discogsApiService.searchAlbum(artist, album, null)
                } catch (e: Exception) {
                    Log.e("discogs search", e.message.toString())
                    null
                }
            } else {
                response
            }
        } catch (e: Exception) {
            Log.e("discogs search", e.message.toString())
            null
        }
    }

    override suspend fun getAlbumDiscogs(releaseId: String): AlbumDiscogsResponse? {
        return try {
            discogsApiService.getAlbum(releaseId)
        } catch (e: Exception) {
            Log.e("discogs album", e.message.toString())
            null
        }
    }

    override suspend fun getAlbumArt(mbid: String): String? {
        return try {
            val response = coverArtArchiveApiService.getAlbumImage(mbid)
            if (response.images.isNotEmpty()) {
                response.images[0].image.replace("http://", "https://")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AlbumArt", "Failed to fetch art for $mbid: ${e.message}")
            null
        }
    }

    override suspend fun getAllCAAOptions(mbid: String): List<String> {
        return try {
            val response = coverArtArchiveApiService.getAlbumImage(mbid)
            if (response.images.isNotEmpty()) {
                response.images.map { it.image.replace("http://", "https://") }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("AlbumArt", "Failed to fetch art for $mbid: ${e.message}")
            emptyList()
        }

    }

    override suspend fun insertAll(albums: List<Album>) {
        albumDao.insertAll(albums)
    }

    override suspend fun insert(album: Album) {
        albumDao.insert(album)
    }

    override suspend fun insertWithReturn(album: Album): Long {
        return albumDao.insertWithReturn(album)
    }

    override suspend fun update(album: Album) {
        albumDao.update(album)
    }

    override suspend fun delete(album: Album) {
        albumDao.delete(album)
    }

    override suspend fun deleteById(albumId: Int) {
        albumDao.deleteById(albumId)
    }

    override suspend fun deleteOrphaned() {
        albumDao.deleteOrphaned()
    }

    override suspend fun moveTracks(oldAlbumId: Int, newAlbumId: Int, tracks: List<Int>?) {
        val trackIds =
            if (tracks != null && tracks.isNotEmpty()) tracks else trackDao.getAlbumTracks(
                oldAlbumId
            ).map { it.trackId }
        trackDao.moveToAlbum(oldAlbumId, newAlbumId, trackIds)
    }

    override suspend fun getAlbumByMbid(mbid: String): Album? {
        return albumDao.getAlbumByMbid(mbid)
    }

    override suspend fun saveCustomImage(
        uri: Uri,
        albumId: Int
    ): String? {
        return imageStorageManager.saveCustomArtwork(uri, ImageTarget.ALBUM, albumId)
    }

    override suspend fun getCustomImages(albumId: Int): List<String> {
        return imageStorageManager.getCustomImagesForEntity(ImageTarget.ALBUM, albumId)
    }

    override suspend fun deleteCustomImage(path: String) {
        imageStorageManager.deleteImageFile(path)
    }
}