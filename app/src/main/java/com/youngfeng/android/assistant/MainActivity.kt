package com.youngfeng.android.assistant

import android.graphics.Paint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val mSupportDeveloperText by lazy { findViewById<TextView>(R.id.text_support_developer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSupportDeveloperText.paint.flags = Paint.UNDERLINE_TEXT_FLAG
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
