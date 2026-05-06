package com.example.musicapp.data.remote.dto

data class ArtistLastfmResponse(
    val artist: ArtistInfoLastfm
)

data class ArtistInfoLastfm(
    val name: String,
    val mbid: String,
    val url: String,
    val tags: TagsWrapper,
    val similar: SimilarArtistsWrapper,
    val bio: LastfmBio
)

data class SimilarArtistsWrapper(
    val artist: List<ArtistInfoLastfm>
)

data class TagsWrapper(
    val tag: List<LastfmTag>
)

data class LastfmTag(
    val name: String,
    val url: String
)

data class LastfmBio(
    val published: String,
    val summary: String,
    val content: String

)
