# MetadataService Client Interface

AIDL-based client library for binding to the MetadataService from external apps.
Provides `IMetadataService` (RxJava3 interface) and all required Parcelable data classes.

Package: `technology.cariad.entertainment.metadataserviceclient`

## Contents

### Generated Code (via rpc2aidl)

The `rpc2aidl.main.kts` script parses `IMetadataService.kt` and generates:

| File | Description |
|------|-------------|
| `IMetadataServiceAidl.aidl` | AIDL service interface (subscribe/dispose pairs) |
| `IMetadataServiceManagement.aidl` | Client registration management |
| `*Callback.aidl` | 8 callback interfaces for async results |
| `IMetadataServiceClientAdapter.kt` | Client-side adapter (RxJava ↔ AIDL) |
| `IMetadataServiceAidlProviderAdapter.kt` | Server-side adapter (AIDL → RxJava) |

### Data Classes (Parcelable)

`StationBearer`, `Station`, `StationMetadata`, `StationLogo`, `StationStream`,
`PermissionData`, `MusicMetadata`, `RecognizedSongInfo`, `CoverArtRequestData`,
`SongLyrics`, `ExternalIdentifier`, `ProviderIdentifier`, `MetadataError`,
`StationAsia`, `StationBearerAsia`

## Usage (Client App)

```kotlin
// Bind to MetadataService
val intent = Intent(IMetadataServiceAidl::class.java.name)
val matches = packageManager.queryIntentServices(intent, 0)
val explicit = Intent(intent).apply {
    component = ComponentName(matches[0].serviceInfo.applicationInfo.packageName,
                              matches[0].serviceInfo.name)
}
bindService(explicit, serviceConnection, Context.BIND_AUTO_CREATE)

// In onServiceConnected:
val service = IMetadataServiceClientAdapter.fromBinder(binder, context)

// Query station data
service.observeRadioStationData(bearer, "")
    .subscribe { station -> /* use station.metadata, station.logo, etc. */ }
```

## Build

Built as a library module included in the MetadataService project:

```gradle
// metadataservice/settings.gradle
include ':clientinterface'
project(':clientinterface').projectDir = new File('../metadataservice_clientinterface/lib')
```

## Regenerating AIDL Code

```bash
cd metadataservice_clientinterface
kotlin rpc2aidl.main.kts
```

This reads `lib/src/rpc/java/.../IMetadataService.kt` and regenerates all
AIDL files and adapters in `lib/src/main/aidl/` and `lib/src/rpc/java/`.

## Dependencies

- RxJava3 3.1.5
- Kotlin 1.9.21
- Android SDK 33+

