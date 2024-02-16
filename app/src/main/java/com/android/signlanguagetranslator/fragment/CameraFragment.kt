package com.android.signlanguagetranslator.fragment

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.fragment.app.Fragment
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
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


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
    private lateinit var gestureResults: GestureRecognizerResultsAdapter
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = 0
    private var imageCapture: ImageCapture? = null
    private var cameraInfo: CameraInfo? = null
    private var torchState: LiveData<Int>? = null
    private var cameraControl: CameraControl? = null
    private var currentLabel: String? = null
    private var handCoordinate: Int? = null


    private lateinit var backgroundExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(
                requireActivity(), R.id.fragment_container
            ).navigate(R.id.action_camera_to_permissions)
        }

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
            /*viewModel.setDetectionThreshold(gestureRecognizerHelper.minHandDetectionConfidence)
            viewModel.setTrackingThreshold(gestureRecognizerHelper.minHandTrackingConfidence)
            viewModel.setPresenceThreshold(gestureRecognizerHelper.minHandPresenceConfidence)
            viewModel.setDelegate(gestureRecognizerHelper.currentDelegate)
            viewModel.setCoordinate(gestureRecognizerHelper.currentHandCoordinate)
            viewModel.setConfidenceThreshold(gestureRecognizerHelper.minConfidence)
            viewModel.setLabelDuration(gestureRecognizerHelper.minLabelDuration)
            viewModel.setHandStableDuration(gestureRecognizerHelper.minHandStableDuration)*/
            viewModel.setIsFacingFront(gestureRecognizerHelper.isFrontFacing)

            // Close the Gesture Recognizer helper and release resources
//            backgroundExecutor.execute { gestureRecognizerHelper.clearGestureRecognizer() }
        }
        //resetTorchState
        fragmentCameraBinding.topSheetLayout.button4.setBackgroundResource(R.drawable.baseline_flashlight_off_24)
    }

    override fun onDestroyView() {
        fragmentCameraBinding.topSheetLayout.button4.setBackgroundResource(R.drawable.baseline_flashlight_off_24)
        _fragmentCameraBinding = null
        super.onDestroyView()
        // Unbind the camera provider
        cameraProvider?.unbindAll()

/*// Release the SurfaceView resources
        fragmentCameraBinding.viewFinder.holder.removeCallback(this)
        fragmentCameraBinding.viewFinder.release()*/


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

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gestureResults = GestureRecognizerResultsAdapter(viewModel)
        with(fragmentCameraBinding.recyclerviewResults) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = gestureRecognizerResultAdapter
        }


        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()
        backgroundExecutor.execute {
            // Initialize GestureRecognizerHelper here
            gestureRecognizerHelper = GestureRecognizerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                isFrontFacing = viewModel.currentIsFrontFacing,
                gestureRecognizerListener = this
            )
        }

        viewModel.currentCoordinate.observe(viewLifecycleOwner){ coordinate ->
            handCoordinate = coordinate
        }

        fragmentCameraBinding.viewFinder.post {
            setUpCamera()
        }

        fragmentCameraBinding.bottomSheetLayout.button3.setOnClickListener{
            rotateCameraLens()
        }
        fragmentCameraBinding.bottomSheetLayout.button2.setOnClickListener{
            //capture image
            takePicture()
        }
        fragmentCameraBinding.topSheetLayout.button5.setOnClickListener {
            findNavController().navigate(R.id.settings_fragment)
        }
        fragmentCameraBinding.topSheetLayout.button4.setOnClickListener {
            if(!requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) return@setOnClickListener
            when (torchState?.value) {
                TorchState.ON -> {
                    cameraControl?.enableTorch(false)
                    fragmentCameraBinding.topSheetLayout.button4.setBackgroundResource(R.drawable.baseline_flashlight_off_24)
                }

                TorchState.OFF -> {
                    cameraControl?.enableTorch(true)
                    fragmentCameraBinding.topSheetLayout.button4.setBackgroundResource(R.drawable.baseline_flashlight_on_24)
                }
                null -> {
                    Log.d("TORCHSTATE", torchState.toString())
                }
            }
        }
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
        fragmentCameraBinding.topSheetLayout.button4.setBackgroundResource(R.drawable.baseline_flashlight_off_24)
        bindCameraUseCases()
    }

    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            Runnable {
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

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .build()
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9)
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
        imageCapture = ImageCapture.Builder()
//            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer, imageCapture
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

    private fun takePicture() {
        // Check if the image capture use case is null
        if (imageCapture == null) return
        // Create a file to store the image
        val photoFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/Camera",
            "photo_${System.currentTimeMillis()}.jpg")
        // Create an output options object
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        // Take a picture and save it to the file
        imageCapture?.takePicture(
            outputOptions,
            backgroundExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Show a toast message
                    val msg = "Photo saved to ${photoFile.absolutePath}"
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Image has been saved", Toast.LENGTH_SHORT).show()
                    }
                    Log.d(TAG, msg)

                    if (currentLabel.isNullOrEmpty()) return
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val canvas = Canvas(mutableBitmap)
                    val context = context?.resources
                    val labelPaint = Paint().apply {
                        color = Color.WHITE
                        textSize =
                            context!!.getDimensionPixelSize(R.dimen.bottom_sheet_text_size).toFloat() * 2 - 12f
                        textAlign = Paint.Align.CENTER
                        // Other text properties can be adjusted here
                    }
                    val bgPaint = Paint().apply {
                        color = Color.BLACK
                    }
                    val bg = RectF((mutableBitmap.width / 2) - labelPaint.measureText(currentLabel) + 2f / 2,(mutableBitmap.height - 100f) - labelPaint.textSize, ((mutableBitmap.width / 2) + labelPaint.measureText(currentLabel) / 2) + 25f, mutableBitmap.height - 100f + 25f)
                    canvas.drawRoundRect(bg, 10f, 10f, bgPaint)
                    canvas.drawText(currentLabel ?: "",  mutableBitmap.width / 2f, mutableBitmap.height - 100f, labelPaint)


                    val outputStream = FileOutputStream(photoFile)
                    mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    // Show a toast message
                    val msg = "Photo capture failed: ${exception.message}"
//                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.e(TAG, msg, exception)
                }
            }
        )
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
                    currentLabel = gestureRecognizerResultAdapter.getCurrentLabel()
                } else {
                    gestureRecognizerResultAdapter.updateResults(emptyList())
                    currentLabel = null
                }

                fragmentCameraBinding.overlay.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM,
                    handCoordinate
                )

                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            gestureRecognizerResultAdapter.updateResults(emptyList())
        }
    }
}
