package com.example.musicapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracks",
    foreignKeys = [
        ForeignKey(entity = Album::class, parentColumns = ["id"], childColumns = ["albumId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Artist::class, parentColumns = ["id"], childColumns = ["artistId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("albumId"), Index("artistId")]
)
data class Track(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val albumId: Int,
    val artistId: Int,
    val duration: Long,
    val plays: Int = 0,
    val mbId: String?,
    val lyrics: String?,
    val trackNumber: Int?,
    val lastPlayed: Long?,
    val fileUri: String,
    val valence: Int?,
    val energy: Int?,
    val key: String?,
    val bpm: Int?

)