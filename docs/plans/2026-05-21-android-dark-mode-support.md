# Android 다크 모드 지원 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Android 앱이 시스템 다크 모드 토글에 정상 대응하도록 `AppColors`에 다크 variant를 추가하고 `PoseTheme`에 `isSystemInDarkTheme()` 분기를 도입한다. iOS와의 디자인 시스템 divergence를 해소한다.

**Architecture:** `AppColors`의 모든 색 토큰을 `val Color` 에서 `val Color @Composable @ReadOnlyComposable get()` 패턴으로 변환해 사용 시점에 라이트/다크 분기. 화면 23개·사용처 120 모두 @Composable scope 안이라 사용처 변경 zero. `PoseTheme`은 `lightColorScheme` 외에 `darkColorScheme` 추가 후 `isSystemInDarkTheme()`로 선택.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, `androidx.compose.foundation.isSystemInDarkTheme`, `androidx.compose.runtime.ReadOnlyComposable`

**관련 티켓**: `티켓/2026-05-21/T06-android-dark-mode-support.md`, T04 진단 (완료)

---

## 디자인 결정 — 라이트는 유지, 다크는 신설

라이트 hex는 사용자에게 보이는 변화 0 유지. 다크 hex는 아래 규칙으로 도출.

### Brand (iOS와 라이트 hex 이미 일치 → iOS dark 차용)

| 토큰 | Light (변화 없음) | Dark (iOS 차용) |
|------|------------------|----------------|
| `BrandPrimary` | `#3B5BDB` | `#5B6EE8` |
| `BrandPrimaryDark` (= iOS press) | `#2A47C3` | `#4054BB` |
| `BrandPrimaryLight` (= iOS accent) | `#5B6EE8` | `#7A8BEC` |

### Status — Android mint 톤 유지를 위해 라이트 hex의 명도 +10% (HSL L)

| 토큰 | Light (변화 없음) | Dark (mint 톤 유지) |
|------|------------------|---------------------|
| `StatusNormal` | `#56BAB0` (mint) | `#6BC8BF` |
| `StatusCaution` | `#E3B341` (amber) | `#EAC471` |
| `StatusSuspect` | `#E07A5F` (coral) | `#E69680` |
| `StatusUnmeasurable` | `#9BA1A6` (gray) | `#B2B7BB` |

> ⚠️ 시각 검증 단계에서 채도·명도 미세 조정 가능. iOS 4상태 dark hex는 차용하지 않음 (iOS는 그린톤 normal, Android는 mint톤 normal — hue 자체가 달라 일치 불가).

### Surface/OnSurface/Divider (iOS dark 차용, 라이트는 그대로)

| 토큰 | Light (변화 없음) | Dark (iOS 차용) |
|------|------------------|----------------|
| `Surface` | `#F7F8FA` | `#1F2532` (iOS bgSurface2) |
| `SurfaceElevated` | `#FFFFFF` | `#161B26` (iOS bgSurface) |
| `SurfaceMuted` | `#EEF0F3` | `#1F2532` (iOS border2 dark) |
| `OnSurface` | `#1B1F26` | `#F2F4F8` (iOS fg1 dark) |
| `OnSurfaceSecondary` | `#5A6068` | `#B4BCCB` (iOS fg2 dark) |
| `OnSurfaceTertiary` | `#8B9099` | `#8A94A6` (iOS fg3 dark) |
| `Divider` | `#E2E5EA` | `#232A38` (iOS border1 dark) |

---

## File Structure

- **Modify**: `app/src/main/java/com/pose/poseanalyzer/presentation/theme/AppColors.kt` — 모든 토큰을 `@Composable @ReadOnlyComposable get()` 패턴으로 변환
- **Modify**: `app/src/main/java/com/pose/poseanalyzer/presentation/theme/PoseTheme.kt` — `darkColorScheme()` 정의 + `isSystemInDarkTheme()` 분기
- **No changes**: 23개 화면 파일 (`AppColors.X` 호출이 모두 @Composable scope이라 자동 분기)

---

## Task 1: AppColors를 @Composable getter 패턴으로 변환 + 다크 variant 추가

**Files:**
- Modify: `app/src/main/java/com/pose/poseanalyzer/presentation/theme/AppColors.kt`

### Step 1: AppColors.kt 전체 rewrite

기존 `val X = Color(...)` 정의를 `val X: Color @Composable @ReadOnlyComposable get()` 형태로 변경. 각 토큰이 `isSystemInDarkTheme()`로 라이트/다크 hex 선택.

```kotlin
package com.pose.poseanalyzer.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * PoseAnalyzer 브랜드 컬러 토큰.
 *
 * 각 토큰은 @Composable getter — 사용 시점에 isSystemInDarkTheme() 분기.
 * 사용 패턴은 기존과 동일: `AppColors.BrandPrimary` (단, @Composable scope 안에서만).
 *
 * 라이트 hex는 iOS `AppColor.swift`와 일부 일치 / 일부 Android 고유.
 * 다크 hex: Brand·Surface·OnSurface·Divider는 iOS 차용, Status는 라이트 hex 명도 +10% (Android mint 톤 유지).
 */
object AppColors {
    // Brand
    val BrandPrimary: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF5B6EE8) else Color(0xFF3B5BDB)

    val BrandPrimaryDark: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF4054BB) else Color(0xFF2A47C3)

    val BrandPrimaryLight: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF7A8BEC) else Color(0xFF5B6EE8)

    // Status (PostureStatus) — 4단계, 라이트 hex 명도 +10% 로 다크 hex 도출
    val StatusNormal: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF6BC8BF) else Color(0xFF56BAB0)

    val StatusCaution: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFFEAC471) else Color(0xFFE3B341)

    val StatusSuspect: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFFE69680) else Color(0xFFE07A5F)

    val StatusUnmeasurable: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFFB2B7BB) else Color(0xFF9BA1A6)

    // Surfaces — iOS dark variant 차용
    val Surface: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF1F2532) else Color(0xFFF7F8FA)

    val SurfaceElevated: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF161B26) else Color(0xFFFFFFFF)

    val SurfaceMuted: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF1F2532) else Color(0xFFEEF0F3)

    // Foregrounds — iOS dark variant 차용
    val OnSurface: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFFF2F4F8) else Color(0xFF1B1F26)

    val OnSurfaceSecondary: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFFB4BCCB) else Color(0xFF5A6068)

    val OnSurfaceTertiary: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF8A94A6) else Color(0xFF8B9099)

    // Border — iOS dark variant 차용
    val Divider: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) Color(0xFF232A38) else Color(0xFFE2E5EA)
}
```

### Step 2: 컴파일 확인

```bash
cd /Users/byunggyusong/1_개발폴더/마스터프로젝트/PoseAnalyzerAndroid
./gradlew :app:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL (오류 없음).

만약 비-composable 사용처가 잡히면 (예상 0건) 해당 위치에 안내 후 사용처 수정.

---

## Task 2: PoseTheme에 다크 colorScheme 분기 도입

**Files:**
- Modify: `app/src/main/java/com/pose/poseanalyzer/presentation/theme/PoseTheme.kt`

### Step 1: PoseTheme.kt 전체 rewrite

`lightColorScheme`/`darkColorScheme` 두 가지를 만들고 `isSystemInDarkTheme()`로 선택. 각 ColorScheme의 색은 AppColors getter (이미 다크 분기 자동 적용)를 그대로 사용. 즉 ColorScheme도 라이트/다크가 동일한 코드로 만들어지지만 호출 시점의 시스템 모드에 따라 다른 hex가 들어감.

명시적 명확성을 위해 `darkColorScheme` 빌더로 별도 분기 가능. AppColors getter가 모드를 자동 결정하므로 분기 자체는 단일 ColorScheme로 충분하지만, Material 3 컴포넌트 일부가 light/dark Builder의 디폴트 값을 다르게 가지므로 안전하게 분기.

```kotlin
package com.pose.poseanalyzer.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * 앱 전역 테마 — 모든 화면을 이 컴포지블로 감싸야 토큰이 적용됨.
 *
 * Material 3 ColorScheme를 [AppColors]로 채움. iOS와 동일하게 시스템 다크 모드 지원.
 * 화면이 직접 사용하는 [AppColors] 토큰들은 @Composable getter 패턴으로 자동 분기되며,
 * 여기선 추가로 Material 컴포넌트(Button·Card 등)가 사용하는 ColorScheme도 다크용으로 분기.
 */
@Composable
fun PoseTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = AppColors.BrandPrimary,
            onPrimary = AppColors.OnSurface,
            primaryContainer = AppColors.BrandPrimaryDark,
            background = AppColors.Surface,
            surface = AppColors.SurfaceElevated,
            surfaceVariant = AppColors.SurfaceMuted,
            onBackground = AppColors.OnSurface,
            onSurface = AppColors.OnSurface,
            onSurfaceVariant = AppColors.OnSurfaceSecondary,
            outline = AppColors.Divider
        )
    } else {
        lightColorScheme(
            primary = AppColors.BrandPrimary,
            onPrimary = AppColors.SurfaceElevated,
            primaryContainer = AppColors.BrandPrimaryLight,
            background = AppColors.Surface,
            surface = AppColors.SurfaceElevated,
            surfaceVariant = AppColors.SurfaceMuted,
            onBackground = AppColors.OnSurface,
            onSurface = AppColors.OnSurface,
            onSurfaceVariant = AppColors.OnSurfaceSecondary,
            outline = AppColors.Divider
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = PoseTypography,
        shapes = PoseShapes,
        content = content
    )
}
```

### Step 2: 컴파일 + 단위 테스트 회귀 확인

```bash
cd /Users/byunggyusong/1_개발폴더/마스터프로젝트/PoseAnalyzerAndroid
./gradlew :app:compileDebugKotlin :app:testDebugUnitTest
```

Expected: BUILD SUCCESSFUL, 모든 단위 테스트 PASS (기존 50+).

---

## Task 3: 시각 검증 (사용자 단계)

**Files:** 없음 (실기기/에뮬레이터 토글)

### Step 1: 디버그 APK 빌드·설치

```bash
cd /Users/byunggyusong/1_개발폴더/마스터프로젝트/PoseAnalyzerAndroid
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 2: 시스템 다크 모드 ON → 화면별 점검

순서대로 진입해 가독성·대비·4상태 색·그라디언트 확인:
1. Splash
2. Home (히어로 CTA·최근 측정 카드)
3. 측정 마법사 (WizardStep·Height·Camera·Analyzing)
4. 결과 화면 (PostureResultCard, 비대칭, 직전 비교)
5. 기록 리스트
6. 기록 상세 + 추이 (Vico 차트 — 라이브러리가 다크 자동 대응하는지 확인)
7. Settings

### Step 3: 시스템 다크 모드 OFF → 라이트 회귀 확인

라이트는 변화가 0이어야 함. 만약 시각 차이가 보이면 hex 변경된 곳을 역추적해 보고.

### Step 4: 발견 이슈 정리

이슈 카테고리:
- 가독성 부족 (대비 부족) → 다크 hex 조정
- 4상태 색 가시성 (Status 4색이 다크 배경에서 명도 충분한지)
- 하드코딩 White/Black 누락 (특히 `AppToast` — 현재 Black 배경에 White 텍스트, 다크에선 다른 처리가 필요한지)
- Vico 차트 다크 대응 (라이브러리 디폴트 색이 어떻게 나오는지)

작은 이슈는 다음 task에서, 큰 이슈는 별도 백로그 항목 신설 제안.

---

## Task 4: 시각 검증 피드백 반영 (필요 시)

**Files:** 작업 도중 결정

### Step 1: hex 조정 (예: Status 다크 명도 추가 보정)

발견된 색이 어둡거나 채도가 부족하면 `AppColors.kt` 의 해당 다크 hex 수정. 예시:

```kotlin
// 만약 StatusCaution 다크가 너무 어두워 보인다면
val StatusCaution: Color
    @Composable @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) Color(0xFFF0CD7E) else Color(0xFFE3B341)
//                                                ^^^^^^ 명도 추가 +5%
```

### Step 2: AppToast 등 하드코딩 검토

`app/src/main/java/com/pose/poseanalyzer/presentation/common/AppToast.kt` 현재 Black 배경 + White 텍스트는 양 모드에서 동일하게 동작해야 함이 일반적. 다크에서 가독성 문제 발견 시 토큰 사용으로 전환:

```kotlin
// Before
.background(Color.Black.copy(alpha = 0.85f), AppShapes.medium)
// After (option)
.background(AppColors.OnSurface.copy(alpha = 0.85f), AppShapes.medium)
```

단 색이 반전되므로 신중. 사용자 결정 후 적용.

### Step 3: 재검증

Step 1·2 변경 후 다시 Task 3의 화면별 점검.

---

## Task 5: 커밋·푸시·티켓 마무리

**Files:**
- `AppColors.kt`, `PoseTheme.kt` (+ 보정 시 다른 파일)
- `티켓/2026-05-21/T06-android-dark-mode-support.md`
- `docs/backlog.md` (완료 아카이브로 이동)
- `PoseAnalyzerAndroid/CLAUDE.md` (디자인 시스템 절대 규칙 항목 갱신 — 다크 지원 명시)

### Step 1: 변경 사항 git diff 확인

```bash
cd /Users/byunggyusong/1_개발폴더/마스터프로젝트/PoseAnalyzerAndroid
git status
git diff
```

### Step 2: 커밋 (단일 커밋)

```bash
git add app/src/main/java/com/pose/poseanalyzer/presentation/theme/AppColors.kt \
        app/src/main/java/com/pose/poseanalyzer/presentation/theme/PoseTheme.kt
git commit -m "$(cat <<'EOF'
feat(theme): Android 다크 모드 지원 (iOS↔Android divergence 해소)

- AppColors의 모든 토큰을 @Composable @ReadOnlyComposable getter로 변환
  → 사용 시점에 isSystemInDarkTheme() 분기, 화면 사용처 변경 zero
- PoseTheme에 darkColorScheme 분기 추가
- 다크 hex: Brand·Surface·OnSurface·Divider는 iOS 차용
           Status 4상태는 라이트 hex 명도 +10% (Android mint 톤 유지)
- 라이트 hex는 모두 그대로 (사용자 시각 변화 0)

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
git push origin main
```

### Step 3: 티켓 T06 갱신

`티켓/2026-05-21/T06-android-dark-mode-support.md`:
- 상태: `🔄 진행 중` → `✅ 완료`
- "한 일" 체크박스 모두 ✅
- "산출물" 섹션에 실제 commit hash·변경 파일·검증 결과 기록
- "트러블슈팅" 섹션에 발견된 이슈/조정 hex 기록

### Step 4: 백로그 정리

`docs/backlog.md`:
- "🟠 안드로이드 다크 모드 지원 (iOS↔Android divergence 해소)" 항목을 "완료 아카이브 / 2026-05-21" 로 이동

### Step 5: CLAUDE.md 디자인 시스템 항목 갱신

`PoseAnalyzerAndroid/CLAUDE.md`:
- "디자인 시스템" 섹션에 다크 모드 지원 명시 한 줄 추가

---

## Self-Review

- **Spec 커버리지**: 토큰 추가 (Task 1), Theme 분기 (Task 2), 시각 검증 (Task 3), 피드백 반영 (Task 4), 마무리 (Task 5) — 백로그 항목 4단계 다음 액션 모두 매핑
- **Placeholder 검사**: "TBD"·"적절히 처리" 없음. 모든 hex 값 명시. 코드 블록 풀로
- **타입·이름 일관성**: `AppColors.X` 호출은 기존과 동일 (이름 변경 없음). 토큰 명 일관
- **No migration**: 23개 화면·120 사용처 변경 0건 — `@Composable scope` 확인 끝

## 위험 / 변경 시 영향

- **시각**: 라이트 hex 변경 0 → 사용자 시각 변화 없음. 단 다크 hex는 첫 시도 → 시각 검증 단계에서 fine-tune 가능
- **빌드**: `@Composable` getter는 호출처가 @Composable scope여야 함. 사전 조사 결과 zero 위반. 만약 새 호출처가 비-composable이면 컴파일 오류로 즉시 잡힘
- **성능**: getter 호출 시 recompose 단위로 hex 결정 — 무시 가능한 비용. `@ReadOnlyComposable` 어노테이션으로 더 최적화
- **iOS와의 hex 차이**: 표면·전경·테두리·Brand는 일치. 4상태 다크만 Android 고유 (mint 톤 유지를 위해)

---

## 실행 핸드오프

Plan complete and saved to `docs/plans/2026-05-21-android-dark-mode-support.md`. 두 실행 방식:

1. **Inline Execution (권장)** — 작은 변경(파일 2개)이고 사용자가 중간에 시각 검증을 직접 해야 함. 한 세션에서 Task 1·2 → 사용자가 Task 3 검증 → Task 4·5.
2. **Subagent-Driven** — 각 task별 subagent dispatch. 이 plan은 task 4개 이하고 의존이 강해 오버킬일 가능성.

**권장: Inline Execution.**
