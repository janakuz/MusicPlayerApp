package com.example.musicapp.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.LocalLibraryScanner
import com.example.musicapp.data.dto.ArtistSearchInfo
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.repository.AlbumGenreRepository
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.GenreRepository
import com.example.musicapp.data.repository.MetadataRepository
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
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlin.text.toInt


@HiltViewModel
class AlbumEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val albumGenreRepository: AlbumGenreRepository,
    private val genreRepository: GenreRepository,
    private val trackRepository: TrackRepository,
    private val localLibraryScanner: LocalLibraryScanner,
    private val metadataRepository: MetadataRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val albumId: Int = savedStateHandle.get<String>("albumId")?.toInt()
        ?: throw IllegalStateException("trackId not found in SavedStateHandle")

    private val _uiState = MutableStateFlow(AlbumEditUiState())
    val uiState = _uiState.asStateFlow()

    private val _workflowState = MutableStateFlow<AlbumArtistEditUiState>(AlbumArtistEditUiState.Idle)
    val workflowState = _workflowState.asStateFlow()

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


    private var initialTitle: String? = null
    private var initialArtist: String? = null
    private var initialDate: String? = null
    private var initialImageUrl: String? = ""
    private var initialLabel: String? = ""
    private var initialGenres: List<String> = emptyList()

    val titleChanged: StateFlow<Boolean> = _uiState.map { state ->
        val hasChanges = state.title != initialTitle
        hasChanges && !state.isSaving
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val artistChanged: StateFlow<Boolean> = _uiState.map { state ->
        val hasChanges = state.artist != initialArtist
        hasChanges && !state.isSaving
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val canSave: StateFlow<Boolean> = _uiState.map { state ->
        val hasChanges = state.draftReleaseDate != initialDate || state.draftImageUrl != initialImageUrl
                || state.draftLabel != initialLabel || state.draftGenres != initialGenres || state.title != initialTitle || state.artist != initialArtist
        hasChanges && !state.isSaving
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadAlbumData()
    }

    private fun loadAlbumData() {
        viewModelScope.launch {
            val albums = albumRepository.getByIdFull(albumId)
            val album = albums[0]
            val genres = albumGenreRepository.getAlbumGenres(albumId)
            initialTitle = album.title
            initialArtist = if (albums.size == 1) album.artistName else "Various Artists"
            initialDate = album.releaseDate
            initialImageUrl = album.image ?: ""
            initialLabel = album.label
            initialGenres = genres
            _uiState.update { it.copy(
                title = album.title,
                artist = if (albums.size == 1) album.artistName else "Various Artists",
                draftReleaseDate = album.releaseDate ?: "",
                draftImageUrl = album.image ?: "",
                draftLabel = album.label ?: "",
                availableImages = if (album.image != null) listOf(ImageOption(url = album.image, source = "")) else emptyList(),
                draftGenres = genres,
                multipleArtists = albums.size > 1
            )
            }

            getAllImageOptions(album.mbId)
        }



    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onArtistChange(newArtist: String) {
        _uiState.update { it.copy(artist = newArtist) }
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

        val localImages =
            if (albumTracks.isNotEmpty())
                localLibraryScanner.findAllAlbumArtOptions(context, albumTracks[0].filePath)
            else null
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


    fun onArtistSelected(artistResult: ArtistSearchInfo, onBack: (Int?) -> Unit){
        viewModelScope.launch {
            val album = (_workflowState.value as AlbumArtistEditUiState.DisambiguationNeeded).album
            _workflowState.value = AlbumArtistEditUiState.Saving(album)
            val currentAlbumInfo = albumRepository.getByIdFull(albumId)
            val currentArtist = artistRepository.getArtist(currentAlbumInfo[0].artistId).first()
            performFinalSave(
                artistResult,
                currentArtist,
                album!!,
                onBack
                )

        }
    }

    fun resetName(){
        _uiState.update { it.copy(
            artist = initialArtist ?: ""
        ) }
    }


    fun onSave(onBack: (Int?) -> Unit){
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val currentAlbum = albumRepository.getAlbum(albumId).first()
            val currentAlbumInfo = albumRepository.getByIdFull(albumId)
            val currentArtist = if (currentAlbumInfo.size == 1) artistRepository.getArtist(currentAlbumInfo[0].artistId).first() else null

            val newAlbum = currentAlbum.copy(
                releaseDate = _uiState.value.draftReleaseDate,
                image = _uiState.value.draftImageUrl,
                label = _uiState.value.draftLabel)
            albumRepository.update(newAlbum)
            albumGenreRepository.updateAlbumGenres(albumId, _uiState.value.draftGenres)

            if (initialTitle != _uiState.value.title){
                try {
                    _workflowState.value = AlbumArtistEditUiState.Saving(currentAlbum)
                    val newIds = metadataRepository.updateAlbum(
                        newAlbumTitle = _uiState.value.title,
                        oldAlbum = currentAlbum,
                        newArtistName = _uiState.value.artist,
                        oldArtist = currentArtist,
                        newReleaseDate = _uiState.value.draftReleaseDate,
                        newAlbumArt = _uiState.value.draftImageUrl
                    )
                    _workflowState.value = AlbumArtistEditUiState.Saved
                    _uiState.update { it.copy(isSaving = false) }
                    onBack(newIds.artist?.id)
                }
                catch (e: SocketTimeoutException) {
                    _workflowState.value = AlbumArtistEditUiState.Error("MusicBrainz is taking too long. Please try again.")
                    _uiState.update { it.copy(isSaving = false) }
                } catch (e: Exception) {
                    _workflowState.value = AlbumArtistEditUiState.Error("Network error: ${e.message}")
                    _uiState.update { it.copy(isSaving = false) }
                } catch (e: Exception) {
                    Log.d("SaveError", "Failed to save: ${e.message}", e)
                    _uiState.update { it.copy(isSaving = false) }
                }

            }
            else if (initialArtist != _uiState.value.artist){
                try {
                    _workflowState.value = AlbumArtistEditUiState.Saving(currentAlbum)

                    val searchResults = artistRepository.findArtistMB(_uiState.value.artist)
                    if (searchResults.isEmpty()) {
                        _workflowState.value = AlbumArtistEditUiState.Error("No artist found")
                    } else if (searchResults.size > 1) {
                        _workflowState.value = AlbumArtistEditUiState.DisambiguationNeeded(searchResults, currentAlbum)
                        return@launch
                    } else {
                        performFinalSave(searchResults[0], currentArtist!!, currentAlbum, onBack)
                    }

                }
                catch (e: SocketTimeoutException) {
                    _workflowState.value = AlbumArtistEditUiState.Error("MusicBrainz is taking too long. Please try again.")
                    _uiState.update { it.copy(isSaving = false) }
                } catch (e: Exception) {
                    _workflowState.value = AlbumArtistEditUiState.Error("Network error: ${e.message}")
                    _uiState.update { it.copy(isSaving = false) }
                } catch (e: Exception) {
                    Log.d("SaveError", "Failed to save: ${e.message}", e)
                    _uiState.update { it.copy(isSaving = false) }
                }

            }

            _uiState.update { it.copy(isSaving = false) }
            onBack(null)

        }
    }

    suspend fun performFinalSave(
        artistResult: ArtistSearchInfo,
        oldArtist: Artist,
        oldAlbum: Album,
        onBack: (Int?) -> Unit) {
            val newId = metadataRepository.updateArtist(
                newArtistName = _uiState.value.artist,
                oldArtist = oldArtist,
                mbArtist = artistResult,
                albumToMove = oldAlbum
            )
//            _uiState.update { it.copy(id = updatedArtist.id) }
            _workflowState.value = AlbumArtistEditUiState.Saved
            onBack(newId.id)
    }


    fun resetToOriginal() {
        _uiState.update { it.copy(
            draftReleaseDate = initialDate ?: "",
            draftImageUrl = initialImageUrl ?: "",
            draftLabel = initialLabel ?: "",
            draftGenres = initialGenres
        )}
    }
}


data class AlbumEditUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val draftReleaseDate: String = "",
    val draftImageUrl: String = "",
    val availableImages: List<ImageOption> = emptyList(),
    val draftLabel: String = "",
    val draftGenres: List<String> = emptyList(),
    val multipleArtists: Boolean = false,
    val isSaving: Boolean = false
)

data class ImageOption(
    val url: String,
    val source: String
)

sealed class AlbumArtistEditUiState {
    object Idle : AlbumArtistEditUiState()
    data class Saving(val album: Album? = null) : AlbumArtistEditUiState()
    data class DisambiguationNeeded(val matches: List<ArtistSearchInfo>, val album: Album? = null) : AlbumArtistEditUiState()
    object Saved : AlbumArtistEditUiState()
    data class Error(val message: String) : AlbumArtistEditUiState() // Show toast
}