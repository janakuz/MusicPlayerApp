package com.example.musicapp.data.dto

data class AlbumDiscogsResponse(
    val artists: List<DiscogsAlbumArtist>
)

data class DiscogsAlbumArtist(
    val name: String,
    val id: String
)
