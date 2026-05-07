package com.diegofg11.pokequiz.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.ui.tooling.preview.Preview
import com.diegofg11.pokequiz.ui.theme.PokequizTheme
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.diegofg11.pokequiz.ui.components.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.diegofg11.pokequiz.api.Network
import com.diegofg11.pokequiz.models.Pokemon
import com.diegofg11.pokequiz.models.TogglePartyRequest
import com.diegofg11.pokequiz.models.PokeType
import com.diegofg11.pokequiz.models.User
import com.diegofg11.pokequiz.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.diegofg11.pokequiz.utils.WallpaperManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

@Composable
fun PCScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf<User?>(null) }
    val pokemonList = remember { mutableStateListOf<Pokemon>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var warningMessage by remember { mutableStateOf<String?>(null) }
    var selectedIndex by remember { mutableStateOf<Int?>( null) }
    
    // Estados de búsqueda y ordenación
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(PokemonSortOption.RECENT) }
    var filterType by remember { mutableStateOf<PokeType?>(null) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var showShiniesOnly by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    // Persistencia de favoritos (SharedPreferences)
    val prefs = remember { context.getSharedPreferences("pokemon_prefs", android.content.Context.MODE_PRIVATE) }
    
    fun toggleFavorite(pokemon: Pokemon) {
        val inventoryId = pokemon.inventoryId ?: return
        val currentFavorites = prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()
        
        if (currentFavorites.contains(inventoryId.toString())) {
            currentFavorites.remove(inventoryId.toString())
            pokemon.isFavorite = false
        } else {
            currentFavorites.add(inventoryId.toString())
            pokemon.isFavorite = true
        }
        
        prefs.edit().putStringSet("favorites", currentFavorites).apply()
        // Forzar recomposición actualizando la lista (truco simple)
        val idx = pokemonList.indexOfFirst { it.inventoryId == inventoryId }
        if (idx != -1) pokemonList[idx] = pokemon.copy()
    }

    // Lista filtrada y ordenada (Usamos derivedStateOf para reaccionar a cambios en pokemonList)
    val filteredSortedList by remember(searchQuery, sortOption, filterType, showFavoritesOnly) {
        derivedStateOf {
            pokemonList
                .filter { it.nombre.contains(searchQuery, ignoreCase = true) }
                .filter { filterType == null || it.tipos.any { t -> t.equals(filterType?.nombreEs, ignoreCase = true) || t.equals(filterType?.name, ignoreCase = true) } }
                .filter { !showFavoritesOnly || it.isFavorite }
                .filter { !showShiniesOnly || it.isShiny }
                .let { list ->
                    when (sortOption) {
                        PokemonSortOption.RECENT -> list.sortedByDescending { it.inventoryId ?: 0 }
                        PokemonSortOption.POKEDEX -> list.sortedBy { it.idPokedex }
                        PokemonSortOption.LEVEL -> list.sortedByDescending { it.level }
                        PokemonSortOption.NAME -> list.sortedBy { it.nombre }
                    }
                }
        }
    }

    LaunchedEffect(Unit) {
        try {
            val userId = com.diegofg11.pokequiz.utils.SessionManager.currentUserId
            val userResp = withContext(Dispatchers.IO) { Network.api.getUser(userId) }
            val pcResp = withContext(Dispatchers.IO) { Network.api.getPc(userId) }

            if (userResp.isSuccessful) user = userResp.body()

            if (pcResp.isSuccessful && pcResp.body() != null) {
                val baseUrl = com.diegofg11.pokequiz.api.Network.BASE_URL.dropLast(1)
                val favorites = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
                val mapped = pcResp.body()!!.map {
                    it.copy(
                        spriteFront = if (it.spriteFront.startsWith("/")) baseUrl + it.spriteFront else it.spriteFront,
                        spriteBack = if (it.spriteBack.startsWith("/")) baseUrl + it.spriteBack else it.spriteBack,
                        spriteIcon = if (it.spriteIcon.startsWith("/")) baseUrl + it.spriteIcon else it.spriteIcon,
                        isFavorite = favorites.contains(it.inventoryId.toString())
                    )
                }
                pokemonList.clear()
                pokemonList.addAll(mapped)
            }
        } catch (e: Exception) {
            Log.e("PCScreen", "Error: ${e.message}")
            errorMessage = "Error de red"
        } finally {
            isLoading = false
        }
    }

    RetroBackground {
        if (errorMessage != null) {
            PokemonAlertDialog(
                title = "¡Error!",
                message = errorMessage!!,
                isError = true,
                onDismiss = { errorMessage = null }
            )
        }
        if (warningMessage != null) {
            PokemonAlertDialog(
                title = "Aviso",
                message = warningMessage!!,
                isError = false,
                onDismiss = { warningMessage = null }
            )
        }



        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldPoke)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Cabecera de Entrenador
                val displayUser = user
                if (displayUser != null) {
                    RetroMenuBox(
                        backgroundColor = Color.Black.copy(alpha = 0.05f),
                        borderColor = GoldPoke
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .border(2.dp, Color.Black, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6C63FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayUser.nombre.split(" ").take(2).joinToString("") { it.first().uppercase() },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                RetroText(
                                    text = displayUser.nombre.uppercase(),
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "NIV: ${displayUser.nivelProgreso}",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                    Text(
                                        text = "🪙 ${displayUser.monedasGacha}",
                                        fontSize = 12.sp,
                                        color = GoldPoke,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Equipo actual
                val partyPokemon = pokemonList.filter { it.inParty }
                Spacer(modifier = Modifier.height(20.dp))
                
                RetroText(
                    text = "EQUIPO ACTUAL",
                    fontSize = 16.sp,
                    color = GoldPoke,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, bottom = 8.dp)
                )
                
                RetroMenuBox(
                    backgroundColor = Color.White.copy(alpha = 0.1f),
                    borderColor = Color(0xFF2D5A27)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) { index ->
                            val pkmn = partyPokemon.getOrNull(index)
                            Box(modifier = Modifier.weight(1f)) {
                                if (pkmn != null) {
                                        PCPokemonCard(pokemon = pkmn, isParty = true) {
                                            selectedIndex = pokemonList.indexOf(pkmn)
                                        }
                                } else {
                                    PCEmptySlot(isParty = true)
                                }
                            }
                        }
                    }
                }

                // Mi Colección
                Spacer(modifier = Modifier.height(20.dp))
                // Mi Colección con Controles
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RetroText(
                        text = "COLECCIÓN PC",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = { isSearchVisible = !isSearchVisible },
                            shape = androidx.compose.ui.graphics.RectangleShape,
                            color = if (isSearchVisible) GoldPoke else Color(0xFF2D5A27),
                            modifier = Modifier.size(36.dp),
                            border = BorderStroke(2.dp, Color(0xFF1B3022))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Search, contentDescription = "Buscador", modifier = Modifier.size(20.dp), tint = Color.White)
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Surface(
                            onClick = { showSortDialog = true },
                            shape = androidx.compose.ui.graphics.RectangleShape,
                            color = if (filterType != null || showFavoritesOnly || showShiniesOnly) GoldPoke else Color(0xFF2D5A27),
                            modifier = Modifier.size(36.dp),
                            border = BorderStroke(2.dp, Color(0xFF1B3022))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Menu, contentDescription = "Ordenar", modifier = Modifier.size(20.dp), tint = Color.White)
                            }
                        }
                    }
                }

                if (isSearchVisible) {
                    Box(modifier = Modifier.padding(bottom = 12.dp)) {
                        RetroMenuBox(
                            backgroundColor = Color.Black.copy(alpha = 0.2f),
                            borderColor = GoldPoke,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("BUSCAR NOMBRE...", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                textStyle = TextStyle(color = Color.White, fontSize = 14.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = GoldPoke,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                RetroMenuBox(
                    backgroundColor = Color.White.copy(alpha = 0.9f),
                    borderColor = Color(0xFF1B3022)
                ) {
                    if (pokemonList.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¡EL PC ESTÁ VACÍO!",
                                color = Color(0xFF1B3022),
                                fontSize = 16.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.height(400.dp), // Altura fija para el grid en el PC
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredSortedList) { pokemon ->
                                    PCPokemonCard(pokemon = pokemon) {
                                        selectedIndex = pokemonList.indexOf(pokemon)
                                    }
                            }
                            // Slots vacíos para completar la estética
                            val totalSlots = 21
                            val empty = (totalSlots - filteredSortedList.size).coerceAtLeast(0)
                            items(empty) { PCEmptySlot() }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "TOTAL: ${pokemonList.size} POKÉMON",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        selectedIndex?.let { idx ->
            val safeIdx = idx.coerceIn(0, pokemonList.lastIndex)
            PokedexDialog(
                pokemonList = pokemonList.toList(),
                initialIndex = safeIdx,
                onDismiss = { selectedIndex = null },
                onToggleFavorite = { toggleFavorite(it) },
                onToggleParty = { targetPokemon, toggleTo ->
                    scope.launch {
                        try {
                            val res = withContext(Dispatchers.IO) {
                                Network.api.toggleParty(
                                    TogglePartyRequest(com.diegofg11.pokequiz.utils.SessionManager.currentUserId, targetPokemon.inventoryId ?: 0, toggleTo)
                                )
                            }
                            if (res.isSuccessful) {
                                val listIdx = pokemonList.indexOfFirst { it.inventoryId == targetPokemon.inventoryId }
                                if (listIdx != -1) pokemonList[listIdx] = targetPokemon.copy(inParty = toggleTo)
                            } else {
                                withContext(Dispatchers.Main) {
                                    warningMessage = "Equipo lleno (Máx 3)"
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorMessage = "Error de red"
                            }
                        }
                    }
                }
            )
        }

        if (showSortDialog) {
            SortDialog(
                currentSort = sortOption,
                currentFilterType = filterType,
                onlyFavorites = showFavoritesOnly,
                onlyShinies = showShiniesOnly,
                onSortSelected = { 
                    sortOption = it
                    showSortDialog = false 
                },
                onTypeFilterSelected = {
                    filterType = it
                    showSortDialog = false
                },
                onToggleFavorites = {
                    showFavoritesOnly = !showFavoritesOnly
                    showSortDialog = false
                },
                onToggleShinies = {
                    showShiniesOnly = !showShiniesOnly
                    showSortDialog = false
                },
                onDismiss = { showSortDialog = false }
            )
        }
    }
}

@Composable
fun SortDialog(
    currentSort: PokemonSortOption,
    currentFilterType: PokeType?,
    onlyFavorites: Boolean,
    onlyShinies: Boolean,
    onSortSelected: (PokemonSortOption) -> Unit,
    onTypeFilterSelected: (PokeType?) -> Unit,
    onToggleFavorites: () -> Unit,
    onToggleShinies: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        contentAlignment = Alignment.Center
    ) {
        RetroMenuBox(
            modifier = Modifier.fillMaxWidth(0.9f).pointerInput(Unit) { detectTapGestures { } },
            backgroundColor = Color.White,
            borderColor = GoldPoke
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 650.dp).padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RetroText(text = "FILTRAR Y ORDENAR", fontSize = 18.sp, color = Color(0xFF1B3022))
                Spacer(modifier = Modifier.height(12.dp))
                PixelDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                // Sección de Favoritos y Shinies
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RetroButton(
                        text = if (onlyFavorites) "VER TODOS" else "FAVORITOS",
                        onClick = onToggleFavorites,
                        modifier = Modifier.weight(1f),
                        containerColor = if (onlyFavorites) GoldPoke else Color(0xFFE53935),
                        contentColor = Color.White,
                        fontSize = 11.sp
                    )
                    RetroButton(
                        text = if (onlyShinies) "VER TODOS" else "SHINIES",
                        onClick = onToggleShinies,
                        modifier = Modifier.weight(1f),
                        containerColor = if (onlyShinies) GoldPoke else Color(0xFFFFA000),
                        contentColor = Color.White,
                        fontSize = 11.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                PixelDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                // Sección de Ordenación
                RetroText(text = "ORDENAR POR:", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                
                val sortOptions = PokemonSortOption.values().toList()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sortOptions.chunked(2).forEach { rowOptions ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowOptions.forEach { option ->
                                RetroButton(
                                    text = option.label,
                                    onClick = { onSortSelected(option) },
                                    modifier = Modifier.weight(1f),
                                    containerColor = if (currentSort == option) GoldPoke else Color(0xFF2D5A27),
                                    contentColor = if (currentSort == option) Color.Black else Color.White,
                                    fontSize = 12.sp
                                )
                            }
                            if (rowOptions.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                PixelDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sección de Filtro por Tipo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RetroText(text = "FILTRAR POR TIPO:", fontSize = 12.sp, color = Color.Gray)
                    if (currentFilterType != null) {
                        Text(
                            text = "LIMPIAR",
                            color = RedPoke,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.clickable { onTypeFilterSelected(null) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                val gen1Types = remember {
                    PokeType.values().filter { 
                        it != PokeType.STEEL && it != PokeType.DARK && it != PokeType.FAIRY 
                    }
                }
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(gen1Types) { type ->
                        val isSelected = currentFilterType == type
                        Surface(
                            onClick = { onTypeFilterSelected(type) },
                            shape = androidx.compose.ui.graphics.RectangleShape,
                            color = if (isSelected) type.color else type.color.copy(alpha = 0.3f),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color.Black else Color.Black.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = type.nombreEs,
                                color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                RetroButton(
                    text = "CERRAR",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PokedexDialog(
    pokemonList: List<Pokemon>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onToggleFavorite: (Pokemon) -> Unit,
    onToggleParty: (Pokemon, Boolean) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { pokemonList.size }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .pointerInput(Unit) { detectTapGestures {} },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera con flechas de navegación y contador
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationArrow(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    modifier = Modifier.size(40.dp),
                    onClick = {
                        if (pagerState.currentPage > 0) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }
                    }
                )

                Text(
                    text = "${pagerState.currentPage + 1} / ${pokemonList.size}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                NavigationArrow(
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.size(40.dp),
                    onClick = {
                        if (pagerState.currentPage < pokemonList.lastIndex) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val pokemon = pokemonList[page]
                RetroMenuBox(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White,
                    borderColor = GoldPoke
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RetroText(text = "DATOS POKÉDEX", fontSize = 14.sp, color = Color(0xFF1B3022))
                                if (pokemon.isShiny) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Star, contentDescription = "Shiny", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { onToggleFavorite(pokemon) }) {
                                    Icon(
                                        imageVector = if (pokemon.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorito",
                                        tint = if (pokemon.isFavorite) Color(0xFFE53935) else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Text(
                                "Nº ${pokemon.idPokedex}",
                                fontSize = 12.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                        }

                        PixelDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .background(Color(0xFFF0F0F0), androidx.compose.ui.graphics.RectangleShape)
                                .border(2.dp, Color.Black.copy(alpha = 0.1f), androidx.compose.ui.graphics.RectangleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = pokemon.spriteFront,
                                contentDescription = pokemon.nombre,
                                modifier = Modifier.size(120.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        RetroText(
                            text = pokemon.nombre.uppercase(),
                            fontSize = 24.sp,
                            color = Color(0xFF1B3022)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pokemon.tipos.forEach { tipo ->
                                Surface(
                                    shape = androidx.compose.ui.graphics.RectangleShape,
                                    color = getPokeTypeColor(tipo),
                                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = tipo.uppercase(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        PixelDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = pokemon.pokedexDescription ?: "Sin datos registrados.",
                            color = Color(0xFF1B3022),
                            fontSize = 13.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatInfo("NIV", "${pokemon.level}")
                            StatInfo("PS", "${pokemon.hpBase}")
                            StatInfo("EXP", "${pokemon.exp}/100")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        RetroButton(
                            text = if (pokemon.inParty) "DEJAR EN PC" else "A EQUIPO",
                            onClick = { onToggleParty(pokemon, !pokemon.inParty) },
                            containerColor = if (pokemon.inParty) RedPoke else Color(0xFF2D5A27),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        RetroButton(
                            text = "CERRAR",
                            onClick = onDismiss,
                            containerColor = Color.Gray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = Color.Gray, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    }
}

@Composable
private fun PCPokemonCard(pokemon: Pokemon, isParty: Boolean = false, onClick: () -> Unit) {
    RetroMenuBox(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        backgroundColor = if (pokemon.inParty) Color(0xFFFFF9C4) else Color.White,
        borderColor = if (pokemon.inParty) GoldPoke else Color(0xFF1B3022)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (pokemon.inParty && !isParty) {
                Text(
                    "⭐",
                    modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
                    fontSize = 10.sp
                )
            }
            AsyncImage(
                model = pokemon.spriteFront,
                contentDescription = pokemon.nombre,
                modifier = Modifier.size(if (isParty) 64.dp else 90.dp),
                contentScale = ContentScale.Fit
            )
            
            if (pokemon.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                )
            }
            
            if (pokemon.isShiny) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                )
            }
        }
    }
}

@Composable
private fun PCEmptySlot(isParty: Boolean = false) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Color.Black.copy(alpha = 0.05f), androidx.compose.ui.graphics.RectangleShape)
            .border(1.dp, Color.Black.copy(alpha = 0.1f), androidx.compose.ui.graphics.RectangleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "?",
            color = Color.Black.copy(alpha = 0.2f),
            fontSize = 16.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

enum class PokemonSortOption(val label: String) {
    RECENT("RECIENTES"),
    POKEDEX("Nº POKÉ"),
    LEVEL("NIVEL"),
    NAME("A-Z")
}

@Preview(showBackground = true)
@Composable
fun PCScreenPreview() {
    PokequizTheme {
        PCScreen()
    }
}
