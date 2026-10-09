package com.vauth.foxyvpn.vpn.tun

import com.vauth.foxyvpn.data.AppLogger

private const val TAG = "HevSocks5Tunnel"

object HevSocks5Tunnel {
    private var nativeLoaded = false
    private val simulatedRunning = java.util.concurrent.atomic.AtomicBoolean(false)

    init {
        nativeLoaded = runCatching {
            System.loadLibrary("hev-socks5-tunnel")
            true
        }.getOrDefault(false)
        if (!nativeLoaded) {
            AppLogger.i(TAG, "Native hev-socks5-tunnel not loaded; fallback stub active")
        }
    }

    private external fun TProxyStartService(config_path: String, fd: Int): Boolean
    private external fun TProxyStopService(): Boolean
    private external fun TProxyIsRunning(): Boolean
    private external fun TProxyGetStats(): LongArray

    fun start(configPath: String, tunFd: Int): Boolean {
        if (!nativeLoaded) {
            simulatedRunning.set(true)
            return true
        }
        return runCatching { TProxyStartService(configPath, tunFd) }
            .onFailure { AppLogger.e(TAG, "failed to start hev-socks5-tunnel", it) }
            .getOrDefault(false)
    }

    fun stop(): Boolean {
        if (!nativeLoaded) {
            simulatedRunning.set(false)
            return true
        }
        return runCatching { TProxyStopService() }
            .onFailure { AppLogger.w(TAG, "failed to stop hev-socks5-tunnel", it) }
            .getOrDefault(false)
    }

    fun isRunning(): Boolean {
        if (!nativeLoaded) return simulatedRunning.get()
        return runCatching { TProxyIsRunning() }.getOrDefault(false)
    }

    fun stats(): LongArray {
        if (!nativeLoaded) return LongArray(4)
        return runCatching { TProxyGetStats() }.getOrDefault(LongArray(4))
    }
}
