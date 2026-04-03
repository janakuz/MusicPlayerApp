package com.example.musicapp.ui.viewmodels

import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import androidx.core.net.toUri
import com.example.musicapp.data.repository.AlbumRepository
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class AllArtistsViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val _pendingDeleteUris = MutableStateFlow<List<String>>(emptyList())
    val pendingDeleteUris = _pendingDeleteUris.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val artistListUiState: StateFlow<ArtistListUiState> = userPreferencesRepository.artistSortOption
        .flatMapLatest { option ->
            artistRepository.getAllArtistsSorted(option.ascending)
                .map {  artists -> ArtistListUiState(artists = artists, isLoading = false) }
                .onStart { emit(ArtistListUiState(isLoading = true)) }
                .catch { e -> emit(ArtistListUiState(error = e.message, isLoading = false)) }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = ArtistListUiState(isLoading = true)
        )


    fun setSort(option: SortOption){
        viewModelScope.launch {
            userPreferencesRepository.updateArtistSort(option)
        }
    }

    init {
        Log.d("Artists VM", "CREATED ${hashCode()}")
    }

    fun getDeleteIntent(context: Context, uriStrings: List<String>): PendingIntent {
        val uris = uriStrings.map { it.toUri() }
        return MediaStore.createDeleteRequest(context.contentResolver, uris)
    }

    fun prepareDeletion(artistId: Int) {
        viewModelScope.launch {
            val uris = artistRepository.getTrackUrisByArtist(artistId)
            _pendingDeleteUris. value = uris
        }
    }

    fun finalizeDeletion(artistId: Int) {
        viewModelScope.launch(Dispatchers.IO) {

            artistRepository.deleteById(artistId)
            artistRepository.deleteOrphaned()
            albumRepository.deleteOrphaned()

            clearPendingDeletion()
        }
    }

    fun clearPendingDeletion(){
        _pendingDeleteUris.value = emptyList()
    }


}

data class ArtistListUiState(
    val isLoading: Boolean = true,
    val artists: List<Artist> = emptyList(),
    val error: String? = null)
