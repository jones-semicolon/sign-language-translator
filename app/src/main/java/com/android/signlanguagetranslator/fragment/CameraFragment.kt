package com.android.signlanguagetranslator.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.signlanguagetranslator.GestureRecognizerHelper
import com.android.signlanguagetranslator.MainViewModel
import com.android.signlanguagetranslator.R
import com.android.signlanguagetranslator.databinding.FragmentCameraBinding
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * Use the [CameraFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CameraFragment : Fragment(),
    GestureRecognizerHelper.GestureRecognizerListener {

    companion object {
        private const val TAG = "Hand gesture recognizer"
    }

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper
    private val viewModel: MainViewModel by activityViewModels()
    private var defaultNumResults = 1
    private val gestureRecognizerResultAdapter: GestureRecognizerResultsAdapter by lazy {
        GestureRecognizerResultsAdapter(viewModel).apply {
            updateAdapterSize(defaultNumResults)
        }
    }
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = 0
    private var imageCapture: ImageCapture? = null
    private var cameraInfo: CameraInfo? = null
    private var torchState: LiveData<Int>? = null
    private var cameraControl: CameraControl? = null


    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(
                requireActivity(), R.id.fragment_container
            ).navigate(R.id.action_camera_to_permissions)
        }

        // Start the GestureRecognizerHelper again when users come back
        // to the foreground.
        backgroundExecutor.execute {
            Log.d("PAUSED", gestureRecognizerHelper.isFrontFacing.toString())
            if (gestureRecognizerHelper.isClosed()) {
                gestureRecognizerHelper.setupGestureRecognizer()
            }
            if(gestureRecognizerHelper.isFrontFacing){
                cameraFacing = 0
                CameraSelector.LENS_FACING_FRONT
            } else {
                cameraFacing = 1
                CameraSelector.LENS_FACING_BACK
            }
            setUpCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::gestureRecognizerHelper.isInitialized) {
            Log.d("PAUSED", "UPDATE")
            viewModel.setMinHandDetectionConfidence(gestureRecognizerHelper.minHandDetectionConfidence)
            viewModel.setMinHandTrackingConfidence(gestureRecognizerHelper.minHandTrackingConfidence)
            viewModel.setMinHandPresenceConfidence(gestureRecognizerHelper.minHandPresenceConfidence)
            viewModel.setDelegate(gestureRecognizerHelper.currentDelegate)
            viewModel.setIsFacingFront(gestureRecognizerHelper.isFrontFacing)
            viewModel.setMinConfidence(gestureRecognizerHelper.minConfidence)

            // Close the Gesture Recognizer helper and release resources
            backgroundExecutor.execute { gestureRecognizerHelper.clearGestureRecognizer() }
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
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
        _fragmentCameraBinding =
            FragmentCameraBinding.inflate(inflater, container, false)

        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(fragmentCameraBinding.recyclerviewResults) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = gestureRecognizerResultAdapter
        }


        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()
        /*        val cameraControl: CameraControl? = camera?.cameraControl
                val torchState: Int? = cameraInfo?.torchState?.value*/
        backgroundExecutor.execute {
            gestureRecognizerHelper = GestureRecognizerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minHandDetectionConfidence = viewModel.currentMinHandDetectionConfidence,
                minHandTrackingConfidence = viewModel.currentMinHandTrackingConfidence,
                minHandPresenceConfidence = viewModel.currentMinHandPresenceConfidence,
                minConfidence = viewModel.currentMinConfidence,
                currentDelegate = viewModel.currentDelegate,
                isFrontFacing = viewModel.currentIsFrontFacing,
                gestureRecognizerListener = this
            )
        }

        if(this::gestureRecognizerHelper.isInitialized){
            Log.d("TAG", "open")
        } else {
            Log.d("TAG", "closed")
        }


        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }
        /* imageCapture = ImageCapture.Builder()
             .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
             .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
             .setTargetAspectRatio(AspectRatio.RATIO_4_3)
             .setTargetRotation(Surface.ROTATION_0)
             .build()*/
        fragmentCameraBinding.bottomSheetLayout.button3.setOnClickListener{
            rotateCameraLens()
        }
/*        fragmentCameraBinding.bottomSheetLayout.button2.setOnClickListener{
            captureImage()
        }*/
        fragmentCameraBinding.topSheetLayout.button5.setOnClickListener {
            findNavController().navigate(R.id.settings_fragment)
        }
        fragmentCameraBinding.topSheetLayout.button4.setOnClickListener {
            when (torchState?.value) {
                TorchState.ON -> {
                    cameraControl?.enableTorch(false)
                    fragmentCameraBinding.topSheetLayout.button4.setBackgroundResource(R.drawable.ic_flash_off)
                }

                TorchState.OFF -> {
                    cameraControl?.enableTorch(true)
                    fragmentCameraBinding.topSheetLayout.button4.setBackgroundResource(R.drawable.ic_flash_on)
                }
                null -> {
                    Log.d("TORCHSTATE", torchState.toString())
                }
            }
        }

        // Create the Hand Gesture Recognition Helper that will handle the
        // inference


        // Attach listeners to UI control widgets
//        initBottomSheetControls()
    }

    private fun rotateCameraLens() {
        // Toggle between front and back camera lenses
        Log.d("CAMERA LENS", "${gestureRecognizerHelper.isFrontFacing} ${cameraFacing}")

        if (gestureRecognizerHelper.isFrontFacing) {
            gestureRecognizerHelper.isFrontFacing = false
            CameraSelector.LENS_FACING_BACK
            cameraFacing = 1
        } else {
            gestureRecognizerHelper.isFrontFacing = true
            CameraSelector.LENS_FACING_FRONT
            cameraFacing = 0
        }

        // Re-bind camera use cases with the updated camera lens
        bindCameraUseCases()

    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }


    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")
        Log.d("PAUSED", "CAMERA FACING IS FRONT? ${cameraFacing}")

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        /*imageCapture?.let { capture ->
            fragmentCameraBinding.bottomSheetLayout.button2.setOnClickListener {
                captureImage()
            }
        }*/


        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        recognizeHand(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )
            cameraInfo = camera!!.cameraInfo
            torchState = cameraInfo!!.torchState
            cameraControl = camera!!.cameraControl

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun recognizeHand(imageProxy: ImageProxy) {
        gestureRecognizerHelper.recognizeLiveStream(
            imageProxy = imageProxy,
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            fragmentCameraBinding.viewFinder.display.rotation
    }

    // Update UI after a hand gesture has been recognized. Extracts original
    // image height/width to scale and place the landmarks properly through
    // OverlayView. Only one result is expected at a time. If two or more
    // hands are seen in the camera frame, only one will be processed.
    override fun onResults(
        resultBundle: GestureRecognizerHelper.ResultBundle
    ) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
                // Show result of recognized gesture
                val gestureCategories = resultBundle.results.first().gestures()
                if (gestureCategories.isNotEmpty()) {
                    gestureRecognizerResultAdapter.updateResults(
                        gestureCategories.first()
                    )
                } else {
                    gestureRecognizerResultAdapter.updateResults(emptyList())
                }

                /*fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.text =
                    String.format("%d ms", resultBundle.inferenceTime)*/

                // Pass necessary information to OverlayView for drawing on the canvas
                fragmentCameraBinding.overlay.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM
                )

                // Force a redraw
                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            gestureRecognizerResultAdapter.updateResults(emptyList())

            /*if (errorCode == GestureRecognizerHelper.GPU_ERROR) {
                fragmentSettingsBinding.spinnerDelegate.setSelection(
                    GestureRecognizerHelper.DELEGATE_CPU, false
                )
            }*/
        }
    }
}