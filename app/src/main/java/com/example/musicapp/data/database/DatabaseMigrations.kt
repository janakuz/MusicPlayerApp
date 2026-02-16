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

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE artists add searchKey TEXT NOT NULL DEFAULT '';")
        db.execSQL("ALTER TABLE albums add searchKey TEXT NOT NULL DEFAULT '';")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_artists_searchKey ON artists(searchKey)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_albums_searchKey ON albums(searchKey)")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE artists add isEnriched INTEGER NOT NULL DEFAULT 0;")
        db.execSQL("ALTER TABLE albums add isEnriched INTEGER NOT NULL DEFAULT 0;")
    }
}


val MIGRATION_9_10 = object : Migration(9,10) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("ALTER TABLE albums RENAME TO albums_old")
        db.execSQL("CREATE TABLE IF NOT EXISTS `albums` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT NOT NULL, " +
                "`searchKey` TEXT NOT NULL, " +
                "`image` TEXT, " +
                "`duration` INTEGER NOT NULL, " +
                "`numTracks` INTEGER NOT NULL, " +
                "`mbId` TEXT, " +
                "`label` TEXT, " +
                "`discogsId` TEXT, " +
                "`releaseDate` TEXT, " +
                "`isEnriched` INTEGER NOT NULL DEFAULT 0, " +
                "`enrichmentAttempted` INTEGER NOT NULL DEFAULT 0)")


        db.execSQL("""
            INSERT INTO albums (id, title, searchKey, image, duration, numTracks, mbId, label, discogsId, releaseDate, isEnriched)
            SELECT id, title, searchKey, image, duration, numTracks, mbId, label, discogsId, releaseDate, isEnriched FROM albums_old
        """)
        db.execSQL("DROP TABLE albums_old")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_albums_searchKey` ON `albums` (`searchKey`)")

        db.execSQL("ALTER TABLE artists RENAME TO artists_old")
        db.execSQL("CREATE TABLE IF NOT EXISTS `artists` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`searchKey` TEXT NOT NULL, " +
                "`bio` TEXT, " +
                "`image` TEXT, " +
                "`mbId` TEXT, " +
                "`discogsId` TEXT, " +
                "`lastFmPage` TEXT, " +
                "`isEnriched` INTEGER NOT NULL DEFAULT 0, " +
                "`enrichmentAttempted` INTEGER NOT NULL DEFAULT 0)")


        db.execSQL("""
            INSERT INTO artists (id, name, searchKey, bio, image, mbId, discogsId, lastFmPage, isEnriched)
            SELECT id, name, searchKey, bio, image, mbId, discogsId, lastFmPage, isEnriched FROM artists_old
        """)
        db.execSQL("DROP TABLE artists_old")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_artists_searchKey` ON `artists` (`searchKey`)")



//        db.execSQL("ALTER TABLE artists add enrichmentAttempted INTEGER NOT NULL DEFAULT 0;")
//        db.execSQL("ALTER TABLE albums add enrichmentAttempted INTEGER NOT NULL DEFAULT 0;")
        db.execSQL("UPDATE artists SET enrichmentAttempted = 1 WHERE isEnriched = 1")
        db.execSQL("UPDATE albums SET enrichmentAttempted = 1 WHERE isEnriched = 1")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `genres` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `name` TEXT NOT NULL
            )
        """)

        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_genres_name` ON `genres` (`name`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `album_genres` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `genreId` INTEGER NOT NULL, 
                `albumId` INTEGER NOT NULL, 
                FOREIGN KEY(`genreId`) REFERENCES `genres`(`id`) ON DELETE CASCADE, 
                FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON DELETE CASCADE
            )
        """)

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_album_genres_genreId` ON `album_genres` (`genreId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_album_genres_albumId` ON `album_genres` (`albumId`)")
    }
}

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7,
    MIGRATION_7_8,
    MIGRATION_8_9,
    MIGRATION_9_10,
    MIGRATION_10_11)