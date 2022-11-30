package com.youngfeng.android.assistant.support

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdLoadType
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd
import com.youngfeng.android.assistant.Constants
import com.youngfeng.android.assistant.R
import com.youngfeng.android.assistant.view.RewardDialog

class DeveloperSupportActivity : AppCompatActivity() {
    private val mStarBtn by lazy { findViewById<LinearLayoutCompat>(R.id.btn_star) }
    private val mWatchAd by lazy { findViewById<LinearLayoutCompat>(R.id.btn_watch_ad) }
    private val mRewardBtn by lazy { findViewById<AppCompatButton>(R.id.btn_reward) }

    private val mRewardDialog by lazy { RewardDialog(this) }

    private var mTTAdNative: TTAdNative? = null
    private var mFullScreenAd: TTFullScreenVideoAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer_support)

        initView()
        initListener()
        initTTAd()
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
        mWatchAd.setOnClickListener {
            showAd()
        }
        mRewardBtn.setOnClickListener {
            mRewardDialog.show()
        }
    }

    private fun initTTAd() {
        mTTAdNative = TTAdSdk.getAdManager().createAdNative(this)
        loadAd()
    }

    private fun loadAd(onVideoCached: ((ad: TTFullScreenVideoAd) -> Unit)? = { ad -> mFullScreenAd = ad }) {
        val adSlot = AdSlot.Builder()
            .setCodeId(Constants.TTAdConst.TT_AD_FULL_CODE_ID)
            .setSupportDeepLink(true)
            .setAdLoadType(TTAdLoadType.PRELOAD) // 推荐使用，用于标注此次的广告请求用途为预加载（当做缓存）还是实时加载，方便后续为开发者优化相关策略
            .build()
        mTTAdNative?.loadFullScreenVideoAd(
            adSlot,
            object : TTAdNative.FullScreenVideoAdListener {
                override fun onError(code: Int, message: String) {
                }

                override fun onFullScreenVideoAdLoad(ad: TTFullScreenVideoAd) {
                }

                override fun onFullScreenVideoCached() {
                }

                override fun onFullScreenVideoCached(ad: TTFullScreenVideoAd) {
                    onVideoCached?.invoke(ad)
                }
            }
        )
    }

    private fun showAd() {
        if (mFullScreenAd != null) {
            mFullScreenAd!!.showFullScreenVideoAd(this)
            mFullScreenAd = null
        } else {
            loadAd { ad ->
                ad.showFullScreenVideoAd(this)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (mFullScreenAd != null) {
            mFullScreenAd = null
        }
    }
}
