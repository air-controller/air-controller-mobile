package com.youngfeng.android.assistant.support

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import com.youngfeng.android.assistant.Constants
import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.view.RewardDialog

class DeveloperSupportActivity : AppCompatActivity() {
    private val mStarBtn by lazy { findViewById<LinearLayoutCompat>(R.id.btn_star) }
    private val mRewardBtn by lazy { findViewById<AppCompatButton>(R.id.btn_reward) }

    private val mRewardDialog by lazy { RewardDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer_support)

        initView()
        initListener()
    }

    private fun initView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initListener() {
        mStarBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(Constants.URL_GITHUB)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        mRewardBtn.setOnClickListener {
            mRewardDialog.show()
        }
    }
}
