package com.example.musicapp.data.remote.dto

data class AlbumDiscogsResponse(
    val artists: List<DiscogsAlbumArtist>
)

data class DiscogsAlbumArtist(
    val name: String,
    val id: String
)
