package com.example.musicapp.ui.viewmodels

import android.app.PendingIntent
import android.content.Context
import android.provider.MediaStore
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.TrackRepository
import com.example.musicapp.data.repository.UserPreferencesRepository
import com.example.musicapp.ui.components.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.map

@HiltViewModel
class AllAlbumsViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val trackRepository: TrackRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val _pendingDeleteUris = MutableStateFlow<List<String>>(emptyList())
    val pendingDeleteUris = _pendingDeleteUris.asStateFlow()



    @OptIn(ExperimentalCoroutinesApi::class)
    val albumListUiState: StateFlow<AlbumListUiState> = userPreferencesRepository.albumSortOption
        .flatMapLatest { option ->
            albumRepository.getAllAlbums(option)
                .map { albums -> AlbumListUiState(albums = toAlbumInfo(albums), isLoading = false) }
                .onStart { emit(AlbumListUiState(isLoading = true)) }
                .catch { e -> emit(AlbumListUiState(error = e.message, isLoading = false)) }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AlbumListUiState(isLoading = true)

        )


    fun toAlbumInfo(albums: List<Album>): List<AlbumInfo>{
        val albumInfos = albums.map { album ->
            AlbumInfo(
                albumId = album.id,
                title = album.title,
                releaseDate = album.releaseDate,
                artistName = "",
                image = album.image,
                duration = album.duration,
                artistId = 0,
                label = album.label
            )
        }
        return albumInfos
    }

    fun setSort(option: SortOption) {
        viewModelScope.launch {
            userPreferencesRepository.updateAlbumSort(option)
        }
    }

    fun prepareDeletion(albumId: Int) {
        viewModelScope.launch {
            val uris = trackRepository.getAlbumTracks(albumId).map { it.fileUri }
            _pendingDeleteUris.value = uris
        }
    }


    fun finalizeDeletion(albumId: Int) {
        viewModelScope.launch(Dispatchers.IO) {

            albumRepository.deleteById(albumId)
            artistRepository.deleteOrphaned()
            albumRepository.deleteOrphaned()

            clearPendingDeletion()
        }

    }

    fun clearPendingDeletion() {
        _pendingDeleteUris.value = emptyList()
    }

    fun getDeleteIntent(context: Context, uriStrings: List<String>): PendingIntent {
        val uris = uriStrings.map { it.toUri() }
        return MediaStore.createDeleteRequest(context.contentResolver, uris)
    }

}

data class AlbumListUiState(
    val isLoading: Boolean = true,
    val albums: List<AlbumInfo> = emptyList(),
    val error: String? = null)
