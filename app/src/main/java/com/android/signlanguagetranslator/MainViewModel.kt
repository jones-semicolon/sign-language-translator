package com.android.signlanguagetranslator

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MainViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private var _delegate: Int = GestureRecognizerHelper.DELEGATE_CPU
    private var _handCoordinate: Int = GestureRecognizerHelper.HAND_COORDINATE_BOUNDING_BOX
    private var _frontFacing: Boolean = GestureRecognizerHelper.ISFRONTFACING
    private var _minHandDetectionConfidence: Float =
        GestureRecognizerHelper.DEFAULT_HAND_DETECTION_CONFIDENCE
    private var _minHandTrackingConfidence: Float = GestureRecognizerHelper
        .DEFAULT_HAND_TRACKING_CONFIDENCE
    private var _minHandPresenceConfidence: Float = GestureRecognizerHelper
        .DEFAULT_HAND_PRESENCE_CONFIDENCE
    private var _minConfidence: Float = GestureRecognizerHelper
        .DEFAULT_CONFIDENCE
    private var _minLabelDuration: Float = GestureRecognizerHelper.LABEL_DURATION
    private var _minHandStableDuration: Float = GestureRecognizerHelper.HAND_STABLE_DURATION
    val currentDelegate: Int get() = _delegate
    val currentHandCoordinate: Int get() = _handCoordinate
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
    val currentLabelDuration: Float
        get() =
            _minLabelDuration
    val currentHandStableDuration: Float
        get() =
            _minHandStableDuration

    fun setDelegate(delegate: Int) {
        _delegate = delegate
    }
    fun setHandCoordinate(handCoordinate: Int) {
        _handCoordinate = handCoordinate
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
    fun setMinLabelDuration(labelDuration: Float) {
        _minLabelDuration = labelDuration
    }
    fun setMinHandStableDuration(handStableDuration: Float) {
        _minHandStableDuration = handStableDuration
    }
}