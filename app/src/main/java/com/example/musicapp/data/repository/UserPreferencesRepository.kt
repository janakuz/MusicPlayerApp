package com.example.musicapp.data.repository

import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    val artistSortOption: Flow<SortOption>
    val albumSortOption: Flow<SortOption>
    val trackSortOption: Flow<SortOption>
    val artistAlbumsSortOption: Flow<SortOption>

    suspend fun updateArtistSort(option: SortOption)
    suspend fun updateAlbumSort(option: SortOption)
    suspend fun updateTrackSort(option: SortOption)
    suspend fun updateArtistAlbumsSort(option: SortOption)

}