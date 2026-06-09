package com.example.musicapp.data.repository

import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    val artistSortOption: Flow<SortOption>
    val albumSortOption: Flow<SortOption>
    val trackSortOption: Flow<SortOption>
    val artistAlbumsSortOption: Flow<SortOption>
    val playlistsSortOption: Flow<SortOption>
    val genresSortOption: Flow<SortOption>
    val countriesSortOption: Flow<SortOption>

    val skipSilenceToggle: Flow<Boolean>

    suspend fun updateArtistSort(option: SortOption)
    suspend fun updateAlbumSort(option: SortOption)
    suspend fun updateTrackSort(option: SortOption)
    suspend fun updateArtistAlbumsSort(option: SortOption)
    suspend fun updatePlaylistsSort(option: SortOption)
    suspend fun updateGenresSort(option: SortOption)
    suspend fun updateCountrySort(option: SortOption)

    suspend fun updateSkipSilence(enabled: Boolean)

}