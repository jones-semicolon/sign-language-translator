package com.android.signlanguagetranslator

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.signlanguagetranslator.data.DataStoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var _frontFacing: Boolean = GestureRecognizerHelper.ISFRONTFACING

    val dataStore = DataStoreRepository(application)


    val currentCoordinate = dataStore.getHandCoordinate()
        .asLiveData(Dispatchers.IO)

    fun setCoordinate(coordinate: Int){
        viewModelScope.launch {
            Log.d("ViewModel", "setHandCoordinate called with value: $coordinate")
            dataStore.setHandCoordinate(coordinate)
        }
    }

    val currentDelegate = dataStore.getDelegate()
        .asLiveData(Dispatchers.IO)

    fun setDelegate(delegate: Int){
        viewModelScope.launch {
            dataStore.setDelegate(delegate)
        }
    }

    val currentDetectionThreshold = dataStore.getdetectionThreshold()
        .asLiveData(Dispatchers.IO)

    fun setDetectionThreshold(threshold: Float){
        viewModelScope.launch {
            dataStore.setDetectionThreshold(threshold)
        }
    }

    val currentTrackingThreshold = dataStore.getTrackingThreshold()
        .asLiveData(Dispatchers.IO)

    fun setTrackingThreshold(threshold: Float){
        viewModelScope.launch {
            dataStore.setTrackingThreshold(threshold)
        }
    }

    val currentPresenceThreshold = dataStore.getPresenceThreshold()
        .asLiveData(Dispatchers.IO)

    fun setPresenceThreshold(threshold: Float){
        viewModelScope.launch {
            dataStore.setPresenceThreshold(threshold)
        }
    }

    val currentConfidenceThreshold = dataStore.getConfidenceThreshold()
        .asLiveData(Dispatchers.IO)

    fun setConfidenceThreshold(threshold: Float){
        viewModelScope.launch {
            dataStore.setConfidenceThreshold(threshold)
        }
    }

    val currentHandStableDuration = dataStore.getHandStableDuration()
        .asLiveData(Dispatchers.IO)

    fun setHandStableDuration(threshold: Float){
        viewModelScope.launch {
            dataStore.setHandStableDuration(threshold)
        }
    }

    val currentLabelDuration = dataStore.getLabelDuration()
        .asLiveData(Dispatchers.IO)

    fun setLabelDuration(threshold: Float){
        viewModelScope.launch {
            dataStore.setLabelDuration(threshold)
        }
    }

    val currentStartup = dataStore.getStartup()
        .asLiveData(Dispatchers.IO)

    fun setStartup(startup: Boolean){
        viewModelScope.launch {
            dataStore.setStartup(startup)
        }
    }

    val currentIsFrontFacing: Boolean get() = _frontFacing

    fun setIsFacingFront(frontFacing: Boolean){
        Log.d("PAUSED", "SETTING FRONT FACING")
        _frontFacing = frontFacing
    }

    /*val currentMinHandDetectionConfidence: Float
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
    fun setMinLabelDuration(labelDuration: Float){
        _minLabelDuration = labelDuration
    }
    fun setMinHandStableDuration(handStableDuration: Float){
        _minHandStableDuration = handStableDuration
    }*/
}