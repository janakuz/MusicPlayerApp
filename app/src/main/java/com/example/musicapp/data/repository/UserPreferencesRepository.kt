package com.example.musicapp.data.repository

import com.example.musicapp.ui.components.SortOption
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    val artistsSortOption: Flow<SortOption>
    val albumsSortOption: Flow<SortOption>
    val tracksSortOption: Flow<SortOption>
    val artistAlbumsSortOption: Flow<SortOption>
    val playlistsSortOption: Flow<SortOption>
    val genresSortOption: Flow<SortOption>
    val countriesSortOption: Flow<SortOption>
    val areasSortOption: Flow<SortOption>
    val labelsSortOption: Flow<SortOption>
    val moodsSortOption: Flow<SortOption>

    val skipSilenceToggle: Flow<Boolean>
    val playThreshold: Flow<Double>
    val minVisibleSimilarityScore: Flow<Double>

    suspend fun updateArtistsSort(option: SortOption)
    suspend fun updateAlbumsSort(option: SortOption)
    suspend fun updateTracksSort(option: SortOption)
    suspend fun updateArtistAlbumsSort(option: SortOption)
    suspend fun updatePlaylistsSort(option: SortOption)
    suspend fun updateGenresSort(option: SortOption)
    suspend fun updateCountriesSort(option: SortOption)
    suspend fun updateAreasSort(option: SortOption)
    suspend fun updateLabelsSort(option: SortOption)
    suspend fun updateMoodsSort(option: SortOption)

    suspend fun updateSkipSilence(enabled: Boolean)
    suspend fun updatePlayThreshold(newValue: Double)
    suspend fun updateMinSimilarityScore(newValue: Double)

}