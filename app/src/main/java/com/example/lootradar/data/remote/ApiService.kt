package com.example.lootradar.data.remote

import com.example.lootradar.data.models.GameDto
import retrofit2.http.GET

interface ApiService{
    @GET("giveaways")
    suspend fun getFreeGames(): List<GameDto>
}