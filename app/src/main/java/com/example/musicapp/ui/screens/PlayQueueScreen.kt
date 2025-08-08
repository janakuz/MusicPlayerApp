package com.example.musicapp.ui.screens

import android.util.Log
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.musicapp.ui.viewmodels.PlayerViewModel
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.ui.components.TrackList
import com.example.musicapp.ui.theme.MusicAppTheme
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun PlayQueueScreen(tracks: List<TrackInfo>,
                    onTrackClick: (TrackInfo) -> Unit,
                    playerViewModel: PlayerViewModel){
    val lazyListState = rememberLazyListState()
    var visibleQueue by remember { mutableStateOf(tracks.toList()) }
    Log.d("PlayQueue", "tracks: ${tracks.size}")
    Log.d("PlayQueue", "queue: ${visibleQueue.size}")
    Log.d("PlayQueue", "IDs: ${visibleQueue.map { it.trackId }}")

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        visibleQueue = visibleQueue.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        playerViewModel.updateQueue(visibleQueue)
    }

    TrackList(visibleQueue, onClick = onTrackClick, showArtwork = true, showReorderIconEnd = true, state = lazyListState, reorderable = reorderableLazyListState, playerViewModel = playerViewModel)

}

@Preview(showBackground = true)
@Composable
fun PlayQueuePreview() {
    MusicAppTheme {
      //  PlayQueueScreen(DataSource.tracks, {}, viewModel())
    }
}