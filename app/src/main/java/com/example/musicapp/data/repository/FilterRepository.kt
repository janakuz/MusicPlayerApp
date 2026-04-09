package com.example.musicapp.data.repository

import com.example.musicapp.data.dto.AlbumInfo
import kotlinx.coroutines.flow.Flow

interface FilterRepository {

    fun getFilteredAlbums(filter: LibraryFilter): Flow<List<AlbumInfo>>

    fun getMinYear(): Flow<Int>

    fun getMaxYear(): Flow<Int>

    fun getAllLabels(): Flow<List<String>>
}

data class LibraryFilter(
    val logic: FilterLogic = FilterLogic.AND,
    val activeRange: IntRange = 1950..2026,
    val dateRanges: List<IntRange> = emptyList(),
    val selectedLabels: Set<String> = emptySet(),
    val durationRanges: List<LongRange> = emptyList()
)

enum class FilterLogic { AND, OR }