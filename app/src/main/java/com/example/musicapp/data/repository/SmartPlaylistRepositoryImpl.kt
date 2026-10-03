package com.example.musicapp.data.repository

import com.google.gson.*
import java.lang.reflect.Type
import com.example.musicapp.data.local.dao.SmartPlaylistDao
import com.example.musicapp.data.local.entity.SmartPlaylist
import com.example.musicapp.data.local.model.TrackInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SmartPlaylistRepositoryImpl(
    private val smartPlaylistDao: SmartPlaylistDao,
    private val filterRepository: FilterRepository
) : SmartPlaylistRepository {

    val smartPlaylistGson = GsonBuilder()
        .registerTypeAdapter(IntRange::class.java, IntRangeAdapter())
        .registerTypeAdapter(LongRange::class.java, LongRangeAdapter())
        .registerTypeAdapter(ClosedFloatingPointRange::class.java, FloatRangeAdapter())
        .create()

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

    override fun getSmartPlaylist(playlistId: Int): Flow<List<TrackInfo>> {
        val playlist = smartPlaylistDao.getSmartPlaylistById(playlistId)

        if (playlist != null) {
            val filter = smartPlaylistGson.fromJson(playlist.filterJson, LibraryFilter::class.java)

            val rawQuery = filterRepository.buildLibraryQuery(filter, FilterSection.GLOBAL)

            return smartPlaylistDao.getFilteredTracks(rawQuery)
        }
        return flowOf(emptyList())
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
