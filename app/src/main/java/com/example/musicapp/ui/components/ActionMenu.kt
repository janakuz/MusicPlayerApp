package com.example.musicapp.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveFromQueue
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class MenuActions(
    val onPlay: (() -> Unit)? = null,
    val onPlayNext: (() -> Unit)? = null,
    val onAddToQueue: (() -> Unit)? = null,
    val onRemoveFromQueue: (() -> Unit)? = null,
    val onAddToPlaylist: (() -> Unit)? = null,
    val onRemoveFromPlaylist: (() -> Unit)? = null,
    val onRemoveSimilar: (() -> Unit)? = null,
    val onEdit: (() -> Unit)? = null,
    val onRename: (() -> Unit)? = null,
    val onDelete: (() -> Unit)? = null,
    val onRefetchMetadata: (() -> Unit)? = null,
    val onExportM3u: (() -> Unit)? = null,
    val onMoveToAlbum: (() -> Unit)? = null,
    val onGoToArtist: (() -> Unit)? = null,
    val onGoToAlbum: (() -> Unit)? = null,
)

val MenuActions.isEmpty: Boolean
    get() = this::class.java.declaredFields
        .filter { field -> !field.isSynthetic && !field.name.contains('$') }
        .all { field ->
        try {
            field.isAccessible = true
            field.get(this) == null
        } catch (e: Exception) {
            true
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionMenu(
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    actions: MenuActions
){
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (actions.isEmpty) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No available actions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {

                MenuActionItem("Play", Icons.Default.PlayArrow, actions.onPlay, onDismiss)
                MenuActionItem(
                    "Play Next",
                    Icons.AutoMirrored.Filled.PlaylistPlay,
                    actions.onPlayNext,
                    onDismiss
                )
                MenuActionItem("Add to Queue", Icons.Default.Queue, actions.onAddToQueue, onDismiss)
                MenuActionItem(
                    "Remove from Queue",
                    Icons.Default.Remove,
                    actions.onRemoveFromQueue,
                    onDismiss
                )
                MenuActionItem(
                    "Add to Playlist",
                    Icons.AutoMirrored.Filled.PlaylistAdd,
                    actions.onAddToPlaylist,
                    onDismiss
                )
                MenuActionItem(
                    "Remove from Playlist",
                    Icons.Default.PlaylistRemove,
                    actions.onRemoveFromPlaylist,
                    onDismiss
                )

                MenuActionItem(
                    "Remove as Similar Artist",
                    Icons.Default.LinkOff,
                    actions.onRemoveSimilar,
                    onDismiss
                )


                if (actions.onEdit != null || actions.onRename != null || actions.onRefetchMetadata != null || actions.onExportM3u != null || actions.onMoveToAlbum != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    MenuActionItem("Artist", Icons.Default.Groups, actions.onGoToArtist, onDismiss)
                    MenuActionItem("Album", Icons.Default.Album, actions.onGoToAlbum, onDismiss)
                    MenuActionItem("Edit Info", Icons.Default.Edit, actions.onEdit, onDismiss)
                    MenuActionItem("Rename", Icons.Default.Edit, actions.onRename, onDismiss)
                    MenuActionItem(
                        "Refetch Metadata",
                        Icons.Default.Refresh,
                        actions.onRefetchMetadata,
                        onDismiss
                    )
                    MenuActionItem(
                        "Export .m3u",
                        Icons.Default.FileDownload,
                        actions.onExportM3u,
                        onDismiss
                    )
                    MenuActionItem(
                        "Move to Album",
                        Icons.AutoMirrored.Filled.DriveFileMove,
                        actions.onMoveToAlbum,
                        onDismiss
                    )
                }

                actions.onDelete?.let {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ListItem(
                        headlineContent = {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        modifier = Modifier.clickable {
                            it()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun MenuActionItem(
    label: String,
    icon: ImageVector,
    action: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    action?.let {
        ListItem(
            headlineContent = { Text(label) },
            leadingContent = { Icon(icon, contentDescription = null) },
            modifier = Modifier.clickable {
                it()
                onDismiss()
            }
        )
    }
}