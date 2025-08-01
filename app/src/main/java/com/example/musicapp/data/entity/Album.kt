package com.example.musicapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
class Album(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val image: String?,
    val duration: Long,
    val mbId: String?,
    val discogsId: String?,
    val releaseDate: String?
)
