package com.example.musicapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.components.TrackList
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.ui.viewmodels.AllTracksViewModel


@Composable
fun AllTracksScreen(onClick: (TrackInfo, List<TrackInfo>) -> Unit){
    val trackViewModel: AllTracksViewModel = hiltViewModel()
    val tracksUIState by trackViewModel.tracksUiState.collectAsState()
    val tracks = tracksUIState.tracks

    TrackList(tracks, onClick = {track -> onClick(track, tracks)}, showArtwork = true)

}

@Preview(showBackground = true)
@Composable
fun TracksPreview() {
    MusicAppTheme {
   //     AllTracksScreen(DataSource.tracks)
    }
}