package com.example.musicapp.data.repository

interface ArtistGenreRepository {

    suspend fun updateArtistGenres(artistId: Int, genres: List<String>)

    suspend fun getArtistGenres(artistId: Int): List<String>

    suspend fun insertArtistGenres(artistId: Int, genres: List<String>)
}