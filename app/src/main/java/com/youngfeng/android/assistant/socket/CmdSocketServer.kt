package com.youngfeng.android.assistant.socket

import com.google.gson.Gson
import com.youngfeng.android.assistant.Constants
import com.youngfeng.android.assistant.app.AirControllerApp
import com.youngfeng.android.assistant.model.Command
import timber.log.Timber

class CmdSocketServer(private val application: AirControllerApp) {
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
            Timber.d("CmdSocketServer start complete.")
            onOpen?.invoke()
        }

        mSocketServer.onStartFail {
            Timber.d("CmdSocketServer start fail, error: $it")
        }

        mSocketServer.onMessage { _, data ->
            application.runOnUiThread {
                val str = String(data)

                val command = mGson.fromJson<Command<Any>>(str, Command::class.java)
                onCommandReceive?.invoke(command)
            }
        }

        mSocketServer.onStopComplete {
            Timber.d("CmdSocketServer stop complete.")
        }
    }

    companion object {
        private val instance = CmdSocketServer(AirControllerApp.getInstance())

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
