package com.example.musicapp

fun String.normalizeForMatching(): String {
    return this.lowercase()
        .replace("&", "and")
        .replace(Regex("\\bthe\\b"), "")
        .replace(Regex("\\((.*?)\\)"), "")
        .replace(Regex("[^a-z0-9\\s]"), "")
        .replace(Regex("\\s+"), " ")
        .replace("ep", "")
        .trim()
}

fun String.cleanForSearching(): String {
    return this.lowercase()
        .replace("&", "and")
        .replace(Regex("\\((.*?)\\)"), "")
        .replace(Regex("[^a-z0-9\\s]"), "")
        .replace(Regex("\\s+"), " ")
        .replace("ep", "")
        .trim()
}