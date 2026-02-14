package com.example.musicapp.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.model.GridItem
import com.example.musicapp.ui.components.Grid
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.ui.viewmodels.AllAlbumsViewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import com.example.musicapp.HomeScreen
import com.example.musicapp.data.dto.AlbumInfo
import com.example.musicapp.data.dto.TrackInfo
import com.example.musicapp.data.entity.Album
import com.example.musicapp.ui.components.SortOption


@Composable
fun AlbumsGrid(
    albums: List<AlbumInfo>,
    onPlayNext: (GridItem) -> Unit,
    onAddToQueue: (GridItem) -> Unit,
    showReleaseDate: Boolean = false,
    onClick: ((GridItem) -> Unit)? = null,
    header: (@Composable () -> Unit)? = null,
    ){


    val items = albums.map { album ->
        GridItem.AlbumItem(
            id = album.albumId,
            displayName = album.title,
            imageRes = album.image.toString(),
            releaseYear = album.releaseDate.orEmpty(),
            numTracks = 0,
            duration = album.duration.toInt(),
        )
    }

    Grid(
        listItems = items,
        isAlbum = true,
        showReleaseDate = showReleaseDate,
        onPlayNext = onPlayNext,
        onAddToQueue = onAddToQueue,
        shape = RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        onClick = onClick,
        header = header,
    )
}


@Composable
fun AllAlbumsScreen(
    onClick: (GridItem) -> Unit,
    onPlayNext: (GridItem) -> Unit,
    onAddToQueue: (GridItem) -> Unit,
    sortRequest: SortOption?,
    ) {
    val albumViewModel: AllAlbumsViewModel = hiltViewModel()

    LaunchedEffect(sortRequest) {
        sortRequest?.let {
            albumViewModel.setSort(it)
        }
    }


    val albumsState by albumViewModel.albumListUiState.collectAsState()
    val albums = albumsState.albums

    AlbumsGrid(
        albums,
        onPlayNext = onPlayNext,
        onAddToQueue = onAddToQueue,
        onClick = onClick,
    )
}


@Preview(showBackground = true)
@Composable
fun AlbumsPreview() {
    MusicAppTheme {
//        AlbumsGrid(DataSource.albums)
    }

}