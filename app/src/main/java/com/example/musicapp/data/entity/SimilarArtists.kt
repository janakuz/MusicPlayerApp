package com.example.musicapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "similar_artists",
//    primaryKeys = ["artistId", "albumId"],
    foreignKeys = [
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["id"],
            childColumns = ["artist1Id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["id"],
            childColumns = ["artist2Id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("artist1Id"), Index("artist2Id")]
)
data class SimilarArtists(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val artist1Id: Int,
    val artist2Id: Int,
    val similarityScore: Double
)