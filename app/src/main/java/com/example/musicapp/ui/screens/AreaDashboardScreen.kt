package com.example.musicapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicapp.data.remote.dto.Area
import com.example.musicapp.data.repository.AreaType
import com.example.musicapp.ui.components.SortOption
import com.example.musicapp.ui.viewmodels.AreaDashboardViewModel
import com.example.musicapp.util.getFlagEmoji

@Composable
fun AreasScreen(
    onAreaClick: (String, String, String) -> Unit,
    sortRequest: SortOption?,
) {
    val areaDashboardViewModel: AreaDashboardViewModel = hiltViewModel()
    val areasList by areaDashboardViewModel.areasWithCounts.collectAsState()

    val visibleCountries = areasList.filter { it.artistCount > 0 || it.albumCount > 0 }

    LaunchedEffect(sortRequest) {
        sortRequest?.let {
            areaDashboardViewModel.setSort(it)
        }
    }


    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(visibleCountries) { item ->
            val flag = when (item.stateName?.lowercase()?.trim()) {
                    "england" -> "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC65\uDB40\uDC6E\uDB40\uDC67\uDB40\uDC7F"
                    "scotland" -> "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC73\uDB40\uDC63\uDB40\uDC74\uDB40\uDC7F"
                    "wales" -> "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC77\uDB40\uDC6C\uDB40\uDC73\uDB40\uDC7F"
                    else -> getFlagEmoji(item.countryCode)
                }

            CountryCard(
                name = item.areaName,
                artistCount = item.artistCount,
                albumCount = item.albumCount,
                onClick = { onAreaClick(item.areaGid, item.countryCode, item.areaType) },
                code = item.countryCode,
                flag = flag
            ) }
    }
}
