package com.example.musicapp.data.service

import com.example.musicapp.data.dto.AlbumDiscogsResponse
import retrofit2.http.Query
import com.example.musicapp.data.dto.ArtistDicogsResponse
import com.example.musicapp.data.dto.DiscogsSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface DiscogsApiService {

    @GET("artists/{artistId}")
    suspend fun getArtist(@Path("artistId") artistId: String) : ArtistDicogsResponse

    @GET("albums/{releaseId}")
    suspend fun getAlbum(@Path("releaseId") albumId: String) : AlbumDiscogsResponse

    @GET("/database/search")
    suspend fun searchAlbum(
        @Query("artist") artist: String,
        @Query("release_title") title: String,
        @Query("year") year: String?
    ) : DiscogsSearchResponse




}