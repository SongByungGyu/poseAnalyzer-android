package com.pose.poseanalyzer.data

import com.pose.poseanalyzer.data.room.PostureEntity
import com.pose.poseanalyzer.data.room.SessionDao
import com.pose.poseanalyzer.data.room.SessionEntity
import com.pose.poseanalyzer.data.room.SessionWithPostures
import com.pose.poseanalyzer.domain.model.AsymmetryResult
import com.pose.poseanalyzer.domain.model.PostureResult
import com.pose.poseanalyzer.domain.model.PostureStatus
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionReport
import com.pose.poseanalyzer.domain.model.SessionView
import com.pose.poseanalyzer.domain.model.Thresholds
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [SessionReport] ↔ Room CRUD + 이미지 파일.
 *
 * iOS `SessionRepository` 1:1 대응.
 */
@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val imageStore: ImageStore
) {

    /**
     * 메모리 [SessionReport]를 Room + 이미지 파일에 저장.
     */
    suspend fun save(report: SessionReport) {
        val frontPath = imageStore.save(report.frontImage, report.id, SessionView.FRONT)
        val sidePath = imageStore.save(report.sideImage, report.id, SessionView.SIDE)

        val sessionEntity = SessionEntity(
            id = report.id.toString(),
            measuredAtMs = report.measuredAt,
            frontImagePath = frontPath,
            sideImagePath = sidePath,
            heightCmAtMeasure = report.heightCmAtMeasure,
            asymmetryShoulderCm = report.asymmetry.shoulder.cm,
            asymmetryShoulderRatio = report.asymmetry.shoulder.ratio,
            asymmetryShoulderAngle = report.asymmetry.shoulder.angleDegrees,
            asymmetryShoulderDirectionRaw = report.asymmetry.shoulder.direction.name,
            asymmetryHipCm = report.asymmetry.hip.cm,
            asymmetryHipRatio = report.asymmetry.hip.ratio,
            asymmetryHipAngle = report.asymmetry.hip.angleDegrees,
            asymmetryHipDirectionRaw = report.asymmetry.hip.direction.name
        )

        val postureEntities = report.postures.map { p ->
            PostureEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionEntity.id,
                typeRaw = p.type.name,
                statusRaw = p.status.name,
                primaryMetric = p.primaryMetric,
                primaryMetricUnitRaw = p.primaryMetricUnit.name,
                confidence = p.confidence,
                advice = p.advice
            )
        }

        sessionDao.insertWithPostures(sessionEntity, postureEntities)
    }

    /** 모든 세션을 시간 역순으로 조회 */
    suspend fun fetchAll(): List<SessionWithPostures> = sessionDao.fetchAll()

    /** 특정 세션 조회 */
    suspend fun fetch(id: UUID): SessionWithPostures? = sessionDao.fetchById(id.toString())

    /**
     * 가장 최근 세션 1건 (excludingId 제외).
     */
    suspend fun fetchLatest(excludingId: UUID? = null): SessionWithPostures? {
        val all = fetchAll()
        return if (excludingId == null) all.firstOrNull()
        else all.firstOrNull { it.session.id != excludingId.toString() }
    }

    /** 세션 삭제 (이미지 파일도 함께) */
    suspend fun delete(id: UUID) {
        imageStore.delete(id)
        sessionDao.deleteById(id.toString())
    }

    /** Entity → 도메인 PostureResult 변환 (UI에서 사용) */
    fun toPostureResult(entity: PostureEntity): PostureResult {
        val type = PostureType.valueOf(entity.typeRaw)
        val status = runCatching { PostureStatus.valueOf(entity.statusRaw) }.getOrDefault(PostureStatus.UNMEASURABLE)
        val unit = runCatching { PostureResult.MetricUnit.valueOf(entity.primaryMetricUnitRaw) }
            .getOrDefault(PostureResult.MetricUnit.DEGREE)
        return PostureResult(
            type = type,
            status = status,
            primaryMetric = entity.primaryMetric,
            primaryMetricUnit = unit,
            thresholds = Thresholds(0.0..0.0, null, Thresholds.Direction.HIGHER_IS_NORMAL),  // 저장된 결과엔 threshold 없음 — placeholder
            usedJointNames = emptyList(),
            confidence = entity.confidence,
            advice = entity.advice
        )
    }

    /** Entity → 도메인 AsymmetryResult 변환 */
    fun toAsymmetryResult(session: SessionEntity): AsymmetryResult {
        fun dir(raw: String) = runCatching { AsymmetryResult.Direction.valueOf(raw) }
            .getOrDefault(AsymmetryResult.Direction.BALANCED)
        return AsymmetryResult(
            shoulder = AsymmetryResult.Difference(
                cm = session.asymmetryShoulderCm,
                ratio = session.asymmetryShoulderRatio,
                angleDegrees = session.asymmetryShoulderAngle,
                direction = dir(session.asymmetryShoulderDirectionRaw)
            ),
            hip = AsymmetryResult.Difference(
                cm = session.asymmetryHipCm,
                ratio = session.asymmetryHipRatio,
                angleDegrees = session.asymmetryHipAngle,
                direction = dir(session.asymmetryHipDirectionRaw)
            )
        )
    }
}
