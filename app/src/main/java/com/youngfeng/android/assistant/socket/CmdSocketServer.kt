package com.youngfeng.android.assistant.socket

import android.util.Log
import com.google.gson.Gson
import com.youngfeng.android.assistant.Constants
import com.youngfeng.android.assistant.app.MobileAssistantApplication
import com.youngfeng.android.assistant.model.Command
import java.net.Socket

class CmdSocketServer(private val application: MobileAssistantApplication) {
    private val mSocketServer by lazy {
        SingleClientSocketServer(Constants.Port.CMD_SERVER)
    }
    private val mGson by lazy {
        Gson()
    }
    private var onCommandReceive: ((cmd: Command<Any>) -> Unit)? = null
    private var onConnected: ((client: Socket) -> Unit)? = null

    init {
        mSocketServer.onStartComplete {
            Log.d(TAG, "CmdSocketServer start complete.")
        }

        mSocketServer.onStartFail {
            Log.d(TAG, "CmdSocketServer start fail, error: $it")
        }

        mSocketServer.onMessage { _, data ->
            application.runOnUiThread {
                val command = mGson.fromJson<Command<Any>>(String(data), Command::class.java)
                onCommandReceive?.invoke(command)
            }
        }

        mSocketServer.onConnected {
            application.runOnUiThread {
                onConnected?.invoke(it)
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
        mSocketServer.sendToClient(json.toByteArray())
    }

    fun onCommandReceive(callback: (cmd: Command<Any>) -> Unit) {
        onCommandReceive = callback
    }

    fun onConnected(callback: (client: Socket) -> Unit) {
        onConnected = callback
    }

    fun isStarted() = mSocketServer.isStarted()

    fun stop() {
        mSocketServer.stop()
    }
}
