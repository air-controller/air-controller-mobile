package com.youngfeng.android.assistant.about

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.youngfeng.android.assistant.BuildConfig
import com.youngfeng.android.assistant.R

class AboutActivity : AppCompatActivity() {
    private val mVersionText by lazy { findViewById<AppCompatTextView>(R.id.text_version) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mVersionText.text = BuildConfig.VERSION_NAME
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
