package com.diegofg11.pokequiz.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.diegofg11.pokequiz.utils.SessionManager

object Network {
    const val BASE_URL = "http://192.168.1.30:8080/"

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val originalUrl = original.url
        val token = SessionManager.currentToken
        val lang = SessionManager.currentLanguage
        android.util.Log.d("POKEQUIZ_NETWORK", "Interceptor sending lang: '$lang' for URL: ${originalUrl.encodedPath}")
        
        // Añadir lang como parámetro de consulta (Query Param) para máxima compatibilidad
        val url = originalUrl.newBuilder()
            .addQueryParameter("lang", lang)
            .build()

        val requestBuilder = original.newBuilder()
            .url(url)
            .header("lang", lang)
            .header("Accept-Language", lang)
            
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
