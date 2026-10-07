package com.example.musicapp.data.repository

import android.R
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.gson.*
import java.lang.reflect.Type
import com.example.musicapp.data.local.dao.SmartPlaylistDao
import com.example.musicapp.data.local.entity.SmartPlaylist
import com.example.musicapp.data.local.model.TrackInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SmartPlaylistRepositoryImpl(
    private val smartPlaylistDao: SmartPlaylistDao,
    private val filterRepository: FilterRepository,
    private val smartPlaylistGson: Gson
) : SmartPlaylistRepository {
    override suspend fun savePlaylist(
        filter: LibraryFilter,
        name: String
    ) {
        val jsonString = smartPlaylistGson.toJson(filter)

        val toInsert = SmartPlaylist(
            name = name,
            filterJson = jsonString
        )

        smartPlaylistDao.insert(toInsert)
    }

    override suspend fun updatePlaylist(playlist: SmartPlaylist) {
        smartPlaylistDao.update(playlist)
    }

    override suspend fun delete(playlistId: Int) {
        smartPlaylistDao.deleteById(playlistId)
    }

    private fun getFilter(filterJson: String, initialTimestamp: Long): SimpleSQLiteQuery {
        val filter = smartPlaylistGson.fromJson(filterJson, LibraryFilter::class.java)
        val rawQuery = filterRepository.buildLibraryQuery(filter, FilterSection.GLOBAL, initialTimestamp)

        return rawQuery
    }

    override fun getSmartPlaylistTracks(filterJson: String, initialTimestamp: Long): Flow<List<TrackInfo>> {
        val filter = smartPlaylistGson.fromJson(filterJson, LibraryFilter::class.java)

        return filterRepository.getGlobalTracks(filter, initialTimestamp)
    }

    override suspend fun getSmartPlaylistTracksFromId(playlistId: Int, initialTimestamp: Long): List<TrackInfo> {
        val playlist = smartPlaylistDao.getSmartPlaylistById(playlistId)
        val query = getFilter(playlist.filterJson, initialTimestamp)

        return smartPlaylistDao.getFilteredTracksSuspend(query)
    }

    override fun getAll(): Flow<List<SmartPlaylist>> {
        return smartPlaylistDao.getAll()
    }

    override fun getSmartPlaylist(playlistId: Int): Flow<SmartPlaylist> {
        return smartPlaylistDao.getById(playlistId)
    }

    override suspend fun getSmartPlaylistById(playlistId: Int): SmartPlaylist {
        return smartPlaylistDao.getSmartPlaylistById(playlistId)
    }

    override fun getSmartPlaylistStats(filterJson: String, initialTimestamp: Long): Flow<PlaylistStats> {
        val filter = smartPlaylistGson.fromJson(filterJson, LibraryFilter::class.java)
        val rawQuery = filterRepository.buildLibraryQueryParts(filter, FilterSection.GLOBAL, initialTimestamp)

        val statsSql = """
                        SELECT COUNT(sub.trackId) AS trackCount, SUM(sub.duration) AS duration 
                        FROM (${rawQuery.sql}) AS sub
                       """.trimIndent()

        val statsQuery = SimpleSQLiteQuery(statsSql, rawQuery.args)

        val artSql = """
                      SELECT DISTINCT sub.albumArt 
                      FROM (${rawQuery.sql}) AS sub 
                      WHERE sub.albumArt IS NOT NULL AND sub.albumArt != '' 
                      LIMIT 4
                    """.trimIndent()

        val artQuery = SimpleSQLiteQuery(artSql, rawQuery.args)

        return combine (
            smartPlaylistDao.getSmartPlaylistStats(statsQuery),
            smartPlaylistDao.getSmartPlaylistCollageArtwork(artQuery)) { rawStats, artworkUris ->
            PlaylistStats(
                trackCount = rawStats.trackCount,
                duration = rawStats.duration,
                images = artworkUris
            )
        }

    }
}


class IntRangeAdapter : JsonSerializer<IntRange>, JsonDeserializer<IntRange> {
    override fun serialize(src: IntRange, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonObject().apply {
            addProperty("start", src.first)
            addProperty("end", src.last)
        }
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): IntRange {
        val obj = json.asJsonObject
        return obj.get("start").asInt..obj.get("end").asInt
    }
}

class LongRangeAdapter : JsonSerializer<LongRange>, JsonDeserializer<LongRange> {
    override fun serialize(src: LongRange, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonObject().apply {
            addProperty("start", src.first)
            addProperty("end", src.last)
        }
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LongRange {
        val obj = json.asJsonObject
        return obj.get("start").asLong..obj.get("end").asLong
    }
}


class FloatRangeAdapter : JsonSerializer<ClosedFloatingPointRange<Float>>, JsonDeserializer<ClosedFloatingPointRange<Float>> {
    override fun serialize(src: ClosedFloatingPointRange<Float>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonObject().apply {
            addProperty("start", src.start)
            addProperty("end", src.endInclusive)
        }
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ClosedFloatingPointRange<Float> {
        val obj = json.asJsonObject
        return obj.get("start").asFloat..obj.get("end").asFloat
    }
}
