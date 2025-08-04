package com.example.musicapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.repository.AlbumArtistRepository
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
class AlbumViewModel@Inject constructor(private val albumRepository: AlbumRepository,
    private val albumArtistRepository: AlbumArtistRepository
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val _albumListUiState = MutableStateFlow(AlbumListUiState())
    val albumListUiState: StateFlow<AlbumListUiState> = _albumListUiState.asStateFlow()

    private val _albumArtistListUiState = MutableStateFlow(AlbumArtistListUiState())
    val albumArtistListUiState: StateFlow<AlbumArtistListUiState> = _albumArtistListUiState.asStateFlow()


    init {
        viewModelScope.launch {
            albumRepository.getAllAlbumsByName()
                .onStart { _albumListUiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _albumListUiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { list ->
                    _albumListUiState.update { it.copy(albums = list, isLoading = false, error = null) }
                }
        }
    }

    fun getAlbumsByArtist(artistId: Int ){
        viewModelScope.launch {
            albumArtistRepository.getAllAlbumsByArtist(artistId)
                .onStart { _albumArtistListUiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _albumArtistListUiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { list ->
                    _albumArtistListUiState.update {
                        it.copy(
                            albums = list,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun getAlbumArtist(albumId: Int){
        albumArtistRepository
    }

}

data class AlbumListUiState(
    val isLoading: Boolean = true,
    val albums: List<Album> = emptyList(),
    val error: String? = null)

data class AlbumArtistListUiState(
    val isLoading: Boolean = true,
    val albums: List<AlbumInfo> = emptyList(),
    val error: String? = null)