package com.example.musicapp.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.ui.viewmodels.LoopState
import com.example.musicapp.ui.viewmodels.PlayerViewModel
import com.example.musicapp.util.SlantedLeftShape
import com.example.musicapp.util.SlantedRightShape
import com.example.musicapp.util.formatDuration
import java.util.Locale

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingView(
    playerViewModel: PlayerViewModel,
    onArtistClick: (Int) -> Unit,
    onAlbumClick: (Int) -> Unit
) {
    val trackState by playerViewModel.currentTrack.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val position by playerViewModel.position.collectAsState()
    val duration by playerViewModel.duration.collectAsState()
    val shuffleOn by playerViewModel.isShuffleEnabled.collectAsState()
    val repeatMode by playerViewModel.repeatMode.collectAsState()
    val currentSpeed by playerViewModel.currentSpeed.collectAsState()
    val loopState by playerViewModel.loopState.collectAsState()
    val loopStart by playerViewModel.loopStart.collectAsState()
    val loopEnd by playerViewModel.loopEnd.collectAsState()

    var sliderPosition by remember { mutableFloatStateOf(position.toFloat()) }

    val fractionA = if (duration > 0 && loopStart != null) loopStart!! / duration.toFloat() else 0f
    val fractionB = if (duration > 0 && loopEnd != null) loopEnd!! / duration.toFloat() else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
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
            Text(
                text = it.artistName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .clickable(onClick = { onArtistClick(track.artistId) })
            )
            Text(
                text = it.albumTitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .clickable(onClick = { onAlbumClick(track.albumId) })
            )
        }
        val accentColor = MaterialTheme.colorScheme.primary

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = position.formatDuration(),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .width(32.dp)
            )
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val constraintsWidth = maxWidth
                if (loopStart != null && loopEnd != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.Transparent)
                            .drawWithContent {
                                val startX = size.width * fractionA
                                val endX = size.width * fractionB
                                drawRect(
                                    color = accentColor.copy(alpha = 0.35f),
                                    topLeft = Offset(startX, 0f),
                                    size = Size(endX - startX, size.height)
                                )
                            }
                    )
                }

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

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                        ) {
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

                if (loopStart != null) {
                    val offsetA = constraintsWidth * fractionA
                    Box(
                        modifier = Modifier
                            .offset(x = offsetA - 2.dp)
                            .size(4.dp, 16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                }

                if (loopEnd != null) {
                    val offsetB = constraintsWidth * fractionB
                    Box(
                        modifier = Modifier
                            .offset(x = offsetB - 2.dp)
                            .size(4.dp, 16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                }

            }
            Text(
                text = duration.formatDuration(),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(32.dp)

            )

        }

        var showPlaybackSpeedDialog by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            PlaybackSpeedButton(
                currentSpeed = currentSpeed,
                onClick = {showPlaybackSpeedDialog = true}
            )


            LoopControlGroup(
                loopState = loopState,
                onSetA = { playerViewModel.setLoopStartToCurrent() },
                onSetB = { playerViewModel.setLoopEndToCurrent() },
                onClear = { playerViewModel.clearLoop() }
            )

        }

        if (showPlaybackSpeedDialog) {
            PlaybackSpeedDialog(
                initialSpeed = currentSpeed,
                onConfirm = { speed ->
                    playerViewModel.updateSpeed(speed)
                    showPlaybackSpeedDialog = false
                },
                onDismissRequest = { showPlaybackSpeedDialog = false }
            )
        }


        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {

            IconButton(
                onClick = {
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


            IconButton(
                onClick = {
                    if (playerViewModel.hasPrevMediaItem() == true) {
                        playerViewModel.skipToPrevious()
                    }
                },
                Modifier.size(64.dp)
            ) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    Modifier.size(32.dp)
                )
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

            IconButton(
                onClick = {
                    if (playerViewModel.hasNextMediaItem() == true) {
                        playerViewModel.skipToNext()
                    }
                },
                Modifier.size(64.dp)
            ) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Next",
                    Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = {
                    playerViewModel.toggleRepeat()
                },
                Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector =
                        if (repeatMode == Player.REPEAT_MODE_OFF) Icons.Default.Repeat
                        else if (repeatMode == Player.REPEAT_MODE_ALL) Icons.Default.RepeatOn
                        else Icons.Default.RepeatOne,
                    contentDescription = "Repeat",
                    Modifier.size(32.dp)
                )

            }

        }
    }

}



@Composable
fun PlaybackSpeedButton(
    currentSpeed: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                shape = RoundedCornerShape(2.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Text(
            text = String.format(Locale.ROOT, "%.2fx", currentSpeed),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSpeedDialog(
    initialSpeed: Float,
    onDismissRequest: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(initialSpeed) }
    val accentColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Playback Speed",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format(Locale.ROOT, "%.2fx", sliderValue),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0.25f..2.0f,
                    steps = 34,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(accentColor, CircleShape)
                        )
                    },
                    track = { sliderState ->
                        val fraction = (sliderState.value - sliderState.valueRange.start) /
                                (sliderState.valueRange.endInclusive - sliderState.valueRange.start)

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                        ) {
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
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { sliderValue = 1.0f }) {
                    Text("RESET")
                }

                Row {
                    TextButton(onClick = onDismissRequest) {
                        Text("CANCEL")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onConfirm(sliderValue) }) {
                        Text("OK")
                    }
                }
            }
        }
    )
}

@Composable
fun LoopControlGroup(
    loopState: LoopState,
    onSetA: () -> Unit,
    onSetB: () -> Unit,
    onClear: () -> Unit
) {
    AnimatedContent(
        targetState = loopState,
        label = "LoopButtonsTransition"
    ) { state ->
        when (state) {
            LoopState.NOT_SET, LoopState.A_SET -> {
                Row(horizontalArrangement = Arrangement.spacedBy((-2).dp)) {
                    val isASet = state == LoopState.A_SET
                    Box(
                        modifier = Modifier
                            .clip(SlantedLeftShape)
                            .border(
                                width = 1.dp,
                                color = if (isASet) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                shape = SlantedLeftShape
                            )
                            .background(
                                color = if (isASet) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent,
                                shape = SlantedLeftShape
                            )
                            .clickable { onSetA() }
                            .padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = "A",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isASet) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    val isBEnabled = state == LoopState.A_SET
                    Box(
                        modifier = Modifier
                            .clip(SlantedRightShape)
                            .border(
                                width = 1.dp,
                                color = if (isBEnabled) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                shape = SlantedRightShape
                            )
                            .clickable(enabled = isBEnabled) { onSetB() }
                            .padding(start = 10.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = "B",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isBEnabled) MaterialTheme.colorScheme.onBackground
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                    }
                }
            }
            LoopState.ACTIVE -> {
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(2.dp)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(2.dp)
                        )
                        .clickable { onClear() }
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Clear Loop",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
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
    onAlbumClick: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetScaffoldState()
    val navigationBarsPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
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
                    { track -> onTrackClick(track.key.toString()) },
                    onPlayNext = { track -> playerViewModel.playNext(track) },
                    onAddToQueue = { track -> playerViewModel.addToQueue(track) },
                    onEdit = onEdit,
                    onAddToPlaylist = onAddToPlaylist,
                    playerViewModel = playerViewModel,
                    onGoToArtist = onArtistClick,
                    onGoToAlbum = onAlbumClick,
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

