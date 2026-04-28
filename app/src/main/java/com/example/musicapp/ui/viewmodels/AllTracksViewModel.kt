package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.repository.TrackRepository
import com.example.musicapp.data.repository.UserPreferencesRepository
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllTracksViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    val currentSortOption: StateFlow<SortOption> = userPreferencesRepository.trackSortOption
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SortOption(SortField.NAME, )
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tracksUiState: StateFlow<TracksUiState> = userPreferencesRepository.trackSortOption
        .flatMapLatest { option ->
            trackRepository.getAllTracks(option)
                .map { tracks -> TracksUiState(tracks = tracks, isLoading = false) }
                .onStart { emit(TracksUiState(isLoading = true))  }
                .catch { e -> emit(TracksUiState(error = e.message, isLoading = false)) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TracksUiState(isLoading = true)
        )


    fun setSort(option: SortOption) {
        viewModelScope.launch {
            userPreferencesRepository.updateTrackSort(option)
        }
    }

}

data class TracksUiState(
    val isLoading: Boolean = true,
    val tracks: List<TrackInfo> = emptyList(),
    val error: String? = null)
