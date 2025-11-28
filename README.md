# AudioCity Android

Aplicación de audioguías que reproduce narraciones automáticas cuando el usuario se acerca a puntos de interés turístico.

## Características

- **Rutas guiadas**: Explora rutas turísticas con paradas de audio
- **Detección automática**: Reproduce audio al acercarte a puntos de interés mediante geofencing
- **Text-to-Speech**: Narración en español con soporte para múltiples idiomas
- **Seguimiento en segundo plano**: Continúa funcionando con la app cerrada
- **Mapas interactivos**: Visualiza tu posición y las paradas en Google Maps

## Tecnologías

- **Kotlin** + **Jetpack Compose**
- **MVVM** con StateFlow
- **Hilt** (Dependency Injection)
- **Firebase Firestore** (Backend)
- **Google Maps SDK** + Maps Compose
- **FusedLocationProvider** + Geofencing
- **TextToSpeech** para narración
- **Foreground Service** para tracking en segundo plano

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
│   ├── model/          # Route, Stop, AudioQueueItem
│   └── repository/     # FirebaseRepository
├── di/                 # Hilt modules
├── services/           # Location, Geofence, Audio, ForegroundService
├── ui/
│   ├── components/     # Componentes reutilizables
│   ├── navigation/     # NavGraph
│   ├── screens/        # Pantallas de la app
│   ├── theme/          # Colores, tipografía, tema
│   └── viewmodel/      # ViewModels
└── AudioCityApplication.kt
```

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

## Licencia

Proyecto privado - JRLabs
