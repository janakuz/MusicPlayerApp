package com.example.musicapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import com.example.musicapp.TrackViewModel
import com.example.musicapp.data.DataSource
import com.example.musicapp.ui.components.TrackList
import com.example.musicapp.ui.theme.MusicAppTheme
import androidx.compose.runtime.getValue
import com.example.musicapp.data.dto.TrackInfo


@Composable
fun AllTracksScreen(tracks: List<TrackInfo>, onClick: (TrackInfo) -> Unit){
    TrackList(tracks, onClick = onClick, showArtwork = true)

}

@Preview(showBackground = true)
@Composable
fun TracksPreview() {
    MusicAppTheme {
   //     AllTracksScreen(DataSource.tracks)
    }
}