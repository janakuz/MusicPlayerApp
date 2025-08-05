package com.example.musicapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "album_artists",
//    primaryKeys = ["artistId", "albumId"],
    foreignKeys = [
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["id"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Album::class,
            parentColumns = ["id"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("artistId"), Index("albumId")]
)
data class AlbumArtist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val artistId: Int,
    val albumId: Int
)