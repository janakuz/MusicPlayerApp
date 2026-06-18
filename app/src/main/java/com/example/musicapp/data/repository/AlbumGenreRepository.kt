package com.example.musicapp.data.repository

interface AlbumGenreRepository {

    suspend fun updateAlbumGenres(albumId: Int, genres: List<String>)

    suspend fun getAlbumGenres(albumId: Int): List<String>

    suspend fun insertAlbumGenres(albumId: Int, genres: List<String>)
}