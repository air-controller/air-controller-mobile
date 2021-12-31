package com.youngfeng.android.assistant.socket

import android.util.Log
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

/**
 * 单客户端版本Socket Server，仅支持单一客户端连接，多个客户端连接将直接断开。
 *
 * @author Scott Smith 2021/12/29 16:13
 */
class SingleClientSocketServer(private val port: Int) {
    private val mExecutorService = Executors.newSingleThreadExecutor()
    private var mClient: Socket? = null
    private var mServerSocket: ServerSocket? = null
    private var isStarted = false
    private var onStartComplete: ((server: ServerSocket) -> Unit)? = null
    private var onStartFail: ((error: String) -> Unit)? = null
    private var onMessage: ((client: Socket, data: ByteArray) -> Unit)? = null
    private var onStopComplete: (() -> Unit)? = null
    private var clientIsConnected = false
    private var onConnected: ((client: Socket) -> Unit)? = null

    companion object {
        private val TAG = SingleClientSocketServer::class.simpleName
    }

    fun start() {
        mExecutorService.submit {
            try {
                mServerSocket = ServerSocket(port)
                onStartComplete?.invoke(mServerSocket!!)
                isStarted = true

                while (isStarted) {
                    if (null == mClient) {
                        mClient = mServerSocket!!.accept()
                        clientIsConnected = true
                        mClient?.apply {
                            onConnected?.invoke(this)
                            val inputStream = this.getInputStream()
                            val data = ByteArray(size = inputStream.available())
                            inputStream.read(data)

                            onMessage?.invoke(this, data)
                        }
                    } else {
                        Log.e(TAG, "There is already a client connected, this connection will be discarded")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                onStartFail?.invoke(e.message ?: "Unknown error")
            }
        }
    }

    fun stop() {
        try {
            mServerSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "Stop server socket cause error: ${e.message}")
        }
        mExecutorService.shutdown()
        isStarted = false
        onStopComplete?.invoke()
    }

    fun sendToClient(data: ByteArray) {
        if (!isStarted) {
            throw IllegalAccessException("Call start() first please")
        }

        mExecutorService.submit {
            mClient?.apply {
                this.getOutputStream().apply {
                    write(data)
                    flush()
                }
            }
        }
    }

    fun markClientDisconnected() {
        mClient?.close()
        mClient = null
        clientIsConnected = false
    }

    fun onStartComplete(callback: (server: ServerSocket) -> Unit) {
        this.onStartComplete = callback
    }

    fun onStartFail(callback: (error: String) -> Unit) {
        this.onStartFail = callback
    }

    fun onMessage(callback: (client: Socket, data: ByteArray) -> Unit) {
        this.onMessage = callback
    }

    fun onStopComplete(callback: () -> Unit) {
        this.onStopComplete = callback
    }

    fun onConnected(callback: (client: Socket) -> Unit) {
        this.onConnected = callback
    }

    fun isStarted(): Boolean {
        return isStarted
    }
}
