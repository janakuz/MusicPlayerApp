package com.example.musicapp.data.remote.dto

data class LRCLibResponse(
    val id: Long,
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val duration: Float,
    val instrumental: Boolean,
    val plainLyrics: String?,
    val syncedLyrics: String?,
)
