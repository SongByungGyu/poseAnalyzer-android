package com.pose.poseanalyzer.di

import android.content.Context
import androidx.room.Room
import com.pose.poseanalyzer.data.asymmetry.DefaultAsymmetryAnalyzer
import com.pose.poseanalyzer.data.detection.MLKitPoseDetector
import com.pose.poseanalyzer.data.room.AppDatabase
import com.pose.poseanalyzer.data.room.SessionDao
import com.pose.poseanalyzer.data.room.UserProfileDao
import com.pose.poseanalyzer.domain.asymmetry.AsymmetryAnalyzer
import com.pose.poseanalyzer.domain.detection.PoseDetector
import com.pose.poseanalyzer.domain.evaluation.AnteriorPelvicTiltEvaluator
import com.pose.poseanalyzer.domain.evaluation.ForwardHeadEvaluator
import com.pose.poseanalyzer.domain.evaluation.HeadTiltEvaluator
import com.pose.poseanalyzer.domain.evaluation.KneeAlignmentEvaluator
import com.pose.poseanalyzer.domain.evaluation.KneeHyperextensionEvaluator
import com.pose.poseanalyzer.domain.evaluation.KyphosisEvaluator
import com.pose.poseanalyzer.domain.evaluation.PostureEvaluator
import com.pose.poseanalyzer.domain.evaluation.RoundShoulderEvaluator
import com.pose.poseanalyzer.domain.evaluation.ScoliosisEvaluator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 앱 전역 DI 컨테이너.
 *
 * iOS `AppDependencies` 1:1 대응 — interface ↔ 구현체 바인딩.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePoseDetector(impl: MLKitPoseDetector): PoseDetector = impl

    @Provides
    @Singleton
    fun provideAsymmetryAnalyzer(impl: DefaultAsymmetryAnalyzer): AsymmetryAnalyzer = impl

    @Provides
    @Singleton
    fun provideEvaluators(): List<PostureEvaluator> = listOf(
        ForwardHeadEvaluator(),
        RoundShoulderEvaluator(),
        KyphosisEvaluator(),
        AnteriorPelvicTiltEvaluator(),
        KneeHyperextensionEvaluator(),
        ScoliosisEvaluator(),
        HeadTiltEvaluator(),
        KneeAlignmentEvaluator()
    )

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()
}
