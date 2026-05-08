package com.diegofg11.pokequiz.utils

import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.MemoryCardData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * @authors: Gaizka, Diego y Xiker
 * Utilidades para la Zona Safari y Minijuegos.
 * Contiene la lógica de recompensas, generación de juegos de memoria y sopas de letras.
 */
object SafariUtils {

    /**
     * Otorga monedas al usuario tras completar un minijuego.
     * @param scope Scope de corrutina para la llamada a red.
     * @param coins Cantidad de monedas a sumar.
     * @param gameType Identificador del juego (ej: "dojo", "memory").
     * @param difficulty Nivel de dificultad jugado.
     * @param onSuccess Callback en caso de éxito.
     * @param onError Callback en caso de error con el mensaje detallado.
     */
    fun rewardUser(
        scope: CoroutineScope,
        coins: Int,
        gameType: String,
        difficulty: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (coins == 0) {
            onSuccess()
            return
        }
        
        scope.launch {
            try {
                val response = Network.api.safariReward(
                    com.diegofg11.pokequiz.models.SafariRewardRequest(
                        userId = SessionManager.currentUserId,
                        coinsEarned = coins,
                        gameType = gameType,
                        difficulty = difficulty
                    )
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onError("No se pudo procesar la transacción del Safari.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Error de conexión: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Generates a deck of memory cards.
     */
    fun generateMemoryDeck(availableIds: List<Int>, pairCount: Int): List<MemoryCardData> {
        val idsToUse = if (availableIds.isEmpty()) {
            val list = mutableListOf<Int>()
            repeat(pairCount) { list.add(Random.nextInt(1, 152)) }
            list
        } else {
            availableIds.shuffled().take(pairCount)
        }
        
        val deck = mutableListOf<MemoryCardData>()
        var cardId = 0
        idsToUse.forEach { pokeId ->
            deck.add(MemoryCardData(id = cardId++, pokemonId = pokeId))
            deck.add(MemoryCardData(id = cardId++, pokemonId = pokeId))
        }
        
        return deck.shuffled()
    }

    /**
     * Helper to generate Word Search grid.
     */
    fun generateWordSearchGrid(
        gridSize: Int,
        words: List<String>,
        allowReverse: Boolean,
        maxDirections: Int
    ): List<List<Char>> {
        val newGrid = Array(gridSize) { CharArray(gridSize) { ' ' } }
        
        for (word in words) {
            var placed = false
            var attempts = 0
            while (!placed && attempts < 100) {
                val isReversed = allowReverse && Random.nextBoolean()
                val wordToPlace = if (isReversed) word.reversed() else word
                
                // Direcciones: 0=Horizontal, 1=Vertical, 2=DiagonalAbajoDerecha, 3=DiagonalArribaDerecha
                val dir = Random.nextInt(maxDirections + 1)
                
                val startRow = Random.nextInt(gridSize)
                val startCol = Random.nextInt(gridSize)
                
                var canPlace = true
                val tempCells = mutableListOf<Pair<Int, Int>>()
                
                for (i in wordToPlace.indices) {
                    val r = startRow + when(dir) { 1 -> i; 2 -> i; 3 -> -i; else -> 0 }
                    val c = startCol + when(dir) { 0 -> i; 2 -> i; 3 -> i; else -> 0 }
                    
                    if (r !in 0 until gridSize || c !in 0 until gridSize) {
                        canPlace = false
                        break
                    }
                    if (newGrid[r][c] != ' ' && newGrid[r][c] != wordToPlace[i]) {
                        canPlace = false
                        break
                    }
                    tempCells.add(Pair(r, c))
                }
                
                if (canPlace) {
                    for (i in wordToPlace.indices) {
                        val pos = tempCells[i]
                        newGrid[pos.first][pos.second] = wordToPlace[i]
                    }
                    placed = true
                }
                attempts++
            }
        }
        
        // Rellenar espacios vacíos
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (newGrid[r][c] == ' ') {
                    newGrid[r][c] = ('A'..'Z').random()
                }
            }
        }
        
        return newGrid.map { it.toList() }
    }
}
