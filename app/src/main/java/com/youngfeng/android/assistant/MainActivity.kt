package com.youngfeng.android.assistant

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.yanzhenjie.andserver.AndServer
import com.youngfeng.android.assistant.manager.DeviceDiscoverManager
import com.youngfeng.android.assistant.model.Command
import com.youngfeng.android.assistant.socket.CmdSocketServer
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

        findViewById<Button>(R.id.btn_test).setOnClickListener {
            val cmd = Command(1, null)
            CmdSocketServer.getInstance().sendCmd(cmd)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mServer.shutdown()
    }
}
