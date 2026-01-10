package com.example.musicapp.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.model.GridItem

@Composable
fun ImageWithTextColumn(
    image: String,
    text: String,
    isAlbum: Boolean,
    onPlayNext: (GridItem) -> Unit,
    onAddToQueue: (GridItem) -> Unit,
    imageModifier: Modifier = Modifier.size(100.dp),
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    imageShape: Shape = RectangleShape,
    modifier: Modifier = Modifier,
    item: GridItem? = null,
    albumArtist: String = "",
    onClick: ((GridItem) -> Unit)? = null
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

//            .then(if (onClick != null) Modifier.clickable {
//                if (item != null) {
//                    onClick(item)
//                }
//            } else Modifier).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .placeholderMemoryCacheKey(image)
                    .build(),
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
    }
}
