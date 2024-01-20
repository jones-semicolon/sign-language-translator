package com.android.signlanguagetranslator

import android.util.Log
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private var _delegate: Int = GestureRecognizerHelper.DELEGATE_CPU
    private var _frontFacing: Boolean = GestureRecognizerHelper.ISFRONTFACING
    private var _minHandDetectionConfidence: Float =
        GestureRecognizerHelper.DEFAULT_HAND_DETECTION_CONFIDENCE
    private var _minHandTrackingConfidence: Float = GestureRecognizerHelper
        .DEFAULT_HAND_TRACKING_CONFIDENCE
    private var _minHandPresenceConfidence: Float = GestureRecognizerHelper
        .DEFAULT_HAND_PRESENCE_CONFIDENCE
    private var _minConfidence: Float = GestureRecognizerHelper
        .DEFAULT_CONFIDENCE
    val currentDelegate: Int get() = _delegate
    val currentMinHandDetectionConfidence: Float
        get() =
            _minHandDetectionConfidence
    val currentMinHandTrackingConfidence: Float
        get() =
            _minHandTrackingConfidence
    val currentMinHandPresenceConfidence: Float
        get() =
            _minHandPresenceConfidence
    val currentMinConfidence: Float
        get() =
            _minConfidence
    val currentIsFrontFacing: Boolean get() = _frontFacing

    fun setDelegate(delegate: Int) {
        _delegate = delegate
    }

    fun setIsFacingFront(frontFacing: Boolean){
        Log.d("PAUSED", "SETTING FRONT FACING")
        _frontFacing = frontFacing
    }

    fun setMinHandDetectionConfidence(confidence: Float) {
        Log.d("PAUSED", "SETTING FRONT FACING")
        _minHandDetectionConfidence = confidence
    }

    fun setMinHandTrackingConfidence(confidence: Float) {
        _minHandTrackingConfidence = confidence
    }

    fun setMinHandPresenceConfidence(confidence: Float) {
        _minHandPresenceConfidence = confidence
    }
    fun setMinConfidence(confidence: Float) {
        _minConfidence = confidence
    }
}