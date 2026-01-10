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
import com.example.musicapp.data.dao.ArtistDao
import com.example.musicapp.data.dao.QueueDao
import com.example.musicapp.data.dao.TrackDao
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.AlbumArtist
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.entity.QueueItem
import com.example.musicapp.data.entity.Track
import java.util.concurrent.Executors

@Database(
    entities = [Artist::class, Album::class, Track::class, AlbumArtist::class, QueueItem::class],
    version = 7,
    exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao
    abstract fun trackDao(): TrackDao
    abstract fun albumArtistDao(): AlbumArtistDao
    abstract fun queueDao(): QueueDao



    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the new play_queue table
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS `play_queue` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `trackId` INTEGER NOT NULL, 
                `orderIndex` INTEGER NOT NULL,
                FOREIGN KEY(`trackId`) REFERENCES `tracks`(`trackId`) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """)
                // Add an index on trackId since we're using it as a Foreign Key
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_play_queue_trackId` ON `play_queue` (`trackId`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java,
                                "music_app_db"
                            )
                    .addMigrations(MIGRATION_4_5)
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
