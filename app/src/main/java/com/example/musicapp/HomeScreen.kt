package com.example.musicapp

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.musicapp.ui.screens.AllTracksScreen
import com.example.musicapp.ui.screens.ArtistView
import com.example.musicapp.ui.screens.AlbumView
import com.example.musicapp.ui.screens.AllArtistsScreen
import com.example.musicapp.ui.components.NowPlayingBar
import com.example.musicapp.ui.screens.AllAlbumsScreen
import com.example.musicapp.ui.screens.NowPlayingWithQueue
import com.example.musicapp.ui.screens.ScanLibraryScreen
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicApp(playerViewModel: PlayerViewModel) {
    val navController = rememberNavController()

    val tabs = listOf(HomeScreen.Artists, HomeScreen.Albums, HomeScreen.Tracks, HomeScreen.Scan)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

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
                                popUpTo(screen.name)
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },

        bottomBar = {
            if (currentRoute != "nowPlaying") {
                NowPlayingBar(
                    playerViewModel = playerViewModel,
                    onClick = { navController.navigate("nowPlaying") }
                )
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
                AllArtistsScreen(
                    onClick = {artist ->
                        navController.navigate("artist/${artist.id}")
                    })
            }

            composable(route = HomeScreen.Albums.name) {
                AllAlbumsScreen(
                    onClick = { album ->
                        navController.navigate("album/${album.id}")
                })
            }

            composable(route = HomeScreen.Tracks.name) {
                AllTracksScreen(
                onClick = {
                    track, tracks -> playerViewModel.playTracks(tracks, track)
                    navController.navigate("nowPlaying")
                })
            }

            composable("scan") {
                ScanLibraryScreen()
            }

            composable("artist/{artistId}") {
                ArtistView(
                    onAlbumClick = {album ->
                        navController.navigate("album/${album.id}")
                    }
                )
            }


            composable("nowPlaying") { backStackEntry ->
                 NowPlayingWithQueue(
                     playerViewModel,
                     onTrackClick = { track, tracks ->
                         playerViewModel.playTracks(tracks, track)
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
                    })
            }

        }
    }
}