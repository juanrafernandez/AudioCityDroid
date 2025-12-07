# AudioCity Android

Aplicación de audioguías que reproduce narraciones automáticas cuando el usuario se acerca a puntos de interés turístico. Versión Android del proyecto iOS AudioCity.

## Características

- **Rutas guiadas**: Explora rutas turísticas con paradas de audio
- **Detección automática**: Reproduce audio al acercarte a puntos de interés mediante geofencing
- **Text-to-Speech**: Narración en español con soporte para múltiples idiomas
- **Seguimiento en segundo plano**: Continúa funcionando con la app cerrada
- **Mapas interactivos**: Visualiza tu posición y las paradas en Google Maps
- **Sistema de viajes**: Planifica viajes con múltiples rutas estilo Wikiloc
- **Favoritos**: Guarda tus rutas favoritas
- **Búsqueda y filtros**: Encuentra rutas por ciudad, dificultad y más
- **Notificaciones locales**: Alertas al llegar a puntos de interés
- **Modo offline**: Descarga rutas para usar sin conexión

## Tecnologías

- **Kotlin** + **Jetpack Compose**
- **MVVM** con StateFlow
- **Hilt** (Dependency Injection)
- **Firebase Firestore** (Backend)
- **Google Maps SDK** + Maps Compose
- **FusedLocationProvider** + Geofencing
- **TextToSpeech** para narración
- **Foreground Service** para tracking en segundo plano
- **SharedPreferences** para persistencia local

## Requisitos

- Android Studio Ladybug o superior
- Android SDK 36
- JDK 11+
- Cuenta de Firebase con Firestore habilitado
- API Key de Google Maps

## Configuración

### 1. Firebase

1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com/)
2. Añade una app Android con package name `com.jrlabs.audiocity`
3. Descarga `google-services.json` y colócalo en `app/`
4. Habilita Firestore en modo de prueba

### 2. Google Maps

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Habilita "Maps SDK for Android"
3. Crea una API Key y restringe para Android
4. Añade el SHA-1 de tu keystore
5. Actualiza la key en `app/src/main/res/values/strings.xml`

### 3. Build

```bash
# Establecer JAVA_HOME (macOS con Android Studio)
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Build debug
./gradlew assembleDebug

# Build release (requiere keystore configurado)
./gradlew assembleRelease
```

## Estructura del proyecto

```
app/src/main/java/com/jrlabs/audiocity/
├── data/
│   ├── model/              # Route, Stop, Trip, CachedRoute, Destination
│   └── repository/         # FirebaseRepository
├── di/                     # Hilt modules
├── services/
│   ├── AudioService        # Text-to-Speech con cola
│   ├── LocationService     # Tracking de ubicación
│   ├── GeofenceService     # Detección de proximidad
│   ├── TripService         # Gestión de viajes
│   ├── FavoritesService    # Gestión de favoritos
│   └── NotificationService # Notificaciones locales
├── ui/
│   ├── components/         # RouteCardCompact, TripCard, StopInfoCard, etc.
│   ├── navigation/         # NavGraph con rutas
│   ├── screens/
│   │   ├── explore/        # ExploreScreen (mapa)
│   │   ├── routes/         # RoutesListScreen, AllRoutesScreen
│   │   ├── trips/          # TripOnboarding, TripDetail, AllTrips
│   │   └── profile/        # ProfileScreen
│   ├── theme/              # Colores, tipografía, tema
│   └── viewmodel/          # RouteViewModel, TripViewModel, ExploreViewModel
└── AudioCityApplication.kt
```

## Pantallas principales

### Rutas (RoutesListScreen)
- **Mis Viajes**: Viajes planificados con contador
- **Rutas Favoritas**: Scroll horizontal
- **Top Rutas**: Ordenadas por número de paradas
- **Rutas de Moda**: Trending (mockeadas)
- **Todas las Rutas**: Botón a búsqueda completa

### Planificación de viaje (TripOnboardingScreen)
1. **Destino**: Seleccionar ciudad
2. **Rutas**: Seleccionar múltiples rutas
3. **Opciones**: Fechas y descarga offline
4. **Resumen**: Confirmar y crear

### Búsqueda (AllRoutesScreen)
- Buscador por texto
- Filtro por dificultad
- Filtro por ciudad
- Ordenación múltiple
- Favoritos en cada card

## Firebase App Distribution

Para distribuir builds de prueba:

```bash
# Build APK
./gradlew assembleDebug

# Subir a Firebase App Distribution
firebase appdistribution:distribute app/build/outputs/apk/debug/app-debug.apk \
  --app "1:294754708542:android:64178b9f450ebc29c5b56f" \
  --release-notes "Descripción de cambios" \
  --testers "email@ejemplo.com"
```

## Estructura de datos en Firestore

### Collection: `routes`
```json
{
  "name": "Ruta del Madrid de los Austrias",
  "description": "Recorre el Madrid histórico...",
  "city": "Madrid",
  "neighborhood": "Centro",
  "difficulty": "easy",
  "durationMinutes": 60,
  "distanceKm": 2.5,
  "numStops": 8
}
```

### Subcollection: `routes/{routeId}/stops`
```json
{
  "name": "Plaza Mayor",
  "description": "Centro neurálgico del Madrid histórico...",
  "latitude": 40.4155,
  "longitude": -3.7074,
  "order": 1,
  "category": "Plaza",
  "funFact": "Dato curioso...",
  "audioText": "Texto que se reproducirá..."
}
```

## Equivalencias iOS → Android

| iOS | Android |
|-----|---------|
| SwiftUI | Jetpack Compose |
| Combine | Flow/StateFlow |
| @StateObject | viewModel() |
| @Published | MutableStateFlow |
| UserDefaults | SharedPreferences |
| CoreLocation | Google Location Services |
| CLCircularRegion | GeofencingClient |
| AVSpeechSynthesizer | TextToSpeech |
| MapKit | Google Maps SDK |

## Licencia

Proyecto privado - JRLabs
