package com.android.signlanguagetranslator.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.android.signlanguagetranslator.GestureRecognizerHelper
import com.android.signlanguagetranslator.MainViewModel
import com.android.signlanguagetranslator.databinding.FragmentSettingsBinding
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {
    private var _fragmentSettingsBinding: FragmentSettingsBinding? = null

    companion object {
        private const val TAG = "Hand gesture recognizer"
    }


    private val fragmentSettingsBinding
        get() = _fragmentSettingsBinding!!
    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper
    private val viewModel: MainViewModel by activityViewModels()
    private var defaultNumResults = 1
    private val gestureRecognizerResultAdapter: GestureRecognizerResultsAdapter by lazy {
        GestureRecognizerResultsAdapter(viewModel).apply {
            updateAdapterSize(defaultNumResults)
        }
    }

    private lateinit var backgroundExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.

        // Start the GestureRecognizerHelper again when users come back
        // to the foreground.
        backgroundExecutor.execute {
            if (gestureRecognizerHelper.isClosed()) {
                gestureRecognizerHelper.setupGestureRecognizer()
            }
        }

    }

    override fun onPause() {
        super.onPause()
        if (this::gestureRecognizerHelper.isInitialized) {
            viewModel.setMinHandDetectionConfidence(gestureRecognizerHelper.minHandDetectionConfidence)
            viewModel.setMinHandTrackingConfidence(gestureRecognizerHelper.minHandTrackingConfidence)
            viewModel.setMinHandPresenceConfidence(gestureRecognizerHelper.minHandPresenceConfidence)
            viewModel.setMinConfidence(gestureRecognizerHelper.minConfidence)
            viewModel.setDelegate(gestureRecognizerHelper.currentDelegate)
            viewModel.setHandCoordinate(gestureRecognizerHelper.currentHandCoordinate)
            viewModel.setIsFacingFront(gestureRecognizerHelper.isFrontFacing)
            viewModel.setMinHandStableDuration(gestureRecognizerHelper.minHandStableDuration)
            viewModel.setMinLabelDuration(gestureRecognizerHelper.minLabelDuration)
        }
    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                getActivity()?.getFragmentManager()?.popBackStack();
            }
        })
    }*/


    override fun onDestroyView() {
        _fragmentSettingsBinding = null
        super.onDestroyView()

        // Shut down our background executor
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentSettingsBinding =
            FragmentSettingsBinding.inflate(inflater, container, false)

        return fragmentSettingsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backgroundExecutor = Executors.newSingleThreadExecutor()
        backgroundExecutor.execute {
            gestureRecognizerHelper = GestureRecognizerHelper(
                context = requireContext(),
                minHandDetectionConfidence = viewModel.currentMinHandDetectionConfidence,
                minHandTrackingConfidence = viewModel.currentMinHandTrackingConfidence,
                minHandPresenceConfidence = viewModel.currentMinHandPresenceConfidence,
                minConfidence = viewModel.currentMinConfidence,
                isFrontFacing = viewModel.currentIsFrontFacing,
                currentDelegate = viewModel.currentDelegate,
                currentHandCoordinate = viewModel.currentHandCoordinate,
                minHandStableDuration =  viewModel.currentHandStableDuration,
                minLabelDuration = viewModel.currentLabelDuration
            )
        }

        fragmentSettingsBinding.button7.setOnClickListener {
//            findNavController().navigate(R.id.camera_fragment)
            findNavController().popBackStack()
        }

        // Attach listeners to UI control widgets
        initBottomSheetControls()
    }

    private fun initBottomSheetControls() {
        // init bottom sheet settings
        fragmentSettingsBinding.detectionThresholdValue.text =
            String.format(
                Locale.US, "%.2f", viewModel.currentMinHandDetectionConfidence
            )
        fragmentSettingsBinding.trackingThresholdValue.text =
            String.format(
                Locale.US, "%.2f", viewModel.currentMinHandTrackingConfidence
            )
        fragmentSettingsBinding.presenceThresholdValue.text =
            String.format(
                Locale.US, "%.2f", viewModel.currentMinHandPresenceConfidence
            )
        fragmentSettingsBinding.confidenceThresholdValue.text =
            String.format(
                Locale.US, "%.2f", viewModel.currentMinConfidence
            )
        fragmentSettingsBinding.labelDurationValue.text =
            String.format(
                Locale.US, "%.2f", viewModel.currentLabelDuration
            )
        fragmentSettingsBinding.handStableDurationValue.text =
            String.format(
                Locale.US, "%.2f", viewModel.currentHandStableDuration
            )

        // When clicked, lower hand detection score threshold floor
        fragmentSettingsBinding.detectionThresholdMinus.setOnClickListener {
            if (gestureRecognizerHelper.minHandDetectionConfidence >= 0.2) {
                gestureRecognizerHelper.minHandDetectionConfidence -= 0.1f
                updateControlsUi()
            }
        }

        // When clicked, raise hand detection score threshold floor
        fragmentSettingsBinding.detectionThresholdPlus.setOnClickListener {
            if (gestureRecognizerHelper.minHandDetectionConfidence <= 0.8) {
                gestureRecognizerHelper.minHandDetectionConfidence += 0.1f
                updateControlsUi()
            }
        }

        // When clicked, lower hand tracking score threshold floor
        fragmentSettingsBinding.trackingThresholdMinus.setOnClickListener {
            if (gestureRecognizerHelper.minHandTrackingConfidence >= 0.2) {
                gestureRecognizerHelper.minHandTrackingConfidence -= 0.1f
                updateControlsUi()
            }
        }

        // When clicked, raise hand tracking score threshold floor
        fragmentSettingsBinding.trackingThresholdPlus.setOnClickListener {
            if (gestureRecognizerHelper.minHandTrackingConfidence <= 0.8) {
                gestureRecognizerHelper.minHandTrackingConfidence += 0.1f
                updateControlsUi()
            }
        }

        // When clicked, lower hand presence score threshold floor
        fragmentSettingsBinding.presenceThresholdMinus.setOnClickListener {
            if (gestureRecognizerHelper.minHandPresenceConfidence >= 0.2) {
                gestureRecognizerHelper.minHandPresenceConfidence -= 0.1f
                updateControlsUi()
            }
        }

        // When clicked, raise hand presence score threshold floor
        fragmentSettingsBinding.presenceThresholdPlus.setOnClickListener {
            if (gestureRecognizerHelper.minHandPresenceConfidence <= 0.8) {
                gestureRecognizerHelper.minHandPresenceConfidence += 0.1f
                updateControlsUi()
            }
        }

        // When clicked, lower hand detection score threshold floor
        fragmentSettingsBinding.confidenceThresholdMinus.setOnClickListener {
            if (gestureRecognizerHelper.minConfidence >= 0.2) {
                gestureRecognizerHelper.minConfidence -= 0.05f
                updateControlsUi()
            }
        }

        // When clicked, raise hand detection score threshold floor
        fragmentSettingsBinding.confidenceThresholdPlus.setOnClickListener {
            if (gestureRecognizerHelper.minConfidence <= 0.9) {
                gestureRecognizerHelper.minConfidence += 0.05f
                updateControlsUi()
            }
        }

        fragmentSettingsBinding.labelDurationMinus.setOnClickListener {
            if (gestureRecognizerHelper.minLabelDuration >= 3) {
                gestureRecognizerHelper.minLabelDuration -= 0.5f
                updateControlsUi()
            }
        }

        // When clicked, raise hand detection score threshold floor
        fragmentSettingsBinding.labelDurationPlus.setOnClickListener {
            if (gestureRecognizerHelper.minLabelDuration <= 10) {
                gestureRecognizerHelper.minLabelDuration += 0.5f
                updateControlsUi()
            }
        }

        fragmentSettingsBinding.handStableDurationMinus.setOnClickListener {
            if (gestureRecognizerHelper.minHandStableDuration >= 1) {
                gestureRecognizerHelper.minHandStableDuration -= 0.5f
                updateControlsUi()
            }
        }

        // When clicked, raise hand detection score threshold floor
        fragmentSettingsBinding.handStableDurationPlus.setOnClickListener {
            if (gestureRecognizerHelper.minHandStableDuration <= 10) {
                gestureRecognizerHelper.minHandStableDuration += 0.5f
                updateControlsUi()
            }
        }

        // When clicked, change the underlying hardware used for inference.
        // Current options are CPU and GPU
        fragmentSettingsBinding.advancedSettingsBut.setOnClickListener{
            if (fragmentSettingsBinding.advancedSettingsRow.visibility == View.VISIBLE) {
                fragmentSettingsBinding.advancedSettingsChevron.rotation = 180f
                fragmentSettingsBinding.advancedSettingsRow.visibility = View.GONE
            } else {
                fragmentSettingsBinding.advancedSettingsChevron.rotation = 270f
                fragmentSettingsBinding.advancedSettingsRow.visibility = View.VISIBLE
            }
        }
        fragmentSettingsBinding.spinnerDelegate.setSelection(
            viewModel.currentDelegate, false
        )
        fragmentSettingsBinding.spinnerDelegate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                ) {
                    try {
                        gestureRecognizerHelper.currentDelegate = p2
                        updateControlsUi()
                    } catch (e: UninitializedPropertyAccessException) {
                        Log.e(TAG, "GestureRecognizerHelper has not been initialized yet.")

                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
//                    no op
                }
            }
        fragmentSettingsBinding.spinnerHandCoordinate.setSelection(
            viewModel.currentHandCoordinate, true
        )
        fragmentSettingsBinding.spinnerHandCoordinate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                ) {
                    try {
                        gestureRecognizerHelper.currentHandCoordinate = p2
                        updateControlsUi()
                    } catch (e: UninitializedPropertyAccessException) {
                        Log.e(TAG, "GestureRecognizerHelper has not been initialized yet.")

                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
//                    no op
                }
            }
    }
    private fun updateControlsUi() {
        fragmentSettingsBinding.detectionThresholdValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureRecognizerHelper.minHandDetectionConfidence
            )
        fragmentSettingsBinding.trackingThresholdValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureRecognizerHelper.minHandTrackingConfidence
            )
        fragmentSettingsBinding.presenceThresholdValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureRecognizerHelper.minHandPresenceConfidence
            )
        fragmentSettingsBinding.confidenceThresholdValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureRecognizerHelper.minConfidence
            )
        fragmentSettingsBinding.labelDurationValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureRecognizerHelper.minLabelDuration
            )
        fragmentSettingsBinding.handStableDurationValue.text =
            String.format(
                Locale.US,
                "%.2f",
                gestureRecognizerHelper.minHandStableDuration
            )

        // Needs to be cleared instead of reinitialized because the GPU
        // delegate needs to be initialized on the thread using it when applicable
        backgroundExecutor.execute {
            gestureRecognizerHelper.clearGestureRecognizer()
            gestureRecognizerHelper.setupGestureRecognizer()
        }
    }

}