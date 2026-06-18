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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.viewmodels.CountriesDashboardViewModel
import com.example.musicapp.util.getFlagEmoji
import com.example.musicapp.util.toTitleCase

@Composable
fun CountriesScreen(
    onCountryClick: (String) -> Unit,
    sortRequest: SortOption?,
    ) {
    val countryDashboardViewModel: CountriesDashboardViewModel = hiltViewModel()
    val countriesList by countryDashboardViewModel.countriesWithCounts.collectAsState()

    val visibleCountries = countriesList.filter { it.artistCount > 0 || it.albumCount > 0 }

    LaunchedEffect(sortRequest) {
        sortRequest?.let {
            countryDashboardViewModel.setSort(it)
        }
    }


    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(visibleCountries) { item ->
            CountryCard(
                name = item.countryName,
                artistCount = item.artistCount,
                albumCount = item.albumCount,
                onClick = { onCountryClick(item.countryCode) },
                code = item.countryCode
            ) }
    }
}

@Composable
fun CountryCard(
    name: String,
    code: String,
    artistCount: Int,
    albumCount: Int,
    onClick: () -> Unit,
    flag: String? = null,
    ) {
    val dynamicColor = remember(name) {
        val hash = name.hashCode()
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
                text = "${flag ?: getFlagEmoji(code)} ${name.toTitleCase()}",
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