package com.example.musicapp.ui.components

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicapp.ui.theme.MusicAppTheme
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import com.example.musicapp.model.GridItem


@Composable
fun Grid(
    listItems: List<GridItem>,
    shape: Shape,
    isAlbum: Boolean,
    onPlayNext: (GridItem) -> Unit,
    onEdit: (GridItem) -> Unit,
    onAddToQueue: (GridItem) -> Unit,
    onAddToPlaylist: (GridItem) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    showReleaseDate: Boolean = false,
    onClick: ((GridItem) -> Unit)? = null,
    header: (@Composable () -> Unit)? = null,
    onDelete: (Int, String) -> Unit,
    onRefetch: (Int) -> Unit,
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {

        if (header != null){
            item(span = {
                GridItemSpan(maxLineSpan)
            }) { header() }
        }


        items(listItems, key = {it.id}) { item ->
            var artist = ""
            if (isAlbum) {
    //            if (!showReleaseDate)
     //               artist = stringResource((item as GridItem.AlbumItem).artist)
    //            else
                    artist = (item as GridItem.AlbumItem).releaseYear
            }
            Box( modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center) {
                ImageWithTextColumn(
                    image = item.imageRes,
                    text = item.displayName,
                    isAlbum = isAlbum,
                    imageModifier = Modifier.size(120.dp),
                    imageShape = shape,
                    albumArtist = artist,
                    textStyle = textStyle,
                    onClick = onClick,
                    onPlayNext = onPlayNext,
                    onAddToQueue = onAddToQueue,
                    onAddToPlaylist = onAddToPlaylist,
                    onEdit = onEdit,
                    item = item,
                    onDelete = onDelete,
                    onRefetch = onRefetch
                )
            }
        }
    }
}





@Preview(showBackground = true)
@Composable
fun GridPreview() {
    MusicAppTheme {
//        Grid(
//            listItems = DataSource.albums,
//            isAlbum = true,
//            showReleaseDate = false,
//            shape = RoundedCornerShape(
//                topStart = 4.dp,
//                topEnd = 4.dp,
//                bottomStart = 4.dp,
//                bottomEnd = 4.dp),
//            textStyle = MaterialTheme.typography.bodyMedium)
    }

}