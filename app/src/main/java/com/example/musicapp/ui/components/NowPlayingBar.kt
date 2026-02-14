package com.example.musicapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.musicapp.ui.viewmodels.PlayerViewModel
import androidx.compose.runtime.getValue

@Composable
fun NowPlayingBar(
    playerViewModel: PlayerViewModel,
    onClick: () -> Unit,
    currentRoute: String?,
    modifier: Modifier?
) {
    val trackState by playerViewModel.currentTrack.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()

//    val nowPlayingState by playerViewModel.nowPlayingUiState.collectAsState()

    if (currentRoute == "nowPlaying") return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val track = trackState?.track
        if (track != null) {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(track.albumArt)
                    .crossfade(false)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .placeholderMemoryCacheKey(track.albumArt)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(text = track.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = track.artistName,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = {
                if (playerViewModel.hasPrevMediaItem() == true) {
                    playerViewModel.skipToPrevious()
                }
            }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
            }

            IconButton(onClick = {
                playerViewModel.togglePlayback()
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }

            IconButton(onClick = {
                if (playerViewModel.hasNextMediaItem() == true) {
                    playerViewModel.skipToNext()
                }
            }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next")
            }
        }
    }

}