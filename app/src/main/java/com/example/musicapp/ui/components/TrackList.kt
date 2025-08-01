package com.example.musicapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.musicapp.model.Track
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import sh.calvin.reorderable.ReorderableItem
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.musicapp.PlayerViewModel


@Composable
fun TrackInfoRow(
    artwork: Painter,
    title: String,
    artist: String,
    modifier: Modifier = Modifier,
    showArtwork: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(showArtwork) {
            Image(
                painter = artwork,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = artist, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun TrackRow(
    artwork: Painter,
    title: String,
    artist: String,
    duration: String,
    track: Track,
    onClick: (Track) -> Unit,
    showReorderIconStart: Boolean = false,
    showReorderIconEnd: Boolean = false,
    showTrackNum: Boolean = false,
    showArtwork: Boolean = false,
    trackNum: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(track) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showReorderIconStart) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Reorder",
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        if (showTrackNum) {
            Text(text = trackNum.toString(), style = MaterialTheme.typography.bodySmall)

        }

        TrackInfoRow(
            artwork = artwork,
            title = title,
            artist = artist,
            modifier = Modifier.weight(1f),
            showArtwork = showArtwork
        )

        Text(text = duration, style = MaterialTheme.typography.bodyMedium)


        if (showReorderIconEnd) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Reorder",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}


@Composable
fun TrackList(
    tracks: List<Track>,
    onClick: (Track) -> Unit,
    showReorderIconStart: Boolean = false,
    showReorderIconEnd: Boolean = false,
    showTrackNum: Boolean = false,
    showArtwork: Boolean = false,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    reorderable: ReorderableLazyListState = rememberReorderableLazyListState(rememberLazyListState()) {from, to->{}},
    playerViewModel: PlayerViewModel = viewModel()
) {
//    val tracks by playerViewModel.queue.collectAsState()
 //   var list by remember { mutableStateOf<List<Track>>(tracks) }
    val hapticFeedback = LocalHapticFeedback.current
    LazyColumn(state = state,
        ) {
        items(tracks, key = { it.id }) { track ->
            ReorderableItem(reorderable, key = track.id) { isDragging ->
                TrackRow(
                    artwork = painterResource(track.art),
                    title = stringResource(track.title),
                    artist = stringResource(track.artist),
                    onClick = onClick,
                    showArtwork = showArtwork,
                    showTrackNum = showTrackNum,
                    showReorderIconStart = showReorderIconStart,
                    showReorderIconEnd = showReorderIconEnd,
                    trackNum = track.trackNum,
                    duration = track.duration.toString(),
                    track = track,
                    modifier = Modifier.
                            draggableHandle(
                            onDragStarted = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                            },
                    onDragStopped = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                    },
                )
                )

            }
        }
    }
}