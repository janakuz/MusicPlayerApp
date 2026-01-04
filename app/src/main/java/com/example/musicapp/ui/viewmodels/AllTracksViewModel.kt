package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.repository.TrackRepository
import com.example.musicapp.ui.components.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllTracksViewModel @Inject constructor(private val trackRepository: TrackRepository,
) : ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val _tracksUiState = MutableStateFlow(TracksUiState())
    val tracksUiState: StateFlow<TracksUiState> = _tracksUiState.asStateFlow()

    private val sortOption = MutableStateFlow(SortOption())


    init {
        viewModelScope.launch {
            trackRepository.getAllTracksByName()
                .onStart { _tracksUiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _tracksUiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { list ->
                    _tracksUiState.update { it.copy(tracks = list, isLoading = false, error = null) }
                }
        }
    }

    fun setSort(option: SortOption) {
        sortOption.value = option
        sortTracks()
    }

    fun sortTracks(){
        viewModelScope.launch {
            trackRepository.getAllTracks(sortOption.value)
                .onStart { _tracksUiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _tracksUiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { tracks -> _tracksUiState.update { it.copy(tracks = tracks, isLoading = false, error = null) } }
        }
    }

}

data class TracksUiState(
    val isLoading: Boolean = true,
    val tracks: List<TrackInfo> = emptyList(),
    val error: String? = null)
