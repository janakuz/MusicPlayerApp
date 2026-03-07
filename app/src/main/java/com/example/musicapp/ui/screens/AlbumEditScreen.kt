package com.example.musicapp.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Public
import com.example.musicapp.ui.viewmodels.ImageOption
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.musicapp.ui.components.EditTopBar
import com.example.musicapp.ui.viewmodels.ArtistEditViewModel
import kotlin.math.absoluteValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import com.example.musicapp.R
import com.example.musicapp.normalizeForMatching
import com.example.musicapp.normalizeGenre
import com.example.musicapp.toTitleCase
import com.example.musicapp.ui.viewmodels.AlbumEditViewModel
import com.example.musicapp.ui.viewmodels.NameEditUiState
import com.example.musicapp.ui.viewmodels.TitleEditUiState


@Composable
fun AlbumImagePicker(
    images: List<ImageOption>,
    currentSelection: String,
    onImageSelected: (String) -> Unit
) {
    val initialPage = images.indexOfFirst { it.url == currentSelection }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { images.size }

    LaunchedEffect(images, initialPage) {
        if (images.isNotEmpty()) {
            pagerState.scrollToPage(initialPage)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (images.isNotEmpty()) {
            onImageSelected(images[pagerState.currentPage].url)
        }
    }


    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(300.dp),
            contentPadding = PaddingValues(horizontal = 80.dp),
            pageSpacing = 16.dp
        ) { page ->
            val pageOffset = (
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    ).absoluteValue

            Card(
                modifier = Modifier
                    .graphicsLayer {
                        val scale = lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleX = scale
                        scaleY = scale
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
                    .fillMaxSize(),
                shape = RoundedCornerShape(16.dp)
            ) {
                AsyncImage(
                    model = images[page].url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.baseline_image_24),
                    error = painterResource(R.drawable.baseline_broken_image_24),
                    filterQuality = FilterQuality.Low,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Row {
            Text(
                text = "${pagerState.currentPage + 1} / ${images.size}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (images.isNotEmpty()) {
                Icon(
                    imageVector = when (images[pagerState.currentPage].source) {
                        "Local" -> Icons.Default.Folder
                        "Web" -> Icons.Default.Public
                        else -> Icons.Default.QuestionMark
                    },
                    contentDescription = images[pagerState.currentPage].source,
                    modifier = Modifier.padding(6.dp).size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenrePicker(
    genres: List<String>,
    suggestions: List<String>,
    onGenreQueryChange: (String) -> Unit,
    onGenresChange: (List<String>) -> Unit,
    label: String
) {
    var textFieldValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("${label}s", style = MaterialTheme.typography.labelMedium)

        FlowRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            genres.forEach { genre ->
                InputChip(
                    selected = true,
                    onClick = {
                        onGenresChange(genres.filter { it != genre })
                    },
                    label = { Text(genre.normalizeGenre().toTitleCase()) },
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded && suggestions.isNotEmpty(),
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    onGenreQueryChange(it)
                    expanded = true
                },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
                label = { Text("Add $label") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (textFieldValue.isNotBlank()) {
                        onGenresChange(genres + textFieldValue.trim())
                        textFieldValue = ""
                    }
                })
            )

            ExposedDropdownMenu(
                expanded = expanded && suggestions.isNotEmpty(),
                onDismissRequest = { expanded = false }
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion.toTitleCase()) },
                        onClick = {
                            if (textFieldValue.isNotBlank()) {
                                onGenresChange(genres + suggestion.trim())
                                textFieldValue = ""
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun AlbumEditScreen(
    onNavigateBack: (Int?) -> Unit,
    ){
    val albumEditViewModel: AlbumEditViewModel = hiltViewModel()

    val albumEditUiState by albumEditViewModel.uiState.collectAsState()
    val titleEditWorkflowState by albumEditViewModel.workflowState.collectAsState()
    val images = albumEditUiState.availableImages
    val canSave by albumEditViewModel.canSave.collectAsState()
    val suggestions by albumEditViewModel.genreSuggestions.collectAsState()

    var showDiscardDialog by remember { mutableStateOf(false) }


    val handleBack = {
        if (canSave)
            showDiscardDialog = true
        else onNavigateBack(null)
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to leave?") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onNavigateBack(null)
                }) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }
    Scaffold(
        topBar = {
            EditTopBar(
                title = "Edit Album",
                onBackClick = handleBack
            )
        },
        floatingActionButton = {
            if (canSave) {
                ExtendedFloatingActionButton(
                    onClick = {
                        albumEditViewModel.onSave(onNavigateBack)
                    },
                    text = { Text("Save") },
                    icon = { Icon(Icons.Default.Check, null) }
                )
            }
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {



            LazyColumn(modifier = Modifier.padding(padding)) {
                item {
                    AlbumImagePicker(
                        images = images,
                        currentSelection = albumEditUiState.draftImageUrl,
                        onImageSelected = { selected -> albumEditViewModel.onImageChange(selected) }
                    )
                }

                item {
                    OutlinedTextField(
                        value = albumEditUiState.title,
                        onValueChange = { albumEditViewModel.onTitleChange(it) },
                        label = { Text("Album Title") },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = albumEditUiState.artist,
                        onValueChange = { albumEditViewModel.onArtistChange(it) },
                        label = { Text("Artist") },
                        enabled = !albumEditUiState.multipleArtists,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }


                item {
                    OutlinedTextField(
                        value = albumEditUiState.draftReleaseDate,
                        onValueChange = { albumEditViewModel.onReleaseDateChange(it) },
                        label = { Text("Release Date") },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }


                item {
                    OutlinedTextField(
                        value = albumEditUiState.draftLabel,
                        onValueChange = { albumEditViewModel.onLabelChange(it) },
                        label = { Text("Record Label") },
                        enabled = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                item {
                    GenrePicker(
                        genres = albumEditUiState.draftGenres,
                        onGenresChange = { list -> albumEditViewModel.onGenresChange(list) },
                        suggestions = suggestions,
                        onGenreQueryChange = { query -> albumEditViewModel.onGenreQueryChange(query) },
                        label = "Genre"
                    )
                }
            }

            when (titleEditWorkflowState) {
                is TitleEditUiState.Saving -> {
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

                is TitleEditUiState.DisambiguationNeeded -> {
                    ArtistDisambiguationDialog(
                        matches = (titleEditWorkflowState as TitleEditUiState.DisambiguationNeeded).matches,
                        onArtistSelected = { selectedArtist ->
                            albumEditViewModel.onArtistSelected(selectedArtist, onNavigateBack)
                        },
                        onDismiss = {
                            albumEditViewModel.resetName()
                        }
                    )
                }

                is TitleEditUiState.Error -> {
                }

                else -> { }
            }

        }
    }

    BackHandler(enabled = canSave) {
        showDiscardDialog = true
    }

}

