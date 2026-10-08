package com.vauth.foxyvpn.vpn.tun

import com.vauth.foxyvpn.data.AppLogger

private const val TAG = "HevSocks5Tunnel"

object HevSocks5Tunnel {
    private external fun TProxyStartService(config_path: String, fd: Int): Boolean
    private external fun TProxyStopService(): Boolean
    private external fun TProxyIsRunning(): Boolean
    private external fun TProxyGetStats(): LongArray

    private var isLoaded: Boolean = false

    init {
        try {
            System.loadLibrary("hev-socks5-tunnel")
            isLoaded = true
        } catch (t: Throwable) {
            AppLogger.w(TAG, "hev-socks5-tunnel native library not loaded: ${t.message}")
            isLoaded = false
        }
    }

    fun start(configPath: String, tunFd: Int): Boolean =
        if (isLoaded) {
            runCatching { TProxyStartService(configPath, tunFd) }
                .onFailure { AppLogger.e(TAG, "failed to start hev-socks5-tunnel", it) }
                .getOrDefault(false)
        } else false

    fun stop(): Boolean =
        if (isLoaded) {
            runCatching { TProxyStopService() }
                .onFailure { AppLogger.w(TAG, "failed to stop hev-socks5-tunnel", it) }
                .getOrDefault(false)
        } else false

    fun isRunning(): Boolean =
        if (isLoaded) {
            runCatching { TProxyIsRunning() }.getOrDefault(false)
        } else false

    fun stats(): LongArray =
        if (isLoaded) {
            runCatching { TProxyGetStats() }.getOrDefault(LongArray(4))
        } else LongArray(4)
}

