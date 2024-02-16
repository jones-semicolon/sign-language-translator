package com.android.signlanguagetranslator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.android.signlanguagetranslator.GestureRecognizerHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


class DataStoreRepository(context: Context){

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val dataStore = context.dataStore

    companion object {
        val handCoordinateKey = intPreferencesKey(name = "HAND_COORDINATE")
        val delegateKey = intPreferencesKey(name = "DELEGATE")
        val detectionThresholdKey = floatPreferencesKey(name = "DETECTION_THRESHOLD")
        val trackingThresholdKey = floatPreferencesKey(name = "TRACKING_THRESHOLD")
        val presenceThresholdKey = floatPreferencesKey(name = "PRESENCE_THRESHOLD")
        val confidenceThresholdKey = floatPreferencesKey(name = "CONFIDENCE_THRESHOLD")
        val handStableDurationKey = floatPreferencesKey(name = "HAND_STABLE_DURATION")
        val labelDurationkey = floatPreferencesKey(name = "LABEL_DURATION")
        val startupKey = booleanPreferencesKey(name = "STARTUP")
    }

    suspend fun setHandCoordinate(handCoordinate: Int){
        dataStore.edit { preferences ->
            preferences[handCoordinateKey] = handCoordinate
        }
    }

    fun getHandCoordinate(): Flow<Int> {
        return dataStore.data
            .catch {exception ->
                emit(emptyPreferences())
            }
            .map { pref ->
                val handCoordinateMode = pref[handCoordinateKey] ?: GestureRecognizerHelper.HAND_COORDINATE_BOUNDING_BOX
                handCoordinateMode
            }
    }

    suspend fun setDelegate(delegate: Int){
        dataStore.edit {preferences ->
            preferences[delegateKey] = delegate
        }
    }

    fun getDelegate(): Flow<Int> {
        return dataStore.data
            .catch { exception ->
                emit(emptyPreferences())
            }
            .map {pref ->
                val delegateMode = pref[delegateKey] ?: GestureRecognizerHelper.DELEGATE_CPU
                delegateMode
            }
    }

    suspend fun setDetectionThreshold(detectionThreshold: Float){
        dataStore.edit {preferences ->
            preferences[detectionThresholdKey] = detectionThreshold
        }
    }

    fun getdetectionThreshold(): Flow<Float> {
        return dataStore.data
            .catch { exception ->
                emit(emptyPreferences())
            }
            .map {pref ->
                val detectionThreshold = pref[detectionThresholdKey] ?: GestureRecognizerHelper.DEFAULT_HAND_DETECTION_CONFIDENCE
                detectionThreshold
            }

    }

    suspend fun setTrackingThreshold(trackingThreshold: Float){
        dataStore.edit {preferences ->
            preferences[trackingThresholdKey] = trackingThreshold
        }
    }

    fun getTrackingThreshold(): Flow<Float> {
        return dataStore.data
            .catch { exception ->
                emit(emptyPreferences())
            }
            .map {pref ->
                pref[trackingThresholdKey] ?: GestureRecognizerHelper.DEFAULT_HAND_TRACKING_CONFIDENCE
            }
    }

    suspend fun setPresenceThreshold(presenceThreshold: Float){
        dataStore.edit {preferences ->
            preferences[presenceThresholdKey] = presenceThreshold
        }
    }

    fun getPresenceThreshold(): Flow<Float> {
        return dataStore.data
            .catch { exception ->
                emit(emptyPreferences())
            }
            .map {pref ->
                pref[presenceThresholdKey] ?: GestureRecognizerHelper.DEFAULT_HAND_PRESENCE_CONFIDENCE
            }
    }

    suspend fun setHandStableDuration(handStableDuration: Float){
        dataStore.edit {preferences ->
            preferences[handStableDurationKey] = handStableDuration
        }
    }

    fun getHandStableDuration(): Flow<Float> {
        return dataStore.data
            .catch { exception ->
                emit(emptyPreferences())
            }
            .map {pref ->
                pref[handStableDurationKey] ?: GestureRecognizerHelper.HAND_STABLE_DURATION
            }
    }

    suspend fun setLabelDuration(labelDuration: Float){
        dataStore.edit {preferences ->
            preferences[labelDurationkey] = labelDuration
        }
    }

    fun getLabelDuration(): Flow<Float> {
        return dataStore.data
            .catch { exception ->
                /*if(exception is IOException) emit(emptyPreferences())
                else throw exception*/
                emit(emptyPreferences())
            }
            .map {pref ->
                pref[labelDurationkey] ?: GestureRecognizerHelper.LABEL_DURATION
            }
    }

    suspend fun setConfidenceThreshold(confidenceThreshold: Float){
        dataStore.edit {preferences ->
            preferences[confidenceThresholdKey] = confidenceThreshold
        }
    }

    fun getConfidenceThreshold(): Flow<Float> {
        return dataStore.data
            .catch { exception ->
                emit(emptyPreferences())
            }
            .map {pref ->
                pref[confidenceThresholdKey] ?: GestureRecognizerHelper.DEFAULT_CONFIDENCE
            }
    }

    suspend fun setStartup(startup: Boolean){
        dataStore.edit {preferences ->
            preferences[startupKey] = startup
        }
    }

    fun getStartup(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                emit(emptyPreferences())
            }
            .map {pref ->
                pref[startupKey] ?: false
            }
    }
}