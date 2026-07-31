package com.custom.vpn

import android.net.VpnService
import android.content.Intent
import android.os.ParcelFileDescriptor

class XrayVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val builder = Builder()
            .setSession("XrayVPN")
            .addAddress("26.26.26.1", 24)
            .addRoute("0.0.0.0", 0)

        vpnInterface = builder.establish()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        vpnInterface?.close()
    }
}