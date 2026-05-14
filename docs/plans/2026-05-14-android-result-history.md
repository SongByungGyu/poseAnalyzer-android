# Plan A2c — Android Result + History 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development.

**Goal:** 분석 결과 화면 + 기록 리스트 + Vico 추이 그래프.

**Architecture:**
- ResultPlaceholder를 진짜 AnalysisResultScreen으로 교체.
- HomeScreen → MeasurementWizard 분석 완료 → AnalysisResultScreen (저장 옵션) → 홈으로 복귀.
- HistoryListScreen은 SessionWithPostures (Room) 직접 조회. 항목 클릭 시 AnalysisResultDetail (저장된 데이터 기반).
- TrendScreen은 PostureType별 시간축 LineChart (Vico).

**iOS ↔ Android 매핑:**
| iOS | Android |
|---|---|
| `Swift Charts` `Chart` | Vico `CartesianChartHost` |
| `UIImage` + Canvas overlay | `Bitmap` + Compose `Canvas` |
| `NavigationStack`/`NavigationLink` | 본 plan에선 state 기반 (Plan A2d에서 Navigation으로 분리) |
| `swipeActions` (List) | `SwipeToDismissBox` 또는 long-press 메뉴 |

---

## Phase 1: 핵심 컴포넌트

### Task 1: AnalysisResultViewModel

`@HiltViewModel`. 생성자 인자 X (report는 `SavedStateHandle` 또는 인자로). 

실제로는 ViewModel이 report를 인자로 받는 게 일반적이지만 — Android에서는 ViewModel이 SavedStateHandle 통해 받기 어려움 (Bitmap 직렬화 불가). 대신:
- 임시 메모리 holder (`ResultStore`) 또는
- Compose state로 ViewModel에 전달 후 보관

여기서는 `MeasurementViewModel`이 만든 SessionReport가 이미 SessionRepository에 저장된 상태이므로 sessionId로 다시 fetch한다. 또는 `ResultHolder` 싱글톤 도입.

→ **선택**: ResultHolder 싱글톤 (앱 메모리에 직전 결과 보관, 화면 전환 사이만 유효). 간단하고 직관적.

### Task 2: PostureResultCard

좌측 4dp status indicator strip + 자세명 + StatusBadge + 큰 수치(°/ratio) + 게이지 + 조언.

### Task 3: PoseOverlayCanvas

Bitmap 위에 Canvas로 관절 본 + 노드 그리기. ML Kit은 좌상단 원점이라 변환 단순 (`p.x * size.width, p.y * size.height`).

`neck`은 LEFT/RIGHT_SHOULDER 중점 사용 (PoseFrame.neck).

## Phase 2: AnalysisResultScreen

상단: 정면+측면 사진 (PoseOverlayCanvas) — 가로 2분할
중간: SectionHeader("자세 판정 (8가지)") + 8 PostureResultCard
하단: SectionHeader("좌우 비대칭") + AppCard (어깨/엉덩이 행)
이전 측정 있으면: SectionHeader("직전 측정 대비") + 변화량 행
상단 액션바: "저장" 버튼 (저장 후 dismiss + toast)

`isReadOnly` (기록에서 진입 시 true)인 경우 저장 버튼 + 직전 비교 섹션 숨김.

## Phase 3: HistoryListScreen + ViewModel

`HistoryViewModel.refresh()` `delete(id)`.

`HistoryListScreen`:
- 빈 상태: AppEmptyState
- 리스트: LazyColumn, 각 항목 HistoryRow (날짜 + 8개 status dot)
- 삭제: 카드 길게 누르면 AlertDialog
- 상단 trailing: "추이" 버튼 → TrendScreen
- 항목 탭 → AnalysisResultScreen (readOnly)

## Phase 4: TrendScreen + Vico

상단: 8 자세 horizontal scroll chip + 7일/30일/전체 segmented.
중앙: Vico LineChart + Point markers (각 점 status color).
빈 데이터: AppEmptyState.

Vico API:
```kotlin
CartesianChartHost(
    chart = rememberCartesianChart(
        rememberLineCartesianLayer(...),
        startAxis = ...,
        bottomAxis = ...
    ),
    modelProducer = ...
)
```

## Phase 5: MainActivity 통합

ResultPlaceholder → AnalysisResultScreen.
홈 → 측정 완료 → 결과(저장가능) → 홈.
홈 → 최근 측정 카드 클릭 → 결과(readOnly).
TopRoute에 History/Trend 추가는 Plan A2d에서 Navigation으로 본격 분리.

## ✅ Plan A2c 완료 정의

- [ ] AnalysisResultViewModel + ResultHolder 싱글톤
- [ ] PostureResultCard, PoseOverlayCanvas
- [ ] AnalysisResultScreen (정상 + readOnly 모두)
- [ ] HistoryViewModel + HistoryListScreen + HistoryRow + AnalysisResultDetail 진입
- [ ] TrendViewModel + TrendScreen (Vico)
- [ ] MainActivity → AnalysisResultScreen 사용
- [ ] `plan-a2c-result-history-complete` tag + push

## ⏭ Plan A2d

- AppNavHost + bottom tab (Home/History/Settings)
- SettingsScreen (키 입력)
- LaunchScreen + Splash
- 통합 UI test 일부
- `poseanalyzer-android-mvp-v1.0` tag
