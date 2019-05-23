package com.tomaszkopacz.ecgmonitor.main

import android.bluetooth.BluetoothDevice

interface BLEView {

    fun notifyBleDevice(device: BluetoothDevice)
    fun notifyBleConnection(connected: Boolean)
    fun notifyBleValue(value: Int)
}