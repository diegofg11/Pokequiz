package com.diegofg11.pokequiz.api

import com.diegofg11.pokequiz.models.GachaRequest
import com.diegofg11.pokequiz.models.GachaResponse
import com.diegofg11.pokequiz.models.Pokemon
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PokeApi {
    @POST("api/gacha/roll")
    suspend fun rollGacha(@Body request: GachaRequest): Response<GachaResponse>

    @GET("api/user/pc")
    suspend fun getPc(@Query("userId") userId: Int): Response<List<Pokemon>>
}
