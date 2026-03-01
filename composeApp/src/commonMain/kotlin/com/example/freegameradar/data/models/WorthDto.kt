package com.radarlabs.freegameradar.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorthDto(
    @SerialName("active_giveaways_number") val activeGiveawaysNumber: Int? = null,
    @SerialName("worth_estimation_usd") val worthEstimationUsd: String? = null
)
