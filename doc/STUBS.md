# Stub Implementations

This document lists all stub/no-op implementations that replace proprietary
vendor (`eso.fw`) and VW framework dependencies removed during the
Cariad fork (Feb 2026). These stubs allow the app to build and run without
access to internal infrastructure.

---

## 1. ASI/Storage (6 files)

**Replaces:** `vendor.eso.fw.android.comm.asi.diag.pers.*`
**Location:** `metadataservice/app/src/main/java/technology/cariad/entertainment/metadataservice/stubs/fw/asi/`

| File | Behavior |
|------|----------|
| `StorageClientAdapter.kt` | No-op — stores nothing |
| `IStorageServiceListener.kt` | Interface only |
| `OpResult.kt` | Enum |
| `OptionalValue.kt` | Value wrapper |
| `OptionalValueOneDimArrayObjectWrapper.kt` | Array wrapper |
| `Consts.kt` | Constants |

**Impact:** Vehicle coding/settings persistence does not work. App falls back to internal storage.

---

## 2. Tracing (3 files)

**Replaces:** `vendor.eso.fw.util.commons.tracing.stapi.*`
**Location:** `metadataservice/app/src/main/java/technology/cariad/entertainment/metadataservice/stubs/fw/tracing/`

| File | Behavior |
|------|----------|
| `TraceClient.kt` | No-op — `registerTraceCallback` does nothing |
| `ITraceCallback.kt` | Interface only |
| `TraceLevels.kt` | Level constants |

**Impact:** None. Logging uses `Logger.kt` → `android.util.Log` instead.

---

## 3. Framework Clients (3 files)

**Replaces:** `vendor.android.framework.clients.*`
**Location:** `metadataservice/app/src/main/java/de/esolutions/android/framework/clients/`

| File | Behavior |
|------|----------|
| `AsiAdmin.kt` | No-op — `init()` logs warning, continues |
| `IServiceCallback.kt` | Interface only |
| `types/OneDimShortArrayWrapper.kt` | Array wrapper |

**Impact:** ASI Admin unavailable. App continues with warning (non-fatal).

---

## 4. Storage Manager (2 files)

**Replaces:** `com.volkswagenag.storagemanager.*`
**Location:** `metadataservice/app/src/main/java/com/volkswagenag/storagemanager/`

| File | Behavior |
|------|----------|
| `ExtStorageManager.kt` | No-op — returns `null` |
| `ExtStorageManagerFactory.kt` | Returns stub instance |

**Impact:** No extended storage. App falls back to internal app storage.

---

## 5. Navigation (4 files)

**Replaces:** `com.volkswagenag.nav.positioninformation.*`
**Location:** `metadataservice/app/src/main/java/com/volkswagenag/nav/positioninformation/`

| File | Behavior |
|------|----------|
| `IPositionInformation.kt` | No-op service interface |
| `IPositionInformationCallback.kt` | Interface only |
| `GeneralPositionInformationType.kt` | Data class |
| `RoadInformationType.kt` | Data class |

**Impact:** No GPS data. Location-based features inactive.

---

## 6. gmdlib (5 files)

**Replaces:** `technology.cariad.entertainment.metadataservice:gmdlib` AAR
**Location:** `metadataservice/app/src/main/java/de/esolutions/gmdlib/`

| File | Behavior |
|------|----------|
| `media/GmdClient.kt` | Always returns error |
| `media/IGracenoteMusicDataCallback.kt` | Interface only |
| `data/GmdErrorType.kt` | Error enum |
| `data/AccountManagerConfig.kt` | Data class |
| `data/EndpointConfig.kt` | Data class |

**Impact:** Online metadata queries via GMD fail gracefully. Gracenote SDK itself (fingerprinting, local lookup) works.

---

## 7. Logger Replacements (3 files)

**Replaces:** vendor `eso.fw.tracing` logging framework
**Location:** `metadataservice/app/src/main/java/technology/cariad/entertainment/metadataservice/common/log/`

| File | Behavior |
|------|----------|
| `Logger.kt` | Full rewrite → `android.util.Log` |
| `PrivacyFlag.kt` | Local enum (replaces vendor `eso.fw` variant) |
| `IRCLogger.kt` | No-op — logs locally only |

**Impact:** None. Logging works fully via Android Log.

---

## Summary

**Total: 26 stub files** across 7 categories.

### What works without real frameworks
- Gracenote SDK (audio fingerprinting, local metadata)
- ConRadLib v2 legacy backend
- AIDL client interface (cross-process IPC)
- Demo app with Settings UI
- All supported build variants (legacy profiles)

### What does NOT work without real frameworks
- ASI vehicle storage (coding persistence)
- Extended Storage Manager
- GPS/Navigation position data
- GMD online metadata lookup
- IRC incident recording

