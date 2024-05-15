package com.code.damahe.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow

class PlayerBuilder(private val context: Context) {

    private var mService: PlayerService? = null
    val mBound = MutableStateFlow(false)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as PlayerService.MusicBinder
            mService = binder.getService()
            mBound.value = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mService = null
            mBound.value = false
        }
    }

    fun getService(): PlayerService? = mService

    fun bindService() {
        context.bindService(Intent(context, PlayerService::class.java), connection, Context.BIND_AUTO_CREATE)
        context.startService(Intent(context, PlayerService::class.java))
    }

    fun unBindService() {
        context.unbindService(connection)
        context.stopService(Intent(context, PlayerService::class.java))
    }
}