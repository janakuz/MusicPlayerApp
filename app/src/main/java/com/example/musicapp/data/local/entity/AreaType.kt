package com.example.musicapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "area_type")
data class AreaType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "child_order") val childOrder: Int,
    val description: String,
    val gid: String,
    )
