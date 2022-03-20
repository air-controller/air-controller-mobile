package com.youngfeng.android.assistant.socket.heartbeat

import com.youngfeng.android.assistant.Constants
import timber.log.Timber
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.Executors

interface HeartbeatServerPlus {
    fun start()

    fun stop()

    fun isStarted(): Boolean

    fun addListener(listener: HeartbeatListener)

    fun disconnectClient()

    companion object {
        fun create(config: HeartbeatConfig? = null) = HeartbeatServerPlusImpl(config ?: HeartbeatConfig())
    }
}

class HeartbeatServerPlusImpl(private var config: HeartbeatConfig) : HeartbeatServerPlus {
    private var isStarted = false
    private val mSingleExecutors by lazy {
        Executors.newCachedThreadPool()
    }
    private lateinit var mServerSocket: ServerSocket
    private val mListeners = mutableListOf<HeartbeatListener>()
    private val mClients = mutableListOf<HeartbeatClient>()

    override fun start() {
        if (isStarted) {
            Timber.d("Warn: the heartbeat server has started!")
            return
        }

        mSingleExecutors.submit {
            try {
                mServerSocket = ServerSocket(Constants.Port.HEARTBEAT_SERVER)
                mServerSocket.reuseAddress = true

                isStarted = true
                mListeners.forEach { it.onStart() }

                while (isStarted) {
                    if (mClients.isEmpty()) {
                        val socket = mServerSocket.accept()

                        val client = HeartbeatClient.create(socket, config, onTimeout = { client ->
                            mListeners.forEach { it.onClientTimeout(client) }
                            mClients.clear()
                        }) { client ->
                            mClients.remove(client)
                            if (mClients.isEmpty()) {
                                mListeners.forEach { it.onClientDisconnected() }
                            }
                        }
                        mClients.add(client)
                        mListeners.forEach {
                            it.onClientConnected(client)
                        }
                    } else {
                        if (config.strategy == HeartbeatStrategy.KeepConnectWhenNewJoin) {
                            Timber.d("Hit strategy KeepConnectWhenNewJoin, drop!")
                        } else {
                            val socket = mServerSocket.accept()

                            if (mClients.isNotEmpty()) {
                                val oldClient = mClients.single()
                                oldClient.disconnect()
                                mClients.clear()
                            }

                            val client = HeartbeatClient.create(socket, config, onTimeout = { client ->
                                mListeners.forEach { it.onClientTimeout(client) }
                                mClients.clear()
                            }) { client ->
                                mClients.remove(client)
                                if (mClients.isEmpty()) {
                                    mListeners.forEach { it.onClientDisconnected() }
                                }
                            }
                            mClients.add(client)
                            mListeners.forEach {
                                it.onClientConnected(client)
                            }
                        }
                    }
                }
            } catch (ex: IOException) {
                isStarted = false
                mListeners.forEach { it.onStop() }

                Timber.e("Heartbeat server start failure, error: ${ex.message}")
            }
        }
    }

    override fun stop() {
        if (!isStarted) {
            Timber.d("Warn: the heartbeat server is stopped, you don't need call stop method!")
            return
        }

        mSingleExecutors.shutdownNow()
        isStarted = false
        mListeners.forEach { it.onStop() }
    }

    override fun isStarted(): Boolean {
        return isStarted
    }

    override fun addListener(listener: HeartbeatListener) {
        mListeners.add(listener)
    }

    override fun disconnectClient() {
        if (mClients.isNotEmpty()) {
            mClients.single().disconnect()
            mClients.clear()
        }
    }
}
