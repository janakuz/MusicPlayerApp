package com.example.musicapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ReleaseSearchResponse(
    val created: String,
    val count: Int,
    val offset: Int,
    val releases: List<Release>
)

data class Release(
    val id: String,
    val score: Int,
    val title: String,
    val status: String?,
    val packaging: String?,
    val date: String?,
    val country: String?,
    @SerializedName("text-representation")
    val textRepresentation: TextRepresentation?,
    @SerializedName("artist-credit")
    val artistCredit: List<ArtistCredit>,
    @SerializedName("release-group")
    val releaseGroup: ReleaseGroup?,
    @SerializedName("release-events")
    val releaseEvents: List<ReleaseEvent>?,
    @SerializedName("label-info")
    val labelInfo: List<LabelInfo>?,
    val asin: String?,
    @SerializedName("track-count")
    val trackCount: Int?,
    val media: List<Media>?,
    val tags: List<Tag>?,
    )

data class TextRepresentation(
    val language: String?,
    val script: String?
)

data class ArtistCredit(
    val name: String?,
    val artist: ArtistSummary
)

data class ArtistSummary(
    val id: String,
    val name: String,
    @SerializedName("sort-name")
    val sortName: String,
    val tags: List<Tag>? = null
)

data class ReleaseGroup(
    val id: String,
    @SerializedName("primary-type-id")
    val primaryTypeId: String?,
    @SerializedName("type-id")
    val typeId: String?,
    val title: String?
)

data class ReleaseEvent(
    val date: String?,
    val area: Area?,
)

data class Area(
    val id: String,
    val name: String,
    @SerializedName("sort-name")
    val sortName: String
)

data class LabelInfo(
    @SerializedName("catalog-number")
    val catalogNumber: String?,
    val label: Label?
)

data class Label(
    val id: String,
    val name: String
)

data class Media(
    val id: String?,
    val format: String?,
    @SerializedName("disc-count")
    val discCount: Int?
)
