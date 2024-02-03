package com.android.signlanguagetranslator.fragment

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.signlanguagetranslator.GestureRecognizerHelper
import com.android.signlanguagetranslator.MainViewModel
import com.android.signlanguagetranslator.databinding.ItemGestureRecognizerResultBinding
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.vision.core.RunningMode
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
    private var confidence: Float? = null
    private var acceptDur: Long =  1000
    private var currentLabelListener: CurrentLabelListener? = null
    private var newLabel: String? = null


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
        // Update the current label whenever the results are updated
        currentLabelListener?.onCurrentLabelChanged(getCurrentLabel())
    }



    fun updateAdapterSize(size: Int) {
        adapterSize = size
    }

    fun updateConfidence(_confidence: Float) {
        confidence = _confidence
    }

    fun getCurrentLabel(): String {
        Log.d("RESULT",resultList.joinToString(""))
        return resultList.joinToString("")
    }

    fun setCurrentLabelListener(listener: CurrentLabelListener) {
        currentLabelListener = listener
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

        private var clearTimer : CountDownTimer? = null
        private var prevLabel: String? = null
        private var addTimer: CountDownTimer? = null
        fun bind(label: String?, score: Float?) {
            with(binding) {
                if (label.isNullOrEmpty() && resultList.isNullOrEmpty()){
                    linearLayout.visibility = View.INVISIBLE
                    return
                }
                val newLabel = label?.replace("space", " ")
                linearLayout.visibility = View.VISIBLE
                resultLabel.text = resultList.joinToString("")
                tvLabel.text = newLabel
            }
            // Check if the label is null or empty

            if (label.isNullOrEmpty()) {
                if(label is String) {
                    clearTimer?.cancel()
                    return
                }
                // Cancel the add timer
                log(label?:2::class.simpleName.toString(), "CONTENT CHECKING")
                addTimer?.cancel()

                // Start a new timer for 5 seconds to clear the result list
                clearTimer = object : CountDownTimer(5000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        // Do nothing
                    }

                    override fun onFinish() {
                        // Clear the result list
                        resultList.clear()
                        log("clearing")

                        // Reset the clear timer
                        clearTimer = null
                    }
                }.start()
            } else {
                newLabel = label.replace("space", " ")

                // Cancel the clear timer
                clearTimer?.cancel()
                if(score!! < confidence!!){
                    return
                }

                // Check if the previous label is null or empty
                if (prevLabel.isNullOrEmpty()) {
                    log(newLabel!!)
                    // Assign the label to the previous label
                    prevLabel = newLabel

                    // Start a new timer for 5 seconds to add the label
                    addTimer = object : CountDownTimer(acceptDur, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            // Do nothing
                        }

                        override fun onFinish() {
                            if(newLabel == "del"){
                                resultList.removeLastOrNull()
                                currentLabelListener?.onCurrentLabelChanged(getCurrentLabel())
                                prevLabel = null
                                addTimer = null
                                return
                            }
                            // Add the previous label to the result list
                            resultList.add(prevLabel!!)

                            // Reset the previous label and the add timer
                            prevLabel = null
                            addTimer = null
                        }
                    }.start()
                } else {
                    // Check if the label is equal to the previous label
                    if (newLabel == prevLabel) {
                        // Do nothing
                    } else {
                        // Cancel the add timer
                        addTimer?.cancel()


                        // Assign the label to the previous label
                        prevLabel = newLabel

                        // Start a new timer for 5 seconds to add the label
                        addTimer = object : CountDownTimer(acceptDur, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                // Do nothing
                            }

                            override fun onFinish() {
                                if(newLabel == "del"){
                                    resultList.removeLastOrNull()
                                    prevLabel = null
                                    addTimer = null
                                    return
                                }
                                // Add the previous label to the result list
                                resultList.add(prevLabel!!)

                                // Reset the previous label and the add timer
                                prevLabel = null
                                addTimer = null
                            }
                        }.start()
                    }
                }
            }
        }



        private fun log(msg: String, status: String = "CHECKING"){
            Log.d(status, msg)
        }
    }
    interface CurrentLabelListener {
        fun onCurrentLabelChanged(currentLabel: String?)
    }
}

