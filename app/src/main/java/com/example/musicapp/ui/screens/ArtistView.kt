package com.example.musicapp.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.musicapp.R
import com.example.musicapp.data.entity.Artist
import com.example.musicapp.ui.components.ImageWithTextColumn
import com.example.musicapp.model.GridItem
import com.example.musicapp.ui.components.DeleteConfirmationDialog
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.theme.MusicAppTheme
import com.example.musicapp.ui.viewmodels.ArtistDetailViewModel
import com.example.musicapp.ui.viewmodels.RefetchAlbumState
import com.example.musicapp.ui.viewmodels.RefetchState

@Composable
fun ArtistDetailHeader(
    image: String,
    title: String,
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
//                .shadow(8.dp, RoundedCornerShape(16.dp)),
//            shape = RoundedCornerShape(16.dp),
//            color = MaterialTheme.colorScheme.surfaceVariant
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
//                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
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
    }
}


@Composable
fun ExpandableBio(bio: String){
    var expanded by remember { mutableStateOf(false)}

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable {expanded = !expanded}
            .animateContentSize()
    ) {
        Text(
            text = bio,
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
fun FullArtistHeader(artist: Artist) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArtistDetailHeader(
            image = artist.image.toString(),
            title = artist.name,
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExpandableBio(artist.bio.toString())

        Spacer(modifier = Modifier.height(8.dp))

    }
}

@Composable
fun ArtistView(
    modifier: Modifier = Modifier,
    onAlbumClick: ((GridItem) -> Unit)? = null,
    onPlayNext: (GridItem) -> Unit,
    onAddToQueue: (GridItem) -> Unit,
    onEdit: (GridItem) -> Unit,
    sortRequest: SortOption?
){

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

    val refetchUiState by artistDetailViewModel.refetchState.collectAsState()


    if (artist != null) {

        Box(modifier = Modifier.fillMaxSize()) {
            AlbumsGrid(
                albums,
                showReleaseDate = true,
                onClick = onAlbumClick,
                onAddToQueue = onAddToQueue,
                onPlayNext = onPlayNext,
                header = { FullArtistHeader(artist) },
                onEdit = onEdit,
                onDelete = { id, title -> pendingDeletion = DeleteEvent(id, title) },
                onRefetch = { id -> artistDetailViewModel.refetchMetadata(id) }
            )


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

@Preview(showBackground = true)
@Composable
fun ArtistPreview() {
    MusicAppTheme {
        val albumArtistSort = null
//        ArtistView(sortRequest = albumArtistSort)
    }

}