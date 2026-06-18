package com.example.musicapp.data.repository

import com.example.musicapp.data.local.entity.AreaHierarchy
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.AlbumInfo
import kotlinx.coroutines.flow.Flow

interface FilterRepository {

    fun getFilteredAlbums(filter: LibraryFilter): Flow<List<AlbumInfo>>

    fun getFilteredArtists(filter: LibraryFilter): Flow<List<Artist>>

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
    val dateRanges: List<IntRange> = emptyList(),
    val selectedLabels: Set<String> = emptySet(),
    val durationRanges: List<LongRange> = emptyList(),
    val selectedGenres: Set<String> = emptySet(),
    val selectedCountries: Set<String> = emptySet(),
    val defunctStatus: DefunctFilterStatus = DefunctFilterStatus.ALL,
    val artistFormedRanges: List<IntRange> = emptyList(),
    val artistEndedRanges: List<IntRange> = emptyList(),
    val selectedAreas: List<AreaHierarchy> = emptyList()
    )

enum class FilterLogic { AND, OR }

enum class FilterSection {
    ARTISTS,
    ALBUMS
}

enum class DefunctFilterStatus {
    ALL,
    ACTIVE,
    DEFUNCT
}