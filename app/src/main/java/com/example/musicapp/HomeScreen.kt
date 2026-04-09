package com.example.musicapp

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.musicapp.ui.components.FilterDrawerContent
import com.example.musicapp.ui.components.LibraryTopBar
import com.example.musicapp.ui.screens.AllTracksScreen
import com.example.musicapp.ui.screens.ArtistView
import com.example.musicapp.ui.screens.AlbumView
import com.example.musicapp.ui.screens.AllArtistsScreen
import com.example.musicapp.ui.components.NowPlayingBar
import com.example.musicapp.ui.components.SelectionTopBar
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.screens.AlbumEditScreen
import com.example.musicapp.ui.screens.AllAlbumsScreen
import com.example.musicapp.ui.screens.ArtistEditScreen
import com.example.musicapp.ui.screens.NowPlayingWithQueue
import com.example.musicapp.ui.screens.ScanLibraryScreen
import com.example.musicapp.ui.screens.SearchContent
import com.example.musicapp.ui.screens.SearchResultsScreen
import com.example.musicapp.ui.screens.TrackEditScreen
import com.example.musicapp.ui.viewmodels.FilterViewModel
import com.example.musicapp.ui.viewmodels.PlayerViewModel
import com.example.musicapp.ui.viewmodels.TrackSelectionViewModel
import kotlinx.coroutines.launch
import com.example.musicapp.data.repository.SearchResult




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
fun MusicApp(playerViewModel: PlayerViewModel, isLibraryInitialized: Boolean) {
    val navController = rememberNavController()

    val startDest = if (isLibraryInitialized) HomeScreen.Artists.name else HomeScreen.Scan.name

    val tabs = listOf(HomeScreen.Artists, HomeScreen.Albums, HomeScreen.Tracks, HomeScreen.Scan)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    var artistSort by remember { mutableStateOf<SortOption?>(null) }
    var albumSort by remember { mutableStateOf<SortOption?>(null) }
    var trackSort by remember { mutableStateOf<SortOption?>(null) }
    var artistDetailSort by remember { mutableStateOf<SortOption?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val editRoutes = listOf<String>("artist/edit", "album/edit", "track/edit")

    val selectionViewModel: TrackSelectionViewModel = hiltViewModel()

    val selectionMode by selectionViewModel.selectionMode.collectAsState()

    val filterViewModel: FilterViewModel = hiltViewModel()


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val draftFilter by filterViewModel.draftFilter.collectAsState()
    val filterCount by filterViewModel.potentialMatches.collectAsState()
    val filterResults by filterViewModel.filteredAlbums.collectAsState()
    val filterDefaults by filterViewModel.filterDefaults.collectAsState()




    LaunchedEffect(Unit) {
        playerViewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                FilterDrawerContent(
                    draft = draftFilter,
                    potentialCount = filterCount,
                    filterDefaults = filterDefaults,
                    onDraftChange = {filter -> filterViewModel.updateDraft(filter)},
                    onApply = {
                        filterViewModel.applyFilters()
                        scope.launch { drawerState.close() }
                        navController.navigate("filter_results")
                    }
                )
            }
        }
    )

    {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            snackbarHost = {
                SnackbarHost(
                    snackbarHostState,
//                    modifier = Modifier
//                        .padding(bottom = 600.dp)
                )
            },
            topBar = {
                Column {
                    val selectedTabIndex =
                        tabs.indexOfFirst { it.name == currentRoute }
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    if (currentRoute != "nowPlaying" && !selectionMode && editRoutes.all {
                            currentRoute?.startsWith(
                                it
                            ) == false
                        } && currentRoute?.startsWith("search") == false) {
                        LibraryTopBar(
                            currentScreen = routeToLibraryScreen(currentRoute),
                            onFilterClick = { scope.launch { drawerState.open() } },
                            onSearchClick = {
                                val artistId =
                                    navBackStackEntry?.arguments?.getString("artistId") ?: ""
                                val albumId =
                                    navBackStackEntry?.arguments?.getString("albumId") ?: ""

                                when {
                                    artistId != "" -> {
                                        navController.navigate("search?scopeType=ARTIST&scopeId=$artistId")
                                    }

                                    albumId != "" -> {
                                        navController.navigate("search?scopeType=ALBUM&scopeId=$albumId")
                                    }

                                    else -> navController.navigate("search")
                                }
                            },
                            onSortClick = { sort ->
                                when (currentRoute) {
                                    HomeScreen.Artists.name -> artistSort = sort
                                    HomeScreen.Albums.name -> albumSort = sort
                                    HomeScreen.Tracks.name -> trackSort = sort
                                    "artist/{artistId}" -> artistDetailSort = sort
                                }
                            },
                            onMenuClick = { },
                            showBack = (selectedTabIndex < 0),
                            onBack = if (selectedTabIndex < 0) ({ navController.popBackStack() }) else null,
                            title = if (selectedTabIndex >= 0) currentRoute else null,
                        )
                    }
                    if (selectionMode) {
                        val selection by selectionViewModel.selectionState.collectAsState()
                        val moveEnabled by selectionViewModel.moveEnabled.collectAsState()
                        SelectionTopBar(
                            count = selection.count,
                            onClear = { selectionViewModel.clearSelection() },
                            onPlayNext = { playerViewModel.playNextListIds(selection.selectedTrackIds) },
                            onAddToQueue = { playerViewModel.addToQueueListIds(selection.selectedTrackIds) },
                            onRemoveFromQueue = { playerViewModel.removeFromQueue(selection.selectedQueueIds) },
                            isQueueScreen = (currentRoute == "nowPlaying"),
                            onDelete = { selectionViewModel.requestDeletionOfSelected() },
                            onMove = { selectionViewModel.requestMove() },
                            moveEnabled = moveEnabled
                        )
                    }
                    if (selectedTabIndex >= 0 && currentRoute != HomeScreen.Scan.name && !selectionMode) {
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
                    onClick = { navController.navigate("nowPlaying") },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
//        }

        ) {

                innerPadding ->

            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.padding(innerPadding)
            ) {


//            navigation(
//                route = "library",
//                startDestination = HomeScreen.Artists.name
//            ) {

                composable(route = HomeScreen.Artists.name) {
                    AllArtistsScreen(
                        sortRequest = artistSort,
                        onClick = { artist ->
                            navController.navigate("artist/${artist.id}")
                            {
                                launchSingleTop = true
                                //                            restoreState = true
                            }
                        },
                        onPlayNext = { artist -> playerViewModel.playNextArtist(artist.id) },
                        onAddToQueue = { artist -> playerViewModel.addToQueueArtist(artist.id) },
                        onEdit = { artist -> navController.navigate("artist/edit/${artist.id}") }
                    )
                }

                composable(route = HomeScreen.Albums.name) {
                    AllAlbumsScreen(
                        sortRequest = albumSort,
                        onClick = { album ->
                            navController.navigate("album/${album.id}") {
                                launchSingleTop = true
                                //                              restoreState = true
                            }
                        },
                        onPlayNext = { album -> playerViewModel.playNextAlbum(album.id) },
                        onAddToQueue = { album -> playerViewModel.addToQueueAlbum(album.id) },
                        onEdit = { album -> navController.navigate("album/edit/${album.id}/all_albums",) }
                    )
                }

                composable(route = HomeScreen.Tracks.name) {
                    AllTracksScreen(
                        sortRequest = trackSort,
                        onClick = { track, tracks ->
                            playerViewModel.playTracks(tracks, track)
                            navController.navigate("nowPlaying")
                        },
                        onPlayNext = { track ->
                            playerViewModel.playNext(track)
                        },
                        onAddToQueue = { track ->
                            playerViewModel.addToQueue(track)
                        },
                        onEdit = { track -> navController.navigate("track/edit/${track.trackId}") }
                    )
                }

                composable("artist/{artistId}") {
                    ArtistView(
                        onAlbumClick = { album ->
                            navController.navigate("album/${album.id}") {
                                launchSingleTop = true
//                                restoreState = true
                            }
                        },
                        sortRequest = artistDetailSort,
                        onPlayNext = { album -> playerViewModel.playNextAlbum(album.id) },
                        onAddToQueue = { album -> playerViewModel.addToQueueAlbum(album.id) },
                        onEdit = { album -> navController.navigate("album/edit/${album.id}/artist_view") }
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
                        onPlayNext = { track -> playerViewModel.playNext(track) },
                        onAddToQueue = { track -> playerViewModel.addToQueue(track) },
                        onEdit = { track -> navController.navigate("track/edit/${track.trackId}") }
                    )
                }

                composable("artist/edit/{artistId}") {
                    ArtistEditScreen(
                        onNavigateBack = {
                            navController.navigate(HomeScreen.Artists.name) {
                                popUpTo(HomeScreen.Artists.name)
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("album/edit/{albumId}/{source}") { backStackEntry ->
                    val source = backStackEntry.arguments?.getString("source") ?: "all_albums"
                    val albumId = backStackEntry.arguments?.getString("albumId")
                    Log.d("back", source)
                    AlbumEditScreen(
                        onNavigateBack = { id ->
                            when (source) {
                                "all_albums" -> navController.popBackStack()
                                "artist_view" -> navController.navigate(HomeScreen.Artists.name)
//                                if (id != null) navController.navigate("artists/${id}"){
//                                popUpTo("album/edit/{albumId}/{source}"){
//                                    inclusive=true
//                                }
//                                launchSingleTop = true
//                            }
//                                             else navController.popBackStack()
                                else -> navController.popBackStack()
                            }
                        }
                    )
                }

                composable("track/edit/{trackId}") {
                    TrackEditScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "search?scopeType={scopeType}&scopeId={scopeId}",
                    arguments = listOf(
                        navArgument("scopeType") { nullable = true; defaultValue = null },
                        navArgument("scopeId") { type = NavType.IntType; defaultValue = -1 },
                    )
                ) {

                    SearchResultsScreen(
                        onArtistClick = { id -> navController.navigate("artist/$id") },
                        onAlbumClick = { id -> navController.navigate("album/$id") },
                        onTrackClick = { tracks, track ->
                            playerViewModel.playTracks(
                                tracks,
                                track
                            )
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("filter_results") {
                    SearchContent(
                        results = SearchResult(albums = filterResults),
                        onArtistClick = { id -> navController.navigate("artist/$id") },
                        onAlbumClick = { id -> navController.navigate("album/$id") },
                        onTrackClick = { tracks, track ->
                            playerViewModel.playTracks(
                                tracks,
                                track
                            )
                        },
                        padding = PaddingValues(0.dp)
                    )
                }


                //          }

                composable(HomeScreen.Scan.name) {
                    ScanLibraryScreen(
                        isInitial = startDest == HomeScreen.Scan.name
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
                            }
                        },
                        onBack = { navController.popBackStack() },
                        onArtistClick = { artistId ->
                            navController.navigate("artist/${artistId}")
                        },
                        onAlbumClick = { albumId ->
                            navController.navigate("album/${albumId}")
                        },
                        onEdit = { track -> navController.navigate("track/edit/${track.trackId}") }
                    )
                }

            }


        }
    }
}


