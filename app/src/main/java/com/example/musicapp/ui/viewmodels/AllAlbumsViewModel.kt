package com.example.musicapp.ui.viewmodels

import android.app.PendingIntent
import android.content.Context
import android.provider.MediaStore
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.currentComposer
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.dto.ArtistSearchInfo
import com.example.musicapp.data.dto.Release
import com.example.musicapp.data.entity.Album
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.data.repository.AlbumArtistRepository
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.MetadataRepository
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
import kotlinx.coroutines.flow.first
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
    private val albumArtistRepository: AlbumArtistRepository,
    private val trackRepository: TrackRepository,
    private val metadataRepository: MetadataRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val _pendingDeleteUris = MutableStateFlow<List<String>>(emptyList())
    val pendingDeleteUris = _pendingDeleteUris.asStateFlow()

    private val _refetchState = MutableStateFlow<RefetchAlbumState>(RefetchAlbumState.Idle)
    val refetchState = _refetchState.asStateFlow()

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


    fun refetchMetadata(id: Int){
        viewModelScope.launch {
            val album = albumRepository.getAlbum(id).first()
            val artists = albumArtistRepository.getAllAlbumArtists(id)
            val albumArtist =  artists.first { it.mbId != null }
            _refetchState.value = RefetchAlbumState.Saving
            val query = if (albumArtist.mbId != null) "arid:${albumArtist.mbId} AND release:${album.searchKey}" else """artist:${albumArtist.searchKey} AND release:${album.searchKey}"""
            val searchResults = albumRepository.findAlbumMB(query)
            if (searchResults == null || searchResults.releases.isEmpty()) {
                _refetchState.value = RefetchAlbumState.Error("No album found")
            } else if (searchResults.releases.size > 1) {
                _refetchState.value = RefetchAlbumState.DisambiguationNeeded(searchResults.releases, album)
                return@launch
            } else {
                performFinalSave(searchResults.releases[0], album)
            }
            _refetchState.value = RefetchAlbumState.Saved

        }
    }

    fun reset(){
        _refetchState.value = RefetchAlbumState.Idle
    }

    suspend fun performFinalSave(
        release: Release,
        oldAlbum: Album,
        ) {
        val newId = metadataRepository.refetchAlbum(
            album = release,
            currentAlbum = oldAlbum
        )
        _refetchState.value = RefetchAlbumState.Saved
    }

    fun onAlbumSelected(release: Release){
        viewModelScope.launch {
            val currentAlbum = (_refetchState.value as RefetchAlbumState.DisambiguationNeeded).album
            _refetchState.value = RefetchAlbumState.Saving
            performFinalSave(
                release,
                currentAlbum,
            )

        }
    }

}

data class AlbumListUiState(
    val isLoading: Boolean = true,
    val albums: List<AlbumInfo> = emptyList(),
    val error: String? = null)


sealed class RefetchAlbumState {
    object Idle: RefetchAlbumState()
    data class DisambiguationNeeded(val matches: List<Release>, val album: Album) : RefetchAlbumState()
    object Saving: RefetchAlbumState()
    object Saved : RefetchAlbumState()
    data class Error(val message: String) : RefetchAlbumState()
}