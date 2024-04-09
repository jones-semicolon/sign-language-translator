package com.android.signlanguagetranslator.fragment

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.android.signlanguagetranslator.GestureRecognizerHelper
import com.android.signlanguagetranslator.MainViewModel
import com.android.signlanguagetranslator.databinding.ItemGestureRecognizerResultBinding
import com.google.mediapipe.tasks.components.containers.Category
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToLong

class GestureRecognizerResultsAdapter(private val viewModel: MainViewModel) :
    RecyclerView.Adapter<GestureRecognizerResultsAdapter.ViewHolder>() {
    companion object {
        private const val NO_VALUE = "--"
    }

    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper
    private lateinit var textToSpeech: TextToSpeech
    private var adapterCategories: MutableList<Category?> = mutableListOf()
    private var adapterSize: Int = 0
    private var resultList: MutableList<String> = mutableListOf()
    private var confidence: Float? = null
    private var handStableDuration: Float? = null
    private var labelDuration: Float? = null
    private var currentLabelListener: CurrentLabelListener? = null
    private var newLabel: String? = null
    private var clearTimer : CountDownTimer? = null
    private var prevLabel: String? = null
    private var addTimer: CountDownTimer? = null
    private var prevRes: String? = null

    init {
        viewModel.currentConfidenceThreshold.observeForever { confidence ->
            confidence?.let {
                updateConfidence(it)
            }
        }

        viewModel.currentLabelDuration.observeForever { duration ->
            duration?.let {
                updateLabelDuration(it)
            }
        }

        viewModel.currentHandStableDuration.observeForever { duration ->
            duration?.let {
                updateHandStableDuration(it)
            }
        }
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
            adapterCategories = adapterCategories.sortedBy { it?.index() }.toMutableList()
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

    fun updateLabelDuration(_labelDuration: Float) {
        labelDuration = _labelDuration
    }

    fun updateHandStableDuration(_handStableDuration: Float) {
        handStableDuration = _handStableDuration
    }

    fun getCurrentLabel(): String {
        Log.d("RESULT",resultList.joinToString(""))
        return resultList.joinToString("")
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemGestureRecognizerResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        textToSpeech = TextToSpeech(parent.context){status ->
            if(status == TextToSpeech.SUCCESS){
                val result = textToSpeech.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED){
                    Log.d("TTS", "TTS Error")
                }
            }
        }
        Log.d("TAG", "CREATED")
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
        val lifecycleOwner = itemView.findViewTreeLifecycleOwner()

        fun bind(label: String?, score: Float?) {
            with(binding) {
                if (label.isNullOrEmpty() || label == "none") {
                    addTimer?.cancel()
                    log(addTimer.toString(), "CONTENT")
                    if(resultList.isNullOrEmpty()) {
                        linearLayout.visibility = View.INVISIBLE
                        return
                    }
                }
                val newLabel = label?.replace(Regex("space|_"), " ")
                linearLayout.visibility = View.VISIBLE
                resultLabel.text = resultList.joinToString("")
                tvLabel.text = newLabel
            }
            // Check if the label is null or empty
            log("TEST", addTimer.toString())
            if (label.isNullOrEmpty()) {
                log("$labelDuration and $handStableDuration")
                if(label is String) {

                    clearTimer?.cancel()
                    return
                }
                if((prevRes == null || prevRes != resultList.joinToString("")) && resultList.isNotEmpty()){
                    textToSpeech.speak(resultList.joinToString(""), TextToSpeech.QUEUE_FLUSH, null, null)
                    prevRes = resultList.joinToString("")
                }
                // Cancel the add timer
                log(addTimer.toString(), "CONTENT")


                // Start a new timer for 5 seconds to clear the result list
                clearTimer = object : CountDownTimer((labelDuration?.times(1000L)!!.roundToLong()), 1000) {
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
                newLabel = label.replace(Regex("space|_"), " ")

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
                    addTimer = object : CountDownTimer((handStableDuration?.times(1000L))!!.roundToLong(), 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            // Do nothing
                        }

                        override fun onFinish() {
                            if(newLabel == "del"){
                                resultList.removeLastOrNull()
                                currentLabelListener?.onCurrentLabelChanged(getCurrentLabel())
                                prevLabel = null
                                prevRes = null
                                addTimer = null
                                return
                            }
                            // Add the previous label to the result list
                            resultList.add(prevLabel!!)

                            // Reset the previous label and the add timer
                            prevLabel = null
                            addTimer = null
                            prevRes = null
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
                        addTimer = object : CountDownTimer((handStableDuration?.times(1000L))!!.roundToLong(), 1000) {
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
