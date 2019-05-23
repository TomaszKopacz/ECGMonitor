package com.tomaszkopacz.ecgmonitor.main

import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.common.primitives.Ints
import com.tomaszkopacz.ecgmonitor.ble.BLE
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.experimental.and


class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_ENABLE_BLE = 100

        private const val HR_SERVICE_UUID = "22c49ea5-3952-4d43-9f8c-dbe0adb66426"
        private const val HRM_CHARACTERISTIC_UUID = "fefcab54-ad43-4d9c-948b-11e83f8d4b35"
        private const val HRM_DESCRIPTOR_UUID = "5093d1ee-65e1-4345-8e73-9751697b5444"
    }

    private var ble: BLE? = null
    private var device: BluetoothDevice? = null
    private var gatt: BluetoothGatt? = null

    // when activity starts running
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.tomaszkopacz.ecgmonitor.R.layout.activity_main)

        initBle()

        setListeners()
    }

    // init bluetooth configuration
    private fun initBle() {
        val bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bleAdapter = bleManager.adapter
        val bleHandler = Handler(Handler.Callback { true })

        if (bleAdapter.isEnabled)
        // if bluetooth is enabled create BLE object
            this.ble = BLE(bleAdapter, bleHandler)
        else
        // if bluetooth is not enabled request user to turn it on
            makeBleEnableIntent()
    }

    // start intent to turn on bluetooth
    private fun makeBleEnableIntent() {
        val enableBleIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBleIntent, REQUEST_ENABLE_BLE)
    }

    // listen to gui buttons
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

    // callback for devices scan
    private val bleScanCallback = BluetoothAdapter.LeScanCallback { device, _, _ ->
        this.device = device
        Log.i("ECGMonitor", "Device discovered: " + device.name)
        stopScanBleDevice()
    }

    private fun connectBleDevice() {
        if (this.device != null)
            this.gatt = this.device!!.connectGatt(this, false, bleGattCallback)
    }


    // callback for device connection and ble status
    private val bleGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {

                    // when connected, search for available services
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

                    // list services
                    Log.i("ECGMonitor", "SERVICES DISCOVERED")
                    for (service in gatt!!.services)
                        Log.i("ECGMonitor", "service: " + service.uuid)

                    // get our service (HEART RATE)
                    val service = gatt.getService(UUID.fromString(HR_SERVICE_UUID))

                    // list characteristics
                    Log.i("ECGMonitor", "CHARACTERISTICS DISCOVERED")
                    for (characteristic in service.characteristics)
                        Log.i("ECGMonitor", "characteristic: " + characteristic.uuid)

                    // get our characteristic (HEART RATE MEASUREMENT)
                    val characteristic =
                        service.getCharacteristic(UUID.fromString(HRM_CHARACTERISTIC_UUID))

                    // list descriptors
                    Log.i("ECGMonitor", "DESCRIPTORS DISCOVERED")
                    for (descriptor in characteristic.descriptors)
                        Log.i("ECGMonitor", "descriptor: " + descriptor.uuid)

                    // get descriptor of characteristic
                    val descriptor =
                        characteristic.getDescriptor(UUID.fromString(HRM_DESCRIPTOR_UUID))

                    // enable notifications on this characteristic
                    gatt.setCharacteristicNotification(characteristic, true)
                    descriptor.apply {
                        value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    }
                    gatt.writeDescriptor(descriptor)
                }

                BluetoothGatt.GATT_FAILURE -> {
                }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            if(UUID.fromString(HRM_DESCRIPTOR_UUID) == descriptor!!.uuid) {
                val characteristic = gatt!!
                    .getService(UUID.fromString(HR_SERVICE_UUID))
                    .getCharacteristic(UUID.fromString(HRM_CHARACTERISTIC_UUID))
                gatt.readCharacteristic(characteristic)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (UUID.fromString(HRM_CHARACTERISTIC_UUID) == characteristic!!.uuid) {
                val data = characteristic.value
                val value = Ints.fromByteArray(data)
                Log.i("ECGMonitor", "Value: $value")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            if (UUID.fromString(HRM_CHARACTERISTIC_UUID) == characteristic!!.uuid) {
                val data = characteristic.value
                val value = Ints.fromByteArray(data)
                Log.i("ECGMonitor", "Value: $value")
            }
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
