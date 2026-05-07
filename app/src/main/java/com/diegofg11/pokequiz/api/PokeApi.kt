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
import com.diegofg11.pokequiz.models.*

interface PokeApi {
    @POST("api/gacha/roll")
    suspend fun rollGacha(@Body request: GachaRequest): Response<GachaResponse>

    @GET("api/user/pc")
    suspend fun getPc(@Query("userId") userId: Int): Response<List<Pokemon>>

    @GET("api/levels/{id}")
    suspend fun getLevelData(@Path("id") id: String): Response<LevelResponse>

    @POST("api/battle/damage")
    suspend fun calculateBattleDamage(@Body request: BattleDamageRequest): Response<BattleDamageResponse>

    @GET("api/user/{id}")
    suspend fun getUser(@Path("id") id: Int): Response<User>

    @POST("api/user/reward")
    suspend fun rewardUser(@Body request: RewardRequest): Response<User>

    @POST("api/user/safari/reward")
    suspend fun safariReward(@Body request: SafariRewardRequest): Response<User>

    @GET("api/minigames/quickbattle")
    suspend fun getQuickBattleOpponent(): Response<QuickBattleOpponent>

    @GET("api/minigames/pokemon")
    suspend fun getMinigamePokemon(@Query("limit") limit: Int = 151): Response<List<MinigamePokemon>>

    @GET("api/minigames/guess/round")
    suspend fun getGuessRound(@Query("difficulty") difficulty: String): Response<GuessRoundResponse>

    @POST("api/user/party/toggle")
    suspend fun toggleParty(@Body request: TogglePartyRequest): Response<Any>

    @POST("api/user/login")
    suspend fun login(@Body request: LoginRequest): Response<User>

    @POST("api/user/register")
    suspend fun register(@Body request: LoginRequest): Response<User>

    @GET("api/pokemon/pokedex/{pokemonId}")
    suspend fun getPokedexInfo(@Path("pokemonId") pokemonId: Int): Response<Pokemon>

    @GET("api/questions")
    suspend fun getMoreQuestions(
        @Query("level") level: Int,
        @Query("count") count: Int = 10,
        @Query("startId") startId: Int = 100
    ): Response<List<Question>>

    @POST("api/user/update")
    suspend fun updateUser(@Body request: UpdateUserRequest): Response<User>

    @POST("api/user/pc/favorite")
    suspend fun toggleFavorite(@Body request: ToggleFavoriteRequest): Response<Any>
}
