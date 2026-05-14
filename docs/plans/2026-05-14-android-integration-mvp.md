# Plan A2d — Android Integration + MVP 완성

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development.

**Goal:** Compose Navigation + Bottom Tab으로 화면 라우팅 본격 분리. SettingsScreen, HistoryDetail, Splash, AppIcon 추가. MVP tag.

**Architecture:**
- AppNavHost (`androidx.navigation:navigation-compose`)
- Bottom Scaffold (Home 탭 + 기록 탭). Settings는 Home 탭 trailing 진입.
- HistoryDetail은 SessionRepository로 sessionId fetch → AnalysisResultViewModel에 inject (readOnly 모드).
- Splash는 PoseAnalyzerApp 진입 시 1.0초 인디고 풀스크린.

## Phase 1: Navigation

### Task 1: navigation-compose 의존성 추가

`libs.versions.toml` + app build.gradle.

### Task 2: AppNavHost

`presentation/AppNavHost.kt` — NavHost + composable("home"), composable("history"), composable("history/detail/{id}"), composable("trend"), composable("settings"), composable("wizard"), composable("result").

### Task 3: AppScaffold + BottomBar

`presentation/AppScaffold.kt` — Material 3 `NavigationBar` (홈/기록 2 tab) + 내부 NavHost.

## Phase 2: Settings + HistoryDetail

### Task 4: SettingsViewModel + SettingsScreen

`presentation/settings/`. `userProfileRepository`로 키 GET/UPDATE. iOS Form → Compose LazyColumn + Section + OutlinedTextField. AlertDialog 저장 완료 시.

### Task 5: HistoryDetailViewModel + HistoryDetailScreen

기존 AnalysisResultViewModel을 readOnly 모드로 재사용: sessionId 인자로 받아 SessionRepository.fetch → SessionReport 복원 (Bitmap은 ImageStore.load, PoseFrame은 비어있음 — 저장된 결과니까 사진 위 관절 오버레이는 빈 frame 표시).

또는: 별도 `HistoryDetailScreen` 만들어서 SessionWithPostures 직접 표시 (PoseOverlayCanvas는 빈 frame인 경우 fallback해서 사진만).

→ **선택**: AnalysisResultViewModel 그대로 사용. `loadFromSession(sessionId)` 메서드 추가 (SessionRepository → SessionReport 변환 + isReadOnly=true).

## Phase 3: Splash + Icon

### Task 6: SplashScreen Composable + AppRoot 통합

`presentation/SplashScreen.kt` — 인디고 그라디언트 + Canvas Mark + 워드마크. `LaunchedEffect`로 1초 후 자동 dismiss.

### Task 7: 앱 아이콘 — Pose Indigo

`drawable/ic_launcher_foreground.xml` 이미 있음. 인디고 배경 + 흰 mark로 디자인 일치.

## Phase 4: 마무리

### Task 8: README 작성

Project overview + 빌드/실행 + 구조 설명.

### Task 9: 통합 빌드 + MVP tag

```
git tag -a poseanalyzer-android-mvp-v1.0
git push origin main && git push --tags
```

## ✅ Plan A2d 완료 정의

- [ ] navigation-compose 적용
- [ ] AppScaffold + 2 tab (Home / History) + Settings 진입
- [ ] SettingsScreen
- [ ] HistoryDetail (저장된 세션 readOnly 보기)
- [ ] SplashScreen
- [ ] AppIcon 인디고 통일
- [ ] README.md
- [ ] `poseanalyzer-android-mvp-v1.0` tag
