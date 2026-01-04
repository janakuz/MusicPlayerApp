package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.example.musicapp.data.entity.Artist

import com.example.musicapp.data.repository.ArtistRepository;
import com.example.musicapp.ui.components.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AllArtistsViewModel @Inject constructor(private val artistRepository: ArtistRepository
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val _artistListUiState = MutableStateFlow(ArtistListUiState())
    val artistListUiState: StateFlow<ArtistListUiState> = _artistListUiState.asStateFlow()

    private val sortOption = MutableStateFlow(SortOption())

    init {
        viewModelScope.launch {
            artistRepository.getAllArtistsSorted(sortOption.value.ascending)
                .onStart { _artistListUiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _artistListUiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { list ->
                    _artistListUiState.update { it.copy(artists = list, isLoading = false, error = null) }
                }
        }
    }

    fun setSort(option: SortOption) {
        sortOption.value = option
        sortArtists()
    }

    fun sortArtists(){
        viewModelScope.launch {
            artistRepository.getAllArtistsSorted(sortOption.value.ascending)
                .onStart { _artistListUiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _artistListUiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { artists -> _artistListUiState.update { it.copy(artists = artists, isLoading = false, error = null) } }
        }
    }


}

data class ArtistListUiState(
    val isLoading: Boolean = true,
    val artists: List<Artist> = emptyList(),
    val error: String? = null)
