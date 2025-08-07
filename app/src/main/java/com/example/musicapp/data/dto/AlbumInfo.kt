package com.example.musicapp.data.dto

data class AlbumInfo(
    val albumId: Int,
    val title: String,
    val releaseDate: String?,
    val artistName: String,
    val artistId: Int,
    val image: String?,
    val label: String?,
    val duration: Long
)