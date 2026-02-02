package com.example.musicapp

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.musicapp.ui.components.LibraryTopBar
import com.example.musicapp.ui.screens.AllTracksScreen
import com.example.musicapp.ui.screens.ArtistView
import com.example.musicapp.ui.screens.AlbumView
import com.example.musicapp.ui.screens.AllArtistsScreen
import com.example.musicapp.ui.components.NowPlayingBar
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.screens.AllAlbumsScreen
import com.example.musicapp.ui.screens.NowPlayingWithQueue
import com.example.musicapp.ui.screens.ScanLibraryScreen
import com.example.musicapp.ui.viewmodels.BackgroundScanViewModel
import com.example.musicapp.ui.viewmodels.PlayerViewModel

enum class HomeScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Artists(title = R.string.artists),
    Albums(title = R.string.albums),
    Playlists(title = R.string.playlists),
    Tracks(title = R.string.tracks),
    NowPLaying(title = R.string.app_name),
    Scan(title=R.string.scan)
}

enum class LibraryScreen {
    ARTISTS, ALBUMS, TRACKS, ALBUM_DETAIL, OTHER
}

fun routeToLibraryScreen(route: String?): LibraryScreen =
    when {
        route?.startsWith(HomeScreen.Artists.name) == true -> LibraryScreen.ARTISTS
        route?.startsWith(HomeScreen.Albums.name) == true -> LibraryScreen.ALBUMS
        route?.startsWith(HomeScreen.Tracks.name) == true -> LibraryScreen.TRACKS
        route?.startsWith("artist/{artistId}") == true -> LibraryScreen.ALBUM_DETAIL
        else -> LibraryScreen.OTHER
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicApp(playerViewModel: PlayerViewModel) {
    val navController = rememberNavController()

    val tabs = listOf(HomeScreen.Artists, HomeScreen.Albums, HomeScreen.Tracks, HomeScreen.Scan)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    var artistSort by remember { mutableStateOf<SortOption?>(null) }
    var albumSort by remember { mutableStateOf<SortOption?>(null) }
    var trackSort by remember { mutableStateOf<SortOption?>(null) }
    var artistDetailSort by remember { mutableStateOf<SortOption?>(null) }


    Scaffold(
        topBar = {
            Column {
                LibraryTopBar(
                    currentScreen = routeToLibraryScreen(currentRoute),
                    onSearchClick = { },
                    onSortClick = { sort ->
                        when (currentRoute) {
                            HomeScreen.Artists.name -> artistSort = sort
                            HomeScreen.Albums.name -> albumSort = sort
                            HomeScreen.Tracks.name -> trackSort = sort
                            "artist/{artistId}" -> artistDetailSort = sort
                        }
                    },
                    onMenuClick = { }
                )
                val selectedTabIndex =
                    tabs.indexOfFirst { it.name == currentRoute }.takeIf { it >= 0 } ?: 0
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, screen ->
                        Tab(
                            text = { Text(stringResource(screen.title)) },
                            selected = index == selectedTabIndex,
                            onClick = {
                                navController.navigate(screen.name) {
                                    popUpTo(screen.name)
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },

        bottomBar = {
//            AnimatedVisibility(
//                visible = currentRoute != "nowPlaying",
//                enter = slideInVertically(initialOffsetY = { it }),
//                exit = slideOutVertically(targetOffsetY = { it })
//            ) {// }
//            if (currentRoute != "nowPlaying") {
                NowPlayingBar(
                    playerViewModel = playerViewModel,
                    currentRoute = currentRoute,
                    onClick = { navController.navigate("nowPlaying") }
                )
            }
//        }

    ) {

        innerPadding ->

        NavHost(
            navController = navController,
            startDestination = HomeScreen.Artists.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = HomeScreen.Artists.name) {
                AllArtistsScreen(
                    sortRequest = artistSort,
                    onClick = {artist ->
                        navController.navigate("artist/${artist.id}")
                    },
                    onPlayNext = { artist -> playerViewModel.playNextArtist(artist.id) },
                    onAddToQueue = { artist -> playerViewModel.addToQueueArtist(artist.id) }
                )
            }

            composable(route = HomeScreen.Albums.name) {
                AllAlbumsScreen(
                    sortRequest = albumSort,
                    onClick = { album ->
                        navController.navigate("album/${album.id}")
                    },
                    onPlayNext = { album -> playerViewModel.playNextAlbum(album.id)},
                    onAddToQueue = { album -> playerViewModel.addToQueueAlbum(album.id)}
                )
            }

            composable(route = HomeScreen.Tracks.name) {
                AllTracksScreen(
                    sortRequest = trackSort,
                    onClick = {
                        track, tracks -> playerViewModel.playTracks(tracks, track)
                        navController.navigate("nowPlaying")
                    },
                    onPlayNext = {
                        track -> playerViewModel.playNext(track)
                    },
                    onAddToQueue = {
                        track -> playerViewModel.addToQueue(track)
                    }
                )
            }

            composable("scan") {
                ScanLibraryScreen()
            }

            composable("artist/{artistId}") {
                ArtistView(
                    onAlbumClick = {album ->
                        navController.navigate("album/${album.id}")
                    },
                    sortRequest = artistDetailSort,
                    onPlayNext = { album -> playerViewModel.playNextAlbum(album.id)},
                    onAddToQueue = { album -> playerViewModel.addToQueueAlbum(album.id)}
                )
            }


            composable("nowPlaying") { backStackEntry ->
                 NowPlayingWithQueue(
                     playerViewModel,
                     onTrackClick = { track ->
                         playerViewModel.playTrack(track)
                         navController.navigate("nowPlaying")
                         {
                             launchSingleTop = true
                         } },
                     onBack = { navController.popBackStack() }
                 )
            }

            composable("album/{albumId}") {
                AlbumView(
                    onTrackClick = { track, tracks ->
                        playerViewModel.playTracks(tracks, track)
                        navController.navigate("nowPlaying")
                        {
                            launchSingleTop = true
                        }
                    },
                    onPlayNext = { track -> playerViewModel.playNext(track)},
                    onAddToQueue = { track -> playerViewModel.addToQueue(track)}
                )
            }

        }
    }
}

