package com.securescreen.app.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.securescreen.app.data.AppRepository

@RequiresApi(Build.VERSION_CODES.N)
class SecureTileService : TileService() {

    private lateinit var repository: AppRepository

    override fun onCreate() {
        super.onCreate()
        repository = AppRepository(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val currentState = repository.isProtectionEnabled()
        val newState = !currentState
        
        // Toggle the state via ForegroundService which also handles the repository and service lifecycle
        ForegroundService.setProtectionEnabled(this, newState)
        
        // Small delay to allow the service to start/stop before updating UI
        qsTile?.state = if (newState) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile?.updateTile()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isEnabled = repository.isProtectionEnabled()
        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}
