package com.example.musicapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.dto.DiscogsImage
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.repository.ArtistRepository
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
import javax.inject.Inject

@HiltViewModel
class ArtistEditViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artistId: Int = savedStateHandle.get<String>("artistId")?.toInt()
        ?: throw IllegalStateException("artistId not found in SavedStateHandle")

    private val _uiState = MutableStateFlow(ArtistEditUiState())
    val uiState = _uiState.asStateFlow()

    private var initialBio: String? = null
    private var initialImageUrl: String? = null

    val canSave: StateFlow<Boolean> = _uiState.map { state ->
        val hasChanges = state.draftBio != initialBio || state.draftImageUrl != initialImageUrl
        hasChanges && !state.isSaving
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadArtistData()
    }

    private fun loadArtistData() {
        viewModelScope.launch {
            val artist = artistRepository.getArtist(artistId).first()
            initialBio = artist.bio
            initialImageUrl = artist.image
            _uiState.update { it.copy(
                name = artist.name,
                draftBio = artist.bio ?: "",
                draftImageUrl = artist.image ?: ""
            )
            }


            if (artist.discogsId != null) getDiscogsInfo(artist.discogsId)
            getLastfmInfo(artist.mbId, artist.name)
        }



    }

    fun onBioChange(newBio: String) {
        _uiState.update { it.copy(draftBio = newBio) }
    }


    fun onImageChange(newImageUrl: String) {
        _uiState.update { it.copy(draftImageUrl = newImageUrl) }
    }


    suspend fun getDiscogsInfo(discogsId: String){
        val discogs = artistRepository.getArtistDiscogsInfo(discogsId)
        if (discogs != null) {
            _uiState.update {
                it.copy(discogsBio = discogs.profile, discogsImages = discogs.images ?: emptyList())
            }
        }
    }

    suspend fun getLastfmInfo(mbId: String?, name: String){
        val lastFm = artistRepository.getArtistBio(mbId, name)
        _uiState.update {
            it.copy(lastFmBio = lastFm)
        }
    }

    fun onSave(){
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val currentArtist = artistRepository.getArtist(artistId).first()

//            val isDirty = _uiState.value.draftBio != originalBio || draftImageUrl != originalImageUrl
            val newArtist = currentArtist.copy(bio = _uiState.value.draftBio, image = _uiState.value.draftImageUrl)
            artistRepository.update(newArtist)
        }
    }

    fun resetToOriginal() {
        _uiState.update { it.copy(
            draftBio = initialBio ?: "",
            draftImageUrl = initialImageUrl ?: ""
        )}
    }
}


data class ArtistEditUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val draftBio: String = "",
    val draftImageUrl: String = "",
    val discogsImages: List<DiscogsImage> = emptyList(),
    val lastFmBio: String = "",
    val discogsBio: String = "",
    val isSaving: Boolean = false
)