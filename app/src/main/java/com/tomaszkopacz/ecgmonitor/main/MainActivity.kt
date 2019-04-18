package com.tomaszkopacz.ecgmonitor.main

import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.tomaszkopacz.ecgmonitor.R
import com.tomaszkopacz.ecgmonitor.ble.BLE
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_ENABLE_BLE = 100
    }

    private var ble: BLE? = null
    private var device: BluetoothDevice? = null
    private var gatt: BluetoothGatt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initBle()

        setListeners()
    }

    private fun initBle() {
        val bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bleAdapter = bleManager.adapter
        val bleHandler = Handler(Handler.Callback { true })

        if (bleAdapter.isEnabled)
            this.ble = BLE(bleAdapter, bleHandler)
        else
            makeBleEnableIntent()
    }

    private fun makeBleEnableIntent() {
        val enableBleIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBleIntent, REQUEST_ENABLE_BLE)
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
        if (ble != null)
            ble!!.startScanBleDevices(bleScanCallback)
    }

    private fun stopScanBleDevice() {
        if (ble != null)
            ble!!.stopScanLeDevices(bleScanCallback)
    }

    private val bleScanCallback = BluetoothAdapter.LeScanCallback { device, _, _ ->
        this.device = device
        Log.i("ECGMonitor", "Device discovered: " + device.name)
        stopScanBleDevice()
    }

    private fun connectBleDevice() {
        if (this.device != null)
            this.gatt = this.device!!.connectGatt(this, false, bleGattCallback)
    }

    private val bleGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i("ECGMonitor", "CONNECTED")
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i("ECGMonitor", "DISCONNECTED")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i("ECGMonitor", "SERVICE DISCOVERED")
                    for (service in gatt!!.services)
                        Log.i("ECGMonitor", "service: " + service.uuid)

                    val service = gatt.getService(UUID.fromString("00001809-0000-1000-8000-00805f9b34fb"))
                    val characteristic =
                        service.getCharacteristic(UUID.fromString("00002a1c-0000-1000-8000-00805f9b34fb"))
                    val descriptor =
                        characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))

                    gatt.setCharacteristicNotification(characteristic, true)
                    descriptor.apply {
                        value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        }
                    gatt.writeDescriptor(descriptor)
                }

                BluetoothGatt.GATT_FAILURE -> {
                }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {

        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {

            val ble = characteristic!!.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 1)
            Log.i("ECGMonitor", "Value: $ble")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            REQUEST_ENABLE_BLE -> {
                initBle()
            }
        }
    }
}
