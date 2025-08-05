package com.example.musicapp.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicapp.data.dao.AlbumArtistDao
import com.example.musicapp.data.dao.AlbumDao
import com.example.musicapp.data.dao.ArtistDao
import com.example.musicapp.data.dao.TrackDao
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.AlbumArtist
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.entity.Track
import java.util.concurrent.Executors

@Database(entities = [Artist::class, Album::class, Track::class, AlbumArtist::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao
    abstract fun trackDao(): TrackDao
    abstract fun albumArtistDao(): AlbumArtistDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java,
                                "music_app_db"
                            )
        //            .setQueryCallback(RoomDatabase.QueryCallback { sqlQuery, bindArgs ->
       //                 Log.d("RoomQuery", "SQL: $sqlQuery\nArgs: $bindArgs")
       //             }, Executors.newSingleThreadExecutor())
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
