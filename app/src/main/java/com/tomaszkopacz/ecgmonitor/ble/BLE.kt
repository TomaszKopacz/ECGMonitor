package com.tomaszkopacz.ecgmonitor.ble

import android.bluetooth.BluetoothAdapter
import android.os.Handler

class BLE(private val adapter: BluetoothAdapter, private val handler: Handler) {

    companion object {
        private const val SCAN_PERIOD: Long = 5000
    }

    fun startScanBleDevices(callback: BluetoothAdapter.LeScanCallback) {
        handler.postDelayed({
            adapter.stopLeScan(callback)
        }, SCAN_PERIOD)

        adapter.startLeScan(callback)
    }

    fun stopScanLeDevices(callback: BluetoothAdapter.LeScanCallback) {
        adapter.stopLeScan(callback)
    }
}
