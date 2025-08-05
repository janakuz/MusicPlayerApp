package com.example.musicapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.Track
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.TrackRepository
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
class TrackViewModel @Inject constructor(private val trackRepository: TrackRepository,
) : ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val _tracksUiState = MutableStateFlow(TracksUiState())
    val tracksUiState: StateFlow<TracksUiState> = _tracksUiState.asStateFlow()


    private val _albumTracksUiState = MutableStateFlow(AlbumTracksUiState())
    val albumTracksUiState: StateFlow<AlbumTracksUiState> = _albumTracksUiState.asStateFlow()

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

    fun getTracksInAlbum(albumId: Int){
        viewModelScope.launch {
            trackRepository.getTracksInAlbum(albumId)
                .onStart { _albumTracksUiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _albumTracksUiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { list ->
                    _albumTracksUiState.update {
                        it.copy(
                            tracks = list,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }

    }





}

data class TracksUiState(
    val isLoading: Boolean = true,
    val tracks: List<TrackInfo> = emptyList(),
    val error: String? = null)

data class AlbumTracksUiState(
    val isLoading: Boolean = true,
    val tracks: List<TrackInfo> = emptyList(),
    val error: String? = null)