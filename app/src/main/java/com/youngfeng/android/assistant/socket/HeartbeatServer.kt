package com.youngfeng.android.assistant.socket

import android.util.Log
import com.google.gson.Gson
import com.youngfeng.android.assistant.Constants
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.model.HeartBeat

class HeartbeatServer(private val application: MobileAssistantApplication) {
    private val mSocketServer by lazy {
        SimpleSocketServer(Constants.Port.HEARTBEAT_SERVER)
    }
    private val mGson by lazy {
        Gson()
    }
    var onDeviceConnected: (() -> Unit)? = null
    var onDeviceDisconnected: (() -> Unit)? = null

    companion object {
        private val instance = HeartbeatServer(MobileAssistantApplication.getInstance())
        private val TAG = HeartbeatServer::class.simpleName

        fun getInstance() = instance
    }

    init {
        mSocketServer.onStartComplete {
            Log.d(TAG, "HeartbeatServer start complete.")
        }

        mSocketServer.onStartFail {
            Log.d(TAG, "HeartbeatServer start fail, error: $it")
        }

        mSocketServer.onMessage { _, data ->
            kotlin.runCatching {
                val str = String(data)
                Log.d(TAG, "onMessage, str: $str")
                mGson.fromJson(str, HeartBeat::class.java)
            }.onSuccess {
                val ip = mSocketServer.getHostname() ?: "Unknown ip"
                val heartBeat = HeartBeat(ip = ip, value = it.value, time = System.currentTimeMillis())
                mSocketServer.sendToAllClient(mGson.toJson(heartBeat).toByteArray())
                Log.d(TAG, "HeartbeatServer, response to client")
            }.onFailure {
                Log.e(TAG, "It's not a valid heartbeat data, error: ${it.message}")
            }
        }

        mSocketServer.onStopComplete {
            Log.d(TAG, "HeartbeatServer stop complete.")
        }

        mSocketServer.onClientConnect = {
            onDeviceConnected?.invoke()
        }

        mSocketServer.onClientDisconnect = {
            onDeviceDisconnected?.invoke()
        }
    }

    fun disconnect() {
        mSocketServer.disconnect()
    }

    fun start() {
        mSocketServer.start()
    }

    fun stop() {
        mSocketServer.stop()
    }
}
