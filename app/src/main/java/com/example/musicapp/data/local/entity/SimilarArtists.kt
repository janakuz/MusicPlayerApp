package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "similar_artists",
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
    indices = [Index("artist2Id"), Index(value=["artist1Id","artist2Id"], unique = true)]
)
data class SimilarArtists(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val artist1Id: Int,
    val artist2Id: Int,
    val similarityScore: Double
)