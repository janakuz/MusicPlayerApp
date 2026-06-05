package com.example.musicapp.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musicapp.data.local.dao.AlbumArtistDao
import com.example.musicapp.data.local.dao.AlbumDao
import com.example.musicapp.data.local.dao.AlbumGenreDao
import com.example.musicapp.data.local.dao.ArtistDao
import com.example.musicapp.data.local.dao.ArtistGenreDao
import com.example.musicapp.data.local.dao.GenreDao
import com.example.musicapp.data.local.dao.MoodDao
import com.example.musicapp.data.local.dao.PlaylistDao
import com.example.musicapp.data.local.dao.PlaylistTracksDao
import com.example.musicapp.data.local.dao.QueueDao
import com.example.musicapp.data.local.dao.TrackDao
import com.example.musicapp.data.local.dao.TrackMoodDao
import com.example.musicapp.data.local.entity.Album
import com.example.musicapp.data.local.entity.AlbumArtist
import com.example.musicapp.data.local.entity.AlbumGenre
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.entity.ArtistGenre
import com.example.musicapp.data.local.entity.Genre
import com.example.musicapp.data.local.entity.Mood
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.local.entity.PlaylistTracks
import com.example.musicapp.data.local.entity.QueueItem
import com.example.musicapp.data.local.entity.Track
import com.example.musicapp.data.local.entity.TrackMood

@Database(
    entities = [
        Artist::class,
        Album::class,
        Track::class,
        AlbumArtist::class,
        QueueItem::class,
        Genre::class,
        AlbumGenre::class,
        Mood::class,
        TrackMood::class,
        Playlist::class,
        PlaylistTracks::class,
        ArtistGenre::class,
    ],
    version = 15,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao
    abstract fun trackDao(): TrackDao
    abstract fun albumArtistDao(): AlbumArtistDao
    abstract fun queueDao(): QueueDao
    abstract fun genreDao(): GenreDao
    abstract fun albumGenreDao(): AlbumGenreDao
    abstract fun moodDao(): MoodDao
    abstract fun trackMoodDao(): TrackMoodDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTracksDao(): PlaylistTracksDao
    abstract fun ArtistGenreDao(): ArtistGenreDao


//    companion object {
////        @Volatile
////        private var INSTANCE: AppDatabase? = null
//
////        fun getDatabase(context: Context): AppDatabase {
////            return INSTANCE ?: synchronized(this) {
////                val instance = Room.databaseBuilder(
////                    context.applicationContext,
////                    AppDatabase::class.java,
////                    "music_app_db"
////                )
////                    .fallbackToDestructiveMigration(true)
////                    .build()
////                INSTANCE = instance
////                instance
////            }
////        }
//    }
}
