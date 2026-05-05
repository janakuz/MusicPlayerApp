package com.example.musicapp.data.local.model

data class AlbumInfo(
    val albumId: Int,
    val title: String,
    val releaseDate: String?,
    val artistName: String,
    val artistId: Int,
    val image: String?,
    val label: String?,
    val mbId: String? = null,
    val duration: Long,
    val numTracks: Int
)