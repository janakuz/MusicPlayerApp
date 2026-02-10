package com.example.musicapp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.material3.Slider
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import com.example.musicapp.data.dto.PlayQueueItemUUID
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.ui.viewmodels.PlayerViewModel
import com.example.musicapp.data.dto.TrackInfo

@Composable
fun NowPlayingView(
    playerViewModel: PlayerViewModel,
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
        modifier = Modifier.fillMaxSize().navigationBarsPadding()
    ) {
        val track = trackState?.track
        track?.let {
            AlbumDetailHeader(
                image = track!!.albumArt.toString(),
                title = track!!.title
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = it.artistName, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = it.albumTitle, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(10.dp))

        Slider(

            value = position.toFloat(),
            onValueChange = { newValue ->
                sliderPosition  = newValue
            },
            onValueChangeFinished = {
                playerViewModel.seekTo(sliderPosition.toLong())
            },            valueRange = 0f..(duration.takeIf { it > 0 } ?: 1).toFloat()

        )

        Spacer(Modifier.height(10.dp))

        Row {

            IconButton(onClick = {
                playerViewModel.toggleShuffle()
            }) {
                Icon(
                    imageVector = if (!shuffleOn) Icons.Default.Shuffle else Icons.Default.ShuffleOn,
                    contentDescription = "Shuffle",
                )
            }


            IconButton(onClick = {
                if (playerViewModel.hasPrevMediaItem() == true) {
                    playerViewModel.skipToPrevious()
                }
            }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
            }


            IconButton(
                onClick = {
                playerViewModel.togglePlayback()
            },
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
            }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next")
            }

            IconButton(onClick = {
                playerViewModel.toggleRepeat()
            }) {
                Icon(
                    imageVector =
                        if (repeatMode== Player.REPEAT_MODE_OFF) Icons.Default.Repeat
                        else if (repeatMode==Player.REPEAT_MODE_ALL) Icons.Default.RepeatOn
                        else Icons.Default.RepeatOne,
                    contentDescription = "Repeat",
                )

            }

        }
        Spacer(Modifier.height(32.dp))
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingWithQueue(
    playerViewModel: PlayerViewModel,
    onTrackClick: (String) -> Unit,
    onBack: () -> Unit
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
                    playerViewModel)
            }
        },
        content = {
            NowPlayingView(
                playerViewModel = playerViewModel,
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