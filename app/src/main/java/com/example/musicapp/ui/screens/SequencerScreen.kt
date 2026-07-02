package com.example.musicapp.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.musicapp.R
import com.example.musicapp.data.local.model.BlockWithTracks
import com.example.musicapp.data.local.model.CompatibleTrack
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.ui.components.TrackInfoRow
import com.example.musicapp.ui.viewmodels.SequencerViewModel
import java.util.Locale
import kotlin.math.roundToInt


@Composable
fun SequencerScreen(
    playlistId: Int
) {
    val sequencerViewModel: SequencerViewModel = hiltViewModel()
    val uiBlocks by sequencerViewModel.uiBlocks.collectAsState()

    val recommendations by sequencerViewModel.compatibleTracks.collectAsState()
    val selectedBlock by sequencerViewModel.selectedBlock.collectAsState()
    val incompatibleOptions by sequencerViewModel.incompatibleTracks.collectAsState()

    val findPrev by sequencerViewModel.findPrev.collectAsState()
    val bpmTolerance by sequencerViewModel.bpmTolerance.collectAsState()
    val loudnessTolerance by sequencerViewModel.loudnessTolerance.collectAsState()

    DisposableEffect(playlistId) {
        onDispose {
            sequencerViewModel.onDiscard()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { sequencerViewModel.onSave() }) {
                Icon(Icons.Default.Save, contentDescription = "Save new order")
            }
        }

    ) { padding ->

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                if (uiBlocks.isEmpty()) {
                    Text("No tracks in sequencer scratchpad")
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(uiBlocks) { index, block ->
                            block.tracks.forEachIndexed { trackOrder, track ->
                                TrackCapsule(
                                    track = track,
                                    onBlockClick = { sequencerViewModel.selectBlock(block) },
                                    isSelected = block.blockNumber == selectedBlock?.blockNumber
                                )


                                if (trackOrder < block.tracks.lastIndex) {
                                    InteractiveLinkSeam(
                                        isMerged = true,
                                        onClick = {
                                                sequencerViewModel.onSplit(block.blockNumber, trackOrder)
                                        }
                                    )
                                }
                            }

                            if (index < uiBlocks.lastIndex) {
                                InteractiveLinkSeam(
                                    isMerged = false,
                                    onClick = {
                                        sequencerViewModel.onMerge(index + 1, index)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween // Spreads title row and slider row evenly
                ) {
                    // ROW 1: Title and Compact Direction Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedBlock != null && recommendations.isNotEmpty()) "Compatible Matches"
                            else if (selectedBlock != null) "Alternative Candidates"
                            else "Select a block to see matches",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { sequencerViewModel.setDirection(lookBack = !findPrev) },
                            enabled = selectedBlock != null
                        ) {
                            Icon(
                                imageVector = if (findPrev) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = if (findPrev) "Matching Previous Track" else "Matching Next Track",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "±${bpmTolerance}BPM",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.width(48.dp)
                            )
                            Slider(
                                value = bpmTolerance.toFloat(),
                                onValueChange = { sequencerViewModel.updateBPMTolerance(it.roundToInt()) },
                                valueRange = 2f..20f,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "±${String.format(Locale.ROOT, "%.1f", loudnessTolerance)}dB",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.width(48.dp)
                            )
                            Slider(
                                value = loudnessTolerance,
                                onValueChange = { sequencerViewModel.updateLoudnessTolerance(it) },
                                valueRange = 0.5f..5.0f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxSize().weight(0.8f).verticalScroll(rememberScrollState())) {
                    recommendations.forEach { compatibleTrack ->
                        CompatibleTrackItem(
                            track = compatibleTrack,
                            inMultiTrackBlock = compatibleTrack.inMultiTrackBlock,
                            onClick = {
                                sequencerViewModel.onMerge(
                                    compatibleTrack.currentBlock,
                                    selectedBlock!!.blockNumber
                                )
                            },
                            isHalfTime = compatibleTrack.halfTime,
                            isDoubleTime = compatibleTrack.doubleTime,
                        )
                    }

                    if (recommendations.isEmpty()){
                        incompatibleOptions.forEach { incompatibleTrack ->
                            CompatibleTrackItem(
                                track = incompatibleTrack,
                                inMultiTrackBlock = incompatibleTrack.inMultiTrackBlock,
                                onClick = {
                                    sequencerViewModel.onMerge(
                                        incompatibleTrack.currentBlock,
                                        selectedBlock!!.blockNumber
                                    )

                                },
                                isHalfTime = incompatibleTrack.halfTime,
                                isDoubleTime = incompatibleTrack.doubleTime,
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun CompatibleTrackItem(
    track: CompatibleTrack,
    inMultiTrackBlock: Boolean,
    onClick: () -> Unit,
    isHalfTime: Boolean,
    isDoubleTime: Boolean,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(track.track.albumArt)
                    .size(128)
                    .crossfade(false)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .diskCacheKey(track.track.albumArt)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .placeholderMemoryCacheKey(track.track.albumArt)
                    .memoryCacheKey(track.track.albumArt)
                    .build(),
                placeholder = painterResource(R.drawable.baseline_album_24),
                error = painterResource(R.drawable.baseline_album_24),
                fallback = painterResource(R.drawable.baseline_album_24),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.track.artistName} • ${track.track.albumTitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (inMultiTrackBlock) {
                    Text(
                        text = "⛓️ Includes linked tracks",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = track.matchDescription,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = if (track.wrongKey) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,

                        )
                }

                Text(
                    text = "${if (track.tempoDifference >= 0) "+" else ""}${track.tempoDifference} BPM " +
                            if (isHalfTime) "(Half Time)" else if (isDoubleTime) "(Double Time)" else "",
                    color = if (track.wrongBPM) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${if (track.loudnessDifference >= 0) "+" else ""}${String.format(Locale.ROOT,"%.1f", track.loudnessDifference)} dB",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (track.wrongLoudness) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



@Composable
fun TrackCapsule(
    track: TrackInfo,
    onBlockClick: () -> Unit,
    isSelected: Boolean) {

    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Surface(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .clickable { onBlockClick() },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(borderWidth, borderColor),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(track.albumArt)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                placeholder = painterResource(R.drawable.baseline_album_24),
                error = painterResource(R.drawable.baseline_album_24),
                fallback = painterResource(R.drawable.baseline_album_24),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = track.artistName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = if (track.bpm != null) "${track.bpm} BPM" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.inversePrimary else Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }}



@Composable
fun InteractiveLinkSeam(
    isMerged: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(40.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        HorizontalDivider(
            color = if (isMerged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.width(40.dp).height(2.dp)
        )

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, if (isMerged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (isMerged) "🔗" else "🔓",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@Composable
fun BlockCard(
    block: BlockWithTracks,
    isSelected: Boolean,
    onBlockClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier
            .width(240.dp)
            .height(280.dp)
            .padding(vertical = 8.dp)
            .clickable { onBlockClick() },
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "BLOCK ${block.blockNumber + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            block.tracks.forEachIndexed { index, track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (index > 0) {
                        Text(
                            text = "🔗",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(track.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(track.artistName, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                }
            }
        }
    }
}


@Composable
fun BlockSeamSeparator(
    onMergeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(48.dp)
            .fillMaxHeight()
            .clickable { onMergeClick() },
        contentAlignment = Alignment.Center
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.fillMaxWidth().width(1.dp)
        )

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.size(32.dp),
            tonalElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Link, contentDescription = "merge")
            }
        }
    }
}