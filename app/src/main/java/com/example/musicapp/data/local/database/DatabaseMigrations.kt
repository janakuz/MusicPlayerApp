package com.example.musicapp.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS play_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                trackId INTEGER NOT NULL, 
                orderIndex INTEGER NOT NULL,
                FOREIGN KEY(trackId) REFERENCES tracks(id) ON UPDATE CASCADE ON DELETE CASCADE 
            )
        """
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_play_queue_trackId ON play_queue (trackId)")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""DROP TABLE IF EXISTS play_queue;""")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS play_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                uuid TEXT NOT NULL,
                trackId INTEGER NOT NULL, 
                orderIndex INTEGER NOT NULL,
                FOREIGN KEY(trackId) REFERENCES tracks(id) ON UPDATE CASCADE ON DELETE CASCADE 
            )
        """
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_play_queue_trackId ON play_queue (trackId)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""DROP TABLE IF EXISTS play_queue;""")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS play_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                uuid TEXT NOT NULL,
                trackId INTEGER NOT NULL, 
                orderIndex INTEGER NOT NULL,
                shuffledIndex INTEGER,
                FOREIGN KEY(trackId) REFERENCES tracks(id) ON UPDATE CASCADE ON DELETE CASCADE 
            )
        """
        )
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


val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("ALTER TABLE albums RENAME TO albums_old")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `albums` " +
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
                    "`enrichmentAttempted` INTEGER NOT NULL DEFAULT 0)"
        )


        db.execSQL(
            """
            INSERT INTO albums (id, title, searchKey, image, duration, numTracks, mbId, label, discogsId, releaseDate, isEnriched)
            SELECT id, title, searchKey, image, duration, numTracks, mbId, label, discogsId, releaseDate, isEnriched FROM albums_old
        """
        )
        db.execSQL("DROP TABLE albums_old")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_albums_searchKey` ON `albums` (`searchKey`)")

        db.execSQL("ALTER TABLE artists RENAME TO artists_old")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `artists` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`searchKey` TEXT NOT NULL, " +
                    "`bio` TEXT, " +
                    "`image` TEXT, " +
                    "`mbId` TEXT, " +
                    "`discogsId` TEXT, " +
                    "`lastFmPage` TEXT, " +
                    "`isEnriched` INTEGER NOT NULL DEFAULT 0, " +
                    "`enrichmentAttempted` INTEGER NOT NULL DEFAULT 0)"
        )


        db.execSQL(
            """
            INSERT INTO artists (id, name, searchKey, bio, image, mbId, discogsId, lastFmPage, isEnriched)
            SELECT id, name, searchKey, bio, image, mbId, discogsId, lastFmPage, isEnriched FROM artists_old
        """
        )
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
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `genres` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `name` TEXT NOT NULL
            )
        """
        )

        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_genres_name` ON `genres` (`name`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `album_genres` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `genreId` INTEGER NOT NULL, 
                `albumId` INTEGER NOT NULL, 
                FOREIGN KEY(`genreId`) REFERENCES `genres`(`id`) ON DELETE CASCADE, 
                FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON DELETE CASCADE
            )
        """
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_album_genres_genreId` ON `album_genres` (`genreId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_album_genres_albumId` ON `album_genres` (`albumId`)")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `moods` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `name` TEXT NOT NULL
            )
        """
        )

        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_moods_name` ON `moods` (`name`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `track_moods` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `trackId` INTEGER NOT NULL, 
                `moodId` INTEGER NOT NULL, 
                FOREIGN KEY(`moodId`) REFERENCES `moods`(`id`) ON DELETE CASCADE, 
                FOREIGN KEY(`trackId`) REFERENCES `tracks`(`id`) ON DELETE CASCADE
            )
        """
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_track_moods_moodId` ON `track_moods` (`moodId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_track_moods_trackId` ON `track_moods` (`trackId`)")
    }
}


val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tracks add filePath TEXT NOT NULL DEFAULT '';")
    }
}


val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `playlists` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `name` TEXT NOT NULL, 
                `image` TEXT, 
                `description` TEXT, 
                `isSmart` INTEGER NOT NULL DEFAULT 0,
                `createdAt` INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000),
                `lastUpdated` INTEGER NOT NULL
            )
        """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `playlist_tracks` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `playlistId` INTEGER NOT NULL, 
                `trackId` INTEGER NOT NULL, 
                `position` INTEGER NOT NULL,
                `addedAt` INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000),
                FOREIGN KEY(`playlistId`) REFERENCES `playlists`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`trackId`) REFERENCES `tracks`(`id`) ON DELETE CASCADE
            )
        """
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_playlist_tracks_trackId` ON `playlist_tracks` (`trackId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_playlist_tracks_playlistId` ON `playlist_tracks` (`playlistId`)")

    }
}

val MIGRATION_14_15 = object : Migration(14,15){
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP INDEX IF EXISTS `index_album_artists_artistId`")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_album_artists_artistId_albumId` ON `album_artists` (`artistId`, `albumId`)")

        db.execSQL("DROP INDEX IF EXISTS `index_track_moods_trackId`")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_track_moods_trackId_moodId` ON `track_moods` (`trackId`, `moodId`)")

        db.execSQL("DROP INDEX IF EXISTS `index_album_genres_albumId`")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_album_genres_albumId_genreId` ON `album_genres` (`albumId`, `genreId`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `artist_genres` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `genreId` INTEGER NOT NULL, 
                `artistId` INTEGER NOT NULL, 
                FOREIGN KEY(`genreId`) REFERENCES `genres`(`id`) ON DELETE CASCADE, 
                FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON DELETE CASCADE
            )
        """
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_artist_genres_genreId` ON `artist_genres` (`genreId`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_artist_genres_artistId_genreId` ON `artist_genres` (`artistId`, `genreId`)")


    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE artists ADD COLUMN country TEXT")
        db.execSQL("ALTER TABLE artists ADD COLUMN homeCity TEXT")
        db.execSQL("ALTER TABLE artists ADD COLUMN currentCity TEXT")
        db.execSQL("ALTER TABLE artists ADD COLUMN activeStartYear TEXT")
        db.execSQL("ALTER TABLE artists ADD COLUMN activeEndYear TEXT")
        db.execSQL("ALTER TABLE artists ADD COLUMN isDefunct INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE artists ADD COLUMN countryCode TEXT")
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE artists ADD COLUMN homeAreaGid TEXT")

        db.execSQL("CREATE TABLE IF NOT EXISTS `area_type` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`child_order` INTEGER NOT NULL, " +
                "`description` TEXT NOT NULL, " +
                "`gid` TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `area` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`gid` TEXT NOT NULL, " +
                "`name` TEXT, " +
                "`type` INTEGER NOT NULL, " +
                "FOREIGN KEY(`type`) REFERENCES `area_type`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE TABLE IF NOT EXISTS `l_area_area` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`entity0` INTEGER NOT NULL, " +
                "`entity1` INTEGER NOT NULL, " +
                "FOREIGN KEY(`entity0`) REFERENCES `area`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`entity1`) REFERENCES `area`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")

        db.execSQL("CREATE TABLE IF NOT EXISTS `area_hierarchy` " +
                "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`gid` TEXT NOT NULL, " +
                "`city` TEXT, " +
                "`city_name` TEXT, " +
                "`municipality` TEXT, " +
                "`municipality_name` TEXT, " +
                "`county` TEXT, " +
                "`county_name` TEXT, " +
                "`state` TEXT, " +
                "`state_name` TEXT, " +
                "`country` TEXT, " +
                "`country_name` TEXT, " +
                "FOREIGN KEY(`gid`) REFERENCES `area`(`gid`) ON UPDATE NO ACTION ON DELETE CASCADE )")

        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_area_gid` ON `area` (`gid`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_l_area_area_entity0` ON `l_area_area` (`entity0`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_l_area_area_entity1` ON `l_area_area` (`entity1`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_area_hierarchy_gid` ON `area_hierarchy` (`gid`)")

    }

}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE area_hierarchy DROP COLUMN municipality")
        db.execSQL("ALTER TABLE area_hierarchy DROP COLUMN municipality_name")
    }
}

val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `similar_artists` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `artist1Id` INTEGER NOT NULL, 
                `artist2Id` INTEGER NOT NULL, 
                `similarityScore` REAL NOT NULL, 
                FOREIGN KEY(`artist1Id`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , 
                FOREIGN KEY(`artist2Id`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """.trimIndent()
        )

        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_similar_artists_artist2Id` ON `similar_artists` (`artist2Id`)"
        )

        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_similar_artists_artist1Id_artist2Id` ON `similar_artists` (`artist1Id`, `artist2Id`)"
        )
    }
}

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tracks ADD COLUMN loudness REAL")
        db.execSQL("ALTER TABLE tracks ADD COLUMN dynamicComplexity REAL")
        db.execSQL("ALTER TABLE tracks ADD COLUMN approachability REAL")
        db.execSQL("ALTER TABLE tracks ADD COLUMN engagement REAL")
        db.execSQL("ALTER TABLE tracks ADD COLUMN danceability REAL")
        db.execSQL("ALTER TABLE tracks ADD COLUMN moodAggressive REAL")
        db.execSQL("ALTER TABLE tracks ADD COLUMN moodHappy REAL")
        db.execSQL("ALTER TABLE tracks ADD COLUMN moodParty REAL")
        db.execSQL("ALTER TABLE tracks ADD COLUMN moodRelaxed REAL")
        db.execSQL("ALTER TABLE tracks ADD COLUMN moodSad REAL")
        db.execSQL("ALTER TABLE tracks ADD COLUMN instrumental INTEGER")
        db.execSQL("ALTER TABLE tracks ADD COLUMN voice TEXT")
        db.execSQL("ALTER TABLE tracks DROP COLUMN energy")
        db.execSQL("ALTER TABLE tracks DROP COLUMN valence")
    }
}

val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS key_compatibility (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                sourceKey TEXT NOT NULL,
                compatibleKey TEXT NOT NULL,
                harmonicDistance REAL NOT NULL,
                matchDescription TEXT NOT NULL
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sequencer_blocks (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                blockNumber INTEGER NOT NULL,
                blockOrder INTEGER NOT NULL,
                trackId INTEGER NOT NULL, 
                FOREIGN KEY(trackId) REFERENCES tracks(id) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """
        )

        val camelotToKey = mapOf(
            "11B" to "A major" ,  "12B" to "E major", "1B" to "B major", "2B" to "F# major",
            "3B" to "C# major", "4B" to "G# major", "5B" to "D# major", "6B" to "A# major",
            "7B" to "F major", "8B" to "C major", "9B" to "G major", "10B" to "D major",

            "11A" to "F# minor", "12A" to "C# minor", "1A" to "G# minor", "2A" to "D# minor",
            "3A" to "A# minor", "4A" to "F minor", "5A" to "C minor", "6A" to "G minor",
            "7A" to "D minor", "8A" to "A minor", "9A" to "E minor", "10A" to "B minor"
        )

        for (key in camelotToKey.keys){
            val number = key.dropLast(1).toInt()
            val letter = key.takeLast(1)

            val minusOne = if (number == 1) 12 else number - 1
            val plusOne = if (number == 12) 1 else number + 1

            val minusTwo = if (number == 1) 11 else if (number == 2) 12 else number - 2
            val plusTwo = if (number == 12) 2 else if (number == 11) 1 else number + 2


            val oppositeLetter = if (letter == "A") "B" else "A"

            val compatibleKeys = listOf(
                Triple(key, 0.0, "Exact"),
                Triple("$minusOne$letter", -1.0, "Adjacent Down"),
                Triple("$plusOne$letter", 1.0, "Adjacent Up"),
                Triple("$number$oppositeLetter", 1.0, "Relative Key"),

                Triple("$minusOne$oppositeLetter", -1.5, "Diagonal Down"),
                Triple("$plusOne$oppositeLetter", 1.5, "Diagonal Up"),

                Triple("$minusTwo$letter", -2, "Energy Drop"),
                Triple("$plusTwo$letter", 2, "Energy Boost"),

                )

            for (compatKey in compatibleKeys) {
                db.execSQL("""
                        INSERT INTO key_compatibility (sourceKey, compatibleKey, harmonicDistance, matchDescription) 
                        VALUES ('${camelotToKey[key]}', '${camelotToKey[compatKey.first]}', ${compatKey.second}, '${compatKey.third}')
                    """.trimIndent())
            }
        }
    }
}

val MIGRATION_22_23 = object : Migration(22,23) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `track_lyrics` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `trackId` INTEGER NOT NULL,
                `plainLyrics` TEXT,
                `syncedLyrics` TEXT,
                FOREIGN KEY(`trackId`) REFERENCES `tracks`(`id`) ON DELETE CASCADE 
            )
        """
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_track_lyrics_trackId` ON `track_lyrics` (`trackId`)")
        db.execSQL("ALTER TABLE tracks DROP COLUMN lyrics")
    }
}

val MIGRATION_23_24 = object : Migration(23,24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP INDEX IF EXISTS `index_track_lyrics_trackId`")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_track_lyrics_trackId` ON `track_lyrics` (`trackId`)")

    }
}


val ALL_MIGRATIONS = arrayOf(
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7,
    MIGRATION_7_8,
    MIGRATION_8_9,
    MIGRATION_9_10,
    MIGRATION_10_11,
    MIGRATION_11_12,
    MIGRATION_12_13,
    MIGRATION_13_14,
    MIGRATION_14_15,
    MIGRATION_15_16,
    MIGRATION_16_17,
    MIGRATION_17_18,
    MIGRATION_18_19,
    MIGRATION_19_20,
    MIGRATION_20_21,
    MIGRATION_21_22,
    MIGRATION_22_23,
    MIGRATION_23_24
)

//fun getAllMigrations(context: Context): Array<Migration> {
//    return arrayOf(
//        MIGRATION_4_5,
//        MIGRATION_5_6,
//        MIGRATION_6_7,
//        MIGRATION_7_8,
//        MIGRATION_8_9,
//        MIGRATION_9_10,
//        MIGRATION_10_11,
//        MIGRATION_11_12,
//        MIGRATION_12_13,
//        MIGRATION_13_14,
//        MIGRATION_14_15,
//        MIGRATION_15_16,
//        MIGRATION_16_17,
//        MIGRATION_17_18,
//        MIGRATION_18_19,
//        MIGRATION_19_20,
//        MIGRATION_20_21,
//        MIGRATION_21_22
//    )
//}

fun populateMetadataFromAsset(context: Context, db: SupportSQLiteDatabase) {
    val tempFile = context.getDatabasePath("area_metadata.db")

    try {
        context.assets.open("area_metadata.db").use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }

        db.execSQL("PRAGMA writable_schema = ON;")
        db.execSQL("ATTACH DATABASE '${tempFile.absolutePath}' AS temp_db;")

        db.execSQL("DELETE FROM area")
        db.execSQL("DELETE FROM area_type")
        db.execSQL("DELETE FROM l_area_area")
        db.execSQL("DELETE FROM area_hierarchy")

        db.execSQL("INSERT INTO area_type (id, name, child_order, description, gid) SELECT * FROM temp_db.area_type;")
        db.execSQL("INSERT INTO area (id, gid, name, type) SELECT * FROM temp_db.area;")
        db.execSQL("INSERT INTO l_area_area (id, entity0, entity1) SELECT * FROM temp_db.l_area_area;")

        db.execSQL("INSERT INTO area_hierarchy (gid, city, city_name, county, county_name, state, state_name, country, country_name) SELECT gid, city, city_name, county, county_name, state, state_name, country, country_name FROM temp_db.area_hierarchy;")

        db.execSQL("DETACH DATABASE temp_db;")
        db.execSQL("PRAGMA writable_schema = OFF;")

        tempFile.delete()
    }
    catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}