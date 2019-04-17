package com.tomaszkopacz.ecgmonitor.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Handler
import com.tomaszkopacz.ecgmonitor.ble.BLE

class MainViewModel : ViewModel() {

    private var ble: BLE? = null

    val device = MutableLiveData<BluetoothDevice>()

    fun init(adapter: BluetoothAdapter, handler: Handler) {
        this.ble = BLE(adapter, handler)
    }

    fun scanBleDevice() {
        if (ble != null)
            ble!!.startScanBleDevices(bleScanCallback)
    }

    fun stopScanBleDevice() {
        if (ble != null)
            ble!!.stopScanLeDevices(bleScanCallback)
    }

    private val bleScanCallback = BluetoothAdapter.LeScanCallback { device, _, _ ->
        this.device.value = device
        stopScanBleDevice()
    }
}