package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val trackRepository: TrackRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val albumId: Int = savedStateHandle.get<String>("albumId")?.toInt()
        ?: throw IllegalStateException("albumId not found in SavedStateHandle")

    private val _currentAlbumUiState = MutableStateFlow(AlbumState())
    val currentAlbumUiState: StateFlow<AlbumState> = _currentAlbumUiState.asStateFlow()

    private val _albumTracksUiState = MutableStateFlow(TracksUiState())
    val albumTracksUiState: StateFlow<TracksUiState> = _albumTracksUiState.asStateFlow()


    init {
        viewModelScope.launch {
            val albumJob = launch {
                getAlbumById(albumId)
            }
            val tracksJob = launch {
                getTracksInAlbum(albumId)
            }

            joinAll(albumJob, tracksJob)
        }
    }


    fun getAlbumById(id: Int){
        viewModelScope.launch {
            albumRepository.getAlbum(id)
                .collect { album -> _currentAlbumUiState.update { it.copy(album = album) } }
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

data class AlbumState(
    val album: Album? = null
)