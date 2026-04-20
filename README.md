# Pokequiz - Resumen de Avances y Lógica RPG 🚀

Este documento detalla todas las mecánicas, cambios estructurales y rediseños de interfaz que hemos implementado para empoderar **Pokequiz** y convertirlo de una sencilla trivia a un RPG más completo y profundo inspirado en los juegos clásicos.

## 🛠️ 1. Backend (Node.js, Express & Prisma)
Hemos modificado el núcleo del servidor para dar luz verde a todas las nuevas características:

*   **Sistema de Experiencia y Equipos:** Modificamos el esquema `schema.prisma`. Ahora la tabla `Inventory` (tu colección) tiene nuevos campos: `level` (Nivel, empieza en 1), `exp` (Experiencia) e `inParty` (Booleano que indica si el Pokémon está en tu equipo de 3).
*   **Rotación en el Gacha ("Duplicados"):** Si un usuario gasta monedas en el Gacha y le toca un Pokémon que ya tenía, en lugar de no hacer nada o dar error, el sistema ahora le inyecta **+50 puntos de Experiencia (Exp)** a ese Pokémon automáticamente.
*   **Generador Infinito de Preguntas:** Se borraron las 3 tristes preguntas estáticas. Se ha programado un algoritmo dinámico en `apiController.ts (getLevelData)` que lee la lista de todos tus Pokémon y en tiempo real formula y genera más de **30 preguntas únicas** de forma algorítmica para cada combate (Ej: ¿De qué tipo es CHARIZARD? ¿Cuánta vida tiene PIKACHU?).
*   **Toggle Party API:** Nuevo endpoint (`POST /api/user/party/toggle`) para gestionar quién entra y sale de tu equipo, limitando estrictamente el servidor a un **máximo de 3 Pokémon equipables en combate**.

## 📱 2. Android (Jetpack Compose & Kotlin)
Rehicimos pantallas enteras para adaptarnos a las nuevas capacidades del backend:

### Ordenador de Bill (PokemonPCActivity)
*   Se lee todo tu progreso vivo del Backend. 
*   **Mecánica de Equipo:** Al pulsar sobre cualquier Pokémon de tu GRID cuadriculado, la app llamará al servidor para añadirlo/quitarlo de tu equipo activo.
*   **UI Estilizada:** Si un Pokémon está en tu equipo, su carta se teñirá de un sutil tono dorado y se le aplicará un borde amarillo vibrante más el icono de "⭐" equipado. También se muestra ahora en texto el `Nv` (Nivel) que tiene al visualizarlo de cerca en la tarjeta.

### Pantalla de Batalla Clásica (BattleScreen)
*   **Rediseño Full-Retro:** La UI ha sido completamente transformada utilizando cajas (`Box/Constraint`) de Jetpack Compose para mimetizar la interfaz de batallas RPG (Enemigo arriba-derecha, tú abajo-izquierda, dos recuadros laterales curvos para el nombre, barras HP realistas e imagen del Back-Sprite de tu Pokémon extraída desde PokéAPI).
*   **Cuadro de Preguntas Separado:** El texto del presentador está anclado elegantemente en un bloque blanco grande encima de 4 clásicos botones simétricos en la división inferior. Si la pregunta es muy larga, automáticamente se expandirá en saltos de línea y se centrará sin cortar las letras.
*   **Rotación en Medio del Combate:** ¡La característica estrella! Descargamos tus 3 Pokémon guardados en el `Party` (Equipo). Si te equivocas muchas veces y el servidor detecta que tu vida cae a 0 HP durante la trivia... en lugar de dar *Game Over* instantáneo, ¡el siguiente Pokémon de la recámara de tu partido rotará automáticamente y restaurará el sistema para seguir intentándolo! Perderás solo si se debilitan tus 3 luchadores.
*   **Daño Balanceado:** Matar a un enemigo ahora siempre consta exactamente de **10 preguntas correctas**. La app lee el HP Base máximo del enemigo, lo divide por 10 y lo redondea hacia arriba, de esta manera da igual contra quién luches, matemáticamente caerá tras 10 aciertos.

---

## 🎯 3. Siguientes Pasos (A partir de aquí)

Compañeros, el proyecto se queda en el punto perfecto para atacar las características jugables y de despliegue antes de enseñarlo:

1.  **Escalado Numérico RPG (Batallas):** Actualmente aunque cada Pokémon tenga un Nivel en su base de datos, el HP que mostramos en la Batalla se basa en la vida "Base". *[Pasos: Aplicar fórmulas (ej. multiplicadores 1.1x / 1.2x) al HP disponible de `playerPokemon` en BattleScreen]*
2.  **Subida de Niveles (Backend):** Hay que crear en un futuro algun disparador o condición en `apiController.ts -> rewardUser` para que si un Pokémon excede de los 100 Exp, asigne `level = level + 1`.
3.  **Minijuegos (UI):** Programar la Screen del botón inferior destinado a los minijuegos, para granjear monedas ajeno a ganar batallas.
4.  **Música / SFX:** Conectar un simple reproductor de sonido en Background (Media Player en Android) cuando la Screen de Batalla se infle.
5.  **Producción Final:** Subir esa Base de datos local conectada de Prisma-PostGres a Railway y cambiar en Retrofit (Android) los dominios de la API `http://10.0.2.2:3001/` a los definitivos para que nos vaya de forma nativa en el móvil físico sin emular.
