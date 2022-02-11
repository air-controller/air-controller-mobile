package com.youngfeng.android.assistant.socket

import android.util.Log
import com.google.gson.Gson
import com.youngfeng.android.assistant.Constants
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.model.Command

class CmdSocketServer(private val application: MobileAssistantApplication) {
    private val mSocketServer by lazy {
        SimpleSocketServer(Constants.Port.CMD_SERVER)
    }
    private val mGson by lazy {
        Gson()
    }
    private var onCommandReceive: ((cmd: Command<Any>) -> Unit)? = null
    var onOpen: (() -> Unit)? = null

    init {
        mSocketServer.onStartComplete {
            Log.d(TAG, "CmdSocketServer start complete.")
            onOpen?.invoke()
        }

        mSocketServer.onStartFail {
            Log.d(TAG, "CmdSocketServer start fail, error: $it")
        }

        mSocketServer.onMessage { _, data ->
            Log.e("@@@", "xxxxx, $data")
            application.runOnUiThread {
                val str = String(data)
                Log.e("@@@", "yyyyyy, $str")

                Log.d(TAG, "Message received: $str")
                val command = mGson.fromJson<Command<Any>>(str, Command::class.java)
                onCommandReceive?.invoke(command)
            }
        }

        mSocketServer.onStopComplete {
            Log.d(TAG, "CmdSocketServer stop complete.")
        }
    }

    companion object {
        private val instance = CmdSocketServer(MobileAssistantApplication.getInstance())
        private val TAG = CmdSocketServer::class.simpleName

        fun getInstance() = instance
    }

    fun start() {
        mSocketServer.start()
    }

    fun <T> sendCmd(cmd: Command<T>) {
        val json = mGson.toJson(cmd)
        mSocketServer.sendToAllClient(json.toByteArray())
    }

    fun onCommandReceive(callback: (cmd: Command<Any>) -> Unit) {
        onCommandReceive = callback
    }

    fun disconnect() {
        mSocketServer.disconnect()
    }

    fun isStarted() = mSocketServer.isStarted()

    fun stop() {
        mSocketServer.stop()
    }
}
