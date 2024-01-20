/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.signlanguagetranslator.fragment

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.signlanguagetranslator.GestureRecognizerHelper
import com.android.signlanguagetranslator.MainViewModel
import com.android.signlanguagetranslator.databinding.ItemGestureRecognizerResultBinding
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ScheduledExecutorService
import kotlin.math.min

class GestureRecognizerResultsAdapter(private val viewModel: MainViewModel) :
    RecyclerView.Adapter<GestureRecognizerResultsAdapter.ViewHolder>() {
    companion object {
        private const val NO_VALUE = "--"
    }

    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper
    private var adapterCategories: MutableList<Category?> = mutableListOf()
    private var adapterSize: Int = 0
    private var resultList: MutableList<String> = mutableListOf()
    private var isTaskRunning: Boolean = false
    private var executorService: ScheduledExecutorService? = null
    private val handler = Handler(Looper.getMainLooper())
    private var lastAddedLabel: String? = null
    private var confidence: Float? = null



    /*private val runnableTask = Runnable {
        isTaskRunning = false
        resultList.clear()
        Log.d("TASK", "TASK RUNNING")
    }*/
    private val delayedAddLabelTask = Runnable {
        isTaskRunning = false
        notifyDataSetChanged()
        Log.d("TIMER", "Label added at: ${System.currentTimeMillis()}")
    }

    private val delayedClearTask = Runnable {
        clearResultList()
        notifyDataSetChanged()
        Log.d("TIMER", "resultList cleared at: ${System.currentTimeMillis()}")
    }

    private fun clearResultList() {
        resultList.clear()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateResults(categories: List<Category>?) {
        adapterCategories = MutableList(adapterSize) { null }
        if (categories != null) {
            val sortedCategories = categories.sortedByDescending { it.score() }
            val min = min(sortedCategories.size, adapterCategories.size)
            for (i in 0 until min) {
                adapterCategories[i] = sortedCategories[i]
            }
            adapterCategories.sortedBy { it?.index() }
            notifyDataSetChanged()
        }
    }



    fun updateAdapterSize(size: Int) {
        adapterSize = size
    }

    fun updateConfidence(_confidence: Float) {
        confidence = _confidence
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        if (!this::gestureRecognizerHelper.isInitialized) {
            gestureRecognizerHelper = GestureRecognizerHelper(
                context = parent.context,
                runningMode = RunningMode.LIVE_STREAM,
                minHandDetectionConfidence = viewModel.currentMinHandDetectionConfidence,
                minHandTrackingConfidence = viewModel.currentMinHandTrackingConfidence,
                minHandPresenceConfidence = viewModel.currentMinHandPresenceConfidence,
                minConfidence = viewModel.currentMinConfidence,
                currentDelegate = viewModel.currentDelegate,
                isFrontFacing = viewModel.currentIsFrontFacing,
            )
        }

        val binding = ItemGestureRecognizerResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        Log.d("TAG", "CREATED")
        confidence = gestureRecognizerHelper.minConfidence
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        adapterCategories[position].let { category ->
            holder.bind(category?.categoryName(), category?.score())
        }
    }

    override fun getItemCount(): Int = adapterCategories.size


    inner class ViewHolder(private val binding: ItemGestureRecognizerResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var scheduledLabel: String? = null
        private var isHandStable = false
        private var detectionTimer: Timer? = null
        private var tempLabel: String? = null
        private var timerEnd: Boolean = false
        private var timerCancelled: Boolean = false


        fun bind(label: String?, score: Float?) {
            with(binding) {
                tvLabel.text = resultList.joinToString("")
                Log.d("DETECTION", "$label $score $confidence")

                if (label.isNullOrBlank()) {
                    tempLabel = null
                    return
                }
                if (label != tempLabel) {
                    if (detectionTimer == null) detectionTimer = Timer()
                    Log.d("CHECKING", "FIRST IF")
                    if (timerCancelled) {
                        Log.d("CHECKING", "SCHEDULING TIMER")
                        detectionTimer?.schedule(object : TimerTask() {
                            override fun run() {
                                Log.d("CHECKING", "TIMER END")
                                timerEnd = true
                            }

                            override fun cancel(): Boolean {
                                timerCancelled = true
                                detectionTimer = null
                                tempLabel = null
                                return super.cancel()
                            }
                        }, 5000)
                    }
                    tempLabel = label
                    timerCancelled = false
                } else if (label.equals(tempLabel)) {
                    Log.d("CHECKING", "SECOND IF")
                    if (!timerEnd) {
                        Log.d("CHECKING", "SECOND BUT TIMER IF")
                        return
                    }
                    resultList.add(label)
                    timerEnd = false
//                    tempLabel = null
                    if (timerCancelled) return
                    Log.d("CHECKING", "SECOND IF BUT ELSE")
//                        timerCancelled = true
                    detectionTimer?.cancel()
//                        detectionTimer = null
                } else {
                    if (timerCancelled) return
                    Log.d("CHECKING", "ELSE")
//                    tempLabel = null
                    detectionTimer?.cancel()
//                    detectionTimer = null
//                    timerCancelled = true
                }
            }
        }

        private fun startHandStabilityTimer() {
            isHandStable = false
            detectionTimer?.cancel() // Cancel any existing timers
            detectionTimer = Timer()
            detectionTimer?.schedule(object : TimerTask() {
                override fun run() {
                    // The timer has elapsed, consider the hand stable
                    isHandStable = true
                    Log.d("TIMER", "Hand stable")

                    // Schedule the detection after the hand is stable
                    scheduleDetectionTimer()
                }
            }, 1000) // Adjust the duration as needed
        }

        private fun scheduleDetectionTimer() {
            detectionTimer?.cancel() // Cancel any existing timers
            detectionTimer = Timer()
            detectionTimer?.schedule(object : TimerTask() {
                override fun run() {
                    // Process the detected label only if the hand is stable and the label matches the latest detected label
                    if (isHandStable && scheduledLabel == lastAddedLabel) {
                        processDetectedLabel(scheduledLabel ?: "")
                        Log.d("TIMER", "Label detection task executed after delay")
                    }
                }
            }, 1000) // Adjust the duration as needed
        }
        private fun processDetectedLabel(label: String) {
            // Perform the actual processing of the detected label here
            // For example, you can update your UI or perform other actions.
            // In this example, we are adding the label to the resultList.
            addLabel(label)
        }

        private val delayedClearTask = Runnable {
            clearResultList()
            handler.post { notifyDataSetChanged() }
            Log.d("TIMER", "resultList cleared after delay")
        }

        private fun addLabel(label: String) {
            resultList.add(label)
            handler.post { notifyDataSetChanged() }
        }

        private fun clearResultList() {
            resultList.clear()
        }
    }
}

