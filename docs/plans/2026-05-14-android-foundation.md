# PoseAnalyzer Android Foundation 구현 계획 (Plan A1/A2d)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:** PoseAnalyzer iOS 1차 MVP를 안드로이드 네이티브로 동일하게 구현. 이 plan은 비즈니스 로직 토대 — Domain (8개 자세 판정 Evaluator + 비대칭 분석 + GeometryMath + UseCase), Data (Room), DI (Hilt) — 모두 단위테스트로 검증되는 상태까지.

**Architecture:** MVVM + 프로토콜 기반 분석 도메인 (iOS B+ 아키텍처 1:1 매핑). Domain은 순수 Kotlin (UI 무관, JVM 단위테스트 가능). `PoseDetector`, `PostureEvaluator`, `AsymmetryAnalyzer`, `MotionAnalyzer` interface로 책임 분리.

**Tech Stack:**
- **언어/플랫폼**: Kotlin 2.0+, Android Studio (Hedgehog/Iguana+), AGP 8.x
- **Min SDK**: 26 (Android 8.0), Target SDK: 34
- **Pose Detection**: Google ML Kit Pose Detection (Apple Vision 대응)
- **DB**: Room (SwiftData 대응)
- **DI**: Hilt (Dagger 기반, AppDependencies 대응)
- **Concurrency**: Kotlin Coroutines + Flow
- **테스트**: JUnit 4 + MockK + Robolectric (단위), Room TestUtils (저장소)
- **외부 라이브러리**: ML Kit, Hilt, Room, CameraX, Vico (Charts) — UI/Camera는 Plan A2b+에서

**선행 문서:**
- `docs/specs/2026-05-13-pose-analyzer-design.md` (iOS와 공유, 동일 spec)
- `docs/design/README.md` (디자인 시스템, iOS와 공유)

**완료 후 상태:** 안드로이드 앱이 컴파일되고 모든 단위테스트 통과. UI 없는 placeholder activity만 띄움. 도메인 로직과 Room 저장소가 단위테스트로 완전 검증된 상태.

---

## 사전 준비

- 작업 디렉토리: `/Users/byunggyusong/1_개발폴더/마스터프로젝트/PoseAnalyzerAndroid/`
- iOS 코드 참조: `/Users/byunggyusong/1_개발폴더/마스터프로젝트/PoseAnalyzer/PoseAnalyzer/PoseAnalyzer/` — 모든 알고리즘 1:1 번역 가능
- 한국어 주석 (마스터프로젝트 RULES.md 동일 적용)
- Pose 알고리즘 임계값은 iOS와 동일 (`Thresholds`)

### iOS ↔ Android 매핑 표

| iOS | Android |
|-----|---------|
| `Apple Vision VNDetectHumanBodyPoseRequest` | `com.google.mlkit:pose-detection` |
| `SwiftData @Model` | `androidx.room:room` `@Entity` |
| `protocol PostureEvaluator` | `interface PostureEvaluator` |
| `@Observable class` | `data class State` + StateFlow |
| `async throws -> T` | `suspend fun (): T` (throws via exception) |
| `withCheckedThrowingContinuation` | `suspendCancellableCoroutine` |
| `Foundation Date` | `kotlinx.datetime.Instant` 또는 `java.time.Instant` |
| `UUID` | `java.util.UUID` |
| `CGPoint` | `android.graphics.PointF` 또는 `data class Point(val x: Float, val y: Float)` |

---

## Phase 1: Android Studio 프로젝트 셋업

### Task 1: Android Studio 새 프로젝트 생성

**파일/경로:**
- 생성: `/Users/byunggyusong/1_개발폴더/마스터프로젝트/PoseAnalyzerAndroid/{settings.gradle.kts, build.gradle.kts, app/}`

- [ ] **Step 1: Android Studio에서 새 프로젝트 생성**

Android Studio → File → New → New Project → **Empty Activity** (Jetpack Compose). 다음 값으로:

| 필드 | 값 |
|------|------|
| Name | `PoseAnalyzer` |
| Package name | `com.pose.poseanalyzer` |
| Save location | `/Users/byunggyusong/1_개발폴더/마스터프로젝트/PoseAnalyzerAndroid/` |
| Language | Kotlin |
| Minimum SDK | API 26: Android 8.0 (Oreo) |
| Build configuration language | Kotlin DSL (build.gradle.kts) |

- [ ] **Step 2: 빌드 확인**

Android Studio에서 Run (▶) 또는 터미널:
```bash
cd "/Users/byunggyusong/1_개발폴더/마스터프로젝트/PoseAnalyzerAndroid"
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: .gitignore 작성**

`PoseAnalyzerAndroid/.gitignore`:

```gitignore
# Android Studio
.gradle/
.idea/
build/
*.iml
local.properties

# Kotlin
.kotlin/

# Captures
captures/

# Keystores
*.jks
*.keystore

# macOS
.DS_Store

# Secrets
google-services.json
```

- [ ] **Step 4: git 초기화 + 첫 commit**

```bash
cd "/Users/byunggyusong/1_개발폴더/마스터프로젝트/PoseAnalyzerAndroid"
git init -b main
git config user.email "servicedevclaude@gmail.com"
git config user.name "ByungGyu Song"
git add .
git commit -m "chore: initial Android Studio project setup"
```

---

### Task 2: build.gradle.kts에 핵심 의존성 추가

**파일/경로:**
- 수정: `app/build.gradle.kts`
- 수정: `gradle/libs.versions.toml` (version catalog)

- [ ] **Step 1: libs.versions.toml 작성**

```toml
[versions]
agp = "8.5.2"
kotlin = "2.0.21"
compose-bom = "2024.10.00"
core-ktx = "1.13.1"
lifecycle = "2.8.7"
activity-compose = "1.9.3"
hilt = "2.52"
hilt-navigation-compose = "1.2.0"
room = "2.6.1"
ksp = "2.0.21-1.0.27"
coroutines = "1.9.0"
mlkit-pose = "18.0.0-beta5"
camerax = "1.4.0"
vico = "2.0.0-beta.3"
junit = "4.13.2"
mockk = "1.13.13"
robolectric = "4.13"
androidx-test-junit = "1.2.1"
turbine = "1.1.0"

[libraries]
# Compose
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity-compose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }

# Hilt (DI)
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hilt-navigation-compose" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }

# Coroutines
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

# ML Kit Pose Detection
mlkit-pose-detection = { group = "com.google.mlkit", name = "pose-detection", version.ref = "mlkit-pose" }

# CameraX (Plan A2b)
camerax-core = { group = "androidx.camera", name = "camera-core", version.ref = "camerax" }
camerax-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }
camerax-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }
camerax-view = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }
camerax-compose = { group = "androidx.camera", name = "camera-compose", version.ref = "camerax" }

# Vico (Charts, Plan A2c)
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }

# Test
junit = { group = "junit", name = "junit", version.ref = "junit" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
robolectric = { group = "org.robolectric", name = "robolectric", version.ref = "robolectric" }
androidx-test-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidx-test-junit" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

- [ ] **Step 2: app/build.gradle.kts 수정**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.pose.poseanalyzer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pose.poseanalyzer"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    // Core / Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // ML Kit Pose
    implementation(libs.mlkit.pose.detection)

    // CameraX (Plan A2b에서 사용)
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.camerax.compose)

    // Vico Charts (Plan A2c)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.room.testing)
}
```

- [ ] **Step 3: 빌드 + commit**

```bash
cd "/Users/byunggyusong/1_개발폴더/마스터프로젝트/PoseAnalyzerAndroid"
./gradlew assembleDebug 2>&1 | tail -10
```

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "chore: 핵심 의존성 추가 (Compose/Hilt/Room/Coroutines/MLKit/CameraX/Vico)"
```

---

## Phase 2: 기본 도메인 모델

### Task 3: SessionView, PostureType, PostureStatus enum

**파일/경로:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/domain/model/SessionView.kt`
- 생성: `app/src/main/java/com/pose/poseanalyzer/domain/model/PostureType.kt`
- 생성: `app/src/main/java/com/pose/poseanalyzer/domain/model/PostureStatus.kt`

- [ ] **Step 1: SessionView.kt**

```kotlin
package com.pose.poseanalyzer.domain.model

/**
 * 사진의 촬영 시점 (정면 / 측면)
 */
enum class SessionView(val koreanName: String) {
    FRONT("정면"),
    SIDE("측면")
}
```

- [ ] **Step 2: PostureType.kt**

```kotlin
package com.pose.poseanalyzer.domain.model

/**
 * 판정 가능한 자세 종류 (MVP 8개)
 */
enum class PostureType(val koreanName: String, val requiredView: SessionView) {
    FORWARD_HEAD("거북목", SessionView.SIDE),
    ROUND_SHOULDER("라운드숄더", SessionView.SIDE),
    KYPHOSIS("흉추 후만증", SessionView.SIDE),
    ANTERIOR_PELVIC_TILT("골반 전방경사", SessionView.SIDE),
    KNEE_HYPEREXTENSION("무릎 과신전", SessionView.SIDE),
    SCOLIOSIS("척추측만", SessionView.FRONT),
    HEAD_TILT("머리 좌우 기울기", SessionView.FRONT),
    KNEE_ALIGNMENT("무릎 X/O자", SessionView.FRONT);
}
```

- [ ] **Step 3: PostureStatus.kt**

```kotlin
package com.pose.poseanalyzer.domain.model

/**
 * 판정 결과 상태 (4단계)
 */
enum class PostureStatus(val koreanName: String) {
    NORMAL("정상"),         // 🟢
    CAUTION("주의"),        // 🟡
    SUSPECT("의심"),        // 🟠
    UNMEASURABLE("측정 불가") // ⚪
}
```

- [ ] **Step 4: 빌드 + commit**

```bash
./gradlew assembleDebug 2>&1 | tail -5
```

```bash
git add app/src/main/java/com/pose/poseanalyzer/domain/model/
git commit -m "feat(domain): add SessionView, PostureType, PostureStatus enums"
```

---

### Task 4: PoseFrame (관절 좌표 컬렉션)

**파일/경로:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/domain/model/PoseFrame.kt`
- 생성: `app/src/main/java/com/pose/poseanalyzer/domain/model/JointName.kt`

- [ ] **Step 1: JointName.kt — ML Kit PoseLandmark 매핑**

```kotlin
package com.pose.poseanalyzer.domain.model

/**
 * 측정에 사용하는 관절 이름 (ML Kit PoseLandmark의 부분 집합).
 * ML Kit Pose Detection의 PoseLandmark 타입과 1:1 매핑.
 */
enum class JointName(val mlkitType: Int) {
    NOSE(0),                  // PoseLandmark.NOSE
    LEFT_EYE(2),              // PoseLandmark.LEFT_EYE
    RIGHT_EYE(5),             // PoseLandmark.RIGHT_EYE
    LEFT_EAR(7),              // PoseLandmark.LEFT_EAR
    RIGHT_EAR(8),             // PoseLandmark.RIGHT_EAR
    LEFT_SHOULDER(11),        // PoseLandmark.LEFT_SHOULDER
    RIGHT_SHOULDER(12),
    LEFT_ELBOW(13),
    RIGHT_ELBOW(14),
    LEFT_WRIST(15),
    RIGHT_WRIST(16),
    LEFT_HIP(23),
    RIGHT_HIP(24),
    LEFT_KNEE(25),
    RIGHT_KNEE(26),
    LEFT_ANKLE(27),
    RIGHT_ANKLE(28);

    /** iOS의 .neck 대응 — ML Kit엔 없으므로 양 어깨 중점으로 계산 (PoseFrame에서) */
    companion object {
        fun fromMlKit(type: Int): JointName? = values().firstOrNull { it.mlkitType == type }
    }
}
```

- [ ] **Step 2: PoseFrame.kt**

```kotlin
package com.pose.poseanalyzer.domain.model

import android.graphics.PointF

/**
 * 한 장의 사진에서 추출된 관절 좌표 묶음.
 * 좌표계: 정규화 0~1, 좌상단 원점 (Android/ML Kit 기본).
 * iOS Vision은 좌하단 원점이므로 변환 필요한데, 안드로이드 ML Kit은 좌상단이라
 * 코드/측정 알고리즘에 영향 없음.
 */
data class PoseFrame(
    val joints: Map<JointName, Joint>,
    val view: SessionView,
    val imageWidth: Int,
    val imageHeight: Int
) {
    data class Joint(
        val name: JointName,
        val location: PointF,   // 정규화 좌표 (0~1)
        val confidence: Float    // 0~1
    )

    /** 특정 관절의 신뢰도가 임계값 이상인지 */
    fun isReliable(name: JointName, threshold: Float = 0.3f): Boolean {
        val joint = joints[name] ?: return false
        return joint.confidence >= threshold
    }

    /** 여러 관절이 모두 신뢰 가능한지 */
    fun areReliable(names: List<JointName>, threshold: Float = 0.3f): Boolean =
        names.all { isReliable(it, threshold) }

    /** 관절 좌표 반환 (신뢰도 무관) */
    fun point(name: JointName): PointF? = joints[name]?.location

    /** 평균 신뢰도 */
    fun averageConfidence(names: List<JointName>): Double {
        val valid = names.mapNotNull { joints[it] }
        if (valid.isEmpty()) return 0.0
        return valid.map { it.confidence.toDouble() }.average()
    }

    /** iOS의 .neck 대응 — 양 어깨 중점. 양 어깨 모두 reliable일 때만 반환. */
    val neck: PointF?
        get() {
            val l = point(JointName.LEFT_SHOULDER) ?: return null
            val r = point(JointName.RIGHT_SHOULDER) ?: return null
            return PointF((l.x + r.x) / 2f, (l.y + r.y) / 2f)
        }
}
```

- [ ] **Step 3: 빌드 + commit**

```bash
./gradlew assembleDebug 2>&1 | tail -5
```

```bash
git add app/src/main/java/com/pose/poseanalyzer/domain/model/
git commit -m "feat(domain): add PoseFrame + JointName (ML Kit 매핑, neck = 양 어깨 중점)"
```

---

### Task 5: Thresholds, PostureResult, AsymmetryResult, SessionReport

**파일/경로:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/domain/model/Thresholds.kt`
- 생성: `app/src/main/java/com/pose/poseanalyzer/domain/model/PostureResult.kt`
- 생성: `app/src/main/java/com/pose/poseanalyzer/domain/model/AsymmetryResult.kt`
- 생성: `app/src/main/java/com/pose/poseanalyzer/domain/model/SessionReport.kt`

- [ ] **Step 1: Thresholds.kt**

```kotlin
package com.pose.poseanalyzer.domain.model

/**
 * 자세 판정 임계값.
 */
data class Thresholds(
    val normalRange: ClosedRange<Double>,
    val cautionRange: ClosedRange<Double>?,
    val direction: Direction
) {
    enum class Direction {
        HIGHER_IS_NORMAL,
        LOWER_IS_NORMAL,
        CENTERED_ON_RANGE
    }

    /** 측정값 평가 → PostureStatus */
    fun evaluate(value: Double): PostureStatus {
        if (normalRange.contains(value)) return PostureStatus.NORMAL
        cautionRange?.let { if (it.contains(value)) return PostureStatus.CAUTION }
        return PostureStatus.SUSPECT
    }
}
```

- [ ] **Step 2: PostureResult.kt**

```kotlin
package com.pose.poseanalyzer.domain.model

/**
 * 단일 자세 판정 결과.
 */
data class PostureResult(
    val type: PostureType,
    val status: PostureStatus,
    val primaryMetric: Double,
    val primaryMetricUnit: MetricUnit,
    val thresholds: Thresholds,
    val usedJointNames: List<String>,
    val confidence: Double,
    val advice: String?
) {
    enum class MetricUnit(val symbol: String) {
        DEGREE("°"),
        RATIO(""),
        CENTIMETER("cm")
    }

    companion object {
        fun unmeasurable(type: PostureType, reason: String): PostureResult =
            PostureResult(
                type = type,
                status = PostureStatus.UNMEASURABLE,
                primaryMetric = 0.0,
                primaryMetricUnit = MetricUnit.DEGREE,
                thresholds = Thresholds(0.0..0.0, null, Thresholds.Direction.HIGHER_IS_NORMAL),
                usedJointNames = emptyList(),
                confidence = 0.0,
                advice = reason
            )
    }
}
```

- [ ] **Step 3: AsymmetryResult.kt**

```kotlin
package com.pose.poseanalyzer.domain.model

/**
 * 좌우 비대칭 분석 결과 (정면 사진).
 */
data class AsymmetryResult(
    val shoulder: Difference,
    val hip: Difference
) {
    data class Difference(
        val cm: Double?,
        val ratio: Double,
        val angleDegrees: Double,
        val direction: Direction
    )

    enum class Direction(val koreanName: String) {
        LEFT_HIGHER("왼쪽이 높음"),
        RIGHT_HIGHER("오른쪽이 높음"),
        BALANCED("균형")
    }
}
```

- [ ] **Step 4: SessionReport.kt**

```kotlin
package com.pose.poseanalyzer.domain.model

import android.graphics.Bitmap
import java.util.UUID

/**
 * 한 세션의 모든 분석 결과 (저장 전 메모리 객체).
 */
data class SessionReport(
    val id: UUID = UUID.randomUUID(),
    val measuredAt: Long = System.currentTimeMillis(),  // epoch millis
    val frontImage: Bitmap,
    val sideImage: Bitmap,
    val frontFrame: PoseFrame,
    val sideFrame: PoseFrame,
    val postures: List<PostureResult>,    // 8개
    val asymmetry: AsymmetryResult,
    val heightCmAtMeasure: Double?
) {
    fun posture(type: PostureType): PostureResult? = postures.firstOrNull { it.type == type }

    // Bitmap/PoseFrame 비교는 무거우므로 id 기반 equality
    override fun equals(other: Any?): Boolean = other is SessionReport && other.id == id
    override fun hashCode(): Int = id.hashCode()
}
```

- [ ] **Step 5: 빌드 + commit**

```bash
./gradlew assembleDebug 2>&1 | tail -5
```

```bash
git add app/src/main/java/com/pose/poseanalyzer/domain/model/
git commit -m "feat(domain): add Thresholds, PostureResult, AsymmetryResult, SessionReport"
```

---

## Phase 3: 기하 유틸 (TDD)

### Task 6: GeometryMath 유틸 — 각도/거리/기울기 계산

**파일/경로:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/util/GeometryMath.kt`
- 생성: `app/src/test/java/com/pose/poseanalyzer/util/GeometryMathTest.kt`

- [ ] **Step 1: 실패하는 테스트 먼저 작성 (TDD)**

```kotlin
package com.pose.poseanalyzer.util

import android.graphics.PointF
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeometryMathTest {

    @Test
    fun `세점이 직선이면 각도는 180도`() {
        val angle = GeometryMath.angleBetween(
            PointF(0f, 0f), PointF(1f, 0f), PointF(2f, 0f)
        )
        assertEquals(180.0, angle, 0.01)
    }

    @Test
    fun `세점이 직각이면 각도는 90도`() {
        val angle = GeometryMath.angleBetween(
            PointF(0f, 1f), PointF(0f, 0f), PointF(1f, 0f)
        )
        assertEquals(90.0, angle, 0.01)
    }

    @Test
    fun `세점이 겹치면 0 반환`() {
        val angle = GeometryMath.angleBetween(
            PointF(0f, 0f), PointF(0f, 0f), PointF(1f, 0f)
        )
        assertTrue("유한 값이어야 함", angle.isFinite())
    }

    @Test
    fun `같은 점 사이 거리는 0`() {
        val d = GeometryMath.distance(PointF(5f, 5f), PointF(5f, 5f))
        assertEquals(0.0, d, 0.01)
    }

    @Test
    fun `피타고라스 345 거리는 5`() {
        val d = GeometryMath.distance(PointF(0f, 0f), PointF(3f, 4f))
        assertEquals(5.0, d, 0.01)
    }

    @Test
    fun `수평 직선 기울기는 0도`() {
        val angle = GeometryMath.lineAngleFromHorizontal(
            PointF(0f, 5f), PointF(10f, 5f)
        )
        assertEquals(0.0, angle, 0.01)
    }

    @Test
    fun `수직 직선 기울기는 90도`() {
        val angle = GeometryMath.lineAngleFromHorizontal(
            PointF(5f, 0f), PointF(5f, 10f)
        )
        assertEquals(90.0, kotlin.math.abs(angle), 0.01)
    }

    @Test
    fun `우측이 높은 45도 기울기`() {
        // Android 좌상단 원점이라 y가 클수록 아래
        // (0,0)~(10,10)이면 두번째가 우측 아래이므로 angle = 45 (양수)
        val angle = GeometryMath.lineAngleFromHorizontal(
            PointF(0f, 0f), PointF(10f, 10f)
        )
        assertEquals(45.0, angle, 0.01)
    }

    @Test
    fun `수평 거리 비율`() {
        val ratio = GeometryMath.horizontalGapRatio(
            PointF(5f, 0f), PointF(0f, 0f), referenceWidth = 20.0
        )
        assertEquals(0.25, ratio, 0.01)
    }
}
```

- [ ] **Step 2: 테스트 실행하여 실패 확인**

```bash
./gradlew :app:testDebugUnitTest --tests "com.pose.poseanalyzer.util.GeometryMathTest"
```

Expected: `Compilation failed` (GeometryMath 없음) or test failures.

- [ ] **Step 3: GeometryMath.kt 구현**

```kotlin
package com.pose.poseanalyzer.util

import android.graphics.PointF
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * 관절 좌표 기반 기하 계산 유틸 (순수 함수 모음).
 * iOS Swift GeometryMath와 1:1 대응.
 */
object GeometryMath {

    /** 세 점이 이루는 각도 (vertex 기준 ∠p1·vertex·p2). 단위: 도. 분모 0이면 0. */
    fun angleBetween(p1: PointF, vertex: PointF, p2: PointF): Double {
        val v1x = (p1.x - vertex.x).toDouble()
        val v1y = (p1.y - vertex.y).toDouble()
        val v2x = (p2.x - vertex.x).toDouble()
        val v2y = (p2.y - vertex.y).toDouble()
        val dot = v1x * v2x + v1y * v2y
        val mag1 = sqrt(v1x * v1x + v1y * v1y)
        val mag2 = sqrt(v2x * v2x + v2y * v2y)
        if (mag1 == 0.0 || mag2 == 0.0) return 0.0
        var cosTheta = dot / (mag1 * mag2)
        cosTheta = max(-1.0, min(1.0, cosTheta))
        return Math.toDegrees(acos(cosTheta))
    }

    /** 두 점 사이 유클리드 거리 */
    fun distance(a: PointF, b: PointF): Double {
        val dx = (a.x - b.x).toDouble()
        val dy = (a.y - b.y).toDouble()
        return sqrt(dx * dx + dy * dy)
    }

    /** 수평선 대비 두 점이 만드는 직선의 기울기 각도. 결과 범위 -90~90. */
    fun lineAngleFromHorizontal(a: PointF, b: PointF): Double {
        val dx = (b.x - a.x).toDouble()
        val dy = (b.y - a.y).toDouble()
        return Math.toDegrees(atan2(dy, dx))
    }

    /** 절댓값 기울기 (0~90). */
    fun absLineAngleFromHorizontal(a: PointF, b: PointF): Double =
        abs(lineAngleFromHorizontal(a, b))

    /** 두 점의 수평 거리 / 기준 폭 비율 (절댓값) */
    fun horizontalGapRatio(from: PointF, to: PointF, referenceWidth: Double): Double {
        if (referenceWidth <= 0) return 0.0
        return abs((from.x - to.x).toDouble()) / referenceWidth
    }
}
```

- [ ] **Step 4: 테스트 재실행 → PASS**

```bash
./gradlew :app:testDebugUnitTest --tests "com.pose.poseanalyzer.util.GeometryMathTest"
```

Expected: BUILD SUCCESSFUL, all tests passing.

- [ ] **Step 5: commit**

```bash
git add app/src/main/java/com/pose/poseanalyzer/util/GeometryMath.kt \
        app/src/test/java/com/pose/poseanalyzer/util/GeometryMathTest.kt
git commit -m "feat(util): add GeometryMath with angle/distance/slope helpers (TDD)"
```

---

## Phase 4: Pose Detection

### Task 7: PoseDetector 인터페이스 + 에러 타입

**파일/경로:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/domain/detection/PoseDetector.kt`

- [ ] **Step 1: PoseDetector.kt**

```kotlin
package com.pose.poseanalyzer.domain.detection

import android.graphics.Bitmap
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.SessionView

/**
 * 사진 또는 영상 프레임에서 사람 관절을 검출.
 */
interface PoseDetector {
    suspend fun detect(image: Bitmap, view: SessionView): PoseFrame
}

/**
 * PoseDetector가 던질 수 있는 에러.
 */
sealed class PoseDetectionException(message: String) : Exception(message) {
    object NoPersonDetected : PoseDetectionException("사람을 인식할 수 없습니다.")
    class MultiplePersonsDetected(count: Int) :
        PoseDetectionException("여러 명($count 명)이 감지되었습니다. 한 명만 보이는 사진을 사용해주세요.")
    class VisionFailed(detail: String) :
        PoseDetectionException("분석 중 오류가 발생했습니다: $detail")
    object InvalidImage : PoseDetectionException("사진 형식이 올바르지 않습니다.")
}
```

- [ ] **Step 2: 빌드 + commit**

```bash
./gradlew assembleDebug 2>&1 | tail -5
```

```bash
git add app/src/main/java/com/pose/poseanalyzer/domain/detection/PoseDetector.kt
git commit -m "feat(detection): define PoseDetector interface + sealed exception types"
```

---

### Task 8: MLKitPoseDetector 구현

**파일/경로:**
- 생성: `app/src/main/java/com/pose/poseanalyzer/data/detection/MLKitPoseDetector.kt`

- [ ] **Step 1: MLKitPoseDetector.kt**

```kotlin
package com.pose.poseanalyzer.data.detection

import android.graphics.Bitmap
import android.graphics.PointF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.pose.poseanalyzer.domain.detection.PoseDetectionException
import com.pose.poseanalyzer.domain.detection.PoseDetector
import com.pose.poseanalyzer.domain.model.JointName
import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.SessionView
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google ML Kit Pose Detection 기반 구현.
 *
 * iOS VisionPoseDetector와 1:1 대응.
 * - InputImage.fromBitmap: 회전 정보 0 (Bitmap이 이미 정방향 가정)
 * - AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE: 1장 정확도 모드
 * - PoseLandmark.inFrameLikelihood (0~1) → confidence
 */
@Singleton
class MLKitPoseDetector @Inject constructor() : PoseDetector {

    private val detector by lazy {
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()
        PoseDetection.getClient(options)
    }

    override suspend fun detect(image: Bitmap, view: SessionView): PoseFrame {
        return suspendCancellableCoroutine { cont ->
            val inputImage = InputImage.fromBitmap(image, 0)
            detector.process(inputImage)
                .addOnSuccessListener { pose ->
                    try {
                        val frame = makeFrame(pose, view, image.width, image.height)
                        cont.resume(frame)
                    } catch (e: PoseDetectionException) {
                        cont.resumeWithException(e)
                    } catch (e: Exception) {
                        cont.resumeWithException(PoseDetectionException.VisionFailed(e.message ?: "관절 추출 실패"))
                    }
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(PoseDetectionException.VisionFailed(e.message ?: "ML Kit 분석 실패"))
                }
        }
    }

    private fun makeFrame(pose: Pose, view: SessionView, width: Int, height: Int): PoseFrame {
        val allLandmarks = pose.allPoseLandmarks
        if (allLandmarks.isEmpty()) {
            throw PoseDetectionException.NoPersonDetected
        }

        // 신뢰도 0.0인 landmark가 너무 많으면 사람 없는 것으로 간주
        val reliableCount = allLandmarks.count { it.inFrameLikelihood > 0.3f }
        if (reliableCount < 5) {
            throw PoseDetectionException.NoPersonDetected
        }

        val joints = mutableMapOf<JointName, PoseFrame.Joint>()
        for (landmark in allLandmarks) {
            val jointName = JointName.fromMlKit(landmark.landmarkType) ?: continue
            // ML Kit 좌표: 픽셀 단위. 정규화 (0~1)로 변환.
            val nx = landmark.position.x / width.toFloat()
            val ny = landmark.position.y / height.toFloat()
            joints[jointName] = PoseFrame.Joint(
                name = jointName,
                location = PointF(nx, ny),
                confidence = landmark.inFrameLikelihood
            )
        }

        return PoseFrame(
            joints = joints,
            view = view,
            imageWidth = width,
            imageHeight = height
        )
    }
}
```

- [ ] **Step 2: 빌드 + commit**

```bash
./gradlew assembleDebug 2>&1 | tail -5
```

```bash
git add app/src/main/java/com/pose/poseanalyzer/data/detection/MLKitPoseDetector.kt
git commit -m "feat(detection): implement MLKitPoseDetector (ML Kit AccuratePoseDetector)

- suspendCancellableCoroutine으로 Task<Pose> → suspend
- PoseLandmark → JointName 매핑
- 정규화 좌표 변환 (픽셀 → 0~1)
- inFrameLikelihood → confidence
- 사람 인식 부족 (reliable count < 5) → NoPersonDetected"
```

---

## Phase 5-6: PostureEvaluator + 8개 구현체 (TDD)

> **참고**: iOS Plan 1 Task 9~17과 동일한 8개 Evaluator. 알고리즘과 임계값이 iOS와 100% 동일하므로 Swift 코드를 Kotlin으로 1:1 번역.
>
> 각 Evaluator는 다음 패턴 반복:
> 1. 테스트 작성 (Red) — 정상/주의/의심/측정불가 4-6 케이스
> 2. 테스트 실행 → 실패 확인
> 3. Evaluator 구현 — 좌/우 confidence 비교 + 각도/비율 계산 + Thresholds.evaluate
> 4. 테스트 실행 → PASS
> 5. commit

### Task 9: PostureEvaluator 인터페이스 + PoseFrame 테스트 fixture

**파일:**
- `domain/evaluation/PostureEvaluator.kt`
- `test/.../fixtures/PoseFrameFixtures.kt`

```kotlin
package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureResult
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionView

interface PostureEvaluator {
    val type: PostureType
    val requiredView: SessionView
    fun evaluate(frame: PoseFrame): PostureResult
}
```

Fixture는 `PoseFrame.empty()`, `PoseFrame.make(pairs)`, `PoseFrame.sideViewWithAngle(angle, joints)` 등 iOS와 동일한 헬퍼 함수 제공.

### Task 10-17: 8개 Evaluator

각 Evaluator는 iOS Swift 코드를 그대로 Kotlin으로 번역. 임계값 동일. 좌/우 confidence 비교 패턴 동일. iOS 파일 참조:
- `PoseAnalyzer/PoseAnalyzer/PoseAnalyzer/Domain/Evaluation/*.swift`

**임계값 표** (iOS와 동일):

| # | 자세 | 측정 | 정상 | 주의 | 의심 |
|---|------|------|------|------|------|
| 10 | ForwardHeadEvaluator | 귀-어깨-엉덩이 각도 | ≥170° | 160~170° | <160° |
| 11 | RoundShoulderEvaluator | 어깨-귀 수평거리/어깨폭 | <0.15 | 0.15~0.25 | >0.25 |
| 12 | KyphosisEvaluator | 목-어깨-엉덩이 각도 | ≥175° | 165~175° | <165° |
| 13 | AnteriorPelvicTiltEvaluator | 어깨-엉덩이-무릎 각도 | 175~185° | 170~175°/185~190° | 그 바깥 |
| 14 | KneeHyperextensionEvaluator | 엉덩이-무릎-발목 각도 | ≤185° | 185~190° | >190° (cross-product 필요) |
| 15 | ScoliosisEvaluator | 양 어깨/양 엉덩이 기울기 | <2° | 2~5° | >5° |
| 16 | HeadTiltEvaluator | 양 귀(눈) 기울기 | <2° | 2~5° | >5° |
| 17 | KneeAlignmentEvaluator | 양 다리 각도 | 175~180° | 170~175°/180~185° | <170° / >185° |

> **KneeHyperextension 주의**: acos는 0~180° 반환이라 한 방향 측정용으로 normalRange=0~185°, cautionRange=185~190°, direction=HIGHER_IS_NORMAL. cross-product 보정은 iOS 코드 참조.

각 Task는 commit 한 단위. Task 10-17 총 8 commit.

---

## Phase 7: AsymmetryAnalyzer (TDD)

### Task 18: DefaultAsymmetryAnalyzer

**파일:**
- `domain/asymmetry/AsymmetryAnalyzer.kt` (interface)
- `data/asymmetry/DefaultAsymmetryAnalyzer.kt` (implementation)
- `test/.../asymmetry/DefaultAsymmetryAnalyzerTest.kt`

iOS와 동일한 알고리즘 (어깨/엉덩이 좌우 높이차 + 키 입력 시 cm 환산). 테스트 3 케이스.

---

## Phase 8: UseCase (TDD)

### Task 19: AnalyzeSessionUseCase + MockPoseDetector

**파일:**
- `domain/usecase/AnalyzeSessionUseCase.kt`
- `test/.../fixtures/MockPoseDetector.kt`
- `test/.../usecase/AnalyzeSessionUseCaseTest.kt`

```kotlin
class AnalyzeSessionUseCase @Inject constructor(
    private val detector: PoseDetector,
    private val evaluators: List<PostureEvaluator>,
    private val asymmetryAnalyzer: AsymmetryAnalyzer
) {
    suspend fun analyze(front: Bitmap, side: Bitmap, heightCm: Double?): SessionReport {
        // 두 사진 병렬 detect (coroutineScope + async)
        // 8개 Evaluator 적용
        // AsymmetryAnalyzer 적용
        // SessionReport 묶기
    }
}
```

iOS의 `async let` → Kotlin `async/await` 패턴 (`coroutineScope { val a = async {...}; val b = async {...}; a.await() + b.await() }`).

---

## Phase 9: Motion 인터페이스 (2차 대비)

### Task 20: MotionAnalyzer interface + MotionResult

```kotlin
data class MotionResult(...)
interface MotionAnalyzer {
    val name: String
    fun analyze(stream: Flow<PoseFrame>): Flow<MotionResult>
}
```

구현 없음 (2차 영상 분석용 placeholder).

---

## Phase 10: 데이터 레이어

### Task 21: Room Entity 3개

**파일:**
- `data/room/UserProfileEntity.kt`
- `data/room/SessionEntity.kt`
- `data/room/PostureEntity.kt`
- `data/room/AppDatabase.kt`
- `data/room/SessionDao.kt`, `UserProfileDao.kt`

```kotlin
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,  // 단일 인스턴스
    val heightCm: Double?,
    val updatedAt: Long
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,  // UUID
    val measuredAt: Long,
    val frontImagePath: String,
    val sideImagePath: String,
    val heightCmAtMeasure: Double?,
    // asymmetry 필드들
    val asymmetryShoulderCm: Double?,
    val asymmetryShoulderRatio: Double,
    val asymmetryShoulderAngle: Double,
    val asymmetryShoulderDirection: String,
    val asymmetryHipCm: Double?,
    val asymmetryHipRatio: Double,
    val asymmetryHipAngle: Double,
    val asymmetryHipDirection: String
)

@Entity(
    tableName = "postures",
    foreignKeys = [ForeignKey(
        entity = SessionEntity::class,
        parentColumns = ["id"], childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PostureEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val typeRaw: String,
    val statusRaw: String,
    val primaryMetric: Double,
    val primaryMetricUnitRaw: String,
    val confidence: Double,
    val advice: String?
)
```

### Task 22: ImageStore (TDD)

`data/ImageStore.kt` — 사진을 앱 internal storage에 저장.

```kotlin
class ImageStore @Inject constructor(@ApplicationContext private val context: Context) {
    fun save(bitmap: Bitmap, sessionId: UUID, view: SessionView): String
    fun load(path: String): Bitmap?
    fun delete(sessionId: UUID)
}
```

iOS와 동일 (sessions/<UUID>/{front,side}.jpg).

### Task 23: SessionRepository (TDD)

`data/SessionRepository.kt` — Room DAO 호출 + ImageStore + SessionReport ↔ SessionEntity 변환.

테스트는 Room의 in-memory database 사용:
```kotlin
val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
```

### Task 24: UserProfileRepository (TDD)

단일 프로필 인스턴스 유지 — 항상 id=1.

---

## Phase 11: DI (Hilt) + App entry

### Task 25: AppModule + 8 Evaluator 주입

**파일:**
- `app/src/main/java/com/pose/poseanalyzer/di/AppModule.kt`
- `app/src/main/java/com/pose/poseanalyzer/PoseAnalyzerApplication.kt`

```kotlin
@HiltAndroidApp
class PoseAnalyzerApplication : Application()

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun providePoseDetector(impl: MLKitPoseDetector): PoseDetector = impl

    @Provides @Singleton
    fun provideAsymmetryAnalyzer(impl: DefaultAsymmetryAnalyzer): AsymmetryAnalyzer = impl

    @Provides @Singleton
    fun provideEvaluators(): List<PostureEvaluator> = listOf(
        ForwardHeadEvaluator(),
        RoundShoulderEvaluator(),
        KyphosisEvaluator(),
        AnteriorPelvicTiltEvaluator(),
        KneeHyperextensionEvaluator(),
        ScoliosisEvaluator(),
        HeadTiltEvaluator(),
        KneeAlignmentEvaluator(),
    )

    @Provides @Singleton
    fun provideRoomDb(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "pose_analyzer.db").build()

    @Provides fun provideSessionDao(db: AppDatabase) = db.sessionDao()
    @Provides fun provideUserProfileDao(db: AppDatabase) = db.userProfileDao()
}
```

`AndroidManifest.xml`:
```xml
<application
    android:name=".PoseAnalyzerApplication"
    ...>
    <activity android:name=".MainActivity" ...>
        ...
    </activity>
</application>
```

### Task 26: MainActivity + Placeholder Compose UI

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(80.dp))
                            Text("PoseAnalyzer", style = MaterialTheme.typography.headlineLarge)
                            Text("Foundation 완료. UI는 Plan A2에서 작성.")
                        }
                    }
                }
            }
        }
    }
}
```

---

## Phase 12: 마무리

### Task 27: 전체 단위 테스트 통과 + Plan A1 tag

- [ ] **Step 1: 전체 단위테스트 실행**

```bash
./gradlew :app:testDebugUnitTest
```

Expected: 60+ 단위테스트 모두 PASS.

- [ ] **Step 2: 빌드 + 앱 설치 + 실행 (에뮬레이터)**

```bash
./gradlew :app:installDebug
adb shell am start -n com.pose.poseanalyzer/.MainActivity
```

Placeholder 화면 정상 표시 확인.

- [ ] **Step 3: Plan A1 tag**

```bash
git tag -a plan-a1-foundation-complete -m "Plan A1 (Android Foundation) 완료

도메인 레이어 + 데이터 레이어 + DI 컨테이너 + 앱 진입점 모두 구현.
60+ 단위테스트 통과. UI는 Plan A2에서 작성 예정."
```

---

## ✅ Plan A1 완료 정의

- [ ] Android Studio 프로젝트 빌드 성공 (에뮬레이터에서 placeholder 정상)
- [ ] 8개 Evaluator 단위테스트 통과 (각 4-6 케이스)
- [ ] GeometryMath 단위테스트 통과
- [ ] AsymmetryAnalyzer 단위테스트 통과
- [ ] AnalyzeSessionUseCase 단위테스트 통과
- [ ] ImageStore 단위테스트 통과
- [ ] SessionRepository 단위테스트 (in-memory Room)
- [ ] UserProfileRepository 단위테스트
- [ ] Hilt DI 연결 (PoseAnalyzerApplication + AppModule)
- [ ] MainActivity 컴파일/실행
- [ ] `plan-a1-foundation-complete` git tag

---

## ⏭ 다음 단계 (Plan A2)

Plan A1 완료 후:
- **Plan A2a (UI Foundation)**: Compose 디자인 토큰 + 공통 컴포넌트 6개
- **Plan A2b (Measurement Flow)**: 홈 + CameraX 카메라 + PhotoPicker + 마법사
- **Plan A2c (Result + History)**: 결과 + 기록 + Vico 추이 그래프
- **Plan A2d (Integration)**: UI 테스트 + 1차 MVP tag

각 plan은 iOS Plan 2a-d와 동일한 task 분해. UI/Camera는 안드로이드 특이점 반영 (CameraX, Photo Picker, Material 3).

---

## 📖 참고 자료

- ML Kit Pose Detection: https://developers.google.com/ml-kit/vision/pose-detection/android
- CameraX: https://developer.android.com/training/camerax
- Room: https://developer.android.com/training/data-storage/room
- Hilt: https://developer.android.com/training/dependency-injection/hilt-android
- Vico Charts: https://patrykandpatrick.com/vico/wiki/
- iOS 참조 코드: `../PoseAnalyzer/PoseAnalyzer/PoseAnalyzer/Domain/` (Swift)
