package com.example.musicapp.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.local.entity.Playlist
import com.example.musicapp.data.repository.PlaylistRepository
import com.example.musicapp.data.repository.PlaylistTracksRepository
import com.example.musicapp.data.repository.TrackRepository
import com.example.musicapp.data.repository.UserPreferencesRepository
import com.example.musicapp.ui.components.SortField
import com.example.musicapp.ui.components.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val playlistTracksRepository: PlaylistTracksRepository,
    private val trackRepository: TrackRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _createInfo = MutableStateFlow<CreatePlaylistState>(CreatePlaylistState())
    val createInfo = _createInfo.asStateFlow()

    private val _addToPlaylistState =
        MutableStateFlow<AddToPlaylistState>(AddToPlaylistState(emptyList(), false))
    val addToPlaylistState = _addToPlaylistState.asStateFlow()

    private val _eventChannel = Channel<String>(Channel.BUFFERED)
    val events = _eventChannel.receiveAsFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val playlists: StateFlow<List<PlaylistUiModel>> = userPreferencesRepository.playlistsSortOption
        .flatMapLatest { option ->
            playlistRepository.getAllPlaylists(option)
                .map { list ->
                    list.map { stats ->
                        PlaylistUiModel(
                            playlist = stats.stats.playlist,
                            top4Images = stats.top4Images,
                            trackCount = stats.stats.trackCount,
                            totalDuration = stats.stats.playlistDuration
                        )
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val playlistsForAdd: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists(
        SortOption(SortField.NAME, true)
    ).map { playlists -> playlists.map { it.stats.playlist } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createPlaylistAndAdd(tracks: List<Int>) {
        viewModelScope.launch {
            val newPlaylist = Playlist(
                name = createInfo.value.name,
                lastUpdated = System.currentTimeMillis()
            )

            val newId = playlistRepository.insert(newPlaylist).toInt()
            playlistTracksRepository.addTracksToPlaylist(newId, tracks)

            _eventChannel.send("Added ${tracks.size} tracks to ${newPlaylist.name}")
        }
    }

    fun setSort(option: SortOption) {
        viewModelScope.launch {
            userPreferencesRepository.updatePlaylistsSort(option)
        }
    }

    fun createPlaylist() {
        viewModelScope.launch {
            val newPlaylist = Playlist(
                name = createInfo.value.name,
                lastUpdated = System.currentTimeMillis()
            )

            playlistRepository.insert(newPlaylist).toInt()

            _eventChannel.send("Created new playlist: ${newPlaylist.name}")

        }
    }


    fun addToPlaylist(tracks: List<Int>, playlist: Playlist) {
        viewModelScope.launch {
            playlistTracksRepository.addTracksToPlaylist(playlist.id, tracks)

            if (tracks.size > 1) _eventChannel.send("Added ${tracks.size} tracks to ${playlist.name}")
            else {
                val trackInfo = trackRepository.getTracksByIds(tracks)
                _eventChannel.send("Added ${trackInfo[0].title} to ${playlist.name}")
            }
            hideCreateDialog()
        }
    }

    fun onAddToPlaylistArtist(artistId: Int) {
        viewModelScope.launch {
            val tracks = trackRepository.getTracksByArtist(artistId).first()
            val trackIds = tracks.map { it.trackId }
            _createInfo.update { it.copy(name = tracks[0].artistName) }
            onAdd(trackIds)
        }
    }

    fun onAddToPlaylistAlbum(albumId: Int) {
        viewModelScope.launch {
            val tracks = trackRepository.getAlbumTracks(albumId)
            val trackIds = tracks.map { it.trackId }
            _createInfo.update { it.copy(name = tracks[0].albumTitle) }
            onAdd(trackIds)
        }
    }


    fun onAddToPlaylistPlaylist(playlistId: Int) {
        viewModelScope.launch {
            val tracks = playlistTracksRepository.getTracksInPlaylist(playlistId)
            val trackIds = tracks.map { it.trackInfo.trackId }
            _createInfo.update { it.copy(name = playlistRepository.getPlaylistById(playlistId).name) }
            onAdd(trackIds)
        }
    }

    fun onAdd(trackIds: List<Int>) {
        _addToPlaylistState.update { it.copy(isShowing = true, trackIds = trackIds) }
    }

    fun hideAddDialog() {
        _addToPlaylistState.update { it.copy(isShowing = false) }
    }

    fun showCreate() {
        val name = if (_createInfo.value.name == "") "New Playlist" else _createInfo.value.name
        _createInfo.update { it.copy(isShowing = true, name = name) }
    }

    fun hideCreateDialog() {
        _createInfo.update { it.copy(isShowing = false, name = "") }
    }

    fun onNameChange(newName: String) {
        _createInfo.update { it.copy(name = newName) }
    }

    fun deletePlaylist(id: Int) {
        viewModelScope.launch {
            playlistRepository.deleteById(id)
        }
    }

    fun removeTrackFromPlaylist(entryId: Int, playlistId: Int) {
        viewModelScope.launch {
            playlistTracksRepository.removeTrackFromPlaylist(entryId, playlistId)
        }
    }

    fun removeTracksFromPlaylist(entryIds: Set<Int>) {
        viewModelScope.launch {
            playlistTracksRepository.removeTracksFromPlaylist(entryIds.toList())
        }
    }

    fun importM3u(uri: Uri) {
        viewModelScope.launch {
            playlistRepository.importPlaylist(uri)
        }
    }

    fun exportM3u(uri: Uri, playlistId: Int) {
        viewModelScope.launch {
            val tracks =
                playlistTracksRepository.getAllTracksInPlaylist(playlistId, "position", true)
                    .first()
            playlistRepository.exportPlaylist(uri, tracks)
        }
    }


}

data class CreatePlaylistState(
    val name: String = "",
    val isShowing: Boolean = false
)

data class PlaylistUiModel(
    val playlist: Playlist,
    val top4Images: List<String>,
    val trackCount: Int,
    val totalDuration: Long
)

data class AddToPlaylistState(val trackIds: List<Int>, val isShowing: Boolean)