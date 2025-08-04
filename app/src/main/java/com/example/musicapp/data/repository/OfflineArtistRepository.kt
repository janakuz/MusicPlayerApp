package com.example.musicapp.data.repository

import com.example.musicapp.data.dao.ArtistDao
import com.example.musicapp.data.entity.Artist
import kotlinx.coroutines.flow.Flow

class OfflineArtistRepository(private val artistDao: ArtistDao) : ArtistRepository {

    //val allArtists: Flow<List<Artist>> = artistDao.getAllArtists()
    override fun getAllArtists(): Flow<List<Artist>> {
        return artistDao.getAllArtists()
    }

    override fun getAllArtistsDesc(): Flow<List<Artist>> {
        return artistDao.getAllArtistsDesc()
    }

    override fun getArtist(id: Int): Flow<Artist> {
        return artistDao.getArtist(id)
    }

    override suspend fun getArtistByName(name: String): Artist {
        return artistDao.getArtistByName(name)
    }

    override suspend fun insertAll(artists: List<Artist>) {
        artistDao.insertAll(artists)
    }

    override suspend fun insert(artist: Artist) = artistDao.insert(artist)

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
