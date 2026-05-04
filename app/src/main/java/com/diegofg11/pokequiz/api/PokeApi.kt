package com.diegofg11.pokequiz.api

import com.diegofg11.pokequiz.models.GachaRequest
import com.diegofg11.pokequiz.models.GachaResponse
import com.diegofg11.pokequiz.models.Pokemon
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path
import com.diegofg11.pokequiz.models.LevelResponse
import com.diegofg11.pokequiz.models.User
import com.diegofg11.pokequiz.models.RewardRequest
import com.diegofg11.pokequiz.models.TogglePartyRequest
import com.diegofg11.pokequiz.models.LoginRequest

interface PokeApi {
    @POST("api/gacha/roll")
    suspend fun rollGacha(@Body request: GachaRequest): Response<GachaResponse>

    @GET("api/user/pc")
    suspend fun getPc(@Query("userId") userId: Int): Response<List<Pokemon>>

    @GET("api/levels/{id}")
    suspend fun getLevelData(@Path("id") id: String): Response<LevelResponse>

    @GET("api/user/{id}")
    suspend fun getUser(@Path("id") id: Int): Response<User>

    @POST("api/user/reward")
    suspend fun rewardUser(@Body request: RewardRequest): Response<User>

    @POST("api/user/party/toggle")
    suspend fun toggleParty(@Body request: TogglePartyRequest): Response<Any>

    @POST("api/user/login")
    suspend fun login(@Body request: LoginRequest): Response<User>

    @POST("api/user/register")
    suspend fun register(@Body request: LoginRequest): Response<User>
}
