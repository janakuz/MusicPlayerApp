package com.example.musicapp.util

import com.example.musicapp.data.local.entity.Artist

fun getFlagEmoji(countryCode: String?): String {
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

fun Artist.getCountryDisplay(): String {
    var countryDisplay = getFlagEmoji(countryCode)
    if (!homeCity.isNullOrEmpty()){
        countryDisplay += " ${homeCity},"
    }
    countryDisplay += " ${country ?: ""}"

    return countryDisplay
}