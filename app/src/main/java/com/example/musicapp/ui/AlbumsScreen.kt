package com.example.musicapp.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.model.GridItem
import com.example.musicapp.ui.components.Grid
import com.example.musicapp.ui.theme.MusicAppTheme



@Composable
fun AlbumsGrid(
//    albumViewModel: AlbumViewModel,
    albums: List<AlbumInfo>,
    showReleaseDate: Boolean = false,
    onClick: ((GridItem) -> Unit)? = null
){
    val items = albums.map { album ->
        GridItem.AlbumItem(
            id = album.albumId,
            displayName = album.title,
            imageRes = album.image.toString(),
            releaseYear = album.releaseDate.orEmpty(),
            numTracks = 0,
            duration = album.duration.toInt(),
     //       artist = 0
        )
    }

    Grid(
        listItems = items,
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
//        AlbumsGrid(DataSource.albums)
    }

}