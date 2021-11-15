package com.youngfeng.android.assistant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yanzhenjie.andserver.AndServer
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val mServer by lazy(mode = LazyThreadSafetyMode.NONE) {
        AndServer.webServer(this)
            .port(8081)
            .timeout(10, TimeUnit.SECONDS)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mServer.startup()
    }

    override fun onDestroy() {
        super.onDestroy()
        mServer.shutdown()
    }
}