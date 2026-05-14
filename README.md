# PoseAnalyzer Android

iOS PoseAnalyzer 앱(`SongByungGyu/poseAnalyzer`)의 안드로이드 포팅 — 정면+측면 사진 2장으로 8가지 자세 이상을 분석.

## 분석 항목

| # | 자세 | 측정 시점 | 측정 방법 |
|---|------|----------|----------|
| 1 | 거북목 | 측면 | 귀-어깨-엉덩이 각도 |
| 2 | 라운드숄더 | 측면 | 귀-어깨 수평거리 / 어깨폭 |
| 3 | 흉추 후만증 | 측면 | 목-어깨-엉덩이 각도 |
| 4 | 골반 전방/후방경사 | 측면 | 어깨-엉덩이-무릎 각도 |
| 5 | 무릎 과신전 | 측면 | 엉덩이-무릎-발목 각도 |
| 6 | 척추측만 | 정면 | 양 어깨 / 양 엉덩이 기울기 |
| 7 | 머리 좌우 기울기 | 정면 | 양 귀 또는 양 눈 기울기 |
| 8 | 무릎 X/O자 | 정면 | 양 다리 각도 |

판정은 **정상 / 주의 / 의심 / 측정 불가** 4단계. 키 입력 시 어깨/엉덩이 비대칭이 cm로 환산됩니다.

## 기술 스택

- **언어/플랫폼**: Kotlin 2.0.21, Android Studio Hedgehog+, AGP 8.6.1
- **Min SDK / Target SDK**: 26 / 35
- **UI**: Jetpack Compose + Material 3 (Pose Indigo #3B5BDB)
- **DI**: Hilt
- **DB**: Room (KSP)
- **사진 입력**:
  - CameraX `LifecycleCameraController` (커스텀 카메라 + 가이드 오버레이)
  - `ActivityResultContracts.PickVisualMedia` (갤러리)
- **관절 검출**: Google ML Kit Pose Detection (Accurate, SINGLE_IMAGE)
- **차트**: Vico 2.0
- **이미지**: `androidx.exifinterface` (EXIF 회전 보정)
- **테스트**: JUnit 4 + Robolectric + MockK + Room in-memory

## 빌드

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
adb shell am start -n com.pose.poseanalyzer/.MainActivity
```

요구: JDK 17, Android SDK API 35 설치.

## 프로젝트 구조

```
app/src/main/java/com/pose/poseanalyzer/
├─ PoseAnalyzerApplication.kt  (@HiltAndroidApp)
├─ MainActivity.kt              (Splash → AppNavHost)
├─ di/AppModule.kt              (Hilt bindings)
├─ domain/
│  ├─ model/                    (SessionView, PostureType, PoseFrame, Point2D, ...)
│  ├─ detection/PoseDetector    (interface + sealed Exception)
│  ├─ evaluation/               (PostureEvaluator + 8 evaluator)
│  ├─ asymmetry/                (interface)
│  ├─ usecase/                  (AnalyzeSessionUseCase)
│  └─ motion/                   (interface placeholder — 영상 분석용)
├─ data/
│  ├─ detection/MLKitPoseDetector.kt
│  ├─ asymmetry/DefaultAsymmetryAnalyzer.kt
│  ├─ room/                     (Entity 3 + DAO 2 + AppDatabase)
│  ├─ ImageStore.kt
│  ├─ SessionRepository.kt
│  └─ UserProfileRepository.kt
├─ presentation/
│  ├─ theme/                    (AppColors, AppTypography, AppSpacing, PoseTheme)
│  ├─ common/                   (StatusBadge, AppButton, AppCard, AppNavBar, ...)
│  ├─ home/                     (HomeScreen + ViewModel)
│  ├─ measurement/              (Wizard 3 step + CameraX + PhotoPicker + PoseGuideOverlay)
│  ├─ result/                   (AnalysisResultScreen, PostureResultCard, PoseOverlayCanvas, ResultHolder, HistoryDetail)
│  ├─ history/                  (HistoryListScreen, TrendScreen + Vico chart)
│  ├─ settings/                 (SettingsScreen + ViewModel)
│  ├─ SplashScreen.kt
│  └─ AppNavHost.kt             (Compose Navigation + Bottom Bar)
└─ util/                        (GeometryMath, AppPermissions)
```

## iOS와의 차이점

| iOS | Android |
|---|---|
| Apple Vision (좌하단 y) | ML Kit (좌상단 y) — 일부 부호 반대 |
| Vision의 `.neck` joint | ML Kit엔 없음 → 양 어깨 중점 계산 |
| SwiftData `@Model` | Room `@Entity` + KSP |
| `withCheckedThrowingContinuation` | `suspendCancellableCoroutine` |
| `async let` 병렬 | `coroutineScope { async/await }` |
| `@StateObject` + `@Environment` | Hilt `@HiltViewModel` + `hiltViewModel()` |
| `UIImage` | `Bitmap` |
| AVCaptureSession | CameraX `LifecycleCameraController` |
| `PHPickerViewController` | `ActivityResultContracts.PickVisualMedia` |
| Swift Charts | Vico 2.0 |
| `NavigationStack` + `TabView` | Navigation Compose + `NavigationBar` |

### KneeHyperextension cross-product 부호

`acos`는 0~180만 반환하므로 cross-product로 과신전 방향 보정. iOS는 좌하단 원점이라 `cross < 0`이 과신전이지만, Android ML Kit은 좌상단 원점이라 **같은 자세에서 cross 부호가 반대**. 코드는 `cross > 0` → 과신전.

### AsymmetryAnalyzer 방향 판정

iOS: `lp.y > rp.y` → LEFT_HIGHER (Vision 좌하단)
Android: `lp.y < rp.y` → LEFT_HIGHER (ML Kit 좌상단)

## 빌드된 화면 흐름

```
[콜드 스타트 — 800ms Splash]
        ↓
[HomeScreen]  ── 측정 시작 ──→  [MeasurementWizardScreen]
   │                                    │
   │  추이 아이콘                       ├─ STEP 1: 정면 사진 (카메라/갤러리)
   │     ↓                              ├─ STEP 2: 측면 사진
   │  [TrendScreen]                     ├─ STEP 3: 키 (옵션, 저장된 값 있으면 skip)
   │                                    └─ 분석 (관절 인식 → 자세 분석)
   │                                        ↓
   │                                  [AnalysisResultScreen]
   │                                        │
   │                                        └─ "저장" → 토스트 → 홈
[기록 탭]
   ├─ 항목 클릭 → [HistoryDetailScreen] (readOnly)
   └─ 추이 → [TrendScreen]
[설정 아이콘 (홈 상단)]
   └─ [SettingsScreen] (키 입력)
```

## Plan 진행

| Plan | 상태 | 내용 |
|------|------|------|
| A1 — Foundation | ✅ | Domain (8 evaluator + asymmetry + UseCase) + Data (Room + ImageStore + Repo) + Hilt DI. 63 unit test |
| A2a — UI Foundation | ✅ | Compose 디자인 토큰 + 7 공통 컴포넌트 |
| A2b — Measurement Flow | ✅ | Home + 3-step wizard + CameraX + PhotoPicker + PoseGuideOverlay |
| A2c — Result + History | ✅ | AnalysisResult + PoseOverlayCanvas + HistoryList + Vico Trend |
| A2d — Integration + MVP | ✅ | Navigation Compose + Bottom Bar + Settings + HistoryDetail + Splash |

## 테스트

```bash
./gradlew :app:testDebugUnitTest
```

63개 단위테스트:
- GeometryMath (11) — TDD
- 8 Evaluator (4-6 case 각)
- AsymmetryAnalyzer (5)
- AnalyzeSessionUseCase (1)
- ImageStore (4) — Robolectric + Bitmap
- SessionRepository (5) — Room in-memory
- UserProfileRepository (4)

## 참고

- iOS 원본: https://github.com/SongByungGyu/poseAnalyzer
- 디자인 토큰: `docs/design/`
- 스펙: `docs/specs/2026-05-13-pose-analyzer-design.md`
- Plan 문서: `docs/plans/`
