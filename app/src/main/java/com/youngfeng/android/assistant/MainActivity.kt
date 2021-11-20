package com.youngfeng.android.assistant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yanzhenjie.andserver.AndServer
import com.youngfeng.android.assistant.manager.DeviceDiscoverManager
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    companion object {
        // 默认超时时间（单位：秒）
        private const val DEFAULT_TIMEOUT = 10
    }

    private val mServer by lazy(mode = LazyThreadSafetyMode.NONE) {
        AndServer.webServer(this)
            .port(Constants.Port.HTTP_SERVER)
            .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mServer.startup()

        DeviceDiscoverManager.getInstance().onDeviceDiscover {
            print("Device: ip => ${it.ipAddress}, name => ${it.name}, platform => ${it.platform}")
        }
        DeviceDiscoverManager.getInstance().startDiscover()
    }

    override fun onDestroy() {
        super.onDestroy()
        mServer.shutdown()
    }
}
