package com.example.musicapp.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.musicapp.R
import com.example.musicapp.data.repository.SearchResult
import com.example.musicapp.ui.components.AddToPlaylistDialog
import com.example.musicapp.ui.components.CreatePlaylistDialog
import com.example.musicapp.ui.components.FilterDrawerContent
import com.example.musicapp.ui.components.LibraryTopBar
import com.example.musicapp.ui.components.NowPlayingBar
import com.example.musicapp.ui.components.SelectionTopBar
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.screens.AboutPage
import com.example.musicapp.ui.screens.AlbumEditScreen
import com.example.musicapp.ui.screens.AlbumView
import com.example.musicapp.ui.screens.AllAlbumsScreen
import com.example.musicapp.ui.screens.AllArtistsScreen
import com.example.musicapp.ui.screens.AllTracksScreen
import com.example.musicapp.ui.screens.ArtistEditScreen
import com.example.musicapp.ui.screens.ArtistView
import com.example.musicapp.ui.screens.CountriesScreen
import com.example.musicapp.ui.screens.CountryDetailScreen
import com.example.musicapp.ui.screens.GenreDetailScreen
import com.example.musicapp.ui.screens.GenresScreen
import com.example.musicapp.ui.screens.NowPlayingWithQueue
import com.example.musicapp.ui.screens.PlaylistDetailScreen
import com.example.musicapp.ui.screens.PlaylistEditScreen
import com.example.musicapp.ui.screens.PlaylistsScreen
import com.example.musicapp.ui.screens.ScanLibraryScreen
import com.example.musicapp.ui.screens.SearchContent
import com.example.musicapp.ui.screens.SearchResultsScreen
import com.example.musicapp.ui.screens.SettingsScreen
import com.example.musicapp.ui.screens.TrackEditScreen
import com.example.musicapp.ui.viewmodels.FilterViewModel
import com.example.musicapp.ui.viewmodels.PlayerViewModel
import com.example.musicapp.ui.viewmodels.PlaylistViewModel
import com.example.musicapp.ui.viewmodels.SelectSource
import com.example.musicapp.ui.viewmodels.TrackSelectionViewModel
import com.example.musicapp.util.toTitleCase
import kotlinx.coroutines.launch


enum class HomeScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Artists(title = R.string.artists),
    Albums(title = R.string.albums),
    Playlists(title = R.string.playlists),
    Tracks(title = R.string.tracks),
    NowPLaying(title = R.string.app_name),
    Scan(title = R.string.scan),
    Settings(title = R.string.settings),
    Genres(title=R.string.genres),
    Countries(title = R.string.countries)
}

enum class LibraryScreen {
    ARTISTS, ALBUMS, TRACKS, ALBUM_DETAIL, PLAYLISTS, GENRES, COUNTRIES, OTHER
}

fun routeToLibraryScreen(route: String?): LibraryScreen =
    when {
        route?.startsWith(HomeScreen.Artists.name) == true -> LibraryScreen.ARTISTS
        route?.startsWith(HomeScreen.Albums.name) == true -> LibraryScreen.ALBUMS
        route?.startsWith(HomeScreen.Tracks.name) == true -> LibraryScreen.TRACKS
        route?.startsWith("artist/{artistId}") == true -> LibraryScreen.ALBUM_DETAIL
        route?.startsWith(HomeScreen.Playlists.name) == true -> LibraryScreen.PLAYLISTS
        route?.startsWith(HomeScreen.Genres.name) == true -> LibraryScreen.GENRES
        route?.startsWith(HomeScreen.Countries.name) == true -> LibraryScreen.COUNTRIES
        else -> LibraryScreen.OTHER
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicApp(playerViewModel: PlayerViewModel, isLibraryInitialized: Boolean) {
    val navController = rememberNavController()

    val startDest = if (isLibraryInitialized) HomeScreen.Artists.name else HomeScreen.Scan.name

    val tabs = listOf(HomeScreen.Artists, HomeScreen.Albums, HomeScreen.Tracks, HomeScreen.Genres,
        HomeScreen.Countries)
    val noBack = tabs + listOf(HomeScreen.Scan, HomeScreen.Playlists, HomeScreen.Settings)
//    HomeScreen.Scan,HomeScreen.Playlists)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    var artistSort by remember { mutableStateOf<SortOption?>(null) }
    var albumSort by remember { mutableStateOf<SortOption?>(null) }
    var trackSort by remember { mutableStateOf<SortOption?>(null) }
    var artistDetailSort by remember { mutableStateOf<SortOption?>(null) }
    var playlistsSort by remember { mutableStateOf<SortOption?>(null) }
    var genresSort by remember { mutableStateOf<SortOption?>(null) }
    var countriesSort by remember { mutableStateOf<SortOption?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var showFilterSheet by remember { mutableStateOf(false) }

    val editRoutes = listOf<String>(
        "artist/edit",
        "album/edit",
        "track/edit",
        "playlist/edit",
        "playlist/create"
    )

    val selectionViewModel: TrackSelectionViewModel = hiltViewModel()

    val selectionMode by selectionViewModel.selectionMode.collectAsState()

    val playlistViewModel: PlaylistViewModel = hiltViewModel()
    val createInfo by playlistViewModel.createInfo.collectAsState()
    val allPlaylists by playlistViewModel.playlistsForAdd.collectAsState()
    val addState by playlistViewModel.addToPlaylistState.collectAsState()
    val playlistUiStates by playlistViewModel.playlists.collectAsState()

    val filterViewModel: FilterViewModel = hiltViewModel()


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val draftFilter by filterViewModel.draftFilter.collectAsState()
    val filterAlbumCount by filterViewModel.potentialAlbumMatches.collectAsState()
    val filterAlbumResults by filterViewModel.filteredAlbums.collectAsState()
    val filterArtistCount by filterViewModel.potentialArtistMatches.collectAsState()
    val filterArtistResults by filterViewModel.filteredArtists.collectAsState()
    val filterDefaults by filterViewModel.filterDefaults.collectAsState()
    val labelSuggestions by filterViewModel.labelSuggestions.collectAsState()
    val sliderInteractionSource = remember { MutableInteractionSource() }
    val genreSuggestions by filterViewModel.genreSuggestions.collectAsState()



    LaunchedEffect(Unit) {
        playerViewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }

    }

    LaunchedEffect(Unit) {
        playlistViewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }

    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.requiredWidth(300.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "My Music Library",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleSmall
                )

                NavigationDrawerItem(
                    label = { Text("Library") },
                    selected = currentRoute in listOf(
                        HomeScreen.Artists.name, HomeScreen.Albums.name,
                        HomeScreen.Tracks.name
                    ),
                    onClick = {
                        navController.navigate(HomeScreen.Artists.name)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.LibraryMusic, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Playlists") },
                    selected = currentRoute == HomeScreen.Playlists.name,
                    onClick = {
                        navController.navigate(HomeScreen.Playlists.name)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Scan Device") },
                    selected = currentRoute == HomeScreen.Scan.name,
                    onClick = {
                        navController.navigate(HomeScreen.Scan.name)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Sync, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = currentRoute == HomeScreen.Settings.name,
                    onClick = {
                        navController.navigate(HomeScreen.Settings.name)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Settings, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )


                NavigationDrawerItem(
                    label = { Text("About") },
                    selected = currentRoute == "about",
                    onClick = {
                        navController.navigate("about")
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Info, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    )


    {

        val importM3uLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let { playlistViewModel.importM3u(it) }
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            snackbarHost = {
                SnackbarHost(
                    snackbarHostState,
                )
            },
            topBar = {
                Column {
                    val selectedTabIndex =
                        tabs.indexOfFirst { it.name == currentRoute }
                    val backIndex = noBack.indexOfFirst { it.name == currentRoute }
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    if (currentRoute != "nowPlaying" && !selectionMode && editRoutes.all {
                            currentRoute?.startsWith(
                                it
                            ) == false
                        } && currentRoute?.startsWith("search") == false) {
                        LibraryTopBar(
                            currentScreen = routeToLibraryScreen(currentRoute),
                            onFilterClick = { showFilterSheet = true },
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
                                    HomeScreen.Playlists.name -> playlistsSort = sort
                                    HomeScreen.Genres.name -> genresSort = sort
                                    HomeScreen.Countries.name -> countriesSort = sort
                                }
                            },
                            onImport = {
                                importM3uLauncher.launch(
                                    arrayOf(
                                        "audio/x-mpegurl",
                                        "text/plain"
                                    )
                                )
                            },
                            onMenuClick = { scope.launch { drawerState.open() } },
                            showBack = backIndex < 0 && currentRoute != "about",
                            onBack = if (backIndex < 0 && currentRoute != "about") ({ navController.popBackStack() }) else null,
                            title = if (backIndex >= 0 || currentRoute=="about") currentRoute.toTitleCase() else null,
                        )
                    }
                    if (selectionMode) {
                        val selection by selectionViewModel.selectionState.collectAsState()
                        val moveEnabled by selectionViewModel.moveEnabled.collectAsState()
                        val playlistScreen = currentRoute != null && currentRoute.startsWith("playlist/")
                        val selectedPlaylistTracks = selection.selectedPlaylistEntryIds.map { it.trackId }
                        val selectedPlaylistEntries = selection.selectedPlaylistEntryIds.map { it.entryId }
                        val selectedQueueTracks = selection.selectedQueueIds.map { it.trackId }
                        val selectedQueueUUIDs = selection.selectedQueueIds.map { it.queueId }
                        SelectionTopBar(
                            count = selection.count,
                            onClear = { selectionViewModel.clearSelection() },
                            onPlayNext = {
                                if (playlistScreen)
                                    playerViewModel.playNextListIds(selectedPlaylistEntries,
                                        SelectSource.PLAYLIST)
                                else if (currentRoute == "nowPlaying")
                                    playerViewModel.playNextListIds(selectedQueueTracks,
                                        SelectSource.QUEUE)
                                else
                                    playerViewModel.playNextListIds(selection.selectedTrackIds.toList()) },
                            onAddToQueue = {
                                if (playlistScreen)
                                    playerViewModel.addToQueueListIds(selectedPlaylistEntries,
                                        SelectSource.PLAYLIST)
                                else if (currentRoute == "nowPlaying")
                                    playerViewModel.addToQueueListIds(selectedQueueTracks,
                                        SelectSource.QUEUE)
                                else
                                    playerViewModel.addToQueueListIds(selection.selectedTrackIds.toList()) },
                            onRemoveFromQueue = { playerViewModel.removeFromQueue(selectedQueueUUIDs.toSet()) },
                            isQueueScreen = (currentRoute == "nowPlaying"),
                            onRemoveFromPlaylist = {
                                playlistViewModel.removeTracksFromPlaylist(
                                    selectedPlaylistEntries.toSet()
                                )
                            },
                            isPlaylistScreen = (playlistScreen),
                            onDelete = { selectionViewModel.requestDeletionOfSelected() },
                            onMove = { selectionViewModel.requestMove() },
                            onAddToPlaylist = {
                                if (playlistScreen)
                                    playlistViewModel.onAdd(selectedPlaylistTracks)
                                else if (currentRoute == "nowPlaying")
                                    playlistViewModel.onAdd(selectedQueueTracks)
                                else
                                    playlistViewModel.onAdd(selection.selectedTrackIds.toList()) },
                            moveEnabled = moveEnabled
                        )
                    }
                    if (selectedTabIndex >= 0 && currentRoute != HomeScreen.Scan.name && !selectionMode) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTabIndex,
                            edgePadding = 16.dp,
                            modifier = Modifier.fillMaxWidth()) {
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
                NowPlayingBar(
                    playerViewModel = playerViewModel,
                    currentRoute = currentRoute,
                    onClick = { navController.navigate("nowPlaying") },
                    modifier = Modifier.navigationBarsPadding()
                )
            }

        ) {

                innerPadding ->

            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.padding(innerPadding)
            ) {


                composable(route = HomeScreen.Artists.name) {
                    AllArtistsScreen(
                        sortRequest = artistSort,
                        onClick = { artist ->
                            navController.navigate("artist/${artist.id}")
                            {
                                launchSingleTop = true
                            }
                        },
                        onPlayNext = { artist -> playerViewModel.playNextArtist(artist.id) },
                        onAddToQueue = { artist -> playerViewModel.addToQueueArtist(artist.id) },
                        onAddToPlaylist = { artist -> playlistViewModel.onAddToPlaylistArtist(artist.id) },
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
                        onEdit = { album -> navController.navigate("album/edit/${album.id}/all_albums") },
                        onAddToPlaylist = { album -> playlistViewModel.onAddToPlaylistAlbum(album.id) }
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
                        onEdit = { track -> navController.navigate("track/edit/${track.trackId}") },
                        onAddToPlaylist = { id -> playlistViewModel.onAdd(listOf(id)) }
                    )
                }

                composable(route = HomeScreen.Genres.name) {
                    GenresScreen(
                        onGenreClick = { id -> navController.navigate("genre/$id") },
                        sortRequest = genresSort)
                }

                composable(route = HomeScreen.Countries.name) {
                    CountriesScreen(
                        onCountryClick = { code -> navController.navigate("country/$code")},
                        sortRequest = countriesSort
                    )
                }

                composable("artist/{artistId}") {
                    ArtistView(
                        onAlbumClick = { album ->
                            navController.navigate("album/${album.id}") {
                                launchSingleTop = true
                            }
                        },
                        onPlayNext = { album -> playerViewModel.playNextAlbum(album.id) },
                        onAddToQueue = { album -> playerViewModel.addToQueueAlbum(album.id) },
                        onEdit = { album -> navController.navigate("album/edit/${album.id}/artist_view") },
                        sortRequest = artistDetailSort,
                        onAddToPlaylist = { album -> playlistViewModel.onAddToPlaylistAlbum(album.id) }
                    )
                }

                composable("album/{albumId}") {
                    AlbumView(
                        onTrackClick = { track, tracks ->
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                playerViewModel.playTracks(tracks, track)
                                navController.navigate("nowPlaying")
                                {
                                    launchSingleTop = true
                                }
                            }
                        },
                        onPlayNext = { track -> playerViewModel.playNext(track) },
                        onAddToQueue = { track -> playerViewModel.addToQueue(track) },
                        onEdit = { track -> navController.navigate("track/edit/${track.trackId}") },
                        onAddToPlaylist = { id -> playlistViewModel.onAdd(listOf(id)) },
                        onGoToArtist = { id -> navController.navigate("artist/$id")}
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

                composable("genre/{genreId}") {
                    GenreDetailScreen(
                        onArtistClick = { id -> navController.navigate("artist/$id") },
                        onAlbumClick = { id -> navController.navigate("album/$id") },
                        onAddToPlaylist = { id -> playlistViewModel.onAdd(listOf(id)) },
                        onAddToPlaylistArtist = { album ->
                            playlistViewModel.onAddToPlaylistArtist(
                                album.id
                            )
                        },
                        onAddToPlaylistAlbum = { album ->
                            playlistViewModel.onAddToPlaylistAlbum(
                                album.id
                            )
                        },
                        onPlayNextArtist = { artist -> playerViewModel.playNextArtist(artist.id) },
                        onPlayNextAlbum = { album -> playerViewModel.playNextAlbum(album.id) },
                        onAddToQueueArtist = { artist -> playerViewModel.addToQueueArtist(artist.id) },
                        onAddToQueueAlbum = { album -> playerViewModel.addToQueueAlbum(album.id) },
                        onEditArtist = { artist -> navController.navigate("artist/edit/${artist.id}") },
                        onEditAlbum = { album -> navController.navigate("album/edit/${album.id}/all_albums") },
                        )
                }

                composable("country/{code}") {
                    CountryDetailScreen(
                        onArtistClick = { id -> navController.navigate("artist/$id") },
                        onAlbumClick = { id -> navController.navigate("album/$id") },
                        onAddToPlaylist = { id -> playlistViewModel.onAdd(listOf(id)) },
                        onAddToPlaylistArtist = { album ->
                            playlistViewModel.onAddToPlaylistArtist(
                                album.id
                            )
                        },
                        onAddToPlaylistAlbum = { album ->
                            playlistViewModel.onAddToPlaylistAlbum(
                                album.id
                            )
                        },
                        onPlayNextArtist = { artist -> playerViewModel.playNextArtist(artist.id) },
                        onPlayNextAlbum = { album -> playerViewModel.playNextAlbum(album.id) },
                        onAddToQueueArtist = { artist -> playerViewModel.addToQueueArtist(artist.id) },
                        onAddToQueueAlbum = { album -> playerViewModel.addToQueueAlbum(album.id) },
                        onEditArtist = { artist -> navController.navigate("artist/edit/${artist.id}") },
                        onEditAlbum = { album -> navController.navigate("album/edit/${album.id}/all_albums") },
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
                                "artist_view" ->
                                {
                                    if (id == null) navController.popBackStack()
                                    else navController.navigate("artist/$id") {
                                        popUpTo(HomeScreen.Artists.name) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
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
                        onBack = { navController.popBackStack() },
                        onAddToPlaylist = { id -> playlistViewModel.onAdd(listOf(id)) },
                        onAddToPlaylistArtist = { artist ->
                            playlistViewModel.onAddToPlaylistArtist(
                                artist.id
                            )
                        },
                        onAddToPlaylistAlbum = { album ->
                            playlistViewModel.onAddToPlaylistAlbum(
                                album.id
                            )
                        },
                        onPlayNextArtist = { artist -> playerViewModel.playNextArtist(artist.id) },
                        onPlayNextAlbum = { album -> playerViewModel.playNextAlbum(album.id) },
                        onAddToQueueArtist = { artist -> playerViewModel.addToQueueArtist(artist.id) },
                        onAddToQueueAlbum = { album -> playerViewModel.addToQueueAlbum(album.id) },
                        onEditArtist = { artist -> navController.navigate("artist/edit/${artist.id}") },
                        onEditAlbum = { album -> navController.navigate("album/edit/${album.id}/all_albums") },
                        onPlayNextTrack = { track ->playerViewModel.playNext(track) },
                        onAddToQueueTrack = { track -> playerViewModel.addToQueue(track) },
                        onEditTrack = { track -> navController.navigate("track/edit/${track.trackId}") },
                        selectionMode = selectionMode
                    )
                }

                composable(
                    route = "filter_results",
                    ) {
                    SearchContent(
                        results = SearchResult(albums = filterAlbumResults, artists = filterArtistResults),
                        onArtistClick = { id -> navController.navigate("artist/$id") },
                        onAlbumClick = { id -> navController.navigate("album/$id") },
                        onTrackClick = { tracks, track ->
                            playerViewModel.playTracks(
                                tracks,
                                track
                            )
                        },
                        padding = PaddingValues(0.dp),
                        onAddToPlaylist = { id -> playlistViewModel.onAdd(listOf(id)) },
                        onAddToPlaylistArtist = { album ->
                            playlistViewModel.onAddToPlaylistArtist(
                                album.id
                            )
                        },
                        onAddToPlaylistAlbum = { album ->
                            playlistViewModel.onAddToPlaylistAlbum(
                                album.id
                            )
                        },
                        onPlayNextArtist = { artist -> playerViewModel.playNextArtist(artist.id) },
                        onPlayNextAlbum = { album -> playerViewModel.playNextAlbum(album.id) },
                        onAddToQueueArtist = { artist -> playerViewModel.addToQueueArtist(artist.id) },
                        onAddToQueueAlbum = { album -> playerViewModel.addToQueueAlbum(album.id) },
                        onEditArtist = { artist -> navController.navigate("artist/edit/${artist.id}") },
                        onEditAlbum = { album -> navController.navigate("album/edit/${album.id}/all_albums") },
                        onPlayNextTrack = { track ->playerViewModel.playNext(track) },
                        onAddToQueueTrack = { track -> playerViewModel.addToQueue(track) },
                        onEditTrack = { track -> navController.navigate("track/edit/${track.trackId}") },

                    )
                }



                composable(HomeScreen.Scan.name) {
                    ScanLibraryScreen(
                        isInitial = startDest == HomeScreen.Scan.name
                    )
                }

                composable(HomeScreen.Settings.name) {
                    SettingsScreen()
                }

                composable(HomeScreen.Playlists.name) {
                    PlaylistsScreen(
                        createInfo = createInfo,
                        onClick = { id -> navController.navigate("playlist/$id") },
                        onCreateNewPlaylist = { navController.navigate("playlist/create") },
                        onDismiss = { playlistViewModel.hideCreateDialog() },
                        onNameChange = { newName -> playlistViewModel.onNameChange(newName) },
                        onConfirm = { playlistViewModel.createPlaylist() },
                        onDelete = { id -> playlistViewModel.deletePlaylist(id) },
                        playlistStates = playlistUiStates,
                        onEdit = { id -> navController.navigate("playlist/edit/$id") },
                        onExport = { uri, id -> playlistViewModel.exportM3u(uri, id) },
                        onPlayNext = { id -> playerViewModel.playNextPlaylist(id) },
                        onAddToQueue = { id -> playerViewModel.addToQueuePlaylist(id) },
                        onPlay = { id -> playerViewModel.playPlaylist(id) },
                        sortRequest = playlistsSort,
                        onSort = { option -> playlistViewModel.setSort(option) }
                    )
                }

                composable("playlist/{playlistId}") {
                    PlaylistDetailScreen(
                        onTrackClick = { track, tracks, entryId, entryIds ->
                            playerViewModel.playTracks(tracks, track, entryId, entryIds)
                            navController.navigate("nowPlaying")
                            {
                                launchSingleTop = true
                            }
                        },
                        onPlayNext = { track -> playerViewModel.playNext(track) },
                        onAddToQueue = { track -> playerViewModel.addToQueue(track) },
                        onEdit = { track -> navController.navigate("track/edit/${track.trackId}") },
                        onRemove = { entry, playlist ->
                            playlistViewModel.removeTrackFromPlaylist(
                                entry,
                                playlist
                            )
                        },
                        onAddToPlaylist = { id -> playlistViewModel.onAdd(listOf(id)) },
                        onShuffle = { tracks -> playerViewModel.playShuffledPlaylist(tracks) },
                        onGoToAlbum = { id -> navController.navigate("album/$id")},
                        onGoToArtist = { id -> navController.navigate("artist/$id")}
                    )
                }

                composable("playlist/edit/{playlistId}") {
                    PlaylistEditScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("playlist/create") {
                    PlaylistEditScreen(
                        onNavigateBack = { navController.popBackStack() }
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
                        onEdit = { track -> navController.navigate("track/edit/${track.trackId}") },
                        onAddToPlaylist = { id -> playlistViewModel.onAdd(listOf(id)) },
                    )
                }

                composable("about") {
                    AboutPage()
                }

            }



            if (addState.isShowing) {
                AddToPlaylistDialog(
                    playlists = allPlaylists,
                    onDismiss = { playlistViewModel.hideAddDialog() },
                    onPlaylistSelected = { playlist ->
                        playlistViewModel.addToPlaylist(addState.trackIds, playlist)
                    },
                    onCreateNewPlaylist = {
                        playlistViewModel.showCreate()
                    }
                )
            }

            if (createInfo.isShowing) {
                CreatePlaylistDialog(
                    createInfo = createInfo,
                    onNameChange = { newName -> playlistViewModel.onNameChange(newName) },
                    onDismiss = { playlistViewModel.hideCreateDialog() },
                    onConfirm = {
                        playlistViewModel.createPlaylistAndAdd(addState.trackIds)
                    }
                )
            }



            if (showFilterSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showFilterSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                ) {
                    FilterDrawerContent(
                        draft = draftFilter,
                        potentialAlbumCount = filterAlbumCount,
                        filterDefaults = filterDefaults,
                        labelSuggestions = labelSuggestions,
                        onDraftChange = { filter -> filterViewModel.updateDraft(filter) },
                        onLabelQueryChange = { query -> filterViewModel.onLabelQueryChange(query) },
                        interaction = sliderInteractionSource,
                        onApply = {
                            filterViewModel.applyFilters()
                            showFilterSheet = false
                            navController.navigate("filter_results")
                            filterViewModel.resetDraft()
                        },
                        genreSuggestions = genreSuggestions,
                        onGenreQueryChange = { query -> filterViewModel.onGenreQueryChange(query) },
                        potentialArtistCount = filterArtistCount,
                        onTabChange = { tab ->
                            filterViewModel.resetAll()
                            filterViewModel.updateType(tab)
                        }
                    )
                }

            }
        }
    }
}

