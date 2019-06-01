package com.tomaszkopacz.ecgmonitor.ble

import android.content.Context
import com.tomaszkopacz.ecgmonitor.main.BLEView

interface BleClient {

    fun onCreate(context: Context, view: BLEView)
    fun startScanBleDevices()
    fun connectDevice()
}