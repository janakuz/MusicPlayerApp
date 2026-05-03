package com.example.musicapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.entity.Playlist
import com.example.musicapp.data.repository.AlbumRepository
import com.example.musicapp.data.repository.ArtistRepository
import com.example.musicapp.data.repository.PlaylistRepository
import com.example.musicapp.data.repository.PlaylistTracksRepository
import com.example.musicapp.data.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
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
    private val trackRepository: TrackRepository
) : ViewModel() {

    private val _createInfo = MutableStateFlow<CreatePlaylistState>(CreatePlaylistState())
    val createInfo = _createInfo.asStateFlow()

    private val _addToPlaylistState = MutableStateFlow<AddToPlaylistState>(AddToPlaylistState(emptyList(), false))
    val addToPlaylistState = _addToPlaylistState.asStateFlow()

    private val _eventChannel = Channel<String>(Channel.BUFFERED)
    val events = _eventChannel.receiveAsFlow()


    val playlists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists("title", true)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    @OptIn(ExperimentalCoroutinesApi::class)
    val playlistUiStates = playlistRepository.getAllPlaylists("title", true).flatMapLatest { playlists ->
        combine(playlists.map { playlist ->
            playlistRepository.getPlaylistStats(playlist.id).map { stats ->
                PlaylistUiModel(
                    playlist = playlist,
                    top4Images = stats.images,
                    trackCount = stats.trackCount,
                    totalDuration = stats.duration
                )
            }
        }) { it.toList() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


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


    fun addToPlaylist(tracks: List<Int> , playlist: Playlist){
        viewModelScope.launch {
            playlistTracksRepository.addTracksToPlaylist(playlist.id, tracks)

            if (tracks.size > 1) _eventChannel.send("Added ${tracks.size} tracks to ${playlist.name}")
            else {
                val trackInfo = trackRepository.getTracksByIds(tracks.toSet())
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

    fun onAdd(trackIds: List<Int>){
        _addToPlaylistState.update { it.copy(isShowing = true, trackIds = trackIds) }
    }

    fun hideAddDialog() {
        _addToPlaylistState.update { it.copy(isShowing = false) }
    }

    fun showCreate(){
        val name = if (_createInfo.value.name == "") "New Playlist" else _createInfo.value.name
        _createInfo.update { it.copy(isShowing = true, name = name) }
    }

    fun hideCreateDialog() {
        _createInfo.update { it.copy(isShowing = false, name = "") }
    }

    fun onNameChange(newName: String){
        _createInfo.update { it.copy(name = newName) }
    }

    fun deletePlaylist(id: Int){
        viewModelScope.launch {
            playlistRepository.deleteById(id)
        }
    }

    fun removeTrackFromPlaylist(entryId: Int, playlistId: Int){
        viewModelScope.launch {
            playlistTracksRepository.removeTrackFromPlaylist(entryId, playlistId)
        }
    }

    fun removeTracksFromPlaylist(entryIds: Set<Int>){
        viewModelScope.launch {
            playlistTracksRepository.removeTracksFromPlaylist(entryIds.toList())
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