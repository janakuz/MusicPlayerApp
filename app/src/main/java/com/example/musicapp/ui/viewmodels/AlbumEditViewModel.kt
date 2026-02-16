package com.example.musicapp.ui.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.LocalLibraryScanner
import com.example.musicapp.data.repository.AlbumGenreRepository
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.GenreRepository
import com.example.musicapp.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.toInt


@HiltViewModel
class AlbumEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val albumRepository: AlbumRepository,
    private val albumGenreRepository: AlbumGenreRepository,
    private val genreRepository: GenreRepository,
    private val trackRepository: TrackRepository,
    private val localLibraryScanner: LocalLibraryScanner,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val albumId: Int = savedStateHandle.get<String>("albumId")?.toInt()
        ?: throw IllegalStateException("artistId not found in SavedStateHandle")

    private val _uiState = MutableStateFlow(AlbumEditUiState())
    val uiState = _uiState.asStateFlow()


    private val _genreQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val genreSuggestions: StateFlow<List<String>> = _genreQuery
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.length < 2) {
                flowOf(emptyList())
            } else {
                genreRepository.findGenre(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var initialDate: String? = null
    private var initialImageUrl: String? = null
    private var initialLabel: String? = null
    private var initialGenres: List<String> = emptyList()

    val canSave: StateFlow<Boolean> = _uiState.map { state ->
        val hasChanges = state.draftReleaseDate != initialDate || state.draftImageUrl != initialImageUrl
                || state.draftLabel != initialLabel || state.draftGenres != initialGenres
        hasChanges && !state.isSaving
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadAlbumData()
    }

    private fun loadAlbumData() {
        viewModelScope.launch {
            val album = albumRepository.getAlbum(albumId).first()
            val genres = albumGenreRepository.getAlbumGenres(albumId)
            initialDate = album.releaseDate
            initialImageUrl = album.image
            initialLabel = album.label
            initialGenres = genres
            _uiState.update { it.copy(
                title = album.title,
                draftReleaseDate = album.releaseDate ?: "",
                draftImageUrl = album.image ?: "",
                draftLabel = album.label ?: "",
                availableImages = if (album.image != null) listOf(ImageOption(url = album.image, source = "")) else emptyList(),
                draftGenres = genres
            )
            }

            getAllImageOptions(album.mbId)
        }



    }

    fun onReleaseDateChange(newDate: String) {
        _uiState.update { it.copy(draftReleaseDate = newDate) }
    }


    fun onImageChange(newImageUrl: String) {
        _uiState.update { it.copy(draftImageUrl = newImageUrl) }
    }

    fun onLabelChange(newLabel: String) {
        _uiState.update { it.copy(draftLabel = newLabel) }
    }

    fun onGenresChange(newGenres: List<String>) {
        _uiState.update { it.copy(draftGenres = newGenres.distinct()) }
        _genreQuery.value = ""
    }

    fun onGenreQueryChange(newQuery: String){
        _genreQuery.value = newQuery
    }



    suspend fun getAllImageOptions(mbId: String?){
        val options = mutableListOf<ImageOption>()
        val albumTracks = trackRepository.getAlbumTracks(albumId)

        val localImages = localLibraryScanner.findAllAlbumArtOptions(context, albumTracks[0].fileUri)
        val localImageOptions = localImages?.map { ImageOption(url = it, source = "Local") } ?: emptyList()

        options.addAll(localImageOptions)

        if (mbId != null) {
            val caa = albumRepository.getAllCAAOptions(mbId)
            val currentImages = _uiState.value.availableImages
            val caaOptions = caa.map { ImageOption(url = it, source = "Web") }

            options.addAll(caaOptions)
        }
        if (options.isNotEmpty()) {
            _uiState.update {
                it.copy(availableImages = options)
            }
        }
    }


    fun onSave(){
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val currentAlbum = albumRepository.getAlbum(albumId).first()

            val newAlbum = currentAlbum.copy(
                releaseDate = _uiState.value.draftReleaseDate,
                image = _uiState.value.draftImageUrl,
                label = _uiState.value.draftLabel)
            albumRepository.update(newAlbum)
            albumGenreRepository.updateAlbumGenres(albumId, _uiState.value.draftGenres)
        }
    }

    fun resetToOriginal() {
        _uiState.update { it.copy(
            draftReleaseDate = initialDate ?: "",
            draftImageUrl = initialImageUrl ?: "",
            draftLabel = initialLabel ?: ""
        )}
    }
}


data class AlbumEditUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val draftReleaseDate: String = "",
    val draftImageUrl: String = "",
    val availableImages: List<ImageOption> = emptyList(),
    val draftLabel: String = "",
    val draftGenres: List<String> = emptyList(),
    val isSaving: Boolean = false
)

data class ImageOption(
    val url: String,
    val source: String
)