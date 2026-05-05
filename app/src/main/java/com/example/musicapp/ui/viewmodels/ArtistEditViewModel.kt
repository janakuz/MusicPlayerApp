package com.example.musicapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.remote.dto.ArtistSearchInfo
import com.example.musicapp.data.remote.dto.DiscogsImage
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.MetadataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ArtistEditViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val metadataRepository: MetadataRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artistId: Int = savedStateHandle.get<String>("artistId")?.toInt()
        ?: throw IllegalStateException("artistId not found in SavedStateHandle")

    private val _uiState = MutableStateFlow(ArtistEditUiState(id = artistId))
    val uiState = _uiState.asStateFlow()

    private val _workflowState = MutableStateFlow<NameEditUiState>(NameEditUiState.Idle)
    val workflowState = _workflowState.asStateFlow()

    private var initialName: String? = null
    private var initialBio: String? = ""
    private var initialImageUrl: String? = ""

    val canSave: StateFlow<Boolean> = _uiState.map { state ->
        val hasChanges =
            state.draftBio != initialBio || state.draftImageUrl != initialImageUrl || state.name != initialName
        Log.d("bio", initialBio + " " + state.draftBio)
        Log.d("image", "init:" + initialImageUrl + " " + "draft:" + state.draftImageUrl)
        Log.d("name", initialName + " " + state.name)
        hasChanges && !state.isSaving
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadArtistData()
    }

    private fun loadArtistData() {
        viewModelScope.launch {
            val artist = artistRepository.getArtist(artistId).first()
            initialName = artist.name
            initialBio = artist.bio
            initialImageUrl = artist.image ?: ""
            _uiState.update {
                it.copy(
                    name = artist.name,
                    draftBio = artist.bio ?: "",
                    draftImageUrl = artist.image ?: ""
                )
            }


            if (artist.discogsId != null) getDiscogsInfo(artist.discogsId)
            getLastfmInfo(artist.mbId, artist.name)
        }


    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onBioChange(newBio: String) {
        _uiState.update { it.copy(draftBio = newBio) }
    }


    fun onImageChange(newImageUrl: String) {
        _uiState.update { it.copy(draftImageUrl = newImageUrl) }
    }


    suspend fun getDiscogsInfo(discogsId: String) {
        val discogs = artistRepository.getArtistDiscogsInfo(discogsId)
        if (discogs != null) {
            _uiState.update {
                it.copy(discogsBio = discogs.profile, discogsImages = discogs.images ?: emptyList())
            }
        }
    }

    suspend fun getLastfmInfo(mbId: String?, name: String) {
        val lastFm = artistRepository.getArtistBio(mbId, name)
        _uiState.update {
            it.copy(lastFmBio = lastFm)
        }
    }

    fun onSave(onBack: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val currentArtist = artistRepository.getArtist(artistId).first()

            val newArtist = currentArtist.copy(
                bio = _uiState.value.draftBio,
                image = _uiState.value.draftImageUrl
            )
            artistRepository.update(newArtist)
            if (initialName != _uiState.value.name) {
                try {
                    _workflowState.value = NameEditUiState.Saving

                    val searchResults = artistRepository.findArtistMB(_uiState.value.name)
                    if (searchResults.isEmpty()) {
                        _workflowState.value = NameEditUiState.Error("No artist found")
                    } else if (searchResults.size > 1) {
                        _workflowState.value = NameEditUiState.DisambiguationNeeded(searchResults)
                        return@launch
                    } else {
                        performFinalSave(searchResults[0], currentArtist, onBack)
                    }
                    _uiState.update { it.copy(isSaving = false) }
                    onBack()

                } catch (e: SocketTimeoutException) {
                    _workflowState.value =
                        NameEditUiState.Error("MusicBrainz is taking too long. Please try again.")
                    _uiState.update { it.copy(isSaving = false) }
                } catch (e: Exception) {
                    _workflowState.value = NameEditUiState.Error("Network error: ${e.message}")
                    _uiState.update { it.copy(isSaving = false) }
                } catch (e: Exception) {
                    Log.d("SaveError", "Failed to save: ${e.message}", e)
                    _uiState.update { it.copy(isSaving = false) }
                }
            } else {
                onBack()
            }
        }
    }

    suspend fun performFinalSave(
        artistResult: ArtistSearchInfo,
        oldArtist: Artist,
        onBack: () -> Unit
    ) {
        val updatedArtist = metadataRepository.updateArtist(
            newArtistName = _uiState.value.name,
            oldArtist = oldArtist,
            mbArtist = artistResult
        )
        _uiState.update { it.copy(id = updatedArtist.id) }
        _workflowState.value = NameEditUiState.Saved
        onBack()
    }

    fun onArtistSelected(artistResult: ArtistSearchInfo, onBack: () -> Unit) {
        viewModelScope.launch {
            _workflowState.value = NameEditUiState.Saving
            val currentArtist = artistRepository.getArtist(artistId).first()
            performFinalSave(artistResult, currentArtist, onBack)

        }
    }

    fun resetName() {
        _uiState.update {
            it.copy(
                name = initialName ?: ""
            )
        }
    }

    fun resetToOriginal() {
        _uiState.update {
            it.copy(
                draftBio = initialBio ?: "",
                draftImageUrl = initialImageUrl ?: ""
            )
        }
    }
}


data class ArtistEditUiState(
    val isLoading: Boolean = false,
    val id: Int,
    val name: String = "",
    val draftBio: String = "",
    val draftImageUrl: String = "",
    val discogsImages: List<DiscogsImage> = emptyList(),
    val lastFmBio: String = "",
    val discogsBio: String = "",
    val isSaving: Boolean = false
)

sealed class NameEditUiState {
    object Idle : NameEditUiState()
    object Saving : NameEditUiState()
    data class DisambiguationNeeded(val matches: List<ArtistSearchInfo>) : NameEditUiState()
    object Saved : NameEditUiState()
    data class Error(val message: String) : NameEditUiState() // Show toast
}