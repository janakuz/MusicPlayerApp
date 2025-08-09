package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.repository.AlbumRepository
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
class AllAlbumsViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val _albumListUiState = MutableStateFlow(AlbumListUiState())
    val albumListUiState: StateFlow<AlbumListUiState> = _albumListUiState.asStateFlow()

    init {
        getAllAlbums()
    }

    fun getAllAlbums(){
        viewModelScope.launch {
            albumRepository.getAllAlbumsByName()
                .onStart { _albumListUiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _albumListUiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { list ->
                    _albumListUiState.update { it.copy(albums = toAlbumInfo(list), isLoading = false, error = null) }
                }
        }

    }

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

}

data class AlbumListUiState(
    val isLoading: Boolean = true,
    val albums: List<AlbumInfo> = emptyList(),
    val error: String? = null)
