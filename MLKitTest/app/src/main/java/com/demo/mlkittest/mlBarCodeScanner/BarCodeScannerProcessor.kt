package com.demo.mlkittest.mlBarCodeScanner

import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.view.PreviewView
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.lucidea.lucidealocationmanager.App
import com.lucidea.lucidealocationmanager.mlBarCodeScanner.MLBarcodeCallback
import com.lucidea.lucidealocationmanager.mlBarCodeScanner.VisionProcessorBase
import com.lucidea.lucidealocationmanager.utils.DeviceUtils
import kotlin.math.abs

internal class BarcodeScannerProcessor(
    private val callback: MLBarcodeCallback,
    private val drawOverlay: Boolean,
    private val drawBanner: Boolean,
    private val focusBoxSize: Int,
    supportedBarcodeFormats: List<Int> = listOf(Barcode.FORMAT_ALL_FORMATS),
    private val previewView: PreviewView
) : VisionProcessorBase<List<Barcode>>() {

    // Note that if you know which format of barcode your app is dealing with, detection will be
    // faster to specify the supported barcode formats one by one
    private val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(
        if (supportedBarcodeFormats.size == 1) {
            BarcodeScannerOptions.Builder().setBarcodeFormats(supportedBarcodeFormats.first())
                .build()
        } else {
            val moreFormats =
                supportedBarcodeFormats.subList(1, supportedBarcodeFormats.size).toIntArray()
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(supportedBarcodeFormats.first(), *moreFormats).build()
        }
    )

    override fun stop() {
        super.stop()
        barcodeScanner.close()
    }

    override fun detectInImage(image: InputImage): Task<List<Barcode>> {
        return barcodeScanner.process(image)
    }

    override fun onSuccess(
        results: List<Barcode>,
        graphicOverlay: GraphicOverlay,
        textList: MutableList<Text.TextBlock>
    ) {

            results.getOrNull(results.size/2)?.let { barcode ->

                val areaConst=if(DeviceUtils.isTablet(graphicOverlay.context)) 0.075 else 0.25
                // Check if the barcode is approximately 25% of Scan area

                if (calculateBarcodeArea(barcode.boundingBox!!)  >=(App.QrScanBoxArea*areaConst)) {
                   // val isInfocus = targetIsInFocusArea(calculateFocusArea(), barcode.boundingBox!!.toRectF())

                    val graphic =
                        BarcodeGraphic(
                            barcode,
                            graphicOverlay,
                            drawOverlay,
                            drawBanner,
                            focusBoxSize
                        ) { isInFocus ->
                            val displayValue = barcode.displayValue
                            val rawValue = barcode.rawValue
                            if (isInFocus&&displayValue != null && rawValue != null) {
                                callback.onNewBarcodeScanned(displayValue, rawValue,textList)
                            }

                        }
                    graphicOverlay.add(graphic)
                }
            }

    }

    override fun onFailure(e: Exception) {
        // do nothing
    }
    private fun calculateBarcodeArea(boundingBox: Rect): Double {
        return abs(boundingBox.height().toDouble() * boundingBox.width().toDouble())
    }
    private fun targetIsInFocusArea(focus: RectF, target: RectF): Boolean {
        return (focus.left < target.left) && (target.right < focus.right)
                && (focus.top < target.top) && (focus.bottom > target.bottom)
    }
    private fun calculateFocusArea(): RectF {
        val height = previewView.measuredHeight.toFloat()
        val width = previewView.measuredWidth.toFloat()
        val left = (width / 2) - (focusBoxSize / 2)
        val right = (width / 2) + (focusBoxSize / 2)
        val top = (height / 2) - (focusBoxSize / 2)
        val bottom = (height / 2) + (focusBoxSize / 2)
        return RectF(left, top, right, bottom)
    }
}
