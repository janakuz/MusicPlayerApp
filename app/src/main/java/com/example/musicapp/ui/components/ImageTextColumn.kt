package com.example.musicapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.musicapp.model.GridItem

@Composable
fun ImageWithTextColumn(
    image: String,
    text: String,
    isAlbum: Boolean,
    imageModifier: Modifier = Modifier.size(100.dp),
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    imageShape: Shape = RectangleShape,
    modifier: Modifier = Modifier,
    item: GridItem? = null,
    albumArtist: String = "",
    onClick: ((GridItem) -> Unit)? = null
) {
    Column(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable {
                if (item != null) {
                    onClick(item)
                }
            } else Modifier).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .crossfade(true)
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
}
