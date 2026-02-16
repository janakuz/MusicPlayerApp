package com.example.musicapp.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.musicapp.data.dao.AlbumArtistDao
import com.example.musicapp.data.dao.AlbumDao
import com.example.musicapp.data.dao.AlbumGenreDao
import com.example.musicapp.data.dao.ArtistDao
import com.example.musicapp.data.dao.GenreDao
import com.example.musicapp.data.dao.QueueDao
import com.example.musicapp.data.dao.TrackDao
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.AlbumArtist
import com.example.musicapp.data.entity.AlbumGenre
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.entity.Genre
import com.example.musicapp.data.entity.QueueItem
import com.example.musicapp.data.entity.Track
import java.util.concurrent.Executors

@Database(
    entities = [
        Artist::class,
        Album::class,
        Track::class,
        AlbumArtist::class,
        QueueItem::class,
        Genre::class,
        AlbumGenre::class
        ],
    version = 11,
    exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao
    abstract fun trackDao(): TrackDao
    abstract fun albumArtistDao(): AlbumArtistDao
    abstract fun queueDao(): QueueDao
    abstract fun genreDao(): GenreDao
    abstract fun albumGenreDao(): AlbumGenreDao



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
