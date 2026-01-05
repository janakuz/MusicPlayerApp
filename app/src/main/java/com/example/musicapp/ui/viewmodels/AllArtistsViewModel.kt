package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.example.musicapp.data.entity.Artist

import com.example.musicapp.data.repository.ArtistRepository;
import com.example.musicapp.data.repository.UserPreferencesRepository
import com.example.musicapp.ui.components.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AllArtistsViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    val artistListUiState: StateFlow<ArtistListUiState> = userPreferencesRepository.artistSortOption
        .flatMapLatest { option ->
            artistRepository.getAllArtistsSorted(option.ascending)
                .map {  artists -> ArtistListUiState(artists = artists, isLoading = false) }
                .onStart { emit(ArtistListUiState(isLoading = true)) }
                .catch { e -> emit(ArtistListUiState(error = e.message, isLoading = false)) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArtistListUiState(isLoading = true)
        )


    fun setSort(option: SortOption){
        viewModelScope.launch {
            userPreferencesRepository.updateArtistSort(option)
        }
    }


}

data class ArtistListUiState(
    val isLoading: Boolean = true,
    val artists: List<Artist> = emptyList(),
    val error: String? = null)
