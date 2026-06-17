package com.example.musicapp.util

import com.example.musicapp.data.repository.AreaType
import com.example.musicapp.data.local.entity.Artist
import com.example.musicapp.data.local.model.ArtistWithArea
import com.example.musicapp.data.local.model.FullArea

fun getFlagEmoji(countryCode: String?, stateName: String? = null): String {

    if (stateName != null) {
        return when (stateName.lowercase().trim()) {
            "england" -> "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC65\uDB40\uDC6E\uDB40\uDC67\uDB40\uDC7F"
            "scotland" -> "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC73\uDB40\uDC63\uDB40\uDC74\uDB40\uDC7F"
            "wales" -> "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC77\uDB40\uDC6C\uDB40\uDC73\uDB40\uDC7F"
            else -> getFlagEmoji(countryCode, null)
        }
    }


    if (countryCode == null || countryCode.length != 2) return ""

    val firstChar = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
    val secondChar = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6

    return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}

fun Artist.getLifespanDisplay(): String {
    if (activeStartYear.isNullOrEmpty() && activeEndYear.isNullOrEmpty()) return ""
    val start = activeStartYear?.take(4) ?: "Unknown"
    return when {
        isDefunct -> "$start–${activeEndYear?.take(4) ?: "Present"}"
        else -> "$start–Present"
    }
}

fun ArtistWithArea.getCountryDisplay(): String {
    val state = if (area.country == "United Kingdom") area.state else null
    var countryDisplay = getFlagEmoji(artist.countryCode, state)
    countryDisplay += if (!area.city.isTrulyBlank()) " ${area.city},"
                            else if (!artist.homeCity.isTrulyBlank()) " ${artist.homeCity},"
                            else if (!area.county.isTrulyBlank()) " ${area.county},"
                            else ""


    countryDisplay += if (!area.state.isTrulyBlank()) " ${area.state}" else if (!area.country.isTrulyBlank()) " ${area.country}" else " ${artist.country ?: ""}"

    return countryDisplay
}

fun getSubtitle(area: FullArea, currentLevel: AreaType): String{
    val city = if (area.city.isTrulyBlank()) "" else "${area.city},"
    val county = if (area.county.isTrulyBlank()) "" else "${area.county},"
    val state = if (area.state.isTrulyBlank()) "" else "${area.state},"
    val country = if (area.country.isTrulyBlank()) "" else "${area.country}"

    return when(currentLevel){
        AreaType.CITY -> "$county $state $country"
        AreaType.COUNTY -> "$state $country"
        AreaType.STATE -> country
        AreaType.COUNTRY -> ""
    }

}