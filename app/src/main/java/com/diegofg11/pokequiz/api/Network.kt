package com.diegofg11.pokequiz.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.diegofg11.pokequiz.utils.SessionManager

/**
 * @authors: Gaizka, Diego y Xiker
 * Gestión centralizada de la red y conexión con la API de Pokequiz.
 * Configura Retrofit con interceptores para autenticación e internacionalización.
 */
object Network {
    const val BASE_URL = "https://pokequizbackend-production.up.railway.app/"

    /**
     * Interceptor que se ejecuta en cada petición saliente.
     * Inyecta:
     * 1. Token de autenticación si el usuario está logueado.
     * 2. Idioma actual del usuario (vía Query Param y Headers) para recibir datos traducidos.
     */
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val originalUrl = original.url
        val token = SessionManager.currentToken
        val lang = SessionManager.currentLanguage
        android.util.Log.d("POKEQUIZ_NETWORK", "Interceptor sending lang: '$lang' for URL: ${originalUrl.encodedPath}")
        
        // Añadir lang como parámetro de consulta (Query Param) para máxima compatibilidad con el backend
        val url = originalUrl.newBuilder()
            .addQueryParameter("lang", lang)
            .build()

        val requestBuilder = original.newBuilder()
            .url(url)
            .header("lang", lang) // Cabecera personalizada
            .header("Accept-Language", lang) // Cabecera estándar HTTP
            
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        
        chain.proceed(requestBuilder.build())
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val api: PokeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApi::class.java)
    }
}
