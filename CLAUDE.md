# AudioCity Android - Contexto para Claude

## Resumen del Proyecto

AudioCity Android es la versión Android de la app iOS AudioCity. Es una aplicación de audioguías que reproduce narraciones automáticas cuando el usuario se acerca a puntos de interés turístico.

## Stack Tecnológico

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Arquitectura**: Clean Architecture + MVVM + SOLID
- **DI**: Hilt
- **Backend**: Firebase Firestore
- **Mapas**: Google Maps SDK + Maps Compose
- **Ubicación**: FusedLocationProvider + Geofencing
- **Audio**: TextToSpeech
- **Persistencia local**: SharedPreferences (trips, favorites, history, points, user routes)
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

---

## Arquitectura Clean + SOLID

El proyecto sigue **Clean Architecture** con principios **SOLID**, al mismo nivel que la app iOS:

### Capas de la Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │  Screens    │  │  ViewModels │  │  Components │          │
│  │  (Compose)  │  │  (StateFlow)│  │  (UI)       │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└────────────────────────────┬────────────────────────────────┘
                             │ depends on
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │  Use Cases  │  │  Models     │  │  Interfaces │          │
│  │  (Business  │  │  (Entities) │  │  (Repos &   │          │
│  │   Logic)    │  │             │  │   Services) │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└────────────────────────────┬────────────────────────────────┘
                             │ depends on
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │  DTOs       │  │  Mappers    │  │  Repository │          │
│  │  (Firebase) │  │  (DTO↔Model)│  │  Impl       │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
│  ┌─────────────┐                                             │
│  │  Service    │                                             │
│  │  Impl       │                                             │
│  └─────────────┘                                             │
└─────────────────────────────────────────────────────────────┘
```

### Principios SOLID Implementados

| Principio | Implementación |
|-----------|----------------|
| **S** - Single Responsibility | Cada clase tiene una única responsabilidad. Ej: `RouteMapper` solo mapea rutas |
| **O** - Open/Closed | Sealed classes para estados y errores. Nuevos tipos sin modificar código |
| **L** - Liskov Substitution | Implementaciones intercambiables vía interfaces |
| **I** - Interface Segregation | Interfaces específicas: `AudioService`, `LocationService`, `FavoritesService`, `PointsService`, etc. |
| **D** - Dependency Inversion | ViewModels dependen de interfaces, no implementaciones concretas |

---

## Estructura del Proyecto

```
app/src/main/java/com/jrlabs/audiocity/
│
├── domain/                          # CAPA DE DOMINIO (Núcleo)
│   ├── common/
│   │   └── Result.kt               # Result wrapper + AudioCityError sealed class
│   ├── model/
│   │   ├── Route.kt                # Modelo inmutable de ruta
│   │   ├── Stop.kt                 # Modelo inmutable de parada
│   │   ├── Trip.kt                 # Modelo inmutable de viaje
│   │   ├── UserPoints.kt           # Sistema de puntos y niveles
│   │   ├── RouteHistory.kt         # Historial de rutas completadas
│   │   └── UserRoute.kt            # Rutas creadas por usuario (UGC)
│   ├── repository/
│   │   ├── RouteRepository.kt      # Interface del repositorio de rutas
│   │   └── TripRepository.kt       # Interface del repositorio de viajes
│   ├── service/
│   │   ├── AudioService.kt         # Interface - Audio ruta activa
│   │   ├── AudioPreviewService.kt  # Interface - Preview de paradas
│   │   ├── LocationService.kt      # Interface - Ubicación
│   │   ├── GeofenceService.kt      # Interface - Geofencing
│   │   ├── FavoritesService.kt     # Interface - Favoritos
│   │   ├── NotificationService.kt  # Interface - Notificaciones
│   │   ├── PointsService.kt        # Interface - Gamificación
│   │   ├── HistoryService.kt       # Interface - Historial
│   │   ├── UserRoutesService.kt    # Interface - Rutas UGC
│   │   ├── RouteOptimizationService.kt  # Interface - Optimización
│   │   ├── RouteDistanceCalculator.kt   # Interface - Cálculo distancias
│   │   └── OfflineCacheService.kt  # Interface - Cache offline
│   └── usecase/
│       ├── route/                  # Use Cases de rutas
│       ├── trip/                   # Use Cases de viajes
│       ├── favorite/               # Use Cases de favoritos
│       ├── audio/                  # Use Cases de audio
│       └── location/               # Use Cases de ubicación
│
├── data/                            # CAPA DE DATOS (Implementaciones)
│   ├── dto/
│   │   ├── RouteDto.kt
│   │   ├── StopDto.kt
│   │   └── TripDto.kt
│   ├── mapper/
│   │   ├── RouteMapper.kt
│   │   ├── StopMapper.kt
│   │   └── TripMapper.kt
│   ├── repository/
│   │   ├── FirebaseRouteRepository.kt
│   │   └── LocalTripRepository.kt
│   ├── service/
│   │   ├── TextToSpeechAudioService.kt
│   │   ├── TextToSpeechAudioPreviewService.kt
│   │   ├── FusedLocationService.kt
│   │   ├── LocalFavoritesService.kt
│   │   ├── LocalPointsService.kt
│   │   ├── LocalHistoryService.kt
│   │   ├── LocalUserRoutesService.kt
│   │   └── DefaultRouteOptimizationService.kt
│   └── model/                       # Modelos legacy
│
├── di/                              # INYECCIÓN DE DEPENDENCIAS
│   ├── AppModule.kt
│   ├── RepositoryModule.kt
│   └── ServiceModule.kt
│
├── services/                        # Servicios Android (Foreground, Broadcast)
│
├── ui/                              # CAPA DE PRESENTACIÓN
│   ├── components/
│   ├── navigation/
│   ├── screens/
│   ├── theme/
│   └── viewmodel/
│
└── AudioCityApplication.kt
```

---

## Funcionalidades Portadas desde iOS

### ✅ Sistema de Rutas y Paradas
- Carga de rutas desde Firebase
- Detalle de ruta con paradas
- Mapa con markers de paradas
- Inicio de ruta con tracking

### ✅ Sistema de Viajes (Trip Planning)
- Wizard de 4 pasos para crear viajes
- Selección de destino
- Selección de rutas
- Fechas opcionales
- Descarga offline

### ✅ Sistema de Favoritos
- Toggle en cards de rutas
- Persistencia en SharedPreferences
- Sección "Rutas Favoritas" en home

### ✅ Sistema de Puntos y Niveles (Gamificación)
| Nivel | Puntos | Icono |
|-------|--------|-------|
| Explorador | 0-99 | directions_walk |
| Viajero | 100-299 | flight |
| Guía Local | 300-599 | map |
| Experto | 600-999 | star |
| Maestro AudioCity | 1000+ | emoji_events |

**Acciones que otorgan puntos:**
- Completar ruta 100%: 30 pts
- Primera ruta del día: 10 pts
- Racha 3 días: 50 pts
- Racha 7 días: 100 pts
- Crear ruta pequeña: 50 pts
- Crear ruta mediana: 100 pts
- Crear ruta extensa: 200 pts
- Publicar ruta: 20 pts

### ✅ Historial de Rutas
- Registro de rutas completadas
- Agrupación por fecha
- Estadísticas: total, distancia, tiempo

### ✅ Creación de Rutas (UGC)
- Crear rutas personalizadas
- Añadir/eliminar paradas
- Reordenar paradas
- Publicar/despublicar

### ✅ Optimización de Rutas
- Detección de parada más cercana
- Propuesta de reordenamiento
- Cálculo de distancias

### ✅ Audio
- AudioService para ruta activa (cola)
- AudioPreviewService para previews en detalle
- Text-to-Speech en español

### ✅ Notificaciones
- Notificación al llegar a parada
- Acciones: Escuchar / Saltar

---

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
| Protocol | Interface (Kotlin) |

---

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

---

## Sincronización con iOS

El proyecto Android debe mantenerse sincronizado con las features del proyecto iOS ubicado en:
`/Users/juanrafernandez/Documents/GitHub/AudioCity`

### Funcionalidades pendientes de UI:
- [ ] HistoryScreen (pantalla de historial)
- [ ] ProfileScreen con puntos y nivel
- [ ] CreateRouteScreen / MyRoutesScreen
- [ ] Búsqueda de direcciones en mapa (Places API)
- [ ] Live Activity (no disponible en Android, alternativa: Notification con ongoing)

---

## Notas de Desarrollo

1. **Clean Architecture**: Usar siempre Use Cases en ViewModels nuevos
2. **Interfaces**: Crear interface antes de implementación para servicios nuevos
3. **Inmutabilidad**: Usar `val` y `copy()` en modelos de dominio
4. **Errores**: Usar `AudioCityError` sealed class para errores tipados
5. **Mappers**: Todo dato de Firebase pasa por Mapper antes de llegar al ViewModel
6. **Testing**: Las interfaces facilitan mocking para unit tests
7. **Hilt**: Usar `@Binds` en módulos abstractos para interfaces
8. **Gamificación**: PointsService maneja automáticamente rachas y bonificaciones
9. **Historia**: HistoryService registra cada ruta completada
10. **UGC**: UserRoutesService permite crear, editar y publicar rutas

---

## Última Actualización: Diciembre 2024

### Arquitectura Clean Completa
- ✅ Capa Domain con interfaces y modelos inmutables
- ✅ Capa Data con DTOs, Mappers e implementaciones
- ✅ Result wrapper tipado con AudioCityError
- ✅ Inyección de dependencias con Hilt

### Servicios Portados de iOS
- ✅ PointsService (gamificación)
- ✅ HistoryService (historial)
- ✅ UserRoutesService (UGC)
- ✅ RouteOptimizationService
- ✅ AudioPreviewService
- ✅ FavoritesService
- ✅ LocationService
- ✅ AudioService

### APK en Firebase App Distribution
- Versión: 1.0 (1)
- Estado: Disponible para android-testers
- Console: https://console.firebase.google.com/project/audiocity-poc/appdistribution/app/android:com.jrlabs.audiocity/releases
