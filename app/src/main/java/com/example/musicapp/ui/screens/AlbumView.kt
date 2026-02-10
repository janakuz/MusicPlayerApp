package com.example.musicapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.dto.VisualTrack
import com.example.musicapp.ui.components.TrackList
import com.example.musicapp.ui.components.formatDuration
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.ui.viewmodels.AlbumDetailViewModel

@Composable
fun AlbumView(
    onTrackClick: (TrackInfo, List<TrackInfo>) -> Unit,
    onPlayNext: (TrackInfo) -> Unit,
    onAddToQueue: (TrackInfo) -> Unit,
    modifier: Modifier = Modifier
){
    val albumDetailViewModel: AlbumDetailViewModel = hiltViewModel()

    val albumUiState by albumDetailViewModel.currentAlbumUiState.collectAsState()
    val album = albumUiState.album

    val tracksUiState by albumDetailViewModel.albumTracksUiState.collectAsState()
    val tracks = tracksUiState.tracks

    val visualTracks = tracks.map { track -> VisualTrack(key = track.trackId, data = track) }

    if (album != null) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AlbumDetailHeader(
                image = album.image.toString(),
                title = album.title
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row() {
                Text(
                    text = album.releaseDate.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(4.dp))

                Text(text = album.numTracks.toString(), style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = formatDuration(album.duration),
                    style = MaterialTheme.typography.bodySmall
                )

            }
            Spacer(modifier = Modifier.height(4.dp))
            TrackList(
                visualTracks,
                onClick = {track -> onTrackClick(track.data, tracks)},
                onPlayNext = onPlayNext,
                onAddToQueue = onAddToQueue,
                showTrackNum = true,
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun AlbumPreview() {
    MusicAppTheme {
//        AlbumView(name= stringResource(R.string.sw),
// //           artist = stringResource(R.string.sw),
//            releaseDate = stringResource(R.string.release),
//            image = "",
//            numTracks = stringResource(R.string.tracksnum),
//            duration = stringResource(R.string.duration),
//            tracks = DataSource.tracks,
//            onTrackClick = {}
//        )
    }

}