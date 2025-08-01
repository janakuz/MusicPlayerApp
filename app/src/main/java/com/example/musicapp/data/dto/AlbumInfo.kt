package com.example.musicapp.data.dto

data class AlbumInfo(
    val albumId: Int,
    val title: String,
    val releaseDate: String?,
    val artistName: String,
    val image: String?,
    val duration: Long
)