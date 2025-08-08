package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.example.musicapp.data.entity.Artist

import com.example.musicapp.data.repository.ArtistRepository;
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(private val artistRepository: ArtistRepository
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val _artistListUiState = MutableStateFlow(ArtistListUiState())
    val artistListUiState: StateFlow<ArtistListUiState> = _artistListUiState.asStateFlow()

    private val _currentArtistUiState = MutableStateFlow(ArtistState())
    val currentArtistUiState: StateFlow<ArtistState> = _currentArtistUiState.asStateFlow()

    init {
        viewModelScope.launch {
            artistRepository.getAllArtists()
                .onStart { _artistListUiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _artistListUiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { list ->
                    _artistListUiState.update { it.copy(artists = list, isLoading = false, error = null) }
                }
        }
    }

    fun getArtistById(id: Int){
        viewModelScope.launch {
            artistRepository.getArtist(id)
                .collect { artist -> _currentArtistUiState.update { it.copy(artist = artist) } }
        }
    }

}

data class ArtistListUiState(
    val isLoading: Boolean = true,
    val artists: List<Artist> = emptyList(),
    val error: String? = null)

data class ArtistState(
    val artist: Artist? = null
)
