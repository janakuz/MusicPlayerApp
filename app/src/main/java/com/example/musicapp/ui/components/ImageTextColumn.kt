package com.example.musicapp.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.musicapp.R
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.model.GridItem

@Composable
fun ImageWithTextColumn(
    image: String,
    text: String,
    isAlbum: Boolean,
    onPlayNext: (GridItem) -> Unit,
    onAddToQueue: (GridItem) -> Unit,
    onAddToPlaylist: (GridItem) -> Unit,
    onEdit: (GridItem) -> Unit,
    imageModifier: Modifier = Modifier.size(100.dp),
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    imageShape: Shape = RectangleShape,
    modifier: Modifier = Modifier,
    item: GridItem? = null,
    albumArtist: String = "",
    onClick: ((GridItem) -> Unit)? = null,
    onDelete: (Int, String) -> Unit,
    onRefetch: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .then(if (onClick != null) Modifier.combinedClickable(
                onClick = {
                    if (item != null) {
                        onClick(item)
                    }
                },
                onLongClick = { expanded = true }
            ) else Modifier).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val defaultImage = if (isAlbum) painterResource(R.drawable.baseline_album_24) else painterResource(R.drawable.rounded_groups_24)
        Surface(modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(imageShape),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image)
                        .size(400)
                        .crossfade(false)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(image)
                        .memoryCacheKey(image)
                        .placeholderMemoryCacheKey(image)
                        .build(),
                    placeholder = defaultImage,
                    error = defaultImage,
                    fallback = defaultImage,
                    contentDescription = null,
                    modifier = imageModifier
                        .clip(imageShape)
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isAlbum){
            Text(
                text = albumArtist,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("Play Next") },
            onClick = {
                if (item != null) onPlayNext(item)
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text("Add to Queue") },
            onClick = { if (item != null) onAddToQueue(item)
                expanded = false
            }
        )

        DropdownMenuItem(
            text = { Text("Add to Playlist") },
            onClick = { if (item != null) onAddToPlaylist(item)
                expanded = false
            }
        )

        DropdownMenuItem(
            text = { Text("Edit") },
            onClick = { if (item != null) onEdit(item)
                expanded = false
            }
        )

        DropdownMenuItem(
            text = { Text("Delete")},
            onClick = {
                if (item != null) onDelete(item.id, item.displayName)
//                showDeleteDialog = true
                expanded = false
            }
        )

        DropdownMenuItem(
            text = {Text("Refetch Metadata")},
            onClick = {
                if (item != null) onRefetch(item.id)
                expanded = false
            }
        )

    }
}

@Composable
fun DeleteConfirmationDialog(
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
){
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Delete ${text}?") },
        text = { Text("This will permanently remove the files from your SD card.") },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
            }) { Text("Delete") }
        }
    )

}
