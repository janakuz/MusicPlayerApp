package com.example.musicapp.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicapp.PlayerViewModel
import com.example.musicapp.data.DataSource
import com.example.musicapp.model.Track
import com.example.musicapp.ui.components.TrackList
import com.example.musicapp.ui.theme.MusicAppTheme
import sh.calvin.reorderable.rememberReorderableLazyListState
//import org.burnoutcrew.composereorderable


@Composable
fun PlayQueueScreen(tracks: List<Track>,
                    onTrackClick: (Track) -> Unit,
                    playerViewModel: PlayerViewModel){
    val lazyListState = rememberLazyListState()
    var visibleQueue by remember { mutableStateOf(tracks.toList()) }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        visibleQueue = visibleQueue.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        playerViewModel.updateQueue(visibleQueue)
//        playerViewModel.moveTrack(from.index, to.index)
    }

 //   val scope = rememberCoroutineScope()



 //   TrackList(visibleQueue, onClick = onTrackClick, showArtwork = true, showReorderIconEnd = true, state = lazyListState, reorderable = reorderableLazyListState, playerViewModel = playerViewModel)

}

@Preview(showBackground = true)
@Composable
fun PlayQueuePreview() {
    MusicAppTheme {
        PlayQueueScreen(DataSource.tracks, {}, viewModel())
    }
}