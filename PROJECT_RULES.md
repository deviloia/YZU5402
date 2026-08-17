# Wi-Fi Fingerprint Android Project Rules

This file is the required development reference for this Android campus Wi-Fi fingerprint positioning research app. Read it before changing code.

## 1. Project Purpose

This project is an Android research app for campus Wi-Fi fingerprint positioning and GPS comparison.

The app is intended to:

- Build a formal Wi-Fi Fingerprint Database.
- Run indoor positioning experiments using KNN / WKNN.
- Compare Wi-Fi prediction, raw phone GPS, and manually confirmed Ground Truth.
- Analyze RSSI stability, AP appearance frequency, device differences, GPS error, Wi-Fi error, confidence, and positioning accuracy.
- Export experiment data later for Excel / Python / thesis analysis.

## 2. Technology Stack

Planned / project-level technologies:

- Android Studio
- Kotlin
- Jetpack Compose
- Firebase Firestore
- Android Wi-Fi API
- Android Location / GPS
- Google Maps SDK
- KNN / WKNN
- CSV export

Current implementation status:

- UI is Jetpack Compose, not XML.
- `MainActivity` is only the Compose app entry point.
- Compose Navigation is implemented with `navigation/AppDestination.kt` and `navigation/AppNavGraph.kt`.
- Firebase Firestore SDK is added through Firebase BoM.
- `app/google-services.json` exists.
- Firestore access is implemented only through repositories.
- `DeviceInfoProvider` exists and reads Android `Build.*` device information.
- GPS, real Wi-Fi scanning, KNN/WKNN, Google Maps, real CSV export, real Collect upload, and real Test Mode are not implemented yet.

## 3. Core Data Rule

The system must always keep these three concepts separate:

- `locations` = manually confirmed Ground Truth.
- `fingerprintSamples` = formal Wi-Fi fingerprint reference database.
- `testResults` = actual positioning test results.

Short version:

- `locations` = true physical position reference.
- `fingerprintSamples` = answer/reference database.
- `testResults` = each experiment/test record.

Never merge these collections or treat them as interchangeable.

## 4. GPS, Ground Truth, and Wi-Fi Prediction

The app must distinguish three position types:

- GPS Position: raw latitude/longitude/accuracy reported by the phone.
- Ground Truth: manually confirmed real location managed by `locations`.
- Wi-Fi Prediction: predicted result from Wi-Fi fingerprint matching and KNN/WKNN.

Hard rules:

- Raw GPS must be permanently preserved.
- Manual Ground Truth must never overwrite GPS fields.
- Wi-Fi Prediction must not be confused with Ground Truth.
- Ground Truth correction must update `locations/{locationId}`, not rewrite GPS coordinates in `fingerprintSamples` or `testResults`.
- If a Ground Truth coordinate or prediction is missing, use `null`; do not use `UNKNOWN`, an empty string, `0`, or `0.0` as fake missing data.

## 5. Firestore Collections

The only main Firestore collections are:

- `locations`
- `fingerprintSamples`
- `testResults`

Current implementation:

- Collection names are centralized in `firebase/FirestoreCollections.kt`.
- Repositories use the constants:
  - `LOCATIONS = "locations"`
  - `FINGERPRINT_SAMPLES = "fingerprintSamples"`
  - `TEST_RESULTS = "testResults"`

Do not hardcode these collection names across unrelated files.

## 6. Collection Relationships

`locations` is the Ground Truth source of truth.

`fingerprintSamples` stores `locationId`, pointing to `locations/{locationId}`.

`testResults` stores `trueLocationId`, pointing to `locations/{locationId}` when Ground Truth is known.

Example:

```text
locations/B5_4F_5402_WINDOW
```

represents:

```text
五館 / 4F / 5402 / 窗戶旁
```

If Ground Truth for that physical place is corrected later, update only:

```text
locations/B5_4F_5402_WINDOW
```

All related fingerprint samples and test results should continue to reference that same location id.

Batch Ground Truth correction should identify a physical location by:

```text
buildingId + floorId + positionName + subPosition
```

Do not batch-correct only by `positionName` unless the user explicitly chooses whole-room shared Ground Truth.

## 7. Current Model Design

All model time fields currently use `Long?` as epoch milliseconds. Do not change these to Firebase `Timestamp` unless a later phase explicitly requests it.

### LocationPoint

File:

```text
app/src/main/java/com/example/yzuwifilocationresearch/model/LocationPoint.kt
```

Fields:

```kotlin
locationId: String
buildingId: String
floorId: String
positionName: String
subPosition: String
manualLatitude: Double?
manualLongitude: Double?
note: String
updatedAt: Long?
```

Rules:

- Represents one manually managed Ground Truth location.
- `manualLatitude` and `manualLongitude` may be `null` before manual confirmation.
- Ground Truth is updated here, not by overwriting GPS fields elsewhere.

### AccessPoint

File:

```text
app/src/main/java/com/example/yzuwifilocationresearch/model/AccessPoint.kt
```

Fields:

```kotlin
ssid: String
bssid: String
frequency: Int
appearanceCount: Int
meanRssi: Double
minRssi: Int
maxRssi: Int
standardDeviation: Double
```

Rules:

- Represents a statistically processed AP after multiple scans.
- BSSID is the main AP identity for matching.
- SSID is display/supporting information only.

### FingerprintSample

File:

```text
app/src/main/java/com/example/yzuwifilocationresearch/model/FingerprintSample.kt
```

Fields:

```kotlin
documentId: String
buildingId: String
floorId: String
positionName: String
subPosition: String
locationId: String
note: String
deviceBrand: String
deviceModel: String
androidVersion: String
gpsLatitude: Double?
gpsLongitude: Double?
gpsAccuracy: Double?
scanCount: Int
accessPoints: List<AccessPoint>
createdAt: Long?
```

Rules:

- Only Collect Mode may create `FingerprintSample`.
- `locationId` must remain non-null because collection happens at a known logical place.
- `gpsLatitude`, `gpsLongitude`, and `gpsAccuracy` are raw phone GPS from the collection moment.
- Manual Ground Truth must never overwrite these GPS fields.

### TestResult

File:

```text
app/src/main/java/com/example/yzuwifilocationresearch/model/TestResult.kt
```

Fields:

```kotlin
documentId: String
trueLocationId: String?
deviceBrand: String
deviceModel: String
androidVersion: String
gpsLatitude: Double?
gpsLongitude: Double?
gpsAccuracy: Double?
gpsTimestamp: Long?
predictedLocationId: String?
predictedBuildingId: String?
predictedFloorId: String?
predictedPositionName: String?
predictedSubPosition: String?
knnK: Int
confidence: Double?
apCount: Int
accessPoints: List<AccessPoint>
gpsErrorMeters: Double?
wifiErrorMeters: Double?
createdAt: Long?
```

Rules:

- Test Mode creates `TestResult`.
- `trueLocationId` may be `null` when manual Ground Truth is not set yet.
- Prediction fields may be `null` if Wi-Fi scan fails, AP count is insufficient, `fingerprintSamples` is empty, or KNN cannot produce a result.
- `gpsErrorMeters` and `wifiErrorMeters` may be `null` until Ground Truth is available.
- Do not use `0.0` for uncalculated errors.
- Test results must never be automatically inserted into `fingerprintSamples`.

### WifiScanResult

File:

```text
app/src/main/java/com/example/yzuwifilocationresearch/model/WifiScanResult.kt
```

Fields:

```kotlin
ssid: String
bssid: String
rssi: Int
frequency: Int
```

Rules:

- Represents one raw AP from one Wi-Fi scan.
- Do not confuse it with `AccessPoint`.
- `WifiScanResult` = raw single scan data.
- `AccessPoint` = processed multi-scan fingerprint AP statistics.

### DeviceInfo

File:

```text
app/src/main/java/com/example/yzuwifilocationresearch/model/DeviceInfo.kt
```

Fields:

```kotlin
deviceBrand: String
deviceModel: String
androidVersion: String
apiLevel: Int
```

Rules:

- `deviceBrand`, `deviceModel`, and `androidVersion` must be the shared source for future `FingerprintSample` and `TestResult` device fields.
- `apiLevel` is retained for app/debug use and is not currently part of the Firestore schema.

## 8. Device Info

Current implementation:

```text
app/src/main/java/com/example/yzuwifilocationresearch/device/DeviceInfoProvider.kt
```

Reads:

```kotlin
Build.MANUFACTURER
Build.MODEL
Build.VERSION.RELEASE
Build.VERSION.SDK_INT
```

Rules:

- Device info must come from `DeviceInfoProvider`.
- Do not hand-build different device strings in Collect/Test flows.
- Brand display formatting may make values readable, such as `samsung -> Samsung` and `HTC -> HTC`.
- Do not invent fake brands when Android returns blank or unknown data.

## 9. Collect Mode

Collect Mode is for building the formal Wi-Fi fingerprint reference database.

Flow:

```text
Home -> Wi-Fi 指紋採集 -> choose building/floor/position/subPosition/note
-> get device info
-> get raw GPS
-> perform multiple Wi-Fi scans
-> group by BSSID
-> compute Wi-Fi statistics
-> create FingerprintSample
-> write to fingerprintSamples
```

Rules:

- Collect Mode may write only to `fingerprintSamples` and related `locations` when needed.
- Collect Mode must not create `testResults`.
- Default future scan count is `10` unless explicitly changed.
- The current app only displays mock Collect UI plus real device info; real Wi-Fi/GPS/upload is not implemented yet.

## 10. Test Mode

Test Mode is for actual positioning experiments.

Flow:

```text
Home -> Wi-Fi 指紋掃描 -> Loading
-> get GPS
-> scan Wi-Fi
-> read fingerprintSamples
-> KNN / WKNN
-> Confidence
-> create TestResult
-> Result
```

Rules:

- Test Mode may write only to `testResults`.
- Test Mode must never automatically add data to `fingerprintSamples`.
- Do not create a separate Wi-Fi scan settings page unless the user explicitly requests it.
- If scan/prediction fails, nullable fields must remain `null`.

## 11. Wi-Fi Scan and Statistics Rules

Wi-Fi scanning design:

- `WifiScanner` calls Android Wi-Fi scan APIs.
- `WifiScanProcessor` filters, organizes, and groups APs by BSSID.
- `WifiStatistics` calculates AP statistics.

AP identity:

- Use BSSID as the main AP matching key.
- Do not match APs only by SSID.

Statistics:

```text
appearanceCount = number of times AP appears
meanRssi = average RSSI
minRssi = weakest RSSI
maxRssi = strongest RSSI
standardDeviation = RSSI standard deviation
```

Standard deviation rule:

```text
population standard deviation = sqrt(sum((RSSI - meanRSSI)^2) / N)
```

Current implementation status:

- Wi-Fi scan and statistics logic are placeholders only.
- Do not implement them unless the current phase explicitly requests it.

## 12. KNN / WKNN and Confidence Rules

KNN/WKNN flow:

```text
current scan
-> compare with fingerprintSamples by common BSSID
-> calculate RSSI distance
-> sort
-> select top K
-> vote / weighted vote
-> predictedLocationId
```

Confidence rule:

- Confidence is a relative positioning confidence value.
- It is not a calibrated real probability.

For WKNN:

```text
Wi = 1 / (Di + 1)
Confidence = winning location weights / total K-neighbor weights
```

Code ownership:

- KNN belongs in `positioning/KnnLocator`.
- Distance calculations belong in `positioning/DistanceCalculator`.
- Confidence calculations belong in `positioning/ConfidenceCalculator`.
- Do not write KNN logic in Activity or UI screens.

Current implementation status:

- KNN, WKNN, confidence, and distance calculation logic are placeholders only.

## 13. Firebase and Repository Rules

Firestore access must go through repositories only.

Current repositories:

```text
firebase/LocationRepository.kt
firebase/FingerprintRepository.kt
firebase/TestResultRepository.kt
```

Functions currently available:

```kotlin
LocationRepository.saveLocation(location)
LocationRepository.getLocation(locationId)
LocationRepository.updateLocation(location)
LocationRepository.getAllLocations()

FingerprintRepository.addFingerprint(sample)
FingerprintRepository.getFingerprint(documentId)
FingerprintRepository.getAllFingerprints()

TestResultRepository.addTestResult(result)
TestResultRepository.getTestResult(documentId)
TestResultRepository.getAllTestResults()
```

Rules:

- Screens must not directly call Firestore.
- Activities must not directly write Firestore queries.
- Do not swallow repository exceptions silently.
- Repository APIs may use `suspend` functions.
- Do not add Firebase Auth or Analytics unless explicitly requested.
- Do not write real data during unit tests.

Current implementation status:

- Firebase SDK and repositories exist.
- UI does not currently call repositories.
- No current UI path writes real Firebase data.

## 14. UI and Responsibility Separation

Current app uses Compose screens, not multiple Activities:

```text
ui/home/HomeScreen.kt
ui/collect/CollectScreen.kt
ui/scan/ScanLoadingScreen.kt
ui/result/ResultScreen.kt
ui/history/HistoryScreen.kt
ui/locationedit/LocationEditScreen.kt
```

Rules:

- `MainActivity` should remain the Compose app entry point.
- Do not put all app logic in `MainActivity`.
- UI screens should display state and handle UI events, not contain Firebase, GPS, Wi-Fi scanning, or KNN logic.
- Use package boundaries:
  - `ui` for Compose UI
  - `model` for data models
  - `device` for device information
  - `firebase` for Firestore repositories/mappers
  - `wifi` for Wi-Fi scanning/processing/statistics
  - `gps` for GPS/location
  - `positioning` for KNN/distance/confidence
  - `map` for map marker management
  - `export` for CSV export

## 15. Null and Missing Data Rules

Do not use fake placeholders for missing data.

Forbidden substitutes for missing values:

```text
""
"UNKNOWN"
0
0.0
```

Use `null` when a value is genuinely unknown or not calculated.

Important nullable fields include:

- `LocationPoint.manualLatitude`
- `LocationPoint.manualLongitude`
- `FingerprintSample.gpsLatitude`
- `FingerprintSample.gpsLongitude`
- `FingerprintSample.gpsAccuracy`
- `TestResult.trueLocationId`
- `TestResult.gpsLatitude`
- `TestResult.gpsLongitude`
- `TestResult.gpsAccuracy`
- `TestResult.gpsTimestamp`
- `TestResult.predictedLocationId`
- `TestResult.predictedBuildingId`
- `TestResult.predictedFloorId`
- `TestResult.predictedPositionName`
- `TestResult.predictedSubPosition`
- `TestResult.confidence`
- `TestResult.gpsErrorMeters`
- `TestResult.wifiErrorMeters`

Default strings in Kotlin models are allowed mainly for Firestore deserialization convenience. They must not be used to pretend real research data exists.

## 16. Phase Discipline

Do not implement future phases early.

Examples:

- Do not add GPS while working on UI-only or model-only phases.
- Do not add Wi-Fi scanning while working on DeviceInfo.
- Do not add KNN while working on Firebase repositories.
- Do not wire UI to Firebase unless the phase explicitly asks for it.
- Do not add CSV export early.
- Do not turn mock UI buttons into real data writes unless requested.

Always stop at the requested phase boundary.

## 17. Prohibited Changes Unless Explicitly Requested

Do not:

- Merge Collect Mode and Test Mode.
- Remove `locations`.
- Use Ground Truth to overwrite raw GPS.
- Add `testResults` into `fingerprintSamples`.
- Change collection names.
- Change location id rules.
- Delete device information fields.
- Delete raw GPS fields.
- Auto-modify Ground Truth.
- Change KNN algorithm without request.
- Add major architecture or large refactors without need.

## 18. Required Start-of-Work Checklist

Before modifying code, the AI/developer must:

1. Read `PROJECT_RULES.md`.
2. Inspect the current project state.
3. Identify relevant files.
4. State the current request.
5. State intended modified files.
6. State whether the request affects Firebase schema.
7. State whether the request affects KNN.
8. State whether the request affects existing data.

Required acknowledgement:

```text
已閱讀專案規格，目前理解本系統分為 locations、fingerprintSamples、testResults，採集與測試完全分離，GPS 原始資料不可被人工 Ground Truth 覆蓋。
```

## 19. Build and Test Rules

After code changes:

- Run relevant unit tests when model, mapper, repository, utility, or logic code changes.
- Run `assembleDebug` after Android app code changes.
- If build/test fails, fix it before reporting completion.
- Report what was changed and what was verified.

Common commands used in this project:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat --no-daemon testDebugUnitTest
.\gradlew.bat --no-daemon assembleDebug
```

## 20. Current Confirmed Implementation Snapshot

As of this document:

- Package / namespace: `com.example.yzuwifilocationresearch`
- UI: Jetpack Compose
- Navigation: Compose Navigation
- Main entry: `MainActivity` -> `AppNavGraph`
- Firestore collections constants: `FirestoreCollections`
- Firestore mappers: `FirestoreMappers`
- Repositories: `LocationRepository`, `FingerprintRepository`, `TestResultRepository`
- Device info: `DeviceInfoProvider`
- Models: `LocationPoint`, `AccessPoint`, `FingerprintSample`, `TestResult`, `WifiScanResult`, `DeviceInfo`
- Time fields: `Long?` epoch milliseconds
- Real Firebase writes from UI: not implemented
- Real GPS: not implemented
- Real Wi-Fi scanning: not implemented
- Real KNN/WKNN: not implemented
- Real Google Maps: not implemented
- Real CSV export: not implemented

## 21. Research Data Cleanliness Principle

Prefer:

- Simple
- Maintainable
- Testable
- Clean research data
- Clear separation of responsibilities

Avoid adding architecture that only looks more sophisticated but does not improve reliability or research data quality.
