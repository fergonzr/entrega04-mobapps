# PetAngels

PetAngels es un juego Android desarrollado con Kotlin y Jetpack Compose en el que el jugador cuida mascotas, administra comida y agua, mejora sus posibilidades con power ups y busca que los animales sean adoptados.

La aplicacion esta pensada para jugarse en orientacion horizontal y combina mecanicas simples de gestion con una interfaz visual basada en recursos graficos, sonidos y animaciones nativas de Compose.

## Objetivo del juego

El objetivo principal es cuidar perros y gatos hasta que esten en condiciones de ser adoptados. Para avanzar, el jugador debe mantener sus barras de comida y agua, sumar puntos, desbloquear mejoras y completar los niveles hasta llegar al final de la partida.

## Funcionalidades principales

- Pantalla inicial animada con logo, botones de navegacion y fondo oscuro.
- Continuacion de partida guardada.
- Pantalla de juego con mascotas activas, recursos, nivel, puntaje y progreso de adopcion.
- Sistema de comida y agua para asignar recursos a cada mascota.
- Mascotas de tipo perro y gato con reglas de puntuacion y adopcion propias.
- Sistema de niveles con nuevas mascotas al avanzar.
- Power ups desbloqueables desde el nivel 2.
- Historial de partidas completadas en la pantalla de puntajes.
- Pantalla final con estadisticas, mascotas adoptadas, rating y celebracion.
- Persistencia local de la partida actual y del historial usando archivos JSON.
- Sonidos para eventos del juego como compra, fallo, nuevo dia, subida de nivel y finalizacion.

## Mecanicas del juego

Durante cada turno, el jugador selecciona comida o agua y la asigna a las mascotas. Al avanzar el dia, el estado del juego calcula:

- El consumo o desgaste de las barras de cada mascota.
- El puntaje obtenido por el estado de comida y agua.
- La posibilidad de adopcion si la mascota cumple las condiciones.
- La generacion de nuevos recursos.
- El avance de nivel cuando se alcanza la meta de adopciones.

Las mascotas ya adoptadas permanecen registradas y se muestran en la pantalla final de la partida.

## Sistema de niveles

El juego usa niveles progresivos definidos en el dominio del proyecto:

- El nivel maximo actual es 6.
- En el nivel 1 aparecen 3 mascotas nuevas.
- En los niveles 2 a 4 aparecen tantas mascotas nuevas como el numero del nivel.
- En niveles superiores aparecen `nivel - 1` mascotas nuevas.
- La meta acumulada de adopciones por nivel se calcula con `level * (level + 1) / 2`.

Cuando el jugador alcanza la meta de adopciones del nivel actual, sube de nivel, se agregan nuevas mascotas y se desbloquean los power ups si todavia estaban bloqueados.

## Sistema de adopcion

Una mascota puede ser adoptada cuando sus barras requeridas estan completas. En ese momento se evalua una probabilidad de adopcion:

- Probabilidad base actual: 35%.
- El power up de visibilidad o marketing de adopcion aumenta la probabilidad.
- La probabilidad final esta limitada a un maximo de 100%.

Cada adopcion otorga una recompensa de puntaje:

- Perro: 250 puntos.
- Gato: 150 puntos.

## Sistema de comida y agua

El juego maneja dos recursos principales:

- Comida.
- Agua.

Cada recurso tiene un valor inicial y una generacion por turno. Las mascotas tienen limites propios para sus barras:

- Los perros tienen barras de comida y agua con rangos mas amplios.
- Los gatos tienen barras mas cortas y requieren menos recursos.

La interfaz muestra los indicadores de Comida y Agua con barras horizontales, etiquetas claras y actualizacion visual durante la partida.

## Power Ups

Los power ups se desbloquean a partir del nivel 2. Si el jugador intenta abrirlos en nivel 1, se muestra un mensaje de advertencia indicando que estaran disponibles mas adelante.

Power ups disponibles:

- Mimos: aumenta el parametro de confort de las mascotas.
- Marketing de adopcion: aumenta la probabilidad de adopcion.
- Incremento de comida: aumenta la generacion de comida.
- Incremento de agua: aumenta la generacion de agua.

Cada power up tiene costo por nivel, nivel maximo y efecto propio sobre los parametros base del juego.

## Animaciones implementadas

El proyecto usa animaciones nativas de Jetpack Compose. Entre las animaciones actuales se incluyen:

- Logo de la pantalla principal con fade in, scale up, pequeno bounce y flotacion suave.
- Fondo oscuro de la pantalla principal con fade in.
- Botones de la pantalla principal con aparicion secuencial, fade in, slide horizontal y escala al presionar.
- Seleccion de comida o agua con resaltado visual y cambio de escala.
- Bounce/scale de la mascota al recibir comida o agua.
- Pequeno salto periodico de la mascota cuando tiene hambre.
- Mensaje temporal de subida de nivel con fade y bounce/scale.
- Salto de celebracion de la mascota al subir de nivel.
- Animacion de compra de power up con bounce/scale y mensaje temporal.
- Mensaje de monedas insuficientes cuando una compra no puede realizarse.
- Pantalla final con fade in y confeti simple durante la celebracion.

## Tecnologias utilizadas

- Kotlin.
- Android SDK.
- Jetpack Compose.
- Material 3.
- Navigation Compose.
- Kotlinx Serialization JSON.
- Coroutines.
- Gradle Kotlin DSL.
- Recursos Android para imagenes, sonidos y temas.

## Arquitectura y estructura del proyecto

El proyecto esta organizado alrededor del paquete principal `com.juego.petangels`.

```text
app/src/main/java/com/juego/petangels/
+-- MainActivity.kt
+-- data/
|   +-- GameRepository.kt
|   +-- GameSummary.kt
|   +-- JsonGameRepository.kt
+-- debug/
|   +-- DebugGameState.kt
+-- domain/
|   +-- GameState.kt
|   +-- Level.kt
|   +-- PowerupType.kt
|   +-- ResourceType.kt
|   +-- ScorerType.kt
|   +-- otros modelos y reglas del juego
+-- ui/
    +-- AppNavigation.kt
    +-- navigation/
    |   +-- Routes.kt
    +-- screen/
    |   +-- SplashScreen.kt
    |   +-- TitleScreen.kt
    |   +-- GameScreen.kt
    |   +-- PowerupStoreDialog.kt
    |   +-- ScoreScreen.kt
    |   +-- EndScreen.kt
    +-- theme/
        +-- Color.kt
        +-- Theme.kt
        +-- Type.kt
```

### Capas principales

- `domain`: contiene el estado inmutable del juego, reglas de niveles, adopcion, recursos, mascotas y power ups.
- `data`: define la abstraccion de persistencia y una implementacion JSON en almacenamiento interno.
- `ui`: contiene navegacion, pantallas Compose, dialogos y tema visual.
- `res/drawable`: imagenes del juego, mascotas, iconos, logo y fondos.
- `res/raw`: efectos de sonido y musica.

## Pantallas

- `SplashScreen`: pantalla inicial de carga.
- `TitleScreen`: pantalla principal con logo animado, continuar juego y puntajes.
- `GameScreen`: pantalla central de juego, cuidado de mascotas, recursos, nivel, puntaje y tienda.
- `PowerupStoreDialog`: dialogo de compra y mejora de power ups.
- `ScoreScreen`: historial de partidas completadas ordenado por turnos.
- `EndScreen`: resumen final de la partida con estadisticas, rating, mascotas adoptadas y celebracion.

## Como ejecutar el proyecto

1. Abre el proyecto en Android Studio.
2. Espera a que Gradle sincronice las dependencias.
3. Selecciona un emulador o dispositivo Android compatible.
4. Ejecuta el modulo `app`.

Tambien puedes compilar desde terminal:

```powershell
.\gradlew.bat :app:assembleDebug
```

En macOS o Linux:

```bash
./gradlew :app:assembleDebug
```

## Requisitos

- Android Studio.
- JDK compatible con Android Studio.
- Android SDK con `minSdk` 29 o superior.
- Dispositivo o emulador Android.
- Gradle Wrapper incluido en el proyecto.

Configuracion Android detectada:

- `applicationId`: `com.juego.petangels`
- `minSdk`: 29
- `targetSdk`: 36
- `compileSdk`: 36.1
- Version de app: `1.0`
- Orientacion principal: landscape.

## Creditos / autores

Proyecto Android desarrollado para PetAngels.

Autor/es: no especificado en el codigo fuente del repositorio.
