package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.UserPreferencesRepository
import com.example.musicapp.ui.HomeScreen
import com.example.musicapp.ui.components.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SortViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    fun getSortOptionForRoute(route: String): StateFlow<SortOption> {
        val flow = when (route) {
            HomeScreen.Artists.name -> userPreferencesRepository.artistsSortOption
            HomeScreen.Albums.name -> userPreferencesRepository.albumsSortOption
            HomeScreen.Tracks.name -> userPreferencesRepository.tracksSortOption
            HomeScreen.Playlists.name -> userPreferencesRepository.playlistsSortOption
            HomeScreen.Genres.name -> userPreferencesRepository.genresSortOption
            "artist/{artistId}" -> userPreferencesRepository.artistAlbumsSortOption
            HomeScreen.Countries.name -> userPreferencesRepository.countriesSortOption
            HomeScreen.Areas.name -> userPreferencesRepository.areasSortOption
            HomeScreen.Labels.name -> userPreferencesRepository.labelsSortOption
            HomeScreen.Moods.name -> userPreferencesRepository.moodsSortOption
            else -> flowOf(SortOption())
        }
        return flow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SortOption()
        )
    }

    fun updateSortOption(route: String, newSort: SortOption) {
        viewModelScope.launch {
            when (route) {
                HomeScreen.Artists.name -> userPreferencesRepository.updateArtistsSort(newSort)
                HomeScreen.Albums.name -> userPreferencesRepository.updateAlbumsSort(newSort)
                HomeScreen.Tracks.name -> userPreferencesRepository.updateTracksSort(newSort)
                HomeScreen.Playlists.name -> userPreferencesRepository.updatePlaylistsSort(newSort)
                HomeScreen.Genres.name -> userPreferencesRepository.updateGenresSort(newSort)
                "artist/{artistId}" -> userPreferencesRepository.updateArtistAlbumsSort(newSort)
                HomeScreen.Countries.name -> userPreferencesRepository.updateCountriesSort(newSort)
                HomeScreen.Areas.name -> userPreferencesRepository.updateAreasSort(newSort)
                HomeScreen.Labels.name -> userPreferencesRepository.updateLabelsSort(newSort)
                HomeScreen.Moods.name -> userPreferencesRepository.updateMoodsSort(newSort)
            }
        }
    }
}