# Plan A2b — Android Measurement Flow 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:** HomeScreen → 측정 마법사 3 step (정면 → 측면 → 키) → 분석 → 결과 화면까지의 전체 측정 플로우 구현.

**Architecture:** MeasurementViewModel이 step state machine 보유 (`MutableStateFlow<Step>`). 각 step은 별도 Composable. CameraX는 `LifecycleCameraController` + AndroidView로 wrap. Photo picker는 `ActivityResultContracts.PickVisualMedia`. 사진 확보 후엔 Bitmap으로 일관 처리.

**Tech Stack:**
- CameraX 1.4.0 (`androidx.camera:camera-view` `LifecycleCameraController`)
- `androidx.activity:activity-compose` `rememberLauncherForActivityResult`
- `androidx.compose.material3` BottomSheet, Dialog
- Hilt `@HiltViewModel` + `hiltViewModel()`

**선행:** Plan A2a (UI Foundation), Plan A1 (Domain/Data 완료)

**완료 후 상태:** 에뮬레이터/실기기에서 홈 → 측정 시작 → 카메라 또는 갤러리에서 사진 2장 선택 → (옵션) 키 입력 → 분석 → 임시 결과 화면 도달까지 작동. Plan A2c에서 결과 화면 본격 구현.

---

## iOS ↔ Android 매핑

| iOS (SwiftUI/UIKit) | Android (Compose/CameraX) |
|---|---|
| `AVCaptureSession` + `AVCapturePhotoOutput` | CameraX `LifecycleCameraController` + `ImageCapture` |
| `UIViewRepresentable` (preview) | `AndroidView { PreviewView(it) }` |
| `PHPickerViewController`/`PhotosPicker` | `ActivityResultContracts.PickVisualMedia` |
| `fullScreenCover` | Compose `Dialog` `usePlatformDefaultWidth=false` 또는 별도 destination |
| `AVCaptureDevice.requestAccess(.video)` | `ActivityResultContracts.RequestPermission(CAMERA)` |
| `@MainActor @Observable` | `@HiltViewModel class … : ViewModel` + `StateFlow` |
| `UIImage` ↔ JPEG Data | `Bitmap` ↔ JPEG |
| `.sheet/.confirmationDialog` | `ModalBottomSheet` |
| `WindowGroup` deep link | Compose Navigation 또는 단일 Activity 내 state |

---

## Phase 1: HomeScreen

### Task 1: HomeViewModel

**파일:** `presentation/home/HomeViewModel.kt`

`@HiltViewModel` + `StateFlow<HomeState>`. `latestSession: SessionWithPostures?`, `isWizardPresented: Boolean`. `refresh()` `startMeasurement()` `dismissWizard()`.

### Task 2: HomeScreen

**파일:** `presentation/home/HomeScreen.kt`

Hero (제목 + 부제목) + CTA 그라디언트 카드 ("측정 시작" + 카메라 아이콘) + 옵션 최근 측정 카드.

`onNavigateToSettings: () -> Unit`, `onMeasurementCompleted: (UUID) -> Unit` 콜백 받음. Wizard sheet 표시.

---

## Phase 2: MeasurementViewModel

### Task 3: MeasurementViewModel — step state machine

**파일:** `presentation/measurement/MeasurementViewModel.kt`

```
enum Step { FRONT, SIDE, HEIGHT, ANALYZING, DONE }
data class State(step, frontImage, sideImage, heightInput, analyzingPhase, report?, errorMessage?)
```

핵심 메서드: `setFrontImage(Bitmap)`, `setSideImage(Bitmap)`, `submitHeight()`, `skipHeight()`, `retryFromBeginning()`. 저장된 키 있으면 height step skip.

분석은 `viewModelScope.launch` + `analyzeUseCase.analyze(...)`. 예외 → errorMessage + step = HEIGHT 유지.

---

## Phase 3: 사진 입력 (BottomSheet + PhotoPicker)

### Task 4: PhotoInputBottomSheet

**파일:** `presentation/measurement/PhotoInputBottomSheet.kt`

`ModalBottomSheet` — "카메라로 촬영" / "갤러리에서 선택" 두 옵션. iOS `PhotoInputSheet`와 동일.

### Task 5: PhotoPicker integration

**파일:** `presentation/measurement/rememberPhotoPicker.kt` (Composable extension)

```kotlin
@Composable
fun rememberPhotoPicker(onImagePicked: (Bitmap) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { ... decode to Bitmap ... onImagePicked(bitmap) }
    }
    return { launcher.launch(PickVisualMediaRequest(ImageOnly)) }
}
```

---

## Phase 4: CameraX 커스텀 카메라

### Task 6: 권한 처리 — AppPermissions

**파일:** `util/AppPermissions.kt`

`rememberCameraPermissionState()` Compose helper — `ActivityResultContracts.RequestPermission` + `ContextCompat.checkSelfPermission`.

### Task 7: CustomCameraScreen

**파일:** `presentation/measurement/CustomCameraScreen.kt`

전체 화면 카메라. AndroidView로 `PreviewView`, `LifecycleCameraController.bindToLifecycle()`. PoseGuideOverlay overlay. 셔터 버튼. X 닫기 버튼 (safeArea 고려).

iOS와 동일하게: 측정 step (정면/측면) 정보와 step 번호 가이드 오버레이에 표시.

### Task 8: PoseGuideOverlay (Compose Canvas)

**파일:** `presentation/measurement/PoseGuideOverlay.kt`

iOS의 `BodyShape` (viewBox 200×470)을 Compose `Path` + `Canvas`로 재작성. 점선 외곽선만, 정면/측면 두 종.

상단 STEP 배지 + 안내 텍스트. dim 마스크 (외부만 어둡게).

---

## Phase 5: Wizard

### Task 9: WizardStepScreen — 정면/측면 step

**파일:** `presentation/measurement/WizardStepScreen.kt`

iOS `WizardStepView`와 동일: 상단 step indicator (1/3, 2/3) + 가이드 텍스트 + 점선 실루엣 일러스트 + "사진 추가" CTA → PhotoInputBottomSheet.

### Task 10: WizardHeightStepScreen

**파일:** `presentation/measurement/WizardHeightStepScreen.kt`

TextField (numeric) + "분석 시작" / "건너뛰기" 버튼. 50~250 cm 유효성 표시.

### Task 11: AnalyzingScreen

**파일:** `presentation/measurement/AnalyzingScreen.kt`

CircularProgressIndicator + 단계 텍스트 ("관절 인식 중…" → "자세 분석 중…"). 풀스크린.

### Task 12: MeasurementWizardScreen — 4 step orchestrator

**파일:** `presentation/measurement/MeasurementWizardScreen.kt`

ViewModel state 관찰해서 step에 따라 적절한 Composable 호출. errorMessage 있으면 AlertDialog. step = DONE 되면 onCompleted(report) 콜백.

`FullScreenDialog` 또는 별도 destination.

---

## Phase 6: Integration

### Task 13: MainActivity → HomeScreen으로 교체

**파일:** `MainActivity.kt`

PreviewGallery 제거 → HomeScreen 직접 표시 (Plan A2d에서 NavHost로 본격 분리). 임시로 "결과" 텍스트만 표시하는 placeholder result screen + back to home.

### Task 14: 권한 + 빌드 확인 + Plan A2b tag

AndroidManifest에 `CAMERA` permission 이미 있음. 실기기 또는 에뮬레이터에서:
- 홈 진입
- "측정 시작" → wizard
- 카메라 권한 요청 → 허용 → 사진 촬영 → 다음
- 또는 갤러리 → 사진 선택 → 다음
- 측면도 반복
- (저장된 키 있으면) 분석 직행 / 없으면 키 입력 → 분석
- "분석 중" 화면
- 임시 결과 placeholder 도달

```bash
git tag -a plan-a2b-measurement-complete -m "Plan A2b 완료"
git push origin main && git push origin plan-a2b-measurement-complete
```

---

## ✅ Plan A2b 완료 정의

- [ ] HomeScreen + HomeViewModel (최근 측정 카드)
- [ ] MeasurementViewModel state machine (5 step)
- [ ] PhotoInputBottomSheet + PhotoPicker (갤러리)
- [ ] AppPermissions (카메라 권한)
- [ ] CustomCameraScreen (CameraX `LifecycleCameraController`)
- [ ] PoseGuideOverlay (Compose Canvas BodyShape)
- [ ] WizardStepScreen / WizardHeightStepScreen / AnalyzingScreen
- [ ] MeasurementWizardScreen orchestrator
- [ ] MainActivity → HomeScreen 교체
- [ ] 실기기 또는 에뮬레이터 분석 직행 확인
- [ ] `plan-a2b-measurement-complete` git tag

---

## ⏭ 다음 (Plan A2c)

분석 결과 화면 + 기록 리스트 + Vico 추이 그래프:
- AnalysisResultScreen + ViewModel + 8 PostureResultCard
- PoseOverlayView (관절 검출 시각화)
- HistoryListScreen + ViewModel + TrendScreen (Vico chart)
