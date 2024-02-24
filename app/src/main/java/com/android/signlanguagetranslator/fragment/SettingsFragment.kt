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
        /*if (this::gestureRecognizerHelper.isInitialized) {
            viewModel.setDetectionThreshold(gestureRecognizerHelper.minHandDetectionConfidence)
            viewModel.setTrackingThreshold(gestureRecognizerHelper.minHandTrackingConfidence)
            viewModel.setPresenceThreshold(gestureRecognizerHelper.minHandPresenceConfidence)
            viewModel.setConfidenceThreshold(gestureRecognizerHelper.minConfidence)
            viewModel.setDelegate(gestureRecognizerHelper.currentDelegate)
            viewModel.setCoordinate(gestureRecognizerHelper.currentHandCoordinate)
            viewModel.setHandStableDuration(gestureRecognizerHelper.minHandStableDuration)
            viewModel.setLabelDuration(gestureRecognizerHelper.minLabelDuration)
        }*/
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
            // Initialize GestureRecognizerHelper here
            gestureRecognizerHelper = GestureRecognizerHelper(
                context = requireContext(),
                isFrontFacing = viewModel.currentIsFrontFacing,
            )
        }

        fragmentSettingsBinding.button7.setOnClickListener {
            findNavController().popBackStack()
        }


        // Attach listeners to UI control widgets
        initBottomSheetControls()
    }

    private fun initBottomSheetControls() {
        // init bottom sheet settings
        viewModel.currentDetectionThreshold.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.detectionThresholdMinus.setOnClickListener {
                if (res >= 0.2) {
                    Log.d("DATASTORE", res.toString())
                    viewModel.setDetectionThreshold(res - 0.1f)
                    updateControlsUi()
                }
            }
            fragmentSettingsBinding.detectionThresholdPlus.setOnClickListener {
                if (res <= 0.8) {
                    viewModel.setDetectionThreshold(res + 0.1f)
                    updateControlsUi()
                }
            }
            fragmentSettingsBinding.detectionThresholdValue.text =
                String.format(
                    Locale.US,
                    "%.2f",
                    res
                )
        }

        viewModel.currentTrackingThreshold.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.trackingThresholdMinus.setOnClickListener {
                if (res >= 0.2) {
                    viewModel.setTrackingThreshold(res - 0.1f)
                    updateControlsUi()
                }
            }
            fragmentSettingsBinding.trackingThresholdPlus.setOnClickListener {
                if (res <= 0.8) {
                    viewModel.setTrackingThreshold(res + 0.1f)
                    updateControlsUi()
                }
            }

            fragmentSettingsBinding.trackingThresholdValue.text =
                String.format(
                    Locale.US,
                    "%.2f",
                    res
                )
        }
        viewModel.currentPresenceThreshold.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.presenceThresholdMinus.setOnClickListener {
                if (res >= 0.2) {
                    viewModel.setPresenceThreshold(res - 0.1f)
                    updateControlsUi()
                }
            }
            fragmentSettingsBinding.presenceThresholdPlus.setOnClickListener {
                if (res <= 0.8) {
                    viewModel.setPresenceThreshold(res + 0.1f)
                    updateControlsUi()
                }
            }
            fragmentSettingsBinding.presenceThresholdValue.text =
                String.format(
                    Locale.US,
                    "%.2f",
                    res
                )
        }

        viewModel.currentConfidenceThreshold.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.confidenceThresholdMinus.setOnClickListener {
                if (res >= 0.2) {
                    viewModel.setConfidenceThreshold(res - 0.05f)
                    updateControlsUi()
                }
            }
            fragmentSettingsBinding.confidenceThresholdPlus.setOnClickListener {
                if (res <= 0.9) {
                    viewModel.setConfidenceThreshold(res + 0.05f)
                    updateControlsUi()
                }
            }
            fragmentSettingsBinding.confidenceThresholdValue.text =
                String.format(
                    Locale.US,
                    "%.2f",
                    res
                )
        }
        viewModel.currentLabelDuration.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.labelDurationMinus.setOnClickListener {
                if (res >= 3) {
                    viewModel.setLabelDuration(res - 0.5f)
                    updateControlsUi()
                }
            }

            // When clicked, raise hand detection score threshold floor
            fragmentSettingsBinding.labelDurationPlus.setOnClickListener {
                if (res <= 10) {
                    viewModel.setLabelDuration(res + 0.5f)
                    updateControlsUi()
                }
            }
            fragmentSettingsBinding.labelDurationValue.text =
                String.format(
                    Locale.US,
                    "%.2f",
                    res
                )
        }

        viewModel.currentHandStableDuration.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.handStableDurationMinus.setOnClickListener {
                if (res >= 1) {
                    viewModel.setHandStableDuration(res - 0.5f)
                    updateControlsUi()
                }
            }

            // When clicked, raise hand detection score threshold floor
            fragmentSettingsBinding.handStableDurationPlus.setOnClickListener {
                if (res <= 10) {
                    viewModel.setHandStableDuration(res + 0.5f)
                    updateControlsUi()
                }
            }
            fragmentSettingsBinding.handStableDurationValue.text =
                String.format(
                    Locale.US,
                    "%.2f",
                    res
                )
        }

        // Current options are CPU and GPU
        fragmentSettingsBinding.advancedSettingsBut.setOnClickListener{
            if (fragmentSettingsBinding.advancedSettingsRow.visibility == View.VISIBLE) {
                fragmentSettingsBinding.advancedSettingsChevron.rotation = 180f
                fragmentSettingsBinding.advancedSettingsRow.visibility = View.GONE
            } else {
                fragmentSettingsBinding.advancedSettingsChevron.rotation = 270f
                fragmentSettingsBinding.advancedSettingsRow.visibility = View.VISIBLE
                updateControlsUi()
            }
        }

        fragmentSettingsBinding.spinnerDelegate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                ) {
                    viewModel.setDelegate(p2)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
//                    no op
                }
            }

        fragmentSettingsBinding.spinnerHandCoordinate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                ) {
                    viewModel.setCoordinate(p2)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
//                    no op
                }
            }
    }
    private fun updateControlsUi() {
        viewModel.currentDetectionThreshold.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.detectionThresholdValue.text =
                String.format(
                    Locale.US,
                    "%.2f",
                    res
                )
        }

        viewModel.currentTrackingThreshold.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.trackingThresholdValue.text =
                String.format(
                    Locale.US,
                    "%.2f",
                    res
                )
        }
        viewModel.currentPresenceThreshold.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.presenceThresholdValue.text =
                String.format(
                    Locale.US,
                    "%.2f",
                    res
                )
        }

        viewModel.currentConfidenceThreshold.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.confidenceThresholdValue.text =
                String.format(
                    Locale.US,
                    "%.2f",
                    res
                )
        }
        viewModel.currentLabelDuration.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.labelDurationValue.text =
                String.format(
                    Locale.US,
                    "%.2f",
                    res
                )
        }

        viewModel.currentHandStableDuration.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.handStableDurationValue.text =
                String.format(
                    Locale.US,
                    "%.2f",
                    res
                )
        }

        viewModel.currentDelegate.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.spinnerDelegate.setSelection(res)
        }

        viewModel.currentCoordinate.observe(viewLifecycleOwner){
                res ->
            fragmentSettingsBinding.spinnerHandCoordinate.setSelection(res)
        }


        // Needs to be cleared instead of reinitialized because the GPU
        // delegate needs to be initialized on the thread using it when applicable
        backgroundExecutor.execute {
            gestureRecognizerHelper.clearGestureRecognizer()
            gestureRecognizerHelper.setupGestureRecognizer()
        }
    }

}