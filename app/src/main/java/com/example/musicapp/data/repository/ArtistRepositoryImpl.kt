package com.example.musicapp.data.repository

import android.util.Log
import com.example.musicapp.data.local.dao.ArtistDao
import com.example.musicapp.data.local.dao.TrackDao
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.entity.SimilarArtists
import com.example.musicapp.data.local.model.ArtistWithArea
import com.example.musicapp.data.local.model.CountryInfo
import com.example.musicapp.data.remote.dto.ArtistDicogsResponse
import com.example.musicapp.data.remote.dto.ArtistInfoLastfm
import com.example.musicapp.data.remote.dto.ArtistLastfmResponse
import com.example.musicapp.data.remote.dto.ArtistMBResponse
import com.example.musicapp.data.remote.dto.ArtistSearchInfo
import com.example.musicapp.data.remote.dto.SimilarArtist
import com.example.musicapp.data.remote.dto.SimilarArtistsResponse
import com.example.musicapp.data.remote.service.DiscogsApiService
import com.example.musicapp.data.remote.service.LastfmApiService
import com.example.musicapp.data.remote.service.MusicbrainzApiService
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.util.normalizeForMatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ArtistRepositoryImpl(
    private val artistDao: ArtistDao,
    private val trackDao: TrackDao,
    private val musicbrainzApiService: MusicbrainzApiService,
    private val discogsApiService: DiscogsApiService,
    private val lastfmApiService: LastfmApiService
) : ArtistRepository {

    override fun getAllArtists(): Flow<List<Artist>> {
        return artistDao.getAllArtists()
    }


    override suspend fun getAll(): List<Artist> {
        return artistDao.getAll()
    }

    override fun getAllArtistsDesc(): Flow<List<Artist>> {
        return artistDao.getAllArtistsDesc()
    }

    override fun getAllArtistsSorted(ascending: Boolean): Flow<List<Artist>> {
        when (ascending) {
            true -> return artistDao.getAllArtistsSortedAsc();
            false -> return artistDao.getAllArtistsSortedDesc();
        }
    }

    override fun getArtist(id: Int): Flow<Artist> {
        return artistDao.getArtist(id)
    }

    override fun getArtistWithArea(id: Int): Flow<ArtistWithArea> {
        return artistDao.getArtistWithArea(id)
    }

    override fun searchArtists(query: String): Flow<List<Artist>> {
        val final = "%${query.trim()}%"

        return artistDao.searchArtists(final)
    }

    override suspend fun getOrCreateArtistByName(name: String, searchKey: String): Int {
        return artistDao.getSingleArtistByName(searchKey)?.id ?: insertByName(name).toInt()
    }

    override suspend fun getArtistByName(name: String): List<Artist> {
        return artistDao.getArtistByName(name)
    }

    override suspend fun getArtistByMbid(mbId: String): Artist? {
        return artistDao.getArtistByMbid(mbId)
    }

    override suspend fun getArtistMusicbrainzInfo(mbid: String): ArtistMBResponse {
        return musicbrainzApiService.getArtist(mbid)
    }

    override suspend fun getArtistDiscogsInfo(discogsId: String): ArtistDicogsResponse? {
        return try {
            discogsApiService.getArtist(discogsId)
        } catch (e: Exception) {
            Log.e("ArtistImage", "Failed to fetch image for $discogsId: ${e.message}")
            null
        }
    }

    override suspend fun getArtistBio(mbid: String?, name: String): String {
        val artistInfo = getArtistLastfmInfo(mbid, name)
        return artistInfo?.bio?.content ?: "No bio available."
//        return try {
//            val response = lastfmApiService.getArtistInfo(mbid = mbid, artist = null)
//            response.artist.bio.content
//        } catch (e: Exception) {
//            try {
//                val fallbackResponse = lastfmApiService.getArtistInfo(artist = name, mbid = null)
//                fallbackResponse.artist.bio.content
//            } catch (e: Exception) {
//                "No bio available."
//            }
//        }

    }

    override suspend fun getArtistLastfmInfo(
        mbid: String?,
        name: String
    ): ArtistInfoLastfm? {
        return try {
            val response = lastfmApiService.getArtistInfo(artist = name, mbid = null)
            response.artist
        } catch (e: Exception) {
//            try {
//                val fallbackResponse = lastfmApiService.getArtistInfo(artist = name, mbid = null)
//                fallbackResponse.artist
//            } catch (e: Exception) {
                Log.e("Last fm info", e.message.toString())
                null
//            }
        }
    }

    override suspend fun getAllSimilarArtists(artistId: Int): List<Int> {
        return artistDao.getAllSimilar(artistId)
    }

    override suspend fun getSimilarArtistsLastfm(artistName: String): List<SimilarArtist> {
        return try {
            val response = lastfmApiService.getSimilarArtists(artist = artistName, mbid=null)
            response.similarartists.artist
        } catch (e: Exception){
            Log.e("Last fm similar", e.message.toString())
            emptyList()
        }
    }

    override suspend fun insertSimilar(artists: List<SimilarArtists>) {
        artistDao.insertSimilarArtists(artists)
    }

    override suspend fun getSimilarArtists(
        artistId: Int,
        minSimilarityScore: Double
    ): Flow<List<Artist>> {
        return artistDao.getSimilarArtists(artistId, minSimilarityScore)
    }

    override suspend fun insertSimilarManual(artist1Id: Int, artist2Id: Int) {
        val entry = SimilarArtists(artist1Id = artist1Id, artist2Id = artist2Id, similarityScore = 1.0)
        artistDao.insertSimilar(entry)
    }

    override suspend fun removeSimilar(artist1Id: Int, artist2Id: Int) {
        artistDao.removeSimilar(artist1Id, artist2Id)
    }

    override suspend fun findArtistMB(artistName: String): List<ArtistSearchInfo> {
        val query = """artist:"${artistName.normalizeForMatching()}" """
        return musicbrainzApiService.findArtist(query).artists
    }

    override suspend fun insertAll(artists: List<Artist>) {
        artistDao.insertAll(artists)
    }

    override suspend fun insert(artist: Artist) = artistDao.insert(artist)

    override suspend fun insertByName(name: String): Long {
        return artistDao.insertWithReturn(
            Artist(
                name = name,
                searchKey = name.normalizeForMatching()
            )
        )
    }

    override suspend fun insertWithReturn(artist: Artist): Long {
        return artistDao.insertWithReturn(artist)
    }

    override suspend fun update(artist: Artist) {
        artistDao.update(artist)
    }

    override suspend fun delete(artist: Artist) {
        artistDao.delete(artist)
    }

    override suspend fun deleteById(artistId: Int) {
        artistDao.deleteById(artistId)
    }

    override suspend fun deleteOrphaned() {
        artistDao.deleteOrphaned()
        artistDao.deleteOrphanedTracks()
    }

    override suspend fun moveTracks(
        oldArtistId: Int,
        newArtistId: Int,
        tracks: List<Int>?
    ) {
        val trackIds =
            if (tracks != null && tracks.isNotEmpty()) tracks else trackDao.getAllTracksByArtist(
                oldArtistId
            ).first().map { it.trackId }
        trackDao.moveToArtist(oldArtistId, newArtistId, trackIds)
    }

    override suspend fun getTrackUrisByArtist(artistId: Int): List<String> {
        return trackDao.getTrackUrisByArtist(artistId)
    }

    override suspend fun insertAllString(names: List<String>) {
        val artists = names.map { Artist(name = it, searchKey = it.normalizeForMatching()) }
        artistDao.insertAll(artists)
    }
}