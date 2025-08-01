package com.example.musicapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicapp.R
import com.example.musicapp.data.DataSource
import com.example.musicapp.model.Track
import com.example.musicapp.ui.components.TrackList
import com.example.musicapp.ui.theme.MusicAppTheme

@Composable
fun AlbumView(
    name: String,
    artist: String,
    releaseDate: String,
    image: String,
    tracks: List<Track>,
    numTracks: String,
    duration: String,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlbumDetailHeader(image=image,
            title = name
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = artist, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Row(){
            Text(text = releaseDate, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(4.dp))

            Text(text = numTracks, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(4.dp))

            Text(text = duration, style = MaterialTheme.typography.bodySmall)

        }
        Spacer(modifier = Modifier.height(4.dp))
        TrackList(tracks, onClick = onTrackClick, showTrackNum = true)


    }

}

@Preview(showBackground = true)
@Composable
fun AlbumPreview() {
    MusicAppTheme {
        AlbumView(name= stringResource(R.string.sw),
            artist = stringResource(R.string.sw),
            releaseDate = stringResource(R.string.release),
            image = "",
            numTracks = stringResource(R.string.tracksnum),
            duration = stringResource(R.string.duration),
            tracks = DataSource.tracks,
            onTrackClick = {}
        )
    }

}