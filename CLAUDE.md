# AudioCity Android - Contexto para Claude

## Resumen del Proyecto

AudioCity Android es la versión Android de la app iOS AudioCity. Es una aplicación de audioguías que reproduce narraciones automáticas cuando el usuario se acerca a puntos de interés turístico.

## Stack Tecnológico

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose
- **Arquitectura**: MVVM con StateFlow
- **DI**: Hilt
- **Backend**: Firebase Firestore
- **Mapas**: Google Maps SDK + Maps Compose
- **Ubicación**: FusedLocationProvider + Geofencing
- **Audio**: TextToSpeech
- **Persistencia local**: SharedPreferences (trips, favorites)
- **Distribución**: Firebase App Distribution

## Credenciales y Configuración

### Firebase
- **Project ID**: `audiocity-poc`
- **App ID**: `1:294754708542:android:64178b9f450ebc29c5b56f`
- **Archivo config**: `app/google-services.json`

### Google Maps
- **API Key**: `AIzaSyCbVzD0lqiMMsZVvv6hM0WyI4kpHJpUrWg`
- **Ubicación**: `app/src/main/res/values/strings.xml`

### Keystore (Release)
- **Path**: `app/keystore/release.keystore`
- **Alias**: `audiocity`
- **Password**: `audiocity123`

### Firebase App Distribution
- **Grupo de testers**: `android-testers`

## Estructura del Proyecto

```
app/src/main/java/com/jrlabs/audiocity/
├── data/
│   ├── model/
│   │   ├── Route.kt          # Modelo de ruta
│   │   ├── Stop.kt           # Modelo de parada
│   │   ├── Trip.kt           # Modelo de viaje (+ Destination, RouteCategory)
│   │   └── CachedRoute.kt    # Modelo para cache offline
│   └── repository/
│       └── FirebaseRepository.kt
├── di/
│   └── AppModule.kt          # Hilt modules
├── services/
│   ├── AudioService.kt       # Text-to-Speech
│   ├── LocationService.kt    # Tracking de ubicación
│   ├── GeofenceService.kt    # Detección de proximidad
│   ├── TripService.kt        # Gestión de viajes
│   ├── FavoritesService.kt   # Sistema de favoritos
│   ├── NotificationService.kt # Notificaciones locales
│   └── LocationForegroundService.kt
├── ui/
│   ├── components/
│   │   ├── RouteCardCompact.kt  # Cards 180dp para carruseles
│   │   ├── TripCard.kt          # Cards de viajes
│   │   ├── StopInfoCard.kt      # Info de paradas
│   │   └── AudioControlBar.kt   # Controles de audio
│   ├── navigation/
│   │   └── NavGraph.kt          # Navegación con rutas
│   ├── screens/
│   │   ├── MainScreen.kt        # Scaffold con bottom nav
│   │   ├── explore/
│   │   │   └── ExploreScreen.kt # Mapa con markers
│   │   ├── routes/
│   │   │   ├── RoutesListScreen.kt  # Lista con secciones
│   │   │   ├── AllRoutesScreen.kt   # Búsqueda y filtros
│   │   │   └── RouteDetailScreen.kt
│   │   ├── trips/
│   │   │   ├── TripOnboardingScreen.kt  # Wizard 4 pasos
│   │   │   ├── TripDetailScreen.kt
│   │   │   └── AllTripsScreen.kt
│   │   └── profile/
│   │       └── ProfileScreen.kt
│   ├── theme/
│   └── viewmodel/
│       ├── RouteViewModel.kt
│       ├── TripViewModel.kt
│       └── ExploreViewModel.kt
└── AudioCityApplication.kt
```

## Pantallas Principales

### 1. RoutesListScreen (Tab principal)
- **Sección "Mis Viajes"**: Carrusel horizontal con TripCard + botón "Planificar Viaje"
- **Sección "Rutas Favoritas"**: Carrusel con RouteCardCompact (filtradas por favoritos)
- **Sección "Top Rutas"**: Ordenadas por número de paradas
- **Sección "Rutas de Moda"**: Trending (mockeadas igual que iOS)
- **Botón "Ver Todas"**: Navega a AllRoutesScreen

### 2. TripOnboardingScreen (Planificación de viajes)
Wizard de 4 pasos con animaciones:
1. **Destino**: Selección de ciudad
2. **Rutas**: Selección múltiple de rutas
3. **Opciones**: Fechas opcionales + descarga offline
4. **Resumen**: Confirmación y creación

### 3. AllRoutesScreen (Búsqueda)
- Barra de búsqueda por texto
- Filtro por dificultad (Fácil, Media, Difícil)
- Filtro por ciudad
- Ordenación (nombre, duración, distancia, paradas)
- Toggle de favoritos en cada card

### 4. ExploreScreen (Mapa)
- Google Maps con markers de paradas
- StopInfoCard al seleccionar marker
- AudioControlBar para reproducción
- Centrado en ubicación del usuario

## Comandos Útiles

### Build
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew clean
./gradlew assembleDebug
```

### Firebase App Distribution
```bash
firebase appdistribution:distribute app/build/outputs/apk/debug/app-debug.apk \
  --app "1:294754708542:android:64178b9f450ebc29c5b56f" \
  --release-notes "Descripción de cambios" \
  --groups "android-testers"
```

## Equivalencias iOS → Android

| iOS | Android |
|-----|---------|
| SwiftUI | Jetpack Compose |
| Combine | Flow/StateFlow |
| @StateObject | viewModel() / hiltViewModel() |
| @Published | MutableStateFlow |
| UserDefaults | SharedPreferences |
| CoreLocation | Google Location Services |
| CLCircularRegion | GeofencingClient |
| AVSpeechSynthesizer | TextToSpeech |
| MapKit | Google Maps SDK |
| UNUserNotificationCenter | NotificationManager |

## Sincronización con iOS

El proyecto Android debe mantenerse sincronizado con las features del proyecto iOS ubicado en:
`/Users/juanrafernandez/Documents/GitHub/AudioCity`

Revisar `CHANGELOG.md` y `CLAUDE.md` del proyecto iOS para ver nuevas funcionalidades a portar.

## Últimas Actualizaciones (Diciembre 2024)

### Funcionalidades Implementadas
- Sistema de viajes estilo Wikiloc (Trip planning)
- Sistema de favoritos con persistencia
- Rediseño de RoutesListScreen con secciones
- AllRoutesScreen con búsqueda y filtros
- Notificaciones locales para llegada a paradas
- Bloqueo de orientación a portrait
- Mapa centrado en ubicación del usuario

### APK en Firebase App Distribution
- Versión: 1.0 (1)
- Estado: Disponible para android-testers
- Console: https://console.firebase.google.com/project/audiocity-poc/appdistribution/app/android:com.jrlabs.audiocity/releases

## Notas de Desarrollo

1. **SharedPreferences**: Se usa para persistir trips y favorites localmente
2. **Hilt**: Todos los ViewModels y Services están inyectados
3. **Navigation**: Se comparte TripViewModel entre pantallas de trips
4. **Trending Routes**: Están mockeadas (hardcoded) igual que en iOS
5. **Offline Mode**: Estructura preparada pero descarga real pendiente
