package com.example.musicapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.CountryRepository
import com.example.musicapp.data.repository.SearchResult
import com.example.musicapp.util.getFlagEmoji
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CountryDetailViewModel@Inject constructor (
    countryRepository: CountryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel()  {

    private val countryCode: String = savedStateHandle.get<String>("code")
        ?: throw IllegalStateException("country code not found in SavedStateHandle")


    val countryItems = countryRepository.getCountryArtistsAndAlbums(countryCode)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResult())

    val countryName = "${getFlagEmoji(countryCode)} ${Locale.Builder().setRegion(countryCode).build().displayCountry}"

}