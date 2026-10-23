package com.vauth.foxyvpn.vpn.tun

import com.vauth.foxyvpn.data.AppLogger

private const val TAG = "HevSocks5Tunnel"

object HevSocks5Tunnel {
    @Volatile
    private var isTunnelRunning = false

    fun start(configPath: String, tunFd: Int): Boolean {
        AppLogger.i(TAG, "hev-socks5-tunnel start requested (fd=$tunFd, config=$configPath)")
        isTunnelRunning = true
        return true
    }

    fun stop(): Boolean {
        AppLogger.i(TAG, "hev-socks5-tunnel stop requested")
        isTunnelRunning = false
        return true
    }

    fun isRunning(): Boolean = isTunnelRunning

    fun stats(): LongArray = LongArray(4)
}
