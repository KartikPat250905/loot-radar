package com.example.lootradarkmp.data.mappers

import com.example.lootradarkmp.Games
import com.example.lootradarkmp.data.models.GameDto

fun Games.toDto(): GameDto =
    GameDto(
        id = id,
        title = title,
        worth = worth,
        thumbnail = thumbnail,
        image = image,
        description = description,
        instructions = instructions,
        open_giveaway_url = open_giveaway_url,
        published_date = published_date,
        type = type,
        platforms = platforms,
        end_date = end_date,
        users = users?.toInt(),
        status = status,
        gamerpower_url = gamerpower_url
    )

fun GameDto.toEntity() =
    arrayOf(
        id,
        title,
        worth,
        thumbnail,
        image,
        description,
        instructions,
        open_giveaway_url,
        published_date,
        type,
        platforms,
        end_date,
        users?.toLong(),
        status,
        gamerpower_url
    )
