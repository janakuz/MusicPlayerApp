package com.example.musicapp.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.viewmodels.PlaylistDetailViewModel
import androidx.compose.runtime.getValue
import androidx.room.Delete
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.dto.VisualTrack
import com.example.musicapp.data.entity.Playlist
import com.example.musicapp.ui.components.TrackList

@Composable
fun PlaylistDetailScreen(
    onTrackClick: (TrackInfo, List<TrackInfo>) -> Unit,
    onPlayNext: (TrackInfo) -> Unit,
    onAddToQueue: (TrackInfo) -> Unit,
    onEdit: (TrackInfo) -> Unit,
    onRemove: (Int, Int) -> Unit
    ) {

    val playlistDetailViewModel: PlaylistDetailViewModel = hiltViewModel()

    val tracks by playlistDetailViewModel.playlistTracks.collectAsState()
    val info by playlistDetailViewModel.playlistInfo.collectAsState()

    val visualTracks = tracks.map { track -> VisualTrack(key = track.entryId, data = track.trackInfo) }
    val trackInfos = tracks.map { it.trackInfo }

    TrackList(
        visualTracks,
        onClick = { track -> onTrackClick(track.data, trackInfos) },
        onPlayNext = onPlayNext,
        onAddToQueue = onAddToQueue,
        showTrackNum = false,
        header = {
            PlaylistHeader(info)
        },
        onEdit = onEdit,
        onRemoveFromPlaylist = { track -> onRemove(track.key as Int, info?.id ?: -1) }
    )

}

@Composable
fun PlaylistHeader(playlistInfo: Playlist?){
    Text(playlistInfo?.name ?: "")
}