package com.example.musicapp.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistEditViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    savedStateHandle: SavedStateHandle

) : ViewModel() {
    private val playlistId: Int? = savedStateHandle.get<String>("playlistId")?.toInt()

    val isEditMode = playlistId != null

    private val _uiState = MutableStateFlow<PlaylistEditUiState>(PlaylistEditUiState())
    val uiState = _uiState.asStateFlow()


    private var initialName: String? = null
    private var initialDescription: String? = null
    private var initialImage: String? = null


    private var initialState: PlaylistEditUiState? = null

    val canSave: StateFlow<Boolean> = _uiState.map { state ->
        val hasChanges =
            state.name != initialState?.name || state.draftImageUrl != initialState?.draftImageUrl
                    || state.draftDescription != initialState?.draftDescription
        (initialState != null && hasChanges && !state.isSaving) || !isEditMode
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    init {
        if (isEditMode) {
            loadPlaylist(playlistId!!)
        } else {
            _uiState.update { it.copy(name = "New Playlist") }
        }
    }

    fun loadPlaylist(playlistId: Int) {
        viewModelScope.launch {
            val playlist = playlistRepository.getPlaylistById(playlistId)

            initialName = playlist.name
            initialDescription = playlist.description
            initialImage = playlist.image

            initialState = PlaylistEditUiState(
                name = playlist.name,
                draftDescription = playlist.description ?: "",
                draftImageUrl = playlist.image
            )

            _uiState.update {
                it.copy(
                    name = playlist.name,
                    draftDescription = playlist.description ?: "",
                    draftImageUrl = playlist.image
                )
            }

        }
    }


    fun onImageChange(newImageUrl: Uri) {
        _uiState.update { it.copy(draftImageUrl = newImageUrl.toString()) }
    }

    fun onRemoveImage() {
        _uiState.update { it.copy(draftImageUrl = null) }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }


    fun onDescChange(newDesc: String) {
        _uiState.update { it.copy(draftDescription = newDesc) }
    }


    fun onSave(context: Context) {
        if (isEditMode) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }

                val currentPlaylist = playlistRepository.getPlaylistById(playlistId!!)

                val newPath =
                    if (_uiState.value.draftImageUrl != null && _uiState.value.draftImageUrl != initialImage) playlistRepository.savePlaylistImage(
                        context,
                        _uiState.value.draftImageUrl!!.toUri()
                    )
                    else _uiState.value.draftImageUrl

                val newPlaylist = currentPlaylist.copy(
                    name = _uiState.value.name,
                    description = _uiState.value.draftDescription,
                    image = newPath,
                )

                playlistRepository.update(newPlaylist)
            }
        } else {
            viewModelScope.launch {

                val newPlaylist = Playlist(
                    name = _uiState.value.name,
                    description = _uiState.value.draftDescription,
                    image = _uiState.value.draftImageUrl,
                    lastUpdated = System.currentTimeMillis()
                )

                playlistRepository.insert(newPlaylist)
            }
        }
    }


}

data class PlaylistEditUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val draftImageUrl: String? = null,
    val draftDescription: String = "",
    val isSaving: Boolean = false
)
