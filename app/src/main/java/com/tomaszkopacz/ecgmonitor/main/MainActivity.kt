package com.tomaszkopacz.ecgmonitor.main

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.tomaszkopacz.ecgmonitor.ble.BleClient
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), BLEView {

    private var bleClient: BleClient = BleClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.tomaszkopacz.ecgmonitor.R.layout.activity_main)

        bleClient.onCreate(this, this)
        setListeners()
    }

    private fun setListeners() {
        startBleScanButton.setOnClickListener {
            startScanBleDevice()
        }

        connectDeviceButton.setOnClickListener {
            connectBleDevice()
        }
    }

    private fun startScanBleDevice() {
        bleClient.startScanBleDevices()
    }

    private fun connectBleDevice() {
        bleClient.connectDevice()
    }

    override fun notifyBleDevice(device: BluetoothDevice?) {
        scannedDeviceTV.text = if (device != null) device.name else "NO DEVICE"
    }

    override fun notifyBleConnection(connected: Boolean) {
        connectedTV.text = if (connected) "CONNECTED" else "DISCONNECTED"
    }

    override fun notifyBleValue(value: Int) {
        valueTV.text = value.toString()
    }
}
