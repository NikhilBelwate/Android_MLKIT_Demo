package com.demo.mlkittest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    lateinit var resultEditTxt:EditText
    lateinit var scanButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resultEditTxt=findViewById(R.id.result_ed_txt)
        scanButton=findViewById(R.id.scanButton)
        scanButton.setOnClickListener {
            resultEditTxt.setText("Scanning...")
        }
    }
}