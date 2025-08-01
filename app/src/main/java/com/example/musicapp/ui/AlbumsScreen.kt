package com.example.musicapp.ui

import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicapp.data.DataSource
import com.example.musicapp.model.GridItem
import com.example.musicapp.ui.components.Grid
import com.example.musicapp.ui.theme.MusicAppTheme



@Composable
fun AlbumsGrid(
    albums: List<GridItem.AlbumItem>,
    showReleaseDate: Boolean = false,
    onClick: ((GridItem) -> Unit)? = null
){
    Grid(
        listItems = albums,
        isAlbum = true,
        showReleaseDate = showReleaseDate,
        shape = RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        onClick = onClick)
}



@Preview(showBackground = true)
@Composable
fun AlbumsPreview() {
    MusicAppTheme {
        AlbumsGrid(DataSource.albums)
    }

}