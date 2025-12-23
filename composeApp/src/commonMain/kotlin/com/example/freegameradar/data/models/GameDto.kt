package com.example.freegameradar.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GameDto(
    val id: Long? = null,
    val title: String? = null,
    val worth: String? = null,
    val thumbnail: String? = null,
    val image: String? = null,
    val description: String? = null,
    val instructions: String? = null,
    val open_giveaway_url: String? = null,
    val published_date: String? = null,
    val type: String? = null,
    val platforms: String? = null,
    val end_date: String? = null,
    val users: Int? = null,
    val status: String? = null,
    val gamerpower_url: String? = null
)
