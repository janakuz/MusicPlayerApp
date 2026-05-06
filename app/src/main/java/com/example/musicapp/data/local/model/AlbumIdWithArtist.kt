package com.example.musicapp.data.local.model

import androidx.room.Embedded
import com.example.musicapp.data.local.entity.Artist

data class AlbumIdWithArtist(
    val albumId: Int,
    @Embedded val artist: Artist
)