package com.securescreen.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.securescreen.app.data.AppRepository
import com.securescreen.app.service.ForegroundService

class WatchdogReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val repository = AppRepository(context)
        if (!repository.isServiceEnabled()) return

        ForegroundService.start(context)
        ForegroundService.setProtectionEnabled(context, repository.isProtectionEnabled())
        ForegroundService.scheduleWatchdog(context)
    }
}
