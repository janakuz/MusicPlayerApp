package com.example.musicapp.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS play_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                trackId INTEGER NOT NULL, 
                orderIndex INTEGER NOT NULL,
                FOREIGN KEY(trackId) REFERENCES tracks(id) ON UPDATE CASCADE ON DELETE CASCADE 
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_play_queue_trackId ON play_queue (trackId)")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""DROP TABLE IF EXISTS play_queue;""")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS play_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                uuid TEXT NOT NULL,
                trackId INTEGER NOT NULL, 
                orderIndex INTEGER NOT NULL,
                FOREIGN KEY(trackId) REFERENCES tracks(id) ON UPDATE CASCADE ON DELETE CASCADE 
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_play_queue_trackId ON play_queue (trackId)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""DROP TABLE IF EXISTS play_queue;""")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS play_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                uuid TEXT NOT NULL,
                trackId INTEGER NOT NULL, 
                orderIndex INTEGER NOT NULL,
                shuffledIndex INTEGER,
                FOREIGN KEY(trackId) REFERENCES tracks(id) ON UPDATE CASCADE ON DELETE CASCADE 
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_play_queue_trackId ON play_queue (trackId)")
    }
}

val ALL_MIGRATIONS = arrayOf(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)