package com.pose.poseanalyzer.domain.evaluation

import com.pose.poseanalyzer.domain.model.PoseFrame
import com.pose.poseanalyzer.domain.model.PostureResult
import com.pose.poseanalyzer.domain.model.PostureType
import com.pose.poseanalyzer.domain.model.SessionView

/**
 * 단일 자세를 평가하는 책임.
 *
 * iOS `PostureEvaluator` 프로토콜과 1:1 대응.
 */
interface PostureEvaluator {
    val type: PostureType
    val requiredView: SessionView
    fun evaluate(frame: PoseFrame): PostureResult
}
