package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artist_genres",
    foreignKeys = [
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["id"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Genre::class,
            parentColumns = ["id"],
            childColumns = ["genreId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value=["artistId", "genreId"], unique = true), Index("genreId")]
)
data class ArtistGenre(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val artistId: Int,
    val genreId: Int
)