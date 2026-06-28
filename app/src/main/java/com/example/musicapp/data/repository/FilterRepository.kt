package com.example.musicapp.data.repository

import androidx.annotation.FloatRange
import com.example.musicapp.data.local.entity.AreaHierarchy
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AlbumInfo
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.data.remote.dto.Key
import kotlinx.coroutines.flow.Flow

interface FilterRepository {

    fun getFilteredAlbums(filter: LibraryFilter): Flow<List<AlbumInfo>>

    fun getFilteredArtists(filter: LibraryFilter): Flow<List<Artist>>

    fun getFilteredTracks(filter: LibraryFilter): Flow<List<TrackInfo>>


    fun getMinYear(): Flow<Int>

    fun getMaxYear(): Flow<Int>

    fun getMinYearArtists(): Flow<Int>

    fun getMaxYearArtists(): Flow<Int>


    fun getAllLabels(): Flow<List<String>>

    fun findLabel(query: String): Flow<List<String>>
}

data class LibraryFilter(
    val logic: FilterLogic = FilterLogic.AND,
    val activeRange: IntRange = 1950..2026,
    val activeArtistStartRange: IntRange = 1950..2026,
    val activeArtistEndRange: IntRange = 1950..2026,
    val activeBPMRange: IntRange = 40..250,
    val dateRanges: List<IntRange> = emptyList(),
    val selectedLabels: Set<String> = emptySet(),
    val durationRanges: List<LongRange> = emptyList(),
    val selectedGenres: Set<String> = emptySet(),
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

    )

enum class FilterLogic { AND, OR }

enum class FilterSection {
    ARTISTS,
    ALBUMS,
    TRACKS
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