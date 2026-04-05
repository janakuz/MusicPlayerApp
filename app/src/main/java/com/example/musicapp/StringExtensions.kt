package com.example.musicapp

fun String.normalizeForMatching(): String {
    return this.lowercase()
        .replace("&", "and")
        .replace(Regex("\\bthe\\b"), "")
//        .replace(Regex("\\((.*?)\\)"), "")
        .replace(Regex("[^a-z0-9\\s]"), "")
        .replace(Regex("\\s+"), " ")
        .replace("ep", "")
        .trim()
}

fun String.normalizeGenre(): String {
    return this.lowercase()
        .replace("-", " ")
        .replace(Regex("\\bthe\\b"), "")
        .replace(Regex("\\((.*?)\\)"), "")
        .replace(Regex("[^a-z0-9\\s]"), "")
        .replace(Regex("\\s+"), " ")
        .replace("ep", "")
        .trim()
}

fun String.toTitleCase() = split(" ").joinToString(" ") {
    it.lowercase().replaceFirstChar { char -> char.uppercase() }
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

fun String.isSimilar(other: String, threshold: Double = 0.85): Boolean {
    val s1 = this.normalizeForMatching()
    val s2 = other.normalizeForMatching()
    if (s1 == s2) return true

    val maxLength = maxOf(s1.length, s2.length)
    if (maxLength == 0) return true

    val distance = levenshtein(s1, s2)
    return ((maxLength - distance).toDouble() / maxLength ) >= threshold
}

private fun levenshtein(s1: String, s2: String): Int{
    if (s1 == s2) return 0
    if (s1.isEmpty()) return s2.length
    if (s2.isEmpty()) return s1.length

    val prev = IntArray(s2.length + 1) { it }
    val curr = IntArray(s2.length + 1)

    for (i in 1..s1.length) {
        curr[0] = i
        for (j in 1..s2.length) {
            val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
            curr[j] = minOf(curr[j - 1] + 1, minOf(prev[j] + 1, prev[j - 1] + cost))
        }
        for (j in 0..s2.length) prev[j] = curr[j]
    }
    return prev[s2.length]}

