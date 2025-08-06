package com.example.musicapp.data.dto

data class AlbumArtImage(
    val images: List<CAAImage>
)

data class CAAImage(
    val front: Boolean,
    val back: Boolean,
    val image: String,
    
)
