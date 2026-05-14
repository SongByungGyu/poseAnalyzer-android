# Plan A2a — Android UI Foundation 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Jetpack Compose 기반 디자인 시스템 토큰 + 7개 공통 컴포넌트 작성. iOS의 `AppColor`/`AppFont`/`AppSpacing` + Common/Components 7종에 1:1 대응.

**Architecture:** Material 3 Theme를 베이스로 두되, Pose Indigo 브랜드 토큰을 별도 `AppTheme`로 wrap. 공통 컴포넌트는 stateless Compose 함수 (state는 caller가 hoist).

**Tech Stack:** Jetpack Compose, Material 3, Compose Foundation, Compose UI Tooling (Preview)

**선행 문서:**
- `docs/specs/2026-05-13-pose-analyzer-design.md` (iOS와 공유)
- `docs/design/README.md` (디자인 시스템)
- `docs/design/colors_and_type.css` (디자인 토큰 원본)

**완료 후 상태:** 모든 디자인 토큰 + 공통 컴포넌트가 `@Preview`로 렌더 확인 가능. 다음 Plan A2b에서 측정 화면 작성 시 곧바로 사용 가능한 상태.

---

## iOS ↔ Android 매핑

| iOS (SwiftUI) | Android (Compose) |
|---|---|
| `Color.brandPrimary` | `AppColors.BrandPrimary` (Material 3 `primary`로도 매핑) |
| `Font.appHeadline` (custom) | `AppTypography.headline` (Material `TextStyle`) |
| `AppSpacing.s4` (16) | `AppSpacing.s4: Dp` |
| `StatusBadge(status:)` | `StatusBadge(status: PostureStatus, modifier: Modifier)` |
| `AppButton(title:action:)` | `AppButton(text, onClick, modifier, variant, enabled, loading)` |
| `AppCard { ... }` | `AppCard(modifier) { content }` |
| `AppToast(message:)` | `AppToast(message, visible, onDismiss)` (SnackbarHost 베이스) |

---

## Phase 1: 디자인 토큰

### Task 1: AppColors

**파일:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/presentation/theme/AppColors.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.pose.poseanalyzer.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * PoseAnalyzer 브랜드 컬러 토큰.
 *
 * Pose Indigo (#3B5BDB)를 중심으로 한 4단계 상태 컬러.
 * iOS `AppColor.swift` 1:1 대응.
 */
object AppColors {
    // Brand
    val BrandPrimary = Color(0xFF3B5BDB)
    val BrandPrimaryDark = Color(0xFF2A47C3)
    val BrandPrimaryLight = Color(0xFF5B6EE8)

    // Status (PostureStatus)
    val StatusNormal = Color(0xFF56BAB0)      // mint
    val StatusCaution = Color(0xFFE3B341)     // amber
    val StatusSuspect = Color(0xFFE07A5F)     // orange-coral
    val StatusUnmeasurable = Color(0xFF9BA1A6) // neutral gray

    // Neutrals (Light Mode)
    val Surface = Color(0xFFF7F8FA)
    val SurfaceElevated = Color(0xFFFFFFFF)
    val SurfaceMuted = Color(0xFFEEF0F3)
    val OnSurface = Color(0xFF1B1F26)
    val OnSurfaceSecondary = Color(0xFF5A6068)
    val OnSurfaceTertiary = Color(0xFF8B9099)
    val Divider = Color(0xFFE2E5EA)
}
```

- [ ] **Step 2: 빌드 + commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -5
git add app/src/main/java/com/pose/poseanalyzer/presentation/theme/AppColors.kt
git commit -m "feat(theme): add AppColors brand + status tokens"
```

---

### Task 2: AppTypography

**파일:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/presentation/theme/AppTypography.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.pose.poseanalyzer.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * PoseAnalyzer 타이포 토큰 (합니다체 톤 — "중립 관찰자").
 *
 * 시스템 폰트(SF Pro / Pretendard 비슷한 SDK 기본)를 그대로 사용.
 * iOS `AppFont.swift` 1:1 대응.
 */
object AppTypography {
    val display = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
    val title = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)
    val headline = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    val body = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp)
    val callout = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
    val caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, color = AppColors.OnSurfaceSecondary)
    val micro = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
}

/** Material 3 Typography로도 노출 (시스템 컴포넌트가 끌어 쓸 수 있게) */
val PoseTypography = Typography(
    displayMedium = AppTypography.display,
    titleLarge = AppTypography.title,
    titleMedium = AppTypography.headline,
    bodyLarge = AppTypography.body,
    bodyMedium = AppTypography.callout,
    bodySmall = AppTypography.caption,
    labelSmall = AppTypography.micro
)
```

- [ ] **Step 2: commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -3
git add app/src/main/java/com/pose/poseanalyzer/presentation/theme/AppTypography.kt
git commit -m "feat(theme): add AppTypography + PoseTypography"
```

---

### Task 3: AppSpacing + AppShapes

**파일:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/presentation/theme/AppSpacing.kt`
- 생성: `app/src/main/java/com/pose/poseanalyzer/presentation/theme/AppShapes.kt`

- [ ] **Step 1: AppSpacing**

```kotlin
package com.pose.poseanalyzer.presentation.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 4dp 베이스의 간격 스케일.
 * iOS `AppSpacing.swift` 1:1 대응 (4pt → 4dp).
 */
object AppSpacing {
    val s0: Dp = 0.dp
    val s1: Dp = 4.dp
    val s2: Dp = 8.dp
    val s3: Dp = 12.dp
    val s4: Dp = 16.dp
    val s5: Dp = 20.dp
    val s6: Dp = 24.dp
    val s7: Dp = 32.dp
    val s8: Dp = 40.dp
    val s9: Dp = 48.dp
    val s10: Dp = 56.dp
    val s11: Dp = 72.dp
}
```

- [ ] **Step 2: AppShapes**

```kotlin
package com.pose.poseanalyzer.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object AppShapes {
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(16.dp)
    val xlarge = RoundedCornerShape(20.dp)
}

val PoseShapes = Shapes(
    small = AppShapes.small,
    medium = AppShapes.medium,
    large = AppShapes.large,
    extraLarge = AppShapes.xlarge
)
```

- [ ] **Step 3: commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -3
git add app/src/main/java/com/pose/poseanalyzer/presentation/theme/AppSpacing.kt \
        app/src/main/java/com/pose/poseanalyzer/presentation/theme/AppShapes.kt
git commit -m "feat(theme): add AppSpacing + AppShapes"
```

---

### Task 4: PoseTheme (전체 wrap)

**파일:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/presentation/theme/PoseTheme.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.pose.poseanalyzer.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * 앱 전역 테마 — 모든 화면을 이 컴포지블로 감싸야 토큰이 적용됨.
 *
 * Material 3 ColorScheme를 [AppColors] 값으로 채움. 다크 모드는 1차 MVP 미지원
 * (iOS와 동일 — Plan B 또는 추후 확장).
 */
@Composable
fun PoseTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
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
    MaterialTheme(
        colorScheme = colorScheme,
        typography = PoseTypography,
        shapes = PoseShapes,
        content = content
    )
}
```

- [ ] **Step 2: commit**

```bash
git add app/src/main/java/com/pose/poseanalyzer/presentation/theme/PoseTheme.kt
git commit -m "feat(theme): add PoseTheme wrapping Material 3"
```

---

## Phase 2: 공통 컴포넌트

### Task 5: StatusBadge

**파일:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/presentation/common/StatusBadge.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.pose.poseanalyzer.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppTypography
import com.pose.poseanalyzer.presentation.theme.PoseTheme

/**
 * 4단계 상태 라벨 (🟢 정상 / 🟡 주의 / 🟠 의심 / ⚪ 측정 불가).
 *
 * iOS `StatusBadge.swift` 1:1 대응.
 */
@Composable
fun StatusBadge(
    status: PostureStatus,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        PostureStatus.NORMAL -> AppColors.StatusNormal
        PostureStatus.CAUTION -> AppColors.StatusCaution
        PostureStatus.SUSPECT -> AppColors.StatusSuspect
        PostureStatus.UNMEASURABLE -> AppColors.StatusUnmeasurable
    }
    Text(
        text = status.koreanName,
        style = AppTypography.micro,
        color = Color.White,
        modifier = modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Preview
@Composable
private fun PreviewStatusBadge() {
    PoseTheme {
        StatusBadge(PostureStatus.NORMAL)
    }
}
```

- [ ] **Step 2: commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -3
git add app/src/main/java/com/pose/poseanalyzer/presentation/common/StatusBadge.kt
git commit -m "feat(ui): add StatusBadge"
```

---

### Task 6: AppButton

**파일:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/presentation/common/AppButton.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.pose.poseanalyzer.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography
import com.pose.poseanalyzer.presentation.theme.PoseTheme

enum class AppButtonVariant { Primary, Secondary, Text }

/**
 * 앱 표준 버튼.
 *
 * - [Primary]: 채움 (브랜드 색)
 * - [Secondary]: 테두리만
 * - [Text]: 텍스트만
 *
 * iOS `AppButton.swift` 1:1 대응.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val content: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (loading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(text, style = AppTypography.headline)
            }
        }
    }
    val btnModifier = modifier.height(48.dp)
    when (variant) {
        AppButtonVariant.Primary -> Button(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = btnModifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.BrandPrimary,
                contentColor = AppColors.SurfaceElevated
            )
        ) { content() }
        AppButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = btnModifier
        ) { content() }
        AppButtonVariant.Text -> TextButton(
            onClick = onClick,
            enabled = enabled && !loading,
            modifier = btnModifier.wrapContentWidth()
        ) { content() }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewButtons() {
    PoseTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s2)) {
            AppButton("측정 시작", {})
            AppButton("취소", {}, variant = AppButtonVariant.Secondary)
            AppButton("자세히", {}, variant = AppButtonVariant.Text)
        }
    }
}
```

- [ ] **Step 2: commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -3
git add app/src/main/java/com/pose/poseanalyzer/presentation/common/AppButton.kt
git commit -m "feat(ui): add AppButton (Primary/Secondary/Text)"
```

---

### Task 7: AppCard

**파일:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/presentation/common/AppCard.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.pose.poseanalyzer.presentation.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppShapes
import com.pose.poseanalyzer.presentation.theme.AppSpacing

/**
 * 콘텐츠를 감싸는 표준 카드 — 흰 배경, 12dp radius, 1dp elevation.
 *
 * iOS `AppCard.swift` 1:1 대응.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.ui.unit.Dp = AppSpacing.s4,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = AppShapes.medium,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}
```

- [ ] **Step 2: commit**

```bash
git add app/src/main/java/com/pose/poseanalyzer/presentation/common/AppCard.kt
git commit -m "feat(ui): add AppCard"
```

---

### Task 8: AppNavBar (TopAppBar wrap)

**파일:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/presentation/common/AppNavBar.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.pose.poseanalyzer.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppTypography
import com.pose.poseanalyzer.presentation.theme.PoseTheme

/**
 * 화면 상단 navigation bar — Center title + 옵션 back/trailing.
 *
 * iOS `AppNavBar.swift` 1:1 대응.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        title = { Text(title, style = AppTypography.headline) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                }
            }
        },
        actions = { trailing?.invoke() ?: Box(Modifier) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = AppColors.Surface,
            titleContentColor = AppColors.OnSurface
        ),
        modifier = modifier
    )
}

@Preview
@Composable
private fun PreviewNavBar() {
    PoseTheme {
        AppNavBar("측정", onBack = {})
    }
}
```

- [ ] **Step 2: commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -3
git add app/src/main/java/com/pose/poseanalyzer/presentation/common/AppNavBar.kt
git commit -m "feat(ui): add AppNavBar"
```

---

### Task 9: AppEmptyState

**파일:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/presentation/common/AppEmptyState.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.pose.poseanalyzer.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * 데이터 없음 등 비어 있는 상태 표시 (아이콘 + 제목 + 설명).
 *
 * iOS `AppEmptyState.swift` 1:1 대응.
 */
@Composable
fun AppEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(AppSpacing.s6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.s3)
    ) {
        Icon(
            icon, contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = AppColors.OnSurfaceTertiary
        )
        Text(title, style = AppTypography.headline, color = AppColors.OnSurface)
        Text(
            description, style = AppTypography.body, color = AppColors.OnSurfaceSecondary,
            textAlign = TextAlign.Center
        )
    }
}
```

- [ ] **Step 2: commit**

```bash
git add app/src/main/java/com/pose/poseanalyzer/presentation/common/AppEmptyState.kt
git commit -m "feat(ui): add AppEmptyState"
```

---

### Task 10: SectionHeader

**파일:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/presentation/common/SectionHeader.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.pose.poseanalyzer.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pose.poseanalyzer.presentation.theme.AppColors
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography

/**
 * 리스트/카드 섹션 상단 — 제목 + 옵션 trailing 액션.
 *
 * iOS `SectionHeader.swift` 1:1 대응.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    onTrailingClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.padding(vertical = AppSpacing.s2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = AppTypography.title, color = AppColors.OnSurface)
        if (trailingText != null && onTrailingClick != null) {
            TextButton(onClick = onTrailingClick) {
                Text(trailingText, style = AppTypography.callout, color = AppColors.BrandPrimary)
            }
        }
    }
}
```

- [ ] **Step 2: commit**

```bash
git add app/src/main/java/com/pose/poseanalyzer/presentation/common/SectionHeader.kt
git commit -m "feat(ui): add SectionHeader"
```

---

### Task 11: AppToast

**파일:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/presentation/common/AppToast.kt`

- [ ] **Step 1: 작성**

```kotlin
package com.pose.poseanalyzer.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pose.poseanalyzer.presentation.theme.AppShapes
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography
import kotlinx.coroutines.delay

/**
 * 하단에서 올라오는 짧은 알림 — SnackbarHost 대용.
 *
 * iOS `AppToast.swift` 1:1 대응.
 */
@Composable
fun AppToast(
    message: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
    durationMs: Long = 2000L,
    onDismiss: () -> Unit
) {
    LaunchedEffect(visible, message) {
        if (visible) {
            delay(durationMs)
            onDismiss()
        }
    }
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            Text(
                text = message,
                style = AppTypography.callout,
                color = Color.White,
                modifier = Modifier
                    .padding(AppSpacing.s4)
                    .background(Color.Black.copy(alpha = 0.85f), AppShapes.medium)
                    .padding(horizontal = AppSpacing.s4, vertical = AppSpacing.s3)
            )
        }
    }
}
```

- [ ] **Step 2: commit**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -3
git add app/src/main/java/com/pose/poseanalyzer/presentation/common/AppToast.kt
git commit -m "feat(ui): add AppToast"
```

---

## Phase 3: 통합 + MainActivity 업데이트

### Task 12: MainActivity가 PoseTheme로 wrap + 공통 컴포넌트 preview 화면

**파일:**
- 수정: `app/src/main/java/com/pose/poseanalyzer/MainActivity.kt`

- [ ] **Step 1: 수정**

```kotlin
package com.pose.poseanalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.presentation.common.*
import com.pose.poseanalyzer.presentation.theme.AppSpacing
import com.pose.poseanalyzer.presentation.theme.AppTypography
import com.pose.poseanalyzer.presentation.theme.PoseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PoseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PreviewGallery()
                }
            }
        }
    }
}

/**
 * Plan A2a 시점의 통합 화면 — 모든 공통 컴포넌트를 한 번에 확인.
 * Plan A2b에서 HomeScreen + 측정 마법사로 교체 예정.
 */
@Composable
private fun PreviewGallery() {
    var toastVisible by remember { mutableStateOf(false) }
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.s4),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s4)
        ) {
            AppNavBar(title = "PoseAnalyzer")

            SectionHeader("상태 뱃지")
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s2)) {
                StatusBadge(PostureStatus.NORMAL)
                StatusBadge(PostureStatus.CAUTION)
                StatusBadge(PostureStatus.SUSPECT)
                StatusBadge(PostureStatus.UNMEASURABLE)
            }

            SectionHeader("버튼")
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s2)) {
                AppButton("측정 시작", { toastVisible = true })
                AppButton("취소", {}, variant = AppButtonVariant.Secondary)
            }

            SectionHeader("카드")
            AppCard {
                Text("거북목", style = AppTypography.headline)
                Text("측면 기준 168°", style = AppTypography.body)
            }

            SectionHeader("빈 상태")
            AppEmptyState(
                icon = Icons.Filled.Inbox,
                title = "측정 기록이 없습니다",
                description = "오른쪽 상단의 새 측정 버튼으로 시작해보세요."
            )
        }
        AppToast(
            message = "토스트 알림입니다",
            visible = toastVisible,
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
            onDismiss = { toastVisible = false }
        )
    }
}
```

- [ ] **Step 2: 빌드 + 에뮬레이터 확인**

```bash
./gradlew :app:assembleDebug 2>&1 | tail -5
./gradlew :app:installDebug 2>&1 | tail -3
adb shell am start -n com.pose.poseanalyzer/.MainActivity
```

확인 사항: PreviewGallery 화면이 PoseIndigo 톤으로 정상 렌더됨, 버튼 누르면 Toast가 잠깐 떴다 사라짐.

- [ ] **Step 3: commit + Plan A2a tag**

```bash
git add app/src/main/java/com/pose/poseanalyzer/MainActivity.kt
git commit -m "feat(ui): wrap MainActivity in PoseTheme + component gallery preview"
git tag -a plan-a2a-ui-foundation-complete -m "Plan A2a (UI Foundation) 완료"
git push origin main
git push origin plan-a2a-ui-foundation-complete
```

---

## ✅ Plan A2a 완료 정의

- [ ] 4개 디자인 토큰 (Colors / Typography / Spacing+Shapes / PoseTheme)
- [ ] 7개 공통 컴포넌트 (StatusBadge / AppButton / AppCard / AppNavBar / AppEmptyState / SectionHeader / AppToast)
- [ ] MainActivity에 PreviewGallery 적용 (에뮬레이터에서 시각 확인)
- [ ] 모든 컴포넌트 `@Preview` 동작
- [ ] `plan-a2a-ui-foundation-complete` git tag + push

---

## ⏭ 다음 단계 (Plan A2b)

Plan A2a 완료 후 측정 플로우:
- HomeScreen (홈 진입 + 최근 결과 1개 미리보기 카드 + 측정 시작 CTA)
- CameraSessionManager (CameraX wrapper)
- CustomCameraScreen + PoseGuideOverlay
- PhotoInputSheet (카메라 / 갤러리 선택)
- PhotoPicker integration
- MeasurementWizardScreen (3 step: 정면 → 측면 → 키 입력)
- AnalyzingScreen (UseCase 호출 + 진행률 표시)
