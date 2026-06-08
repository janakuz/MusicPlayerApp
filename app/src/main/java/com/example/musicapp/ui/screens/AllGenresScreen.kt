package com.example.musicapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.viewmodels.GenresViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun GenresScreen(
    onGenreClick: (Int) -> Unit
) {
    val genresViewModel: GenresViewModel = hiltViewModel()
    val genresList by genresViewModel.genresWithCounts.collectAsState()

    val visibleGenres = genresList.filter { it.countArtists > 0 || it.countAlbums > 0 }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(visibleGenres) { item ->
            GenreCard(
            genreName = item.genre.name,
            artistCount = item.countArtists,
            albumCount = item.countAlbums,
            onClick = { onGenreClick(item.genre.id) }
        ) }
    }
}

@Composable
fun GenreCard(
    genreName: String,
    artistCount: Int,
    albumCount: Int,
    onClick: () -> Unit
) {
    val dynamicColor = remember(genreName) {
        val hash = genreName.hashCode()
        val r = (hash and 0xFF0000 shr 16) * 120 / 255
        val g = (hash and 0x00FF00 shr 8) * 120 / 255
        val b = (hash and 0x0000FF) * 120 / 255
        Color(red = r, green = g, blue = b, alpha = 255)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(dynamicColor)
            .clickable { onClick() }
            .padding(14.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = genreName.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = if (artistCount == 1) "1 Artist" else "$artistCount Artists",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f)
            )

            Text(
                text = if (albumCount == 1) "1 Album" else "$albumCount Albums",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }}