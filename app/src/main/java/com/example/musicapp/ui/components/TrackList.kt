package com.example.musicapp.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.musicapp.data.dto.PlayQueueItemUUID
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.dto.VisualTrack
import java.util.Locale


@Composable
fun TrackInfoRow(
    artwork: String,
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
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artwork)
                        .crossfade(false)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .placeholderMemoryCacheKey(artwork)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Crop
                )
            }

//            Image(
//                painter = artwork,
//                contentDescription = null,
//                modifier = Modifier.size(48.dp)
//            )
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
    artwork: String,
    title: String,
    artist: String,
    duration: String,
    track: VisualTrack,
    trackIndex: Int,
    onClick: (VisualTrack) -> Unit,
    onPlayNext: (TrackInfo) -> Unit,
    onAddToQueue: (TrackInfo) -> Unit,
    onRemoveFromQueue: ((Int) -> Unit)? = null,
    showReorderIconStart: Boolean = false,
    showReorderIconEnd: Boolean = false,
    showTrackNum: Boolean = false,
    showArtwork: Boolean = false,
    trackNum: Int = 0,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(track) },
                onLongClick = { expanded = true }
            )
//            .clickable {
//                onClick(track)}
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

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("Play Next") },
            onClick = {
                onPlayNext(track.data)
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("Add to Queue") },
            onClick = {
                onAddToQueue(track.data)
                expanded = false
            }
        )
        if (onRemoveFromQueue!= null){
            DropdownMenuItem(
                text = {(Text("Remove from Queue"))},
                onClick = {
                    onRemoveFromQueue(trackIndex)
                    expanded = false
                }
            )
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
}

@Composable
fun TrackList(
    tracks: List<VisualTrack>,
    onClick: (VisualTrack) -> Unit,
    onPlayNext: (TrackInfo) -> Unit,
    onAddToQueue: (TrackInfo) -> Unit,
    onRemoveFromQueue: ((Int) -> Unit)? = null,
    showReorderIconStart: Boolean = false,
    showReorderIconEnd: Boolean = false,
    showTrackNum: Boolean = false,
    showArtwork: Boolean = false,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    reorderable: ReorderableLazyListState = rememberReorderableLazyListState(rememberLazyListState()) { from, to->{}},
) {
    val hapticFeedback = LocalHapticFeedback.current
    LazyColumn(state = state,
        ) {
        itemsIndexed(tracks, key = { index, track -> track.key }) { id, queueTrack ->
            val track = queueTrack.data
            ReorderableItem(reorderable, key = queueTrack.key) { isDragging ->
                TrackRow(
                    artwork = track.albumArt.toString(),
                    title = track.title,
                    artist = track.artistName,
                    onClick = onClick,
                    onPlayNext = onPlayNext,
                    onAddToQueue = onAddToQueue,
                    showArtwork = showArtwork,
                    showTrackNum = showTrackNum,
                    showReorderIconStart = showReorderIconStart,
                    showReorderIconEnd = showReorderIconEnd,
                    trackNum = track.trackNum ?: 0,
                    duration = formatDuration(track.duration),
                    track = queueTrack,
                    trackIndex = id,
                    onRemoveFromQueue = onRemoveFromQueue,
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