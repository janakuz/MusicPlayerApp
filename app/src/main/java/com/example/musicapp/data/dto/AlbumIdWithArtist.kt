package com.example.musicapp.data.dto

import androidx.room.Embedded
import com.example.musicapp.data.entity.Artist

data class AlbumIdWithArtist(
    val albumId: Int,
    @Embedded val artist: Artist
)