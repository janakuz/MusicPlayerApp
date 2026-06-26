package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "tracks",
    foreignKeys = [
        ForeignKey(
            entity = Album::class,
            parentColumns = ["id"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["id"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE
        )
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
    val filePath: String,
    val key: String? = null,
    val bpm: Int? = null,
    val loudness: Double? = null,
    val dynamicComplexity: Double? = null,
    val approachability: Double? = null,
    val engagement: Double? = null,
    val danceability: Double? = null,
    val moodAggressive: Double? = null,
    val moodHappy: Double? = null,
    val moodParty: Double? = null,
    val moodRelaxed: Double? = null,
    val moodSad: Double? = null,
    val instrumental: Boolean? = null,
    val voice: String? = null
)