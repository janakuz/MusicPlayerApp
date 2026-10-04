package com.example.musicapp.data.repository

import androidx.annotation.FloatRange
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.musicapp.data.local.entity.AreaHierarchy
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.data.remote.dto.Key
import com.example.musicapp.ui.viewmodels.FilterDefaults
import kotlinx.coroutines.flow.Flow

interface FilterRepository {

    fun getFilteredAlbums(filter: LibraryFilter): Flow<List<AlbumInfo>>

    fun getFilteredArtists(filter: LibraryFilter): Flow<List<Artist>>

    fun getFilteredTracks(filter: LibraryFilter): Flow<List<TrackInfo>>

    fun buildLibraryQuery(filter: LibraryFilter, type: FilterSection,): SimpleSQLiteQuery

    fun buildLibraryQueryParts(filter: LibraryFilter, section: FilterSection): BoundQuery

    fun getMinYear(): Flow<Int>

    fun getMaxYear(): Flow<Int>

    fun getMinYearArtists(): Flow<Int>

    fun getMaxYearArtists(): Flow<Int>


    fun getAllLabels(): Flow<List<String>>

    fun findLabel(query: String): Flow<List<String>>
}

data class LibraryFilter(
    val logic: FilterLogic = FilterLogic.AND,
//    val activeRange: IntRange = 1950..2026,
//    val activeArtistStartRange: IntRange = 1950..2026,
//    val activeArtistEndRange: IntRange = 1950..2026,
//    val activeBPMRange: IntRange = 40..250,
    val activeRange: IntRange? = null,
    val activeArtistStartRange: IntRange? = null,
    val activeArtistEndRange: IntRange? = null,
    val activeBPMRange: IntRange? = null,
    val dateRanges: List<IntRange> = emptyList(),
    val selectedLabels: Set<String> = emptySet(),
    val durationRanges: List<LongRange> = emptyList(),
    val selectedArtistGenres: Set<String> = emptySet(),
    val selectedAlbumGenres: Set<String> = emptySet(),
    val selectedMoods: Set<String> = emptySet(),
    val selectedCountries: Set<String> = emptySet(),
    val defunctStatus: DefunctFilterStatus = DefunctFilterStatus.ALL,
    val artistFormedRanges: List<IntRange> = emptyList(),
    val artistEndedRanges: List<IntRange> = emptyList(),
    val selectedAreas: List<AreaHierarchy> = emptyList(),
    val instrumental: Instrumental = Instrumental.ANY,
    val voice: VoiceGender = VoiceGender.ALL,
    val approachabilityRanges: List<ClosedFloatingPointRange<Float>> = emptyList(),
    val engagementRanges: List<ClosedFloatingPointRange<Float>> = emptyList(),
    val danceabilityRanges: List<ClosedFloatingPointRange<Float>> = emptyList(),
    val moodAggressiveRanges: List<ClosedFloatingPointRange<Float>> = emptyList(),
    val moodHappyRanges: List<ClosedFloatingPointRange<Float>> = emptyList(),
    val moodPartyRanges: List<ClosedFloatingPointRange<Float>> = emptyList(),
    val moodRelaxedRanges: List<ClosedFloatingPointRange<Float>> = emptyList(),
    val moodSadRanges: List<ClosedFloatingPointRange<Float>> = emptyList(),
    val bpmRanges: List<IntRange> = emptyList(),
    val selectedKeys: List<Key> = emptyList(),
    val activeApproachabilityRange: ClosedFloatingPointRange<Float> = 0f..1f,
    val activeEngagementRange: ClosedFloatingPointRange<Float> = 0f..1f,
    val activeAggressiveRange: ClosedFloatingPointRange<Float> = 0f..1f,
    val activeHappyRange: ClosedFloatingPointRange<Float> = 0f..1f,
    val activePartyRange: ClosedFloatingPointRange<Float> = 0f..1f,
    val activeRelaxedRange: ClosedFloatingPointRange<Float> = 0f..1f,
    val activeSadRange: ClosedFloatingPointRange<Float> = 0f..1f,
    val activeDanceabilityRange: ClosedFloatingPointRange<Float> = 0f..1f,
    val activeKeySelection: Key = Key(null, null),
    val addedInPastDays: Int? = null,
    val playedInPastDays: Int? = null,
    val minPlays: Int? = null,
    val maxPlays: Int? = null,
)

enum class FilterLogic { AND, OR }

enum class FilterSection {
    ARTISTS,
    ALBUMS,
    TRACKS,
    GLOBAL
}

enum class DefunctFilterStatus {
    ALL,
    ACTIVE,
    DEFUNCT
}

enum class Instrumental {
    ANY,
    INSTRUMENTAL,
    VOCAL
}

enum class VoiceGender {
    ALL,
    MALE,
    FEMALE,
    MIXED
}

data class BoundQuery(
    val sql: String,
    val args: Array<Any?>
) {
    fun toSimpleSQLiteQuery(): SimpleSQLiteQuery {
        return SimpleSQLiteQuery(sql, args)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BoundQuery
        if (sql != other.sql) return false
        if (!args.contentEquals(other.args)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = sql.hashCode()
        result = 31 * result + args.contentHashCode()
        return result
    }
}