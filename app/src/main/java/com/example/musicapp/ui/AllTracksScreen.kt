package com.example.musicapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.musicapp.data.DataSource
import com.example.musicapp.model.Track
import com.example.musicapp.ui.components.TrackList
import com.example.musicapp.ui.theme.MusicAppTheme

@Composable
fun AllTracksScreen(tracks: List<Track>){
    TrackList(tracks, onClick = {}, showArtwork = true)

}

@Preview(showBackground = true)
@Composable
fun TracksPreview() {
    MusicAppTheme {
        AllTracksScreen(DataSource.tracks)
    }
}