package com.example.musicapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "key_compatibility")
data class KeyCompatibility(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceKey: String,
    val compatibleKey: String,
    val harmonicDistance: Float,
    val matchDescription: String,
    )
