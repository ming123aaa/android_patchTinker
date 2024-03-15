package com.ohuang.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class TestMainActivity : AppCompatActivity() {
    val TAG="TestMainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_main)
        Log.d(TAG, "onCreate: layoutId="+R.layout.activity_test_main)
    }
}