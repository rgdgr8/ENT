# MetadataService Client Implementation Guide

## Übersicht

Der **MetadataService (MDS)** ist ein Android-System-Service, der Metadaten für Audioinhalte bereitstellt. Er fungiert als zentrale Schnittstelle zwischen Client-Apps (z.B. Radio-Apps, Media-Apps) und verschiedenen Backend-Diensten (Gracenote SDK, ConnectedRadio Backend).

### Hauptfunktionen

- **Cover Art Lookup**: Album-Cover basierend auf Artist/Album/Title
- **Song Recognition**: Musikerkennung via Audio-Fingerprinting
- **Radio Station Data**: Metadaten zu Radiosendern (Logo, Genre, Webstreams, etc.)
- **Lyrics**: Songtexte für aktuell spielende Titel

## Architektur

```
┌─────────────────────┐
│   Client App        │
│   (Radio/Media)     │
└──────────┬──────────┘
           │ AIDL Binding
           │
┌──────────▼──────────────────────────────────┐
│     MetadataService (System Service)        │
│  ┌────────────────────────────────────────┐ │
│  │  IMetadataServiceAidl Interface        │ │
│  └────────────────────────────────────────┘ │
│           │           │           │          │
│     ┌─────▼─────┐ ┌──▼──────┐ ┌─▼─────┐   │
│     │ CoverArt  │ │SongInfo │ │Radio  │   │
│     │Repository │ │Repository│ │Data   │   │
│     └─────┬─────┘ └────┬─────┘ └─┬─────┘   │
└───────────┼────────────┼─────────┼─────────┘
            │            │         │
     ┌──────▼──────┐ ┌───▼────┐ ┌▼──────────┐
     │  Gracenote  │ │Gracenote│ │ConnectedRadio│
     │  Cover Art  │ │ MusicID │ │  Backend    │
     │     SDK     │ │   SDK   │ │             │
     └─────────────┘ └─────────┘ └─────────────┘
```

## Client Implementation

### 1. Dependencies

Füge das MetadataService Client Library zu deiner `build.gradle` hinzu:

```gradle
dependencies {
    implementation files('libs/metadataservice-client.aar')
    // oder als Maven/Artifact wenn deployed
}
```

### 2. Manifest Permissions

Füge folgende Permission zu deiner `AndroidManifest.xml` hinzu:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.myradioapp">
    
    <!-- Required: Permission to access MetadataService -->
    <uses-permission android:name="technology.cariad.entertainment.metadataservice.USE_SERVICE" />
    
    <application>
        ...
    </application>
</manifest>
```

**Wichtig**: Diese Permission hat `protectionLevel="signature|privileged"`, d.h.:
- Apps müssen mit dem **gleichen Signatur-Key** wie MetadataService signiert sein **ODER**
- Als **System/Privileged App** installiert sein (vorinstalliert auf `/system/priv-app`)

Für **Entwicklung/Testing**:
- Nutze das gleiche Debug-Keystore wie MetadataService
- Oder installiere beide Apps mit `adb install -g` (grant permissions)

### 3. Service Binding

#### 3.1 AIDL Interface definieren

Die AIDL-Schnittstelle ist bereits im Client-Library enthalten:

```kotlin
import technology.cariad.entertainment.metadataserviceclient.api.IMetadataServiceAidl
```

#### 3.2 Service Connection erstellen

```kotlin
class MyRadioActivity : AppCompatActivity() {
    
    private var metadataService: IMetadataServiceAidl? = null
    private var clientId: Int = -1
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            metadataService = IMetadataServiceAidl.Stub.asInterface(service)
            
            // Registriere Client beim Service
            clientId = metadataService?.register(
                "MyRadioApp",
                android.os.Process.myUid()
            ) ?: -1
            
            Log.d(TAG, "Connected to MetadataService, clientId=$clientId")
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            metadataService = null
            clientId = -1
            Log.d(TAG, "Disconnected from MetadataService")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindToMetadataService()
    }
    
    private fun bindToMetadataService() {
        val intent = Intent().apply {
            component = ComponentName(
                "technology.cariad.entertainment.metadataservice",
                "technology.cariad.entertainment.metadataservice.service.MetadataService"
            )
        }
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Unregister Client
        metadataService?.unregister(clientId)
        unbindService(serviceConnection)
    }
}
```

### 4. Cover Art Lookup

#### 4.1 Cover Art Request

```kotlin
import technology.cariad.entertainment.metadataserviceclient.api.ICoverArtCallback
import technology.cariad.entertainment.metadataserviceclient.api.CoverArtRequestData

fun requestCoverArt(artist: String, album: String, title: String) {
    val requestData = CoverArtRequestData().apply {
        this.artist = artist
        this.album = album
        this.title = title
    }
    
    val callback = object : ICoverArtCallback.Stub() {
        override fun onCoverArtResult(
            requestId: Int,
            imageUri: String?,
            error: String?
        ) {
            runOnUiThread {
                when {
                    imageUri != null -> {
                        // Cover Art gefunden
                        loadCoverArtImage(imageUri)
                    }
                    error != null -> {
                        Log.e(TAG, "Cover Art Error: $error")
                    }
                    else -> {
                        Log.w(TAG, "No Cover Art found")
                    }
                }
            }
        }
    }
    
    metadataService?.getCoverArt(clientId, requestData, callback)
}

private fun loadCoverArtImage(uri: String) {
    // Lade Bild mit Glide, Picasso oder ContentProvider
    val contentUri = Uri.parse(uri)
    
    Glide.with(this)
        .load(contentUri)
        .into(coverArtImageView)
}
```

#### 4.2 Request canceln

```kotlin
fun cancelCoverArtRequest(requestId: Int) {
    metadataService?.disposeCoverArtRequest(clientId, requestId)
}
```

### 5. Radio Station Data Lookup

#### 5.1 Station Info Request

```kotlin
import technology.cariad.entertainment.metadataserviceclient.api.IRadioStationCallback
import technology.cariad.entertainment.metadataserviceclient.api.RadioStationRequestData
import technology.cariad.entertainment.metadataserviceclient.api.RadioStation

fun requestRadioStationData(frequency: Int, pi: Int) {
    val requestData = RadioStationRequestData().apply {
        this.frequency = frequency
        this.pi = pi
    }
    
    val callback = object : IRadioStationCallback.Stub() {
        override fun onRadioStationResult(
            requestId: Int,
            station: RadioStation?,
            error: String?
        ) {
            runOnUiThread {
                when {
                    station != null -> {
                        updateStationUI(station)
                    }
                    error != null -> {
                        Log.e(TAG, "Station Lookup Error: $error")
                    }
                }
            }
        }
    }
    
    metadataService?.getRadioStationData(clientId, requestData, callback)
}

private fun updateStationUI(station: RadioStation) {
    // Logo
    station.logoUri?.let { loadLogo(it) }
    
    // Station Name
    stationNameTextView.text = station.name
    
    // Genre
    genreTextView.text = station.genre
    
    // Webstreams
    station.webStreams?.forEach { stream ->
        addWebStreamOption(stream.url, stream.bitrate)
    }
}
```

### 6. Song Recognition

```kotlin
import technology.cariad.entertainment.metadataserviceclient.api.ISongRecognitionCallback
import technology.cariad.entertainment.metadataserviceclient.api.SongInfo

fun startSongRecognition(audioData: ByteArray) {
    val callback = object : ISongRecognitionCallback.Stub() {
        override fun onSongRecognitionResult(
            requestId: Int,
            songInfo: SongInfo?,
            error: String?
        ) {
            runOnUiThread {
                when {
                    songInfo != null -> {
                        displaySongInfo(songInfo)
                    }
                    error != null -> {
                        Log.e(TAG, "Recognition Error: $error")
                    }
                }
            }
        }
    }
    
    // Audio-Fingerprint wird automatisch erstellt
    metadataService?.recognizeSong(clientId, audioData, callback)
}

private fun displaySongInfo(info: SongInfo) {
    titleTextView.text = info.title
    artistTextView.text = info.artist
    albumTextView.text = info.album
    
    // Optional: Cover Art für erkannten Song laden
    requestCoverArt(info.artist ?: "", info.album ?: "", info.title ?: "")
}
```

## Best Practices

### 1. Lifecycle Management

```kotlin
class MetadataServiceClient(private val context: Context) : LifecycleObserver {
    
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun connect() {
        bindToService()
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun disconnect() {
        cancelAllRequests()
        unbindService()
    }
}
```

### 2. Request Tracking

```kotlin
class CoverArtManager {
    private val activeRequests = mutableMapOf<Int, CoverArtRequest>()
    
    fun requestCoverArt(data: CoverArtRequestData): Int {
        val requestId = generateRequestId()
        val request = CoverArtRequest(requestId, data)
        
        activeRequests[requestId] = request
        
        metadataService?.getCoverArt(clientId, data, request.callback)
        
        return requestId
    }
    
    fun cancelRequest(requestId: Int) {
        activeRequests.remove(requestId)?.let {
            metadataService?.disposeCoverArtRequest(clientId, requestId)
        }
    }
    
    fun cancelAll() {
        activeRequests.keys.forEach { cancelRequest(it) }
    }
}
```

### 3. Error Handling

```kotlin
sealed class MetadataResult<T> {
    data class Success<T>(val data: T) : MetadataResult<T>()
    data class Error<T>(val message: String) : MetadataResult<T>()
    data class NoContent<T>(val reason: String) : MetadataResult<T>()
}

fun handleCoverArtResult(uri: String?, error: String?): MetadataResult<Uri> {
    return when {
        uri != null -> MetadataResult.Success(Uri.parse(uri))
        error != null -> MetadataResult.Error(error)
        else -> MetadataResult.NoContent("No cover art found")
    }
}
```

### 4. Caching

Der MetadataService cached automatisch, aber Client-seitiges Caching verbessert Performance:

```kotlin
class CoverArtCache {
    private val memoryCache = LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    )
    
    fun getCoverArt(
        artist: String,
        album: String,
        onResult: (Bitmap?) -> Unit
    ) {
        val key = "$artist|$album"
        
        // Check memory cache
        memoryCache.get(key)?.let {
            onResult(it)
            return
        }
        
        // Request from service
        requestFromService(artist, album) { bitmap ->
            bitmap?.let { memoryCache.put(key, it) }
            onResult(bitmap)
        }
    }
}
```

## Datenmodelle

### CoverArtRequestData

```kotlin
class CoverArtRequestData : Parcelable {
    var artist: String? = null
    var album: String? = null
    var title: String? = null
}
```

### RadioStationRequestData

```kotlin
class RadioStationRequestData : Parcelable {
    var frequency: Int = 0           // in kHz (z.B. 98500 für 98.5 MHz)
    var pi: Int = 0                  // RDS Programme Identification
    var providerIdentifier: ProviderIdentifier? = null
}
```

### RadioStation

```kotlin
class RadioStation : Parcelable {
    var name: String? = null
    var genre: String? = null
    var logoUri: String? = null
    var logoSmallUri: String? = null
    var webStreams: List<WebStream>? = null
    var description: String? = null
}
```

### SongInfo

```kotlin
class SongInfo : Parcelable {
    var title: String? = null
    var artist: String? = null
    var album: String? = null
    var duration: Int? = null
    var externalIdentifiers: List<ExternalIdentifier>? = null
}
```

## Debugging

### Logcat Filtern

```bash
adb logcat -s MetadataService:V YourApp:V
```

### Demo App verwenden

Die MetadataService Demo-App (`devRowDebug` Build) kann zum Testen verwendet werden:

1. **Cover Art Lookup Fragment**: Teste Cover Art Anfragen manuell
2. **Settings**: 
   - ServiceList Check deaktivieren (für Emulator)
   - DisableReasons Check deaktivieren (für Emulator)
   - Token override für Backend-Tests

## Troubleshooting

### Service Binding schlägt fehl

**Problem**: `bindService()` gibt `false` zurück

**Lösung**:
- Prüfe, ob MetadataService installiert ist: `adb shell pm list packages | grep metadataservice`
- Prüfe Component Name: `technology.cariad.entertainment.metadataservice/.service.MetadataService`

### Keine Ergebnisse (Cover Art)

**Problem**: Callback wird mit `null` aufgerufen

**Mögliche Ursachen**:
- Netzwerkverbindung fehlt
- Ungültige Suchdaten (Artist/Album leer)
- Service List blockiert Anfragen (in Demo-App: "Check ServiceList" deaktivieren)
- Backend-Token abgelaufen

### Radio Station Lookup schlägt fehl

**Problem**: `onRadioStationResult` gibt Error zurück

**Mögliche Ursachen**:
- Backend-Token fehlt oder ungültig
- Frequenz/PI nicht gefunden
- Netzwerk nicht verfügbar
- Backend-URL falsch konfiguriert

## Beispiel-Implementation

Ein vollständiges Beispiel findet sich im `ConnectedRadioClient` Modul:

```
metadataservice_conradlib/ConnectedRadioClient/
├── app/src/main/java/de/esolutions/connectedradioclient/
│   ├── MetadataServiceClient.kt
│   ├── CoverArtManager.kt
│   └── RadioStationManager.kt
```

## Support

Bei Fragen oder Problemen:

- **Issues**: Siehe `Readme.txt` im Projekt-Root
- **Dokumentation**: `metadataservice/user_manual/`
- **API Changes**: `CHANGELOG.md`

---

**Version**: 1.7.2  
**Letzte Aktualisierung**: 2026-02-27

