package com.example.musicapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.viewmodels.PlaylistDetailViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.dto.VisualTrack
import com.example.musicapp.data.entity.Playlist
import com.example.musicapp.ui.components.TrackList
import com.example.musicapp.ui.components.formatDuration
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun PlaylistDetailScreen(
    onTrackClick: (TrackInfo, List<TrackInfo>, Int, List<Int>) -> Unit,
    onPlayNext: (TrackInfo) -> Unit,
    onAddToQueue: (TrackInfo) -> Unit,
    onEdit: (TrackInfo) -> Unit,
    onRemove: (Int, Int) -> Unit,
    ) {

    val playlistDetailViewModel: PlaylistDetailViewModel = hiltViewModel()

    val tracks by playlistDetailViewModel.playlistTracks.collectAsState()
    val info by playlistDetailViewModel.playlistInfo.collectAsState()
    val stats by playlistDetailViewModel.playlistStats.collectAsState()

    val visualTracks = tracks.map { track -> VisualTrack(key = track.entryId, data = track.trackInfo) }
    val trackInfos = tracks.map { it.trackInfo }
    val entryIds = tracks.map { it.entryId }


    val lazyListState = rememberLazyListState()
    var isDragging by remember { mutableStateOf(false) }
    var visiblePlaylist by remember { mutableStateOf(tracks.toList()) }

    LaunchedEffect(tracks) {
        if (!isDragging) {
            visiblePlaylist = tracks.toList()
        }
    }

    val reorderableLazyListState = rememberReorderableLazyListState (
        lazyListState = lazyListState,
        onMove = { from, to ->
            visiblePlaylist = visiblePlaylist.toMutableList().apply {
                add(to.index-1, removeAt(from.index-1))
            }
        },
    )


    LaunchedEffect(reorderableLazyListState.isAnyItemDragging) {
        if (reorderableLazyListState.isAnyItemDragging) {
            isDragging = true
        } else if (isDragging) {
            playlistDetailViewModel.reorder(visiblePlaylist)
            isDragging = false
        }
    }

    TrackList(
        visualTracks,
        onClick = { track -> onTrackClick(track.data, trackInfos, track.key as Int, entryIds) },
        onPlayNext = onPlayNext,
        onAddToQueue = onAddToQueue,
        showTrackNum = false,
        playlistHighlight = true,
        header = {
            PlaylistHeader(
                info,
                top4Images = stats?.images ?: emptyList(),
                trackCount = stats?.trackCount ?: 0,
                duration = stats?.duration ?: 0L,
                onPlayAll = { onTrackClick(trackInfos[0], trackInfos, tracks[0].entryId, entryIds) },
                onShuffle = {  },
            )
        },
        onEdit = onEdit,
        onRemoveFromPlaylist = { track -> onRemove(track.key as Int, info?.id ?: -1) },
        state = lazyListState,
        reorderable = reorderableLazyListState,
        showReorderIconStart = true,
        )

}

@Composable
fun PlaylistHeader(
    playlistInfo: Playlist?,
    top4Images: List<String>,
    trackCount: Int,
    duration: Long,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (top4Images.isNotEmpty()) {
            PlaylistCollage(
                images = top4Images,
                modifier = Modifier
                    .size(200.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
            )
        }
        else {
            Icon(Icons.Default.MusicNote, contentDescription = "")
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = playlistInfo?.name ?: "Loading...",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            textAlign = TextAlign.Center
        )

        Text(
            text = "$trackCount tracks • ${formatDuration(duration)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onPlayAll,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Play")
            }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(
                onClick = onShuffle,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Shuffle")
            }
        }
    }
}