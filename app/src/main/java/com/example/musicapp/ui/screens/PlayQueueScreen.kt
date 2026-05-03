package com.example.musicapp.ui.screens

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.musicapp.data.dto.PlayQueueItemUUID
import com.example.musicapp.ui.viewmodels.PlayerViewModel
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.dto.VisualTrack
import com.example.musicapp.ui.components.TrackList
import com.example.musicapp.ui.theme.MusicAppTheme
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun PlayQueueScreen(
    tracks: List<PlayQueueItemUUID>,
    onTrackClick: (VisualTrack) -> Unit,
    onPlayNext: (TrackInfo) -> Unit,
    onAddToQueue: (TrackInfo) -> Unit,
    onAddToPlaylist: (Int) -> Unit,
    onEdit: (TrackInfo) -> Unit,
    playerViewModel: PlayerViewModel
){
    val lazyListState = rememberLazyListState()
    var isDragging by remember { mutableStateOf(false) }
    var visibleQueue by remember { mutableStateOf(tracks.toList()) }

    LaunchedEffect(tracks) {
        if (!isDragging) {
            visibleQueue = tracks.toList()
        }
    }

    val reorderableLazyListState = rememberReorderableLazyListState (
        lazyListState = lazyListState,
        onMove = { from, to ->
            visibleQueue = visibleQueue.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            playerViewModel.moveTrack(from.index, to.index)
        },
    )
//        playerViewModel.updateQueue(visibleQueue)

    LaunchedEffect(reorderableLazyListState.isAnyItemDragging) {
        if (reorderableLazyListState.isAnyItemDragging) {
            playerViewModel.startDragging()
            isDragging = true
        } else if (isDragging) {
            playerViewModel.finalizeMove(visibleQueue)
            isDragging = false
        }
    }


    val visualTracks = visibleQueue.map { track -> VisualTrack(key = track.queueId, data = track.track) }

    TrackList(
        visualTracks,
        onClick = onTrackClick,
        onPlayNext = onPlayNext,
        onAddToQueue = onAddToQueue,
        onRemoveFromQueue = { index -> playerViewModel.removeTrackAt(index) },
        showReorderIconEnd = true,
        showArtwork = true,
        strictHighlight = true,
        state = lazyListState,
        reorderable = reorderableLazyListState,
        onEdit = onEdit,
        onAddToPlaylist = onAddToPlaylist,
    //    playerViewModel = playerViewModel
    )

}

@Preview(showBackground = true)
@Composable
fun PlayQueuePreview() {
    MusicAppTheme {
      //  PlayQueueScreen(DataSource.tracks, {}, viewModel())
    }
}