# PoseAnalyzer (Android) — Claude Code 작업 컨텍스트

> Google ML Kit 기반 자세 분석 Android 앱. Kotlin + Jetpack Compose.
> iOS 버전 별도 repo: `../PoseAnalyzer`

## 공통 룰
상위 `../RULES.md` 적용. iOS와 1:1 매칭되는 알고리즘 — 좌표계만 다름.

## 프로젝트 식별자
- **Package**: `com.pose.poseanalyzer`
- **Min SDK**: 26, **Compile/Target SDK**: 35
- **AGP**: 8.6.1, **Kotlin**: 2.0.21
- **GitHub**: https://github.com/SongByungGyu/poseAnalyzer-android

## 외부 라이브러리 제한 — 중요
**Android는 Google + Jetpack 공식만 사용.**
- ✅ 허용: Compose, ML Kit Pose Detection (accurate), Room + KSP, Hilt, CameraX, Vico 2.0 (차트), Coroutines, EXIF
- ❌ 금지: 비공식 3rd party (확장은 신중히)
- **새 의존성 추가 시 반드시 사전 확인.**

## 단일 진실 문서
- 기능 명세: `docs/specs/2026-05-13-pose-analyzer-design.md` (iOS와 동일 spec)
- 디자인 시스템: `docs/design/SKILL.md`
- 구현 계획: `docs/plans/2026-05-14-android-*.md` (A1 Foundation → A2a UI → A2b Measurement → A2c Result → A2d Integration)
- **트러블슈팅 인덱스**: `../docs/troubleshooting/README.md` — 새 버그·이슈 만나면 여기 먼저 검색

## 도메인
iOS와 **동일한 8 자세, 동일한 4단계 판정, 동일한 임계값.**
→ iOS `CLAUDE.md`의 임계값 표 참고.

## ML Kit 좌표계 — Vision과 반대
**좌상단 원점, y 아래로 증가, 픽셀 좌표 → 정규화 0~1로 변환.**

### iOS ↔ Android 차이 (중요)
1. **neck 관절 없음**
   - Vision은 `.neck` 제공 / ML Kit은 없음
   - 흉추후만증에서 양 어깨 중점으로 대체
2. **무릎과신전 cross-product 부호 반대**
   - Vision (좌하단): `cross < 0` = 과신전
   - ML Kit (좌상단): `cross > 0` = 과신전
3. **`lineAngleFromHorizontal`**: [-90, 90] 정규화 동일 적용 필수
4. **Hilt + Kotlin generics**: `List<@JvmSuppressWildcards PostureEvaluator>` 필요

## 머리 기준점 (양 플랫폼 공통 규약)
- **거북목·라운드숄더**: 코+눈 추정 1순위, 검출 귀 2순위 fallback
- **머리기울기**: 양 눈 1순위, 양 귀 2순위 fallback
- 둘 다 안 되면 측정 불가 + 재촬영 안내

추정식: `귀 = 눈 + (눈 - 코) × 1.4`

## 데이터 영속화
- **Room + KSP**: `UserProfileEntity`, `SessionEntity`, `PostureEntity`
- `PostureResult`에 `deviationValue` computed property (편차 표시용)
- **ResultHolder 싱글톤**: Bitmap이 직렬화 불가 → 메모리 임시 보관

## 카메라
- **CameraX `LifecycleCameraController` + `PreviewView`**
- ⚠️ **`PreviewView.implementationMode = COMPATIBLE` (TextureView) 필수** — 기본 PERFORMANCE 모드(SurfaceView)는 Compose Canvas 오버레이와 z-order 충돌 → 검은 화면
- `PoseGuideOverlay`의 `BlendMode.Clear`는 `graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }` 적용 필수

## 화면 흐름
```
Splash → Home → 측정마법사 → AnalyzingScreen → AnalysisResultScreen → 저장
              → HistoryListScreen → HistoryDetailScreen → TrendScreen
```
Navigation Compose 2.8.4 + Scaffold + NavigationBar (홈/기록 2 tab).

## WizardStepScreen 주의사항
실루엣 Box는 **`weight(1f)` 사용** (`aspectRatio(0.5f)` 절대 X — 너비 × 2 높이로 "사진 추가" 버튼이 화면 밖으로 밀림).

## 디자인 시스템
iOS와 동일 토큰 (`AppColors`, `AppTypography`, `AppShapes`, `PoseTheme`). **라이트·다크 모드 지원 (시스템 토글)** — `AppColors`의 각 토큰은 `@Composable @ReadOnlyComposable get()` 패턴으로 `isSystemInDarkTheme()` 분기, `PoseTheme`은 `darkColorScheme()` 별도 분기.
1. 상태 색상 신성불가침 — 4단계만, 빨간색 X
2. 한국어 합니다체
3. 수치는 tabular nums
4. 그라디언트는 브랜드 CTA만
5. 본문 이모지 X

## 작업 시 자동 적용 사항
- **티켓 자동 생성**: 의미 있는 작업 단위마다 `티켓/YYYY-MM-DD/T##-*.md`
- **백로그 참고**: `~/.claude/.../memory/project_backlog.md`

## 실기기 검증 — ADB
- 사진 push: `adb push <local> /sdcard/Pictures/`
- 사진 pull: `adb pull /sdcard/DCIM/Screenshots/* <local>`
- logcat: `adb logcat | grep PoseCoords` (디버그 좌표 덤프)
- 디버그 APK install: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

## 진행 중 / 후속
→ **단일 백로그**: `../docs/backlog.md` — 모든 진행·후속 작업 여기서 관리

## 편차 표시 (UX)
iOS와 동일 형식. `PostureResult.deviationValue` computed property로 변환.
