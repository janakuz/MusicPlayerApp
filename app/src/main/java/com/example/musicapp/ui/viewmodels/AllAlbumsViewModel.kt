package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.repository.AlbumRepository
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllAlbumsViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val albumListUiState: StateFlow<AlbumListUiState> = userPreferencesRepository.albumSortOption
        .flatMapLatest { option ->
            albumRepository.getAllAlbums(option)
                .map { albums -> AlbumListUiState(albums = toAlbumInfo(albums), isLoading = false) }
                .onStart { emit(AlbumListUiState(isLoading = true)) }
                .catch { e -> emit(AlbumListUiState(error = e.message, isLoading = false)) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
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

}

data class AlbumListUiState(
    val isLoading: Boolean = true,
    val albums: List<AlbumInfo> = emptyList(),
    val error: String? = null)
