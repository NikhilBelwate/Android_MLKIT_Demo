package com.demo.mlkittest.mlBarCodeScanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.text.Text
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MLBarcodeScanner(
    private val callback: MLBarcodeCallback,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val focusBoxSize: Int,
    private val graphicOverlay: GraphicOverlay,
    private val previewView: PreviewView,
    private val drawOverlay: Boolean = true,
    private val drawBanner: Boolean = false,
    private val targetResolution: Size = Size(768, 1024),
    private val supportedBarcodeFormats: List<Int> = listOf(Barcode.FORMAT_ALL_FORMATS)
) : DefaultLifecycleObserver {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    val RATIO_4_3_VALUE = 4.0 / 3.0
    val RATIO_16_9_VALUE = 16.0 / 9.0

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private val cameraXViewModel: CameraXViewModel by lazy {
        ViewModelProvider(lifecycleOwner as ViewModelStoreOwner)[CameraXViewModel::class.java]
    }
    private lateinit var cameraSelector: CameraSelector
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var imageProcessor: VisionImageProcessor? = null
    private var imageCapture: ImageCapture? = null

    init {
        initialize()
    }

    /**
     * initialize instance members
     */
    fun initialize() {
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        cameraXViewModel.processCameraProvider.observe(lifecycleOwner) { provider: ProcessCameraProvider? ->
            cameraProvider = provider
            bindAllCameraUseCases()
        }
    }


    private fun bindAllCameraUseCases() {
        // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
        cameraProvider?.unbindAll()
        bindPreviewUseCase()
        bindAnalysisUseCase()
    }

    private fun bindPreviewUseCase() {
        if (cameraProvider == null) {
            return
        }
        cameraProvider?.unbind(previewUseCase)
        val builder = Preview.Builder().setTargetResolution(Size(2000, 2000))
        previewUseCase = builder.build()
        previewUseCase?.setSurfaceProvider(previewView.surfaceProvider)
        cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, previewUseCase)
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        cameraProvider?.unbind(analysisUseCase)
        imageProcessor?.stop()
        imageProcessor =
            BarcodeScannerProcessor(
                callback, drawOverlay, drawBanner, focusBoxSize, supportedBarcodeFormats,previewView
            )
        val builder = ImageAnalysis.Builder().setTargetResolution(getTargetResolution(context,1.0))
        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase?.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(context)
        ) { imageProxy: ImageProxy ->
            if (needUpdateGraphicOverlayImageSourceInfo) {
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                    graphicOverlay.setImageSourceInfo(imageProxy.width, imageProxy.height, false)
                } else {
                    graphicOverlay.setImageSourceInfo(imageProxy.height, imageProxy.width, false)
                }
                needUpdateGraphicOverlayImageSourceInfo = false
            }

            try {
                imageProcessor?.processImageProxy(imageProxy, graphicOverlay)
            } catch (e: MlKitException) {
                Log.e("TAG", "Failed to process image. Error: " + e.localizedMessage)
            }
        }
        cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, analysisUseCase)
    }

    fun getTargetResolution(context: Context, factor: Double): Size {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val aspectRatio = screenWidth.toDouble() / screenHeight.toDouble()
        val reductionFactor = factor // Adjust this factor as needed

        val reducedWidth = (screenWidth * reductionFactor).toInt()
        val reducedHeight = (reducedWidth / aspectRatio).toInt()
        val isPortrait =
            context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
//        if (isPortrait) {
//            return Size(720, 1280)
//        } else {
//            return Size(1280, 720)
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return Size(reducedWidth, reducedWidth)
        }
        return Size(reducedWidth,reducedHeight)
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    override fun onPause(owner: LifecycleOwner) {
        imageProcessor?.stop()
        super.onPause(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        bindAllCameraUseCases()
        super.onResume(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        imageProcessor?.stop()
        lifecycleOwner.lifecycle.removeObserver(this)
        super.onDestroy(owner)
    }


    fun getCameraCharacteristics(
        context: Context,
        lensFacing: Int? = CameraCharacteristics.LENS_FACING_BACK
    ): List<Size> {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraList = cameraManager.cameraIdList.toList()
            for (availableCameraId in cameraList) {
                val availableCameraCharacteristics =
                    cameraManager.getCameraCharacteristics(availableCameraId)
                val availableLensFacing =
                    availableCameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                if (availableLensFacing == null) {
                    continue
                }
                if (availableLensFacing == lensFacing) {
                    val streamConfigurationMap =
                        availableCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val sizes = streamConfigurationMap?.getOutputSizes(SurfaceTexture::class.java)
                    val list = sizes?.map { size -> size }
                    val resolution =
                        sizes?.get(4) // You may choose the resolution according to your preference
                    // return Size(resolution?.height!!,resolution?.width!!)?:Size(720,1280)
                    val isPortrait =
                        context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                    if (list != null) {
                        if (isPortrait) {
                            return (list)
                        } else {
                            return (list)
                        }
                    }
                }
            }
        } catch (e: CameraAccessException) {
            // Accessing camera ID info got error
        }
        return listOf(Size(720, 1280))
    }
}

/**
 * Listen to new scanned barcodes
 */
fun interface MLBarcodeCallback {
    /**
     * @param displayValue Returns barcode value in a user-friendly format.
     *  This method may omit some of the information encoded in the barcode. For example, if getRawValue() returns 'MEBKM:TITLE:Google;URL://www.google.com;;', the display value might be '//www.google.com'.
     *  This value may be multiline, for example, when line breaks are encoded into the original TEXT barcode value. May include the supplement value.
     *
     * @param rawValue Returns barcode value as it was encoded in the barcode. Structured values are not parsed, for example: 'MEBKM:TITLE:Google;URL://www.google.com;;'.
     */
    fun onNewBarcodeScanned(
        displayValue: String,
        rawValue: String,
        newtextList: List<Text.TextBlock>
    )
}
