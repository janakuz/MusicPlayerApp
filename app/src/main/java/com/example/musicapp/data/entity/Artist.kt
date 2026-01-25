package com.example.musicapp.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "artists",
        indices = [Index(value = ["searchKey"])])
data class Artist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val searchKey: String,
    val bio: String? = null,
    val image: String? = null,
    val mbId: String? = null,
    val discogsId: String? = null,
    val lastFmPage: String? = null,
    val isEnriched: Boolean = false
)
