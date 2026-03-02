package com.example.musicapp.data.repository

import android.util.Log
import com.example.musicapp.data.dao.ArtistDao
import com.example.musicapp.data.dto.ArtistDicogsResponse
import com.example.musicapp.data.dto.ArtistMBResponse
import com.example.musicapp.data.dto.ArtistSearchInfo
import com.example.musicapp.data.dto.ArtistSearchResponse
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.service.DiscogsApiService
import com.example.musicapp.data.service.LastfmApiService
import com.example.musicapp.data.service.MusicbrainzApiService
import com.example.musicapp.normalizeForMatching
import kotlinx.coroutines.flow.Flow

class OfflineArtistRepository(
    private val artistDao: ArtistDao,
    private val musicbrainzApiService: MusicbrainzApiService,
    private val discogsApiService: DiscogsApiService,
    private val lastfmApiService: LastfmApiService) : ArtistRepository {

    override fun getAllArtists(): Flow<List<Artist>> {
        return artistDao.getAllArtists()
    }

    override fun getAllArtistsDesc(): Flow<List<Artist>> {
        return artistDao.getAllArtistsDesc()
    }

    override fun getAllArtistsSorted(ascending: Boolean): Flow<List<Artist>> {
        when(ascending){
            true -> return artistDao.getAllArtistsSortedAsc();
            false -> return artistDao.getAllArtistsSortedDesc();
        }
    }

    override fun getArtist(id: Int): Flow<Artist> {
        return artistDao.getArtist(id)
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
        return try {
            val response = lastfmApiService.getArtistInfo(mbid = mbid, artist = null)
            response.artist.bio.content
        }
        catch (e: Exception) {
            try {
                val fallbackResponse = lastfmApiService.getArtistInfo(artist = name, mbid = null)
                fallbackResponse.artist.bio.content
            }
            catch (e: Exception){
                "No bio available."
            }
        }

    }

    override suspend fun findArtistMB(artistName: String): List<ArtistSearchInfo> {
        return musicbrainzApiService.findArtist("artist:${artistName}").artists
    }

    override suspend fun insertAll(artists: List<Artist>) {
        artistDao.insertAll(artists)
    }

    override suspend fun insert(artist: Artist) = artistDao.insert(artist)

    override suspend fun insertByName(name: String): Long {
        return artistDao.insertWithReturn(Artist(name = name, searchKey = name.normalizeForMatching()))
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

    override suspend fun deleteOrphaned() {
        artistDao.deleteOrphaned()
    }

    override suspend fun insertAllString(names: List<String>) {
        val artists = names.map { Artist(name = it, searchKey = it.normalizeForMatching()) }
        artistDao.insertAll(artists)
    }
}
