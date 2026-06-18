package com.example.musicapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "l_area_area",
    foreignKeys = [
        ForeignKey(
            entity = Area::class,
            parentColumns = ["id"],
            childColumns = ["entity0"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Area::class,
            parentColumns = ["id"],
            childColumns = ["entity1"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("entity0"), Index("entity1")]
)
data class AreaArea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name="entity0") val parent: Int,
    @ColumnInfo(name="entity1") val child: Int,
    )
