package com.example.musicapp.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.ui.components.formatDuration
import com.example.musicapp.ui.viewmodels.PlayerViewModel
import java.nio.file.WatchEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingView(
    playerViewModel: PlayerViewModel,
    onArtistClick: (Int) -> Unit,
    onAlbumClick: (Int) -> Unit
){
    val trackState by playerViewModel.currentTrack.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val position by playerViewModel.position.collectAsState()
    val duration by playerViewModel.duration.collectAsState()
    val shuffleOn by playerViewModel.isShuffleEnabled.collectAsState()
    val repeatMode by playerViewModel.repeatMode.collectAsState()

    var sliderPosition by remember { mutableStateOf(position.toFloat()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxSize().navigationBarsPadding()
    ) {
        val track = trackState?.track
        val gradientColors = playerViewModel.albumColors

        track?.let {
            playerViewModel.getAlbumColors(track.albumArt.toString())

            AlbumDetailHeader(
                image = track.albumArt.toString(),
                title = track.title,
                gradientColors = gradientColors
            )
//            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it.artistName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .clickable(onClick = { onArtistClick(track.artistId) }))
//            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it.albumTitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .clickable(onClick = { onAlbumClick(track.albumId) }))
        }
//        Spacer(Modifier.height(10.dp))
        val accentColor = MaterialTheme.colorScheme.primary

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)) {
            Text(
                text = formatDuration(position),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .width(32.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Slider(
                    value = position.toFloat(),
                    onValueChange = { newValue ->
                        sliderPosition = newValue
                    },
                    onValueChangeFinished = {
                        playerViewModel.seekTo(sliderPosition.toLong())
                    }, valueRange = 0f..(duration.takeIf { it > 0 } ?: 1).toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTickColor = accentColor
//                thumbColor = gradientColors.secondaryColor,
//                activeTrackColor = gradientColors.secondaryColor,
//                inactiveTrackColor = gradientColors.onColor.copy(alpha = 0.1f)
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(accentColor, CircleShape)
                        )
                    },
                    track = { sliderState ->
                        val fraction = (sliderState.value - sliderState.valueRange.start) /
                                (sliderState.valueRange.endInclusive - sliderState.valueRange.start)

                        Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                            drawRoundRect(
                                color = accentColor.copy(alpha = 0.2f),
                                size = size,
                            )
                            drawRoundRect(
                                color = accentColor,
                                size = size.copy(width = size.width * fraction),
                            )
                        }
                    }
                )
            }
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(32.dp)

            )

        }

//        Spacer(Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            ) {

            IconButton(onClick = {
                playerViewModel.toggleShuffle()
            },
                Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (!shuffleOn) Icons.Default.Shuffle else Icons.Default.ShuffleOn,
                    contentDescription = "Shuffle",
                    Modifier.size(32.dp)
                )
            }


            IconButton(onClick = {
                if (playerViewModel.hasPrevMediaItem() == true) {
                    playerViewModel.skipToPrevious()
                }
            },
                Modifier.size(64.dp)) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    Modifier.size(32.dp))
            }


            IconButton(
                onClick = {
                playerViewModel.togglePlayback()
            },
                Modifier.size(128.dp)
                ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    Modifier.size(80.dp)
                )
            }

            IconButton(onClick = {
                if (playerViewModel.hasNextMediaItem() == true) {
                    playerViewModel.skipToNext()
                }
            },
                Modifier.size(64.dp)) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Next",
                    Modifier.size(32.dp))
            }

            IconButton(onClick = {
                playerViewModel.toggleRepeat()
            },
                Modifier.size(64.dp)) {
                Icon(
                    imageVector =
                        if (repeatMode== Player.REPEAT_MODE_OFF) Icons.Default.Repeat
                        else if (repeatMode==Player.REPEAT_MODE_ALL) Icons.Default.RepeatOn
                        else Icons.Default.RepeatOne,
                    contentDescription = "Repeat",
                    Modifier.size(32.dp)
                )

            }

        }
//        Spacer(Modifier.height(32.dp))
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingWithQueue(
    playerViewModel: PlayerViewModel,
    onTrackClick: (String) -> Unit,
    onAddToPlaylist: (Int) -> Unit,
    onBack: () -> Unit,
    onEdit: (TrackInfo) -> Unit,
    onArtistClick: (Int) -> Unit,
    onAlbumClick: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetScaffoldState()
    val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val totalPeekHeight = 10.dp

    val tracks by playerViewModel.queue.collectAsState()


    BottomSheetScaffold(
        sheetPeekHeight = totalPeekHeight,
        topBar = {
            TopAppBar(
                title = { Text("Now Playing") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                }
            )
        },
        scaffoldState = sheetState,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .navigationBarsPadding()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                PlayQueueScreen(
                    tracks,
                    {track -> onTrackClick(track.key.toString())},
                    onPlayNext = { track -> playerViewModel.playNext(track) },
                    onAddToQueue = { track -> playerViewModel.addToQueue(track)},
                    onEdit = onEdit,
                    onAddToPlaylist = onAddToPlaylist,
                    playerViewModel = playerViewModel
                )
            }
        },
        content = {
            NowPlayingView(
                playerViewModel = playerViewModel,
                onArtistClick = onArtistClick,
                onAlbumClick = onAlbumClick
            )
        }
    )
}

//@Preview(showBackground = true)
//@Composable
//fun NowPlayingPreview() {
//    MusicAppTheme {
//        NowPlayingView(
//            playerViewModel = viewModel(),
//        )
//    }
//
//}