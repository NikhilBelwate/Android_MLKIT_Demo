package com.demo.mlkittest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lucidea.lucidealocationmanager.databinding.ActivityQrcaptureBinding
import com.lucidea.lucidealocationmanager.mlBarCodeScanner.MLBarcodeScanner
import com.lucidea.lucidealocationmanager.models.LogLevel
import com.lucidea.lucidealocationmanager.utils.Alerts
import com.lucidea.lucidealocationmanager.utils.DeviceUtils
import com.lucidea.lucidealocationmanager.utils.IntentConstants
import com.lucidea.lucidealocationmanager.utils.Utils

class QRCaptureActivity : AppCompatActivity() {
    private val TAG = "QRCaptureActivity.kt"
    private lateinit var binding: ActivityQrcaptureBinding
    private lateinit var barcodeScanner: MLBarcodeScanner
    val handler = Handler(Looper.getMainLooper())
    private var callbackCount=0

    private fun initBarcodeScanner() {

        handler.postDelayed({
            binding.title.text = "Scanning"
        }, 1000)

        //Set height and width of binding.qrCodeScannerBox based on device type
        binding.qrCodeScannerBox.layoutParams.height =
            if (DeviceUtils.isTablet(context = this)) resources.getDimensionPixelSize(
                R.dimen.size_396
            ) else resources.getDimensionPixelSize(R.dimen.size_264)
        binding.qrCodeScannerBox.layoutParams.width =
            if (DeviceUtils.isTablet(context = this)) resources.getDimensionPixelSize(
                R.dimen.size_396
            ) else resources.getDimensionPixelSize(R.dimen.size_264)

        var oldResult: String = ""
        barcodeScanner = MLBarcodeScanner(
            callback = { displayValue, rawValue, textList ->
                // you can process your barcode here
                //LogLevel.info("QrCaptureActivity", "Result Callback : ${Utils.getCurrentTime()}")

                //val prefs = this@QRCaptureActivity.getSharedPreferences(Utils.PREFS_KEY, Context.MODE_PRIVATE)
                var newVal=rawValue
//                textList.forEach{
//                    Log.d("TextList",  it.text)
//
//                }
                Log.d("TextList1",  rawValue)
                if (displayValue.contains("000")&& callbackCount<6) {
                    val templist =textList.map { it.text }
                    val list=processStrings(templist)
                    if(!checktextListContainBarcode(list)){
                        callbackCount++
                    }
                    val closeMatch= findClosestMatch(rawValue,list) ?: return@MLBarcodeScanner
                    newVal = closeMatch
                } else {
                    if (oldResult != rawValue) {
                        oldResult = rawValue
                        return@MLBarcodeScanner
                    }
                }
                val intent = Intent(IntentConstants.ACTION)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                intent.putExtra(IntentConstants.RESULT, newVal)
                intent.putExtra(IntentConstants.RESULT_FORMAT, displayValue)
                setResult(Activity.RESULT_OK, intent)
                finish()
            },
            focusBoxSize = if (DeviceUtils.isTablet(context = this)) resources.getDimensionPixelSize(
                R.dimen.size_396
            )
            else resources.getDimensionPixelSize(R.dimen.size_264),
            graphicOverlay = binding.graphicOverlay,
            previewView = binding.previewViewCameraScanning,
            lifecycleOwner = this,
            context = this,
            drawOverlay = true, // show rectangle around detected barcode
            drawBanner = false // show detected barcode value on top of it
        )
    }
    //function to remove spaces and add 0 in places of 'O'
    fun processStrings(list: List<String>): List<String> {
        val processedList = mutableListOf<String>()

        for (str in list) {
            val processedStr = StringBuilder()
            for ((index, char) in str.withIndex()) {
                when {
                    index < 3 -> processedStr.append(char)
                    char == 'o' || char == 'O' -> processedStr.append('0')
                    else -> processedStr.append(char)
                }
            }
            processedList.add(processedStr.toString().replace(" ", ""))
        }

        return processedList
    }
    fun findClosestMatch(input: String, list: List<String>): String? {
        for (item in list) {
            val match = calculatematch(input, item)
            if(match)return item
        }
        return null
    }

    private fun calculatematch(Input: String, Item: String): Boolean {
        val processedInput=Input
        var processedItem = Item // Make sure to initialize processedItem properly
        val builder = StringBuilder(processedItem)


        processedItem = builder.toString()
        //check similarity from beginning
        processedInput.forEachIndexed { index, c ->
            if(processedItem.length>index){
               if(index<6){
                   if(c!=processedItem[index]){
                       return false
                   }
               }
            }
        }
       val revprocessedInput= processedInput.reversed()
        val revprocessedItem= processedItem.reversed()
        //it check last two char must be same
        //check similarity from end
        revprocessedInput.forEachIndexed { index, c ->
            if(revprocessedItem.length>index){
                if(index<4){
                    if(c!=revprocessedItem[index]){
                        return false
                    }
                }
            }
        }
       return true
    }
    private fun checktextListContainBarcode(textList: List<String>): Boolean {
        textList.forEach {
            if(it.contains("0000"))return true
        }
        return false
    }



    /**
     * Activity to capture barcode
     * @param savedInstanceState saved previous state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        LogLevel.verbose(TAG, "#### onCreate ####")
        super.onCreate(savedInstanceState)
        binding = ActivityQrcaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        LogLevel.info("QrCaptureActivity", "Setup Started: ${Utils.getCurrentTime()}")
        setupScanCancel()
        setupCameraText()
        setupBarCodeBoxSize()
        LogLevel.info("QrCaptureActivity", "Setup completed : ${Utils.getCurrentTime()}")
        checkCameraPermission()
    }

    private fun setupBarCodeBoxSize() {
        binding.qrCodeScannerBox.layoutParams.height =
            if (DeviceUtils.isTablet(context = this)) resources.getDimensionPixelSize(
                R.dimen.size_396
            ) else resources.getDimensionPixelSize(R.dimen.size_264)

        binding.qrCodeScannerBox.layoutParams.width =
            if (DeviceUtils.isTablet(context = this)) 2 * resources.getDimensionPixelSize(
                R.dimen.size_396
            ) else resources.getDimensionPixelSize(R.dimen.size_264)
        if (DeviceUtils.isTablet(context = this)) {
            App.QrScanBoxArea = (2 * 200.0) * 200
        } else {
            App.QrScanBoxArea = 264 * 264.0
        }
    }

    private fun setupCameraText() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            binding.title.text = "Preparing for Barcode Scanning"
        }, 500)
    }

    private fun setupScanCancel() {
        binding.scanCancel.setOnClickListener { v: View? ->
            //go back
            finish()
        }
    }

    /**
     * 1. This function is responsible to request the required CAMERA permission
     */
    private fun checkCameraPermission() {
        try {
            val requiredPermissions = arrayOf(android.Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(this, requiredPermissions, 0)
        } catch (e: IllegalArgumentException) {
            checkIfCameraPermissionIsGranted()
        }
    }

    /**
     * 2. This function will check if the CAMERA permission has been granted.
     * If so, it will call the function responsible to initialize the camera preview.
     * Otherwise, it will raise an alert.
     */
    private fun checkIfCameraPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission granted: start the preview
            initBarcodeScanner()
        } else {
            // Permission denied: show an alert
            val builder = AlertDialog.Builder(this@QRCaptureActivity)
            builder.setTitle("Camera Permission Required")
                .setMessage("This application needs to access the camera to process barcodes")
                .setPositiveButton("OK") { _, _ ->
                    // Keep asking for permission until granted
                    try {
                        val requiredPermissions = arrayOf(android.Manifest.permission.CAMERA)
                        ActivityCompat.requestPermissions(this, requiredPermissions, 0)
                        if (ContextCompat.checkSelfPermission(
                                this, android.Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            // Permission granted: start the preview
                            initBarcodeScanner()
                        } else {
                            finish()
                        }
                    } catch (e: IllegalArgumentException) {
                        finish()
                    }

                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    // You can show a message or take any action when the user cancels the permission request
                    finish()
                    Alerts.alertDialog(
                        this@QRCaptureActivity,
                        "Warning",
                        "You've denied permission to use the camera. You can change this in Android app settings."
                    )
                }.create().show()
        }
    }

    /**
     * 3. This function is executed once the user has granted or denied the missing permission
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkIfCameraPermissionIsGranted()
    }

    /**
     * handle the toolbar items
     * @param item Item clicked on toolbar
     * @return selected a correct button true or false.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        LogLevel.verbose(TAG, "#### onOptionsItemSelected ####")
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}