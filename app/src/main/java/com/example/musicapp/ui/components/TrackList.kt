package com.example.musicapp.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.musicapp.R
import com.example.musicapp.data.local.model.TrackInfo
import com.example.musicapp.data.local.model.VisualTrack
import com.example.musicapp.ui.viewmodels.PlayerViewModel
import com.example.musicapp.ui.viewmodels.TrackDeletionViewModel
import com.example.musicapp.ui.viewmodels.TrackSelectionViewModel
import com.example.musicapp.util.formatDuration
import kotlinx.coroutines.launch


@Composable
fun LiveEqualizer(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier.size(16.dp)
) {
    val transition = rememberInfiniteTransition(label = "equalizer")

    val heights = listOf(
        transition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse)),
        transition.animateFloat(0.2f, 0.8f, infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse)),
        transition.animateFloat(0.4f, 0.9f, infiniteRepeatable(tween(520, easing = FastOutSlowInEasing), RepeatMode.Reverse))
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { heightState ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightState.value)
                    .background(color, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
            )
        }
    }
}

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
                        .size(128)
                        .crossfade(false)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(artwork)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .placeholderMemoryCacheKey(artwork)
                        .memoryCacheKey(artwork)
                        .build(),
                    placeholder = painterResource(R.drawable.baseline_album_24),
                    error = painterResource(R.drawable.baseline_album_24),
                    fallback = painterResource(R.drawable.baseline_album_24),
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
    isPlaying: Boolean = false,
    onClick: (VisualTrack) -> Unit,
    onPlayNext: (TrackInfo) -> Unit,
    onAddToQueue: (TrackInfo) -> Unit,
    onAddToPlaylist: (Int) -> Unit,
    onRemoveFromQueue: ((Int) -> Unit)? = null,
    onEdit: (TrackInfo) -> Unit,
    showReorderIconStart: Boolean = false,
    showReorderIconEnd: Boolean = false,
    showTrackNum: Boolean = false,
    showArtwork: Boolean = false,
    useQueueId: Boolean = false,
    usePlaylistId: Boolean = false,
    trackNum: Int = 0,
    modifier: Modifier = Modifier,
    reorderModifier: Modifier = Modifier,
    onDelete: (List<Int>) -> Unit,
    onMove: ((List<Int>) -> Unit)? = null,
    onRemoveFromPlaylist: ((VisualTrack) -> Unit)? = null,
    ) {
    var expanded by remember { mutableStateOf(false) }

    val selectionViewModel: TrackSelectionViewModel = hiltViewModel(LocalActivity.current as ViewModelStoreOwner)
    val selectionState by selectionViewModel.selectionState.collectAsState()
    val selectionMode by selectionViewModel.selectionMode.collectAsState()
    val selection by selectionViewModel.selectionState.collectAsState()

    LaunchedEffect(Unit) {
        selectionViewModel.deletionRequestTrigger.collect {
            val idsToDelete = selection.selectedTrackIds
            onDelete(idsToDelete.toList())
        }
    }


    LaunchedEffect(Unit) {
        selectionViewModel.moveTrigger.collect {
            if (onMove != null) {
                val idsToMove = selection.selectedTrackIds
                onMove(idsToMove.toList())
            }
        }
    }


    Column {

        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else if (!useQueueId && ! usePlaylistId && track.data.trackId in selectionState.selectedTrackIds) MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.7f
                    )
                    else if (useQueueId && track.key in selectionState.selectedQueueIds) MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.7f
                    )
                    else if (usePlaylistId && track.key in selectionState.selectedPlaylistEntryIds) MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.7f
                    )
                    else Color.Transparent
                )
                .combinedClickable(
                    onClick = {
                        if (!selectionMode) onClick(track)
                        else if (!useQueueId && !usePlaylistId) selectionViewModel.toggleSelection(track.data.trackId)
                        else if (useQueueId) selectionViewModel.toggleSelection(track.key.toString())
                        else selectionViewModel.toggleSelectionPlaylist(track.key as Int)
                    },
                    onLongClick = {
                        if (!useQueueId && !usePlaylistId) selectionViewModel.toggleSelection(track.data.trackId)
                        else if (useQueueId) selectionViewModel.toggleSelection(
                            track.key.toString()
                        )
                        else selectionViewModel.toggleSelectionPlaylist(track.key as Int)
                    }
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,

            ) {
            if (showReorderIconStart) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Reorder",
                    modifier = reorderModifier
                        .padding(end = 8.dp)
                )
            }
            if (showTrackNum) {
                if (isPlaying) LiveEqualizer() else Text(
                    text = trackNum.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
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
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Reorder",
                    modifier = reorderModifier
                        .padding(start = 8.dp)
                )
            }

            Box {
                IconButton(onClick = { expanded = true }) {

                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                    )
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

                    DropdownMenuItem(
                        text = { Text("Add to Playlist") },
                        onClick = {
                            onAddToPlaylist(track.data.trackId)
                            expanded = false
                        }
                    )

                    if (onRemoveFromQueue != null) {
                        DropdownMenuItem(
                            text = { (Text("Remove from Queue")) },
                            onClick = {
                                onRemoveFromQueue(trackIndex)
                                expanded = false
                            }
                        )
                    }
                    if (onRemoveFromPlaylist != null) {
                        DropdownMenuItem(
                            text = { (Text("Remove from Playlist")) },
                            onClick = {
                                onRemoveFromPlaylist(track)
                                expanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onEdit(track.data)
                            expanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete(listOf(track.data.trackId))
                            expanded = false
                        }
                    )

                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun FastScrollbar(
    listState: LazyListState,
    totalItems: Int,
    tracks: List<TrackInfo>,
    sortOption: SortOption,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val scrollPercentage by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsVisible = layoutInfo.visibleItemsInfo.size
            if (totalItemsVisible == 0) 0f
            else {
                val firstItem = layoutInfo.visibleItemsInfo.firstOrNull()
                val index = firstItem?.index ?: 0
                index.toFloat() / totalItems.toFloat()
            }
        }
    }

    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxHeight()) {
        val bottomInset = 16.dp
        val bottomInsetPx = with(density) { bottomInset.toPx() }
        val heightPx = constraints.maxHeight.toFloat() - bottomInsetPx

        LaunchedEffect(scrollPercentage) {
            if (!isDragging) {
                offsetY = scrollPercentage * heightPx
            }
        }

        if (isDragging) {
            val percentage = (offsetY / heightPx).coerceIn(0f, 1f)
            val currentItemIndex = (percentage * (totalItems - 1)).toInt()
            val track = tracks.getOrNull(currentItemIndex)

            val label =
                when (sortOption.field) {
                    SortField.NAME -> track?.title?.firstOrNull()?.uppercase() ?: ""
                    SortField.DURATION -> track?.duration!!.toLong().formatDuration()
                    else -> ""
                }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset {
                        IntOffset(
                            x = -100,
                            y = offsetY.toInt() - 50
                        )
                    }
                    .size(64.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = label, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }

        Box(
            modifier = modifier
                .fillMaxHeight()
                .width(16.dp)
                .pointerInput(totalItems) {
                    detectVerticalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false }
                    ) { change, _ ->
                        offsetY = change.position.y.coerceIn(0f, size.height.toFloat())
                        val percentage = offsetY / size.height
                        val targetIndex =
                            (percentage * totalItems).toInt().coerceIn(0, totalItems - 1)

                        coroutineScope.launch {
                            listState.scrollToItem(targetIndex)
                        }
                    }
                }
                .drawBehind {
                    val thumbWidth = 4.dp.toPx()
                    val thumbHeight = 40.dp.toPx()
                    val xPosition = size.width - thumbWidth - 8.dp.toPx()

//                    drawRoundRect(
//                        color = Color.Gray.copy(alpha = 0.1f),
//                        topLeft = Offset(xPosition, 0f),
//                        size = Size(thumbWidth, size.height),
//                        cornerRadius = CornerRadius(2f, 2f)
//                    )

                    drawRoundRect(
                        color = if (isDragging) Color.Black else Color.Gray.copy(alpha = 0.5f),
                        topLeft = Offset(xPosition, offsetY - (thumbHeight / 2).coerceAtLeast(0f)),
                        size = Size(thumbWidth, thumbHeight),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                }
        )
    }
}


@Composable
fun TrackList(
    tracks: List<VisualTrack>,
    onClick: (VisualTrack) -> Unit,
    onPlayNext: (TrackInfo) -> Unit,
    onAddToQueue: (TrackInfo) -> Unit,
    onRemoveFromQueue: ((Int) -> Unit)? = null,
    onEdit: (TrackInfo) -> Unit,
    onAddToPlaylist: (Int) -> Unit,
    onMove: ((List<Int>) -> Unit)? = null,
    onRemoveFromPlaylist: ((VisualTrack) -> Unit)? = null,
    showReorderIconStart: Boolean = false,
    showReorderIconEnd: Boolean = false,
    showTrackNum: Boolean = false,
    showArtwork: Boolean = false,
    strictHighlight: Boolean = false,
    playlistHighlight: Boolean = false,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    reorderable: ReorderableLazyListState = rememberReorderableLazyListState(rememberLazyListState()) { from, to -> {} },
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    ) {
    val hapticFeedback = LocalHapticFeedback.current

    val activity = LocalActivity.current

    val playerViewModel: PlayerViewModel = hiltViewModel(activity as ViewModelStoreOwner)
    val currentTrack by playerViewModel.currentTrack.collectAsState()

    val currentTrackId = if (strictHighlight) currentTrack?.queueId else if (playlistHighlight) currentTrack?.playlistEntryId else currentTrack?.track?.trackId

    val trackDeletionViewModel: TrackDeletionViewModel = hiltViewModel()

    data class DeleteEvent(val ids: List<Int>)

    var pendingDeletion by remember { mutableStateOf<DeleteEvent?>(null) }
    val pendingUris by trackDeletionViewModel.pendingDeleteUris.collectAsState()


    val context = LocalContext.current
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && pendingDeletion != null) {
            trackDeletionViewModel.finalizeDeletion(pendingUris)
        } else {
            trackDeletionViewModel.clearPendingDeletion()
        }
    }


    LaunchedEffect(pendingUris) {
        if (pendingUris.isNotEmpty()) {
            val pendingIntent = trackDeletionViewModel.getDeleteIntent(context, pendingUris)
            deleteLauncher.launch(
                IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            )
        }
    }

    pendingDeletion?.let { item ->
        DeleteConfirmationDialog(
            text = "",
            onConfirm = {
                trackDeletionViewModel.prepareDeletion(item.ids)
                pendingDeletion = null
            },
            onDismiss = { pendingDeletion = null },
        )
    }

    LazyColumn(state = state,
        ) {
        if (header != null){
            item { header() }
        }
        itemsIndexed(tracks, key = { index, track -> track.key }) { id, queueTrack ->
            val track = queueTrack.data
            ReorderableItem(reorderable, key = queueTrack.key) { isDragging ->
                TrackRow(
                    artwork = track.albumArt.toString(),
                    title = track.title,
                    artist = track.artistName,
                    isPlaying = currentTrackId == queueTrack.key,
                    onClick = onClick,
                    onPlayNext = onPlayNext,
                    onAddToQueue = onAddToQueue,
                    showArtwork = showArtwork,
                    showTrackNum = showTrackNum,
                    showReorderIconStart = showReorderIconStart,
                    showReorderIconEnd = showReorderIconEnd,
                    trackNum = track.trackNum ?: 0,
                    duration = track.duration.formatDuration(),
                    track = queueTrack,
                    useQueueId = strictHighlight,
                    usePlaylistId = playlistHighlight,
                    trackIndex = id,
                    onRemoveFromQueue = onRemoveFromQueue,
                    reorderModifier = Modifier.draggableHandle(
                        onDragStarted = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                        },
                        onDragStopped = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                        },
                    ),
                    onEdit = onEdit,
                    onDelete = {ids -> pendingDeletion = DeleteEvent(ids)},
                    onMove = onMove,
                    onRemoveFromPlaylist = onRemoveFromPlaylist,
                    onAddToPlaylist = onAddToPlaylist
                )

            }
        }

        if (footer != null){
            item { footer() }
        }
    }


}