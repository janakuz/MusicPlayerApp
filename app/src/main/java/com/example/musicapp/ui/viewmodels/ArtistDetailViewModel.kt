package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.ArtistRepository
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
class ArtistDetailViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val albumArtistRepository: AlbumArtistRepository,
    savedStateHandle: SavedStateHandle
    ) : ViewModel() {

        companion object {
            private const val TIMEOUT_MILLIS = 5_000L
        }

    private val artistId: Int = savedStateHandle.get<String>("artistId")?.toInt()
        ?: throw IllegalStateException("artistId not found in SavedStateHandle")


    private val _currentArtistUiState = MutableStateFlow(ArtistState())
    val currentArtistUiState: StateFlow<ArtistState> = _currentArtistUiState.asStateFlow()


    private val _albumListUiState = MutableStateFlow(AlbumListUiState())
    val albumListUiState: StateFlow<AlbumListUiState> = _albumListUiState.asStateFlow()

    init {
        viewModelScope.launch {
            val artistJob = launch {
                getArtistById(artistId)
            }
            val albumsJob = launch {
                getAlbumsByArtist(artistId)
            }

            joinAll(artistJob, albumsJob)
        }
    }

    fun getArtistById(id: Int){
        viewModelScope.launch {
            artistRepository.getArtist(id)
                .collect { artist -> _currentArtistUiState.update { it.copy(artist = artist) } }
        }
    }

    fun getAlbumsByArtist(artistId: Int){
        viewModelScope.launch {
            albumArtistRepository.getAllAlbumsByArtist(artistId)
                .onStart { _albumListUiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _albumListUiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { list ->
                    _albumListUiState.update {
                        it.copy(
                            albums = list,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
}

data class ArtistState(
    val artist: Artist? = null
)