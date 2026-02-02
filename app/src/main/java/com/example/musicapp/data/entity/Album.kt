package com.example.musicapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "albums",
        indices = [Index(value = ["searchKey"])])
data class Album(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val searchKey: String,
    val image: String?,
    val duration: Long,
    val numTracks: Int,
    val mbId: String?,
    val label: String?,
    val discogsId: String?,
    val releaseDate: String?,
    @ColumnInfo(defaultValue = "0")
    val isEnriched: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val enrichmentAttempted: Boolean = false
)
