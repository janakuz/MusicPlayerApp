package com.example.musicapp.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed class GridItem {
    abstract val id: Int
    abstract val imageRes: String
    abstract val displayName: String

    data class ArtistItem(
        override val id: Int,
        override val displayName: String,
        override val imageRes: String,
        val description: String
    ) : GridItem() {
//        fun createRoute(artistId: Int) = "artist/$id"
    }

    data class AlbumItem(
        override val id: Int,
        override val displayName: String,
        override val imageRes: String,
        val releaseYear: String,
        @StringRes val numTracks: Int,
        @StringRes val duration: Int,
  //      val artist: Int
    ) : GridItem() {
  //      fun createRoute(artistId: Int) = "artist/$id"
    }
}