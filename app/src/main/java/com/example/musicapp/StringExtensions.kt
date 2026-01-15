package com.example.musicapp

fun String.normalizeForMatching(): String {
    return this.lowercase()
        .replace("&", "and")
        .replace("the", "")
        .replace(Regex("\\((.*?)\\)"), "")
        .replace(Regex("[^a-z0-9\\s]"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
}