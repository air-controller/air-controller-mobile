package com.youngfeng.android.assistant.socket.heartbeat

import android.os.SystemClock
import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.youngfeng.android.assistant.model.HeartBeat
import timber.log.Timber
import java.io.IOException
import java.net.Socket
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

interface HeartbeatClient {
    fun disconnect()

    companion object {
        fun create(
            socket: Socket,
            config: HeartbeatConfig,
            onTimeout: (client: HeartbeatClient) -> Unit,
            onPeerReset: (client: HeartbeatClient) -> Unit
        ) = HeartbeatClientImpl(socket, config, onTimeout, onPeerReset)
    }
}

class HeartbeatClientImpl(
    private val socket: Socket,
    config: HeartbeatConfig,
    private val onTimeout: (client: HeartbeatClient) -> Unit,
    private val onPeerReset: (client: HeartbeatClient) -> Unit
) : HeartbeatClient {
    private val mExecutors by lazy {
        val executor = ThreadPoolExecutor(
            4, 8,
            Long.MAX_VALUE, TimeUnit.SECONDS,
            SynchronousQueue()
        )
        executor.allowCoreThreadTimeOut(false)
        executor
    }
    private val mGson by lazy { Gson() }

    // 记录上次响应客户端的时间
    private var mLastTimeToRespond = -1L

    // 超时等待时间
    private var mTimeout = if (config.allowRetry) config.retryWaitTimeInMills.toLong() else config.timeoutInMills.toLong()
    private val mTimer by lazy { Timer() }
    private val mCountDownTask by lazy {
        object : TimerTask() {
            override fun run() {
                currentWaitTime += 1000

                Timber.d("currentWaitTime: $currentWaitTime")

                if (currentWaitTime >= mTimeout) {
                    onTimeout.invoke(this@HeartbeatClientImpl)
                    disconnect()
                }
            }
        }
    }
    private var isCountDownStarted = false
    private var currentWaitTime = 0

    init {
        Timber.d("Timeout duration: $mTimeout")

        mExecutors.submit {
            try {
                val inputStream = socket.getInputStream()
                val buffer = ByteArray(1024)

                var bytesRead = inputStream.read(buffer)

                while (bytesRead != -1) {
                    resetCountDownTimer()
                    handleMessage(String(buffer.slice(0 until bytesRead).toByteArray()))

                    bytesRead = inputStream.read(buffer)
                }

                onPeerReset.invoke(this)
                disconnect()
            } catch (e: IOException) {
                Timber.e("init, read input stream exception, message: ${e.message}")
                onPeerReset.invoke(this)
            }
        }
    }

    private fun startCountDownTimer() {
        if (!isCountDownStarted) {
            mTimer.schedule(mCountDownTask, 0, 1000)
            isCountDownStarted = true
        }
    }

    private fun resetCountDownTimer() {
        currentWaitTime = 0
        Timber.d("resetCountDownTimer!")
    }

    private fun stopCountDownTimer() {
        if (isCountDownStarted) {
            mTimer.cancel()
        }
    }

    private fun handleMessage(msg: String) {
        Timber.d("handleMessage, msg: $msg")

        mExecutors.submit {
            try {
                val heartBeat = mGson.fromJson(msg, HeartBeat::class.java)
                val respondHeartBeat = HeartBeat(
                    ip = socket.inetAddress.hostName, value = heartBeat.value + 1,
                    time = System.currentTimeMillis()
                )

                sendToClient(mGson.toJson(respondHeartBeat))

                startCountDownTimer()
                mLastTimeToRespond = SystemClock.uptimeMillis()
            } catch (e: JsonIOException) {
                Timber.e("handleMessage, message: ${e.message}")
            }
        }
    }

    private fun sendToClient(msg: String) {
        mExecutors.submit {
            Timber.d("sendToClient, $msg")
            try {
                val outputStream = socket.getOutputStream();
                outputStream.write(msg.toByteArray())
                outputStream.flush()
            } catch (e: IOException) {
                Timber.e("init, write to output stream exception, message: ${e.message}")
            }
        }
    }

    override fun disconnect() {
        stopCountDownTimer()

        try {
            socket.close()
            mExecutors.shutdownNow()
        } catch (e: Exception) {
            Timber.e("disconnect, ${e.message}")
        }
    }
}
