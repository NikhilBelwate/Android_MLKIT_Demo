package com.demo.mlkittest

import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.demo.mlkittest.utils.IntentConstants
import com.google.mlkit.vision.barcode.common.Barcode


class MainActivity : AppCompatActivity() {
    lateinit var resultEditTxt:TextView
    lateinit var scanButton: Button
    private val MY_CAMERA_REQUEST_CODE=1
    lateinit var qrScan: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resultEditTxt=findViewById(R.id.result_ed_txt)
        scanButton=findViewById(R.id.scanButton)
        qrScan = Intent(this, QRCaptureActivity::class.java)
        qrScan.putExtra(
            "barcodeFormat",
            Barcode.FORMAT_QR_CODE or Barcode.FORMAT_CODE_39 or Barcode.FORMAT_CODE_128
        )
        scanButton.setOnClickListener {
            resultEditTxt.setText("Scanning...")
            checkCameraPermission(qrScan)
        }
    }

    private fun checkCameraPermission(qrScan: Intent) {
        if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions( arrayOf(CAMERA), MY_CAMERA_REQUEST_CODE);
        }else{
            scanLauncher.launch(qrScan)
        }
    }


    val scanLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult? ->
            if (null != result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
//                        activity?.let { App.beepSound(applicationContext = it.applicationContext) }
                    val data: Intent? = result.data
                    if (null != data) {
                        val extra: Bundle? = data.extras
                        if (null != extra) {
                            val entry: String? = extra.getString(IntentConstants.RESULT)
                            resultEditTxt.setText(entry)
                            //editor.apply()
                            //Toast.makeText(activity, "Scan Completed.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
            checkCameraPermission(qrScan)
        }
    }
}