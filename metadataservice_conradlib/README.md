# ConnectedRadio Client Library (ConRadLib)

Eine Android-Bibliothek, die als Verbindungskomponente zwischen dem **MetaDataService** und dem **Connected Radio Backend** dient.

## Übersicht

Der ConnectedRadio Client (ConRadClient) ist eine Kotlin-basierte Android Library, die folgende Aufgaben übernimmt:

- **Datenaustausch**: Bereitet Daten vor und verarbeitet sie zwischen dem Backend und der MetaDataService-Anwendung
- **Hintergrundservice**: Holt und liefert Daten wie **Radio-Logos** und **Senderinformationen** vom Backend
- **Sichere Verbindung**: Kommuniziert sicher mit dem Connected Radio Backend
- **Datenoptimierung**: Minimiert Datenvolumen und Anzahl der Requests

## Voraussetzungen

| Komponente | Version |
|------------|---------|
| JDK | 17 |
| Android SDK | compileSdk 33 (Android 13), minSdk 28 (Android 9) |
| Gradle | 8.5 (via Wrapper) |
| Kotlin | 1.9.21 |

## Installation

### 1. Android SDK konfigurieren

Erstelle eine `local.properties` Datei im `ConnectedRadioClient/` Verzeichnis:

```properties
sdk.dir=/path/to/your/android/sdk
```

Beispiel für macOS:
```properties
sdk.dir=/Users/USERNAME/Library/Android/sdk
```

### 2. Build ausführen

```bash
cd ConnectedRadioClient
./gradlew clean build
```

### 3. Nur Tests ausführen

```bash
./gradlew test
```

### 4. Release AAR erstellen

```bash
./gradlew assembleRelease
```

## Build Output

Nach erfolgreichem Build findest du die AAR-Dateien in:

```
ConnectedRadioClient/app/build/outputs/aar/
├── app-debug.aar      # Debug-Version mit Debug-Symbolen
└── app-release.aar    # Release-Version (optimiert)
```

## Integration in ein Android-Projekt

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation(files("path/to/app-release.aar"))
    
    // Transitive Dependencies (müssen ebenfalls hinzugefügt werden)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation files('path/to/app-release.aar')
    
    // Transitive Dependencies
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.moshi:moshi-kotlin:1.15.0'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1'
}
```

## Projektstruktur

```
ConnectedRadioClient/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/de/esolutions/connectedradioclient/
│   │   │   │   ├── api/              # API-Kommunikation, Parser
│   │   │   │   ├── data/             # Datenmodelle
│   │   │   │   ├── handlers/         # Request-Handler (Backend, Media, etc.)
│   │   │   │   ├── irc/              # Incident Recording (No-Op)
│   │   │   │   └── logger/           # Logging mit android.util.Log
│   │   │   ├── res/                  # Android Resources
│   │   │   └── AndroidManifest.xml
│   │   └── test/                     # Unit Tests
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml            # Version Catalog
│   └── wrapper/
├── build.gradle.kts                  # Root Build Script
├── settings.gradle.kts               # Settings & Repositories
└── local.properties                  # Lokale Konfiguration (nicht committen!)
```

## Hauptkomponenten

| Komponente | Beschreibung |
|------------|--------------|
| `ConRadClient` | Einstiegspunkt der Bibliothek |
| `GenericMoshiParser` | JSON-Parsing mit Moshi |
| `LookupRequestJob` | Backend-Anfragen für Radio-Lookup |
| `TokenHandler` | Authentifizierung und Token-Management |
| `Logger` | Logging via `android.util.Log` |

## Abhängigkeiten

### Runtime Dependencies

| Dependency | Version | Beschreibung |
|------------|---------|--------------|
| `kotlin-stdlib-jdk8` | 1.9.21 | Kotlin Standard Library |
| `kotlin-reflect` | 1.9.21 | Kotlin Reflection |
| `androidx.core:core-ktx` | 1.10.1 | AndroidX Core Extensions |
| `okhttp` | 4.12.0 | HTTP Client |
| `moshi-kotlin` | 1.15.0 | JSON Parser |
| `kotlinx-coroutines-core` | 1.8.1 | Kotlin Coroutines |

### Test Dependencies

| Dependency | Version | Beschreibung |
|------------|---------|--------------|
| `junit-jupiter` | 5.11.2 | JUnit 5 Testing Framework |
| `mockito-core` | 5.14.2 | Mocking Framework |
| `okhttp-mockwebserver` | 4.12.0 | OkHttp Mock Server |

## Logging

Die Library verwendet `android.util.Log` für Logging. Log-Ausgaben sind mit dem Tag `connectedradioclient` versehen:

```kotlin
// Initialisierung (wird automatisch durchgeführt)
Logger.init("metadataservice")

// Log-Levels
Logger.t(PrivacyFlags.SAFE, logInfo, "Trace message")     // VERBOSE
Logger.d(PrivacyFlags.SAFE, logInfo, "Debug message")     // DEBUG
Logger.i(PrivacyFlags.SAFE, logInfo, "Info message")      // INFO
Logger.w(PrivacyFlags.SAFE, logInfo, "Warning message")   // WARN
Logger.e(PrivacyFlags.SAFE, logInfo, "Error message")     // ERROR
```

## Quality Tools

Das Projekt enthält integrierte Quality-Tools:

```bash
# Detekt (Kotlin Static Analysis)
./gradlew detekt

# Checkstyle
./gradlew checkstyle

# Code Coverage Report
./gradlew jacocoTestReport
```

## Dokumentation

Weitere Dokumentation findet sich im `doc/` Verzeichnis:

- [Architektur-Dokumentation](doc/main.adoc) - AsciiDoc Format
- [How to Build Custom MDS](doc/how_to_build_custom_mds.adoc)
- [How to Search in Logs](doc/how_to_search_in_logs.adoc)

## Berechtigungen

Die Library benötigt folgende Android-Berechtigungen:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Lizenz

Proprietär - e.solutions GmbH

## Support

Bei Fragen wende dich an das MetaDataService-Team.
