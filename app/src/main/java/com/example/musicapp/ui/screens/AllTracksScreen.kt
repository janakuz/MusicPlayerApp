package com.example.musicapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.data.local.model.VisualTrack
import com.example.musicapp.ui.components.FastScrollbar
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.components.TrackList
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.ui.viewmodels.AllTracksViewModel


@Composable
fun AllTracksScreen(
    onClick: (TrackInfo, List<TrackInfo>) -> Unit,
    onPlayNext: (TrackInfo) -> Unit,
    onAddToQueue: (TrackInfo) -> Unit,
    onAddToPlaylist: (Int) -> Unit,
    onEdit: (TrackInfo) -> Unit,
    sortRequest: SortOption?,
) {
    val trackViewModel: AllTracksViewModel = hiltViewModel()

    LaunchedEffect(sortRequest) {
        sortRequest?.let {
            trackViewModel.setSort(it)
        }
    }


    val tracksUIState by trackViewModel.tracksUiState.collectAsState()
    val tracks = tracksUIState.tracks
    val currentSort = trackViewModel.currentSortOption.collectAsState()

    val visualTracks = tracks.map { track -> VisualTrack(key = track.trackId, data = track) }

    val sharedListState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        TrackList(
            visualTracks,
            onClick = { track -> onClick(track.data, tracks) },
            onPlayNext = onPlayNext,
            onAddToQueue = onAddToQueue,
            showArtwork = true,
            state = sharedListState,
            onEdit = onEdit,
            onAddToPlaylist = onAddToPlaylist
        )

        FastScrollbar(
            listState = sharedListState,
            totalItems = visualTracks.size,
            tracks = tracks,
            sortOption = currentSort.value,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(bottom = 16.dp)
        )
    }


}

@Preview(showBackground = true)
@Composable
fun TracksPreview() {
    MusicAppTheme {
    }
}