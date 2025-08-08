package com.example.musicapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.musicapp.ui.components.TrackList
import com.example.musicapp.ui.theme.MusicAppTheme
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