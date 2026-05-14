package com.pose.poseanalyzer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt 진입점.
 *
 * 모든 [javax.inject.Inject] 객체 그래프의 루트.
 * AndroidManifest의 `android:name=".PoseAnalyzerApplication"`로 등록.
 */
@HiltAndroidApp
class PoseAnalyzerApplication : Application()
