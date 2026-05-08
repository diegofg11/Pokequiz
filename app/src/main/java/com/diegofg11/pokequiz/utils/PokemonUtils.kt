/**
 * @authors: Gaizka, Diego y Xiker
 * Utilidades auxiliares para la gestión de sprites y URLs de Pokémon.
 */
package com.diegofg11.pokequiz.utils

import com.diegofg11.pokequiz.api.Network

object PokemonUtils {
    /**
     * Asegura que la URL del sprite sea absoluta usando la BASE_URL del backend.
     */
    fun fixSpriteUrl(url: String?): String {
        if (url.isNullOrBlank()) return ""
        val baseUrl = Network.BASE_URL.dropLast(1)
        return if (url.startsWith("/")) baseUrl + url else url
    }
}
