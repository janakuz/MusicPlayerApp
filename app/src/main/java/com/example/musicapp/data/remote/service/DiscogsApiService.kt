package com.example.musicapp.data.remote.service

import com.example.musicapp.data.remote.dto.AlbumDiscogsResponse
import com.example.musicapp.data.remote.dto.ArtistDicogsResponse
import com.example.musicapp.data.remote.dto.DiscogsSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DiscogsApiService {

    @GET("artists/{artistId}")
    suspend fun getArtist(@Path("artistId") artistId: String): ArtistDicogsResponse

    @GET("releases/{releaseId}")
    suspend fun getAlbum(@Path("releaseId") albumId: String): AlbumDiscogsResponse

    @GET("/database/search")
    suspend fun searchAlbum(
        @Query("artist") artist: String,
        @Query("release_title") title: String,
        @Query("year") year: String?
    ): DiscogsSearchResponse


}