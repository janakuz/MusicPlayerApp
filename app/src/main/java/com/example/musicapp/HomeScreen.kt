package com.example.musicapp

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.musicapp.data.DataSource
import com.example.musicapp.ui.AlbumsGrid
import com.example.musicapp.ui.AllTracksScreen
import com.example.musicapp.ui.ArtistView
import com.example.musicapp.ui.AlbumView
import com.example.musicapp.ui.ArtistsGrid
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.NowPlayingWithQueue

enum class HomeScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Artists(title = R.string.artists),
    Albums(title = R.string.albums),
    Playlists(title = R.string.playlists),
    Tracks(title = R.string.tracks),
    NowPLaying(title = R.string.app_name)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicAppBar(
    currentScreen: HomeScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicApp(playerViewModel: PlayerViewModel) {
    val navController = rememberNavController()

    val tabs = listOf(HomeScreen.Artists, HomeScreen.Albums, HomeScreen.Tracks)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
//    val backStackEntry by navController.currentBackStackEntryAsState()
 //   val currentScreen = HomeScreen.valueOf(
 //       backStackEntry?.destination?.route ?: HomeScreen.Start.name
 //   )

    Scaffold(
topBar = {
    val selectedTabIndex = tabs.indexOfFirst { it.name == currentRoute }.takeIf { it >= 0 } ?: 0
    TabRow(selectedTabIndex = selectedTabIndex) {
        tabs.forEachIndexed { index, screen ->
            Tab(
                text = { Text(stringResource(screen.title)) },
                selected = index == selectedTabIndex,
                onClick = {
                    navController.navigate(screen.name) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
    ) {

        innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeScreen.Artists.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = HomeScreen.Artists.name) {
                val artistViewModel: ArtistViewModel = hiltViewModel()

                ArtistsGrid(artistViewModel,
                    //uiState.artistList,
                    onClick = {artist ->
                navController.navigate("artist/${artist.id}")
                    })
            }
            composable(route = HomeScreen.Albums.name) {
                AlbumsGrid(DataSource.albums,
                    onClick = {album ->
                    navController.navigate("album/${album.id}")
                })
            }
            composable(route = HomeScreen.Tracks.name) {
                AllTracksScreen(DataSource.tracks)
            }

//            composable("artist/{artistId}") { backStackEntry ->
//                val artistId = backStackEntry.arguments?.getString("artistId")?.toIntOrNull()
//                val artist = DataSource.artists.find { it.id == artistId }
//
//                if (artist != null) {
//                    ArtistView(name= artist.displayName,
//                        bio = artist.description,
//                        image = artist.imageRes,
//                        albums = DataSource.albums,
//                        onAlbumClick = {album ->
//                            navController.navigate("album/${album.id}")
//                        }
//                    )
//                }
//            }

            composable("nowPlaying") { backStackEntry ->
                val track by playerViewModel.currentTrack.collectAsState()
                val tracks by playerViewModel.queue.collectAsState()

                val scope = rememberCoroutineScope()
                val sheetState = rememberBottomSheetScaffoldState()


                if (track != null) {

                    NowPlayingWithQueue(playerViewModel, onTrackClick = {track ->
                        playerViewModel.playTracks(DataSource.tracks, track)
                        navController.navigate("nowPlaying")
                        {
                            launchSingleTop = true
                        }
                    }, )

//                    NowPlayingView(
//                        name = stringResource(track!!.album),
//                        artist = stringResource(track!!.artist),
//                        image = painterResource(track!!.art),
//                        track1 = track!!,
//                        playerViewModel = playerViewModel,
//                        tracks = tracks,
//                        onQueueClick = {
//                            scope.launch {
//                                sheetState.bottomSheetState.expand()
//                            }
//                        }
//                    )
                }
                else {
                    // Optionally show a loading spinner or placeholder
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            composable("album/{albumId}") { backStackEntry ->
                val albumId = backStackEntry.arguments?.getString("albumId")?.toIntOrNull()
                val album = DataSource.albums.find { it.id == albumId }

                if (album != null) {
                    AlbumView(name = album.displayName,
                        artist = stringResource(album.artist),
                        releaseDate = stringResource(album.releaseYear),
                        image = album.imageRes,
                        tracks= DataSource.tracks,
                        numTracks = album.numTracks.toString(),
                        duration = album.duration.toString(),
                        onTrackClick = {track ->
                            playerViewModel.playTracks(DataSource.tracks, track)
                            navController.navigate("nowPlaying")
                            {
                                launchSingleTop = true
                            }
                        })
                }
            }

        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun MusicAppPreview() {
//    MusicAppTheme {
//        MusicApp()
//    }
//}