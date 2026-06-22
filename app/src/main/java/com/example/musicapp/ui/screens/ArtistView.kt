package com.example.musicapp.ui.screens

import android.app.Activity
import android.widget.Space
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.musicapp.R
import com.example.musicapp.data.local.entity.AreaHierarchy
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.ArtistWithArea
import com.example.musicapp.data.local.model.GridItem
import com.example.musicapp.ui.components.DeleteConfirmationDialog
import com.example.musicapp.ui.components.ImageWithTextColumn
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.viewmodels.ArtistDetailViewModel
import com.example.musicapp.ui.viewmodels.RefetchAlbumState
import com.example.musicapp.util.getCountryDisplay
import com.example.musicapp.util.getLifespanDisplay
import com.example.musicapp.util.normalizeGenre
import com.example.musicapp.util.toTitleCase
import com.google.common.collect.ForwardingIterator
import kotlin.collections.chunked
import kotlin.text.trim


@Composable
fun ArtistDetailHeader(
    image: String,
    title: String,
    artist: ArtistWithArea,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
        ) {

            val defaultImage = painterResource(R.drawable.rounded_groups_24)

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .size(400)
                    .crossfade(false)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .placeholderMemoryCacheKey(image)
                    .build(),
                placeholder = defaultImage,
                error = defaultImage,
                fallback = defaultImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))



        val subtitle = listOfNotNull(
            artist.getCountryDisplay(),
            artist.artist.getLifespanDisplay().takeIf { it.isNotEmpty() }
        ).joinToString(separator = "  •  ")
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}




@Composable
fun ExpandableBio(bio: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { expanded = !expanded }
            .animateContentSize()
    ) {
        val formattedBio = bio.replace("Read more on Last.fm", "<br/><br/>Read more on Last.fm")
        Text(
            text = AnnotatedString.fromHtml(
                formattedBio,
                linkStyles = TextLinkStyles(
                    style = SpanStyle(
                        color = Color(0xFF2196F3),
                        textDecoration = TextDecoration.Underline
                    ),
                )
            ),
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis
        )

    }

}

@Composable
fun FullArtistHeader(artist: ArtistWithArea) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val artistInfo = artist.artist
        ArtistDetailHeader(
            image = artistInfo.image.toString(),
            title = artistInfo.name,
            artist = artist,
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExpandableBio(artistInfo.bio.toString())

        Spacer(modifier = Modifier.height(8.dp))

    }
}

@Composable
fun SimilarGrid(
    artists: List<Artist>,
    onPlayNext: (GridItem) -> Unit,
    onAddToQueue: (GridItem) -> Unit,
    onEdit: (GridItem) -> Unit,
    onClick: (GridItem) -> Unit,
    onAddToPlaylist: (GridItem) -> Unit,
    onRemoveSimilar: ((Int) -> Unit)? = null,
){
    val artistRows = remember(artists) { artists.chunked(3) }

    artistRows.forEach { row ->
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (artist in row) {
                key(artist.id) {
                    Box(modifier = Modifier.weight(1f)) {
                        ImageWithTextColumn(
                            item = GridItem.ArtistItem(
                                id = artist.id,
                                displayName = artist.name,
                                imageRes = artist.image ?: "",
                                description = artist.bio ?: ""
                            ),
                            image = artist.image ?: "",
                            text = artist.name,
                            isAlbum = false,
                            onPlayNext = onPlayNext,
                            imageShape = CircleShape,
                            imageModifier = Modifier.size(80.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            onAddToQueue = onAddToQueue,
                            onEdit = onEdit,
                            onClick = onClick,
                            onAddToPlaylist = onAddToPlaylist,
                            onRemoveSimilar = onRemoveSimilar
                        )
                    }
                }

            }
            val emptySlots = 3 - (row.size)
            if (emptySlots < 3) {
                repeat(emptySlots) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

        }
    }
}

@Composable
fun SimilarSection(
    similarArtists: List<Artist>,
    modifier: Modifier = Modifier,
    onPlayNext: (GridItem) -> Unit,
    onAddToQueue: (GridItem) -> Unit,
    onEdit: (GridItem) -> Unit,
    onClick: (GridItem) -> Unit,
    onAddToPlaylist: (GridItem) -> Unit,
    onRemoveSimilar: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
//            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "SIMILAR ARTISTS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = modifier.padding(horizontal = 16.dp)

            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        SimilarGrid(
            artists = similarArtists,
            onPlayNext = onPlayNext,
            onAddToQueue = onAddToQueue,
            onEdit = onEdit,
            onClick = onClick,
            onAddToPlaylist = onAddToPlaylist,
            onRemoveSimilar
        )
    }
}

@Composable
fun SceneExpansionSection(
    area: AreaHierarchy,
    countryCode: String?,
    country: String?,
    cityArtists: List<Artist>,
    countyArtists: List<Artist>,
    stateArtists: List<Artist>,
    countryArtists: List<Artist>,
    countryCount: Int,
    modifier: Modifier = Modifier,
    onPlayNext: (GridItem) -> Unit,
    onAddToQueue: (GridItem) -> Unit,
    onEdit: (GridItem) -> Unit,
    onClick: (GridItem) -> Unit,
    onAddToPlaylist: (GridItem) -> Unit,
) {
    var expandedLevels by remember { mutableStateOf(emptySet<String>()) }

    Column(
        modifier = modifier
            .fillMaxWidth(),
//            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "LOCAL SCENES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }


        area.cityName?.let { name ->
            val isExpanded = expandedLevels.contains("city")
            Column {
                Text(
                    text = "More artists from $name",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedLevels =
                                if (isExpanded) expandedLevels - "city" else expandedLevels + "city"
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )

                if (isExpanded) {
                    Spacer(Modifier.height(8.dp))

                    SimilarGrid(
                        cityArtists,
                        onPlayNext = onPlayNext,
                        onAddToQueue = onAddToQueue,
                        onAddToPlaylist = onAddToPlaylist,
                        onClick = onClick,
                        onEdit = onEdit)

                }
            }
        }

        area.countyName?.let { name ->
            val isExpanded = expandedLevels.contains("county")
            Column {
                Text(
                    text = "More artists from $name",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedLevels =
                                if (isExpanded) expandedLevels - "county" else expandedLevels + "county"
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )

                if (isExpanded) {
                    Spacer(Modifier.height(8.dp))

                    SimilarGrid(
                        countyArtists,
                        onPlayNext = onPlayNext,
                        onAddToQueue = onAddToQueue,
                        onAddToPlaylist = onAddToPlaylist,
                        onClick = onClick,
                        onEdit = onEdit)

                }
            }
        }


        area.stateName?.let { name ->
            val isExpanded = expandedLevels.contains("state")
            Column {
                Text(
                    text = "More artists from $name",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedLevels =
                                if (isExpanded) expandedLevels - "state" else expandedLevels + "state"
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )

                if (isExpanded) {
                    Spacer(Modifier.height(8.dp))

                    SimilarGrid(
                        stateArtists,
                        onPlayNext = onPlayNext,
                        onAddToQueue = onAddToQueue,
                        onAddToPlaylist = onAddToPlaylist,
                        onClick = onClick,
                        onEdit = onEdit)


                }
            }
        }

        if (countryCode != null && countryCount in 1..15) {
            val isExpanded = expandedLevels.contains("country")
            Column {
                Text(
                    text = "More artists from ${country}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedLevels =
                                if (isExpanded) expandedLevels - "country" else expandedLevels + "country"
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )

                if (isExpanded) {
                    Spacer(Modifier.height(8.dp))

                    SimilarGrid(
                        countryArtists,
                        onPlayNext = onPlayNext,
                        onAddToQueue = onAddToQueue,
                        onAddToPlaylist = onAddToPlaylist,
                        onClick = onClick,
                        onEdit = onEdit)

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSimilarArtistDialog(
    suggestions: List<Artist>,
    onDismissRequest: () -> Unit,
    onArtistSelected: (artistId: Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    currentSimilar: List<Artist>,
) {
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        title = {
            Text(
                text = "Add Similar Artist",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded && suggestions.isNotEmpty(),
                    onExpandedChange = {
                        expanded = it
                    }
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onSearchQueryChange(it)
                            expanded = true
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    )

                    ExposedDropdownMenu(
                        expanded = expanded && suggestions.isNotEmpty(),
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        suggestions.forEach { suggestion ->
                            if (!currentSimilar.contains(suggestion)) {
                                DropdownMenuItem(
                                    text = {


                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onArtistSelected(suggestion.id)
                                                    onDismissRequest()
                                                }
                                                .padding(vertical = 8.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = suggestion.image,
                                                contentDescription = null,
                                                placeholder = painterResource(R.drawable.rounded_groups_24),
                                                error = painterResource(R.drawable.rounded_groups_24),
                                                fallback = painterResource(R.drawable.rounded_groups_24),
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                                contentScale = ContentScale.Crop
                                            )

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = suggestion.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (!suggestion.country.isNullOrBlank()) {
                                                    Text(
                                                        text = suggestion.country,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }


                                    },
                                    onClick = {
                                        onArtistSelected(suggestion.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ArtistView(
    modifier: Modifier = Modifier,
    onAlbumClick: ((GridItem) -> Unit)? = null,
    onPlayNext: (GridItem) -> Unit,
    onAddToQueue: (GridItem) -> Unit,
    onEdit: (GridItem) -> Unit,
    onPlayNextArtist: (GridItem) -> Unit,
    onAddToQueueArtist: (GridItem) -> Unit,
    onEditArtist: (GridItem) -> Unit,
    onClickArtist: (GridItem) -> Unit,
    onAddToPlaylistArtist: (GridItem) -> Unit,
    sortRequest: SortOption?,
    onAddToPlaylist: (GridItem) -> Unit,
    showAddSimilarDialog: Boolean,
    onDismissSimilar: () -> Unit,
) {

    val artistDetailViewModel: ArtistDetailViewModel = hiltViewModel()


    data class DeleteEvent(val id: Int, val name: String)

    var pendingDeletion by remember { mutableStateOf<DeleteEvent?>(null) }


    val context = LocalContext.current
    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && pendingDeletion != null) {
            artistDetailViewModel.finalizeDeletion(pendingDeletion!!.id)
        } else {
            artistDetailViewModel.clearPendingDeletion()
        }
    }

    val pendingUris by artistDetailViewModel.pendingDeleteUris.collectAsState()

    LaunchedEffect(pendingUris) {
        if (pendingUris.isNotEmpty()) {
            val pendingIntent = artistDetailViewModel.getDeleteIntent(context, pendingUris)
            deleteLauncher.launch(
                IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            )
        }
    }


    LaunchedEffect(sortRequest) {
        sortRequest?.let {
            artistDetailViewModel.setSort(it)
        }
    }


    val artistDetailUiState by artistDetailViewModel.artistDetailUiState.collectAsState()
    val artist = artistDetailUiState.artist
    val albums = artistDetailUiState.albums
    val countryArtistsCount by artistDetailViewModel.countryArtistCount.collectAsState()

    val similarArtists by artistDetailViewModel.similarArtists.collectAsState()

    val refetchUiState by artistDetailViewModel.refetchState.collectAsState()

    val sameCityArtists by artistDetailViewModel.sameCityArtists.collectAsState()
    val sameCountyArtists by artistDetailViewModel.sameCountyArtists.collectAsState()
    val sameStateArtists by artistDetailViewModel.sameStateArtists.collectAsState()
    val sameCountryArtists by artistDetailViewModel.sameCountryArtists.collectAsState()

    val similarSuggestions by artistDetailViewModel.similarArtistSuggestions.collectAsState()


    if (artist != null) {

        Box(modifier = Modifier.fillMaxSize()) {
            AlbumsGrid(
                albums,
                showReleaseDate = true,
                onClick = onAlbumClick,
                onAddToQueue = onAddToQueue,
                onAddToPlaylist = onAddToPlaylist,
                onPlayNext = onPlayNext,
                header = { FullArtistHeader(artist) },
                footer = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        SimilarSection(
                            similarArtists = similarArtists,
                            onPlayNext = onPlayNextArtist,
                            onAddToQueue = onAddToQueueArtist,
                            onAddToPlaylist = onAddToPlaylistArtist,
                            onClick = onClickArtist,
                            onEdit = onEditArtist,
                            onRemoveSimilar = { id -> artistDetailViewModel.removeSimilar(artist.artist.id, id) }
                        )

                        SceneExpansionSection(
                            artist.area,
                            cityArtists = sameCityArtists,
                            countyArtists = sameCountyArtists,
                            stateArtists = sameStateArtists,
                            countryArtists = sameCountryArtists,
                            countryCount = countryArtistsCount,
                            onPlayNext = onPlayNextArtist,
                            onAddToQueue = onAddToQueueArtist,
                            onAddToPlaylist = onAddToPlaylistArtist,
                            onClick = onClickArtist,
                            onEdit = onEditArtist,
                            country = artist.artist.country,
                            countryCode = artist.artist.countryCode
                        )
                    }},
                onEdit = onEdit,
                onDelete = { id, title -> pendingDeletion = DeleteEvent(id, title) },
                onRefetch = { id -> artistDetailViewModel.refetchMetadata(id) },
            )

            if (showAddSimilarDialog){
                AddSimilarArtistDialog(
                    suggestions = similarSuggestions,
                    onDismissRequest = onDismissSimilar,
                    onArtistSelected = { suggestionId -> artistDetailViewModel.addSimilar(artist.artist.id, suggestionId) },
                    onSearchQueryChange = { query -> artistDetailViewModel.onQueryChange(query) },
                    currentSimilar = similarArtists
                )
            }





            when (refetchUiState) {
                is RefetchAlbumState.Saving -> {
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Saving...", color = Color.White)
                        }
                    }
                    BackHandler(enabled = true) { }
                }

                is RefetchAlbumState.DisambiguationNeeded -> {
                    AlbumDisambiguationDialog(
                        matches = (refetchUiState as RefetchAlbumState.DisambiguationNeeded).matches,
                        onAlbumSelected = { selectedAlbum ->
                            artistDetailViewModel.onAlbumSelected(selectedAlbum)
                        },
                        onDismiss = {
                            artistDetailViewModel.reset()
                        }
                    )
                }

                is RefetchAlbumState.Error -> {
                    Text(text = (refetchUiState as RefetchAlbumState.Error).message)
                }

                else -> {}
            }


        }

    }

    pendingDeletion?.let { item ->
        DeleteConfirmationDialog(
            text = item.name,
            onConfirm = {
                artistDetailViewModel.prepareDeletion(item.id)
                pendingDeletion = null
            },
            onDismiss = { pendingDeletion = null },
        )
    }


}

//@Preview(showBackground = true)
//@Composable
//fun ArtistPreview() {
//    MusicAppTheme {
//    }
//
//}