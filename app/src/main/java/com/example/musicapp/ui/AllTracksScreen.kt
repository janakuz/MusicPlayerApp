package com.example.musicapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import com.example.musicapp.TrackViewModel
import com.example.musicapp.data.DataSource
import com.example.musicapp.ui.components.TrackList
import com.example.musicapp.ui.theme.MusicAppTheme
import androidx.compose.runtime.getValue


@Composable
fun AllTracksScreen(trackViewModel: TrackViewModel){
    val tracksUIState by trackViewModel.tracksUiState.collectAsState()
    val tracks = tracksUIState.tracks
    TrackList(tracks, onClick = {}, showArtwork = true)

}

@Preview(showBackground = true)
@Composable
fun TracksPreview() {
    MusicAppTheme {
   //     AllTracksScreen(DataSource.tracks)
    }
}