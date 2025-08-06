package com.example.musicapp.data.dto

import com.google.gson.annotations.SerializedName

data class ArtistMBResponse(
    val id: String,
    val name: String,
    @SerializedName("sort-name") val sortName: String?,
    val type: String?,
    val disambiguation: String?,
    val country: String?,
    val area: Area?,
    val tags: List<Tag>?,
    @SerializedName("begin-area") val beginArea: Area?,
    @SerializedName("life-span") val lifeSpan: LifeSpan?,
    @SerializedName("relations") val urlRelations: List<UrlRelation>?
)


//data class Area(
//    val name: String?
//)

data class Tag(
    val name: String,
    val count: Int
)


data class LifeSpan(
    val begin: String?,
    val end: String?,
    val ended: Boolean?
)

data class UrlRelation(
    val type: String?,
    val url: UrlWrapper?
)

data class UrlWrapper(
    val resource: String?
)


