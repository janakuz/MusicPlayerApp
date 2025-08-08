package com.example.musicapp.data.repository

import com.example.musicapp.data.dao.ArtistDao
import com.example.musicapp.data.dto.ArtistDicogsResponse
import com.example.musicapp.data.dto.ArtistMBResponse
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.service.DiscogsApiService
import com.example.musicapp.data.service.LastfmApiService
import com.example.musicapp.data.service.MusicbrainzApiService
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

    override fun getArtist(id: Int): Flow<Artist> {
        return artistDao.getArtist(id)
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

    override suspend fun getArtistImage(discogsId: String): String {
        return discogsApiService.getImages(discogsId).images[0].resourceUrl
    }

    override suspend fun getArtistBio(mbid: String): String {
        return lastfmApiService.getArtistInfo(mbid = mbid).artist.bio.content
    }

    override suspend fun insertAll(artists: List<Artist>) {
        artistDao.insertAll(artists)
    }

    override suspend fun insert(artist: Artist) = artistDao.insert(artist)

    override suspend fun insertWithReturn(artist: Artist): Long {
        return artistDao.insertWithReturn(artist)
    }

    override suspend fun update(artist: Artist) {
        artistDao.update(artist)
    }

    override suspend fun delete(artist: Artist) {
        artistDao.delete(artist)
    }

    override suspend fun insertAllString(names: List<String>) {
        val artists = names.map { Artist(name = it) }
        artistDao.insertAll(artists)
    }
}
