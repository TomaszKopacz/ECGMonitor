package com.tomaszkopacz.ecgmonitor.ble

import android.app.Activity
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import com.tomaszkopacz.ecgmonitor.main.BLEView
import java.util.*


class BleNordicClient : BleClient{

    companion object {
        private const val SCAN_PERIOD: Long = 5000

        private const val NORDIC_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
        private const val TX_CHARACTERISTIC_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
        private const val RX_CHARACTERISTIC_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"
        private const val TX_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"
    }

    private lateinit var context: Context
    private lateinit var view: BLEView
    private lateinit var bleManager: BluetoothManager
    private lateinit var bleAdapter: BluetoothAdapter
    private lateinit var bleHandler: Handler

    private var device: BluetoothDevice? = null
    private var gatt: BluetoothGatt? = null

    override fun onCreate(context: Context, view: BLEView) {
        this.context = context
        this.view = view
        this.bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        this.bleAdapter = bleManager.adapter
        this.bleHandler = Handler(Handler.Callback { true })

        if (!checkBluetoothSupport(bleAdapter)) {
            throw RuntimeException("GATT server requires Bluetooth support")
        }

        registerForBluetoothEvents()
    }

    private fun checkBluetoothSupport(bluetoothAdapter: BluetoothAdapter?): Boolean {
        if (bluetoothAdapter == null) {
            Log.w("ECGMonitor", "Bluetooth is not supported")
            return false
        }

        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.w("ECGMonitor", "Bluetooth LE is not supported")
            return false
        }

        return true
    }

    private fun registerForBluetoothEvents() {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(mBluetoothReceiver, filter)

        if (!bleAdapter.isEnabled) {
            Log.d("ECGMonitor", "Bluetooth is currently disabled... enabling")
            bleAdapter.enable()
        }
    }

    private val mBluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)

            when (state) {
                BluetoothAdapter.STATE_ON -> {

                }

                BluetoothAdapter.STATE_OFF -> {

                }

                else -> {
                }
            }
        }
    }

    override fun startScanBleDevices() {
        bleHandler.postDelayed({
            bleAdapter.stopLeScan(bleScanCallback)
        }, SCAN_PERIOD)

        bleAdapter.startLeScan(bleScanCallback)
    }

    private fun stopScanLeDevices() {
        bleAdapter.stopLeScan(bleScanCallback)
    }

    private val bleScanCallback = BluetoothAdapter.LeScanCallback { device, _, _ ->
        this.device = device
        Log.i("ECGMonitor", "Device discovered: " + device.name)
        stopScanLeDevices()

        view.notifyBleDevice(device)
    }

    override fun connectDevice() {
        if (this.device != null)
            this.gatt = this.device!!.connectGatt(context, false, bleGattCallback)
    }

    private val bleGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {

                    Log.i("ECGMonitor", "CONNECTED")

                    (context as Activity).runOnUiThread {
                        view.notifyBleConnection(true)
                    }

                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i("ECGMonitor", "DISCONNECTED")

                    device = null

                    (context as Activity).runOnUiThread {
                        view.notifyBleConnection(false)
                        view.notifyBleDevice(device)
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {

                    logServices()
                    val service = gatt!!.getService(UUID.fromString(NORDIC_SERVICE_UUID))

                    logCharacteristics(service)
                    val characteristic = service.getCharacteristic(UUID.fromString(TX_CHARACTERISTIC_UUID))

                    logDescriptors(characteristic)

                    enableNotificationToCharacteristic(characteristic)

                }

                BluetoothGatt.GATT_FAILURE -> {

                }
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            if(UUID.fromString(TX_DESCRIPTOR_UUID) == descriptor!!.uuid) {
                requestReadCharacteristic()
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            readCharacteristic(characteristic)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            readCharacteristic(characteristic)
        }
    }

    private fun logServices() {
        Log.i("ECGMonitor", "SERVICES DISCOVERED")
        for (service in gatt!!.services)
            Log.i("ECGMonitor", "service: " + service.uuid)
    }

    private fun logCharacteristics(service: BluetoothGattService) {
        Log.i("ECGMonitor", "CHARACTERISTICS DISCOVERED")
        for (characteristic in service.characteristics)
            Log.i("ECGMonitor", "characteristic: " + characteristic.uuid)
    }

    private fun logDescriptors(characteristic: BluetoothGattCharacteristic?) {
        Log.i("ECGMonitor", "DESCRIPTORS DISCOVERED")
        for (descriptor in characteristic!!.descriptors)
            Log.i("ECGMonitor", "descriptor: " + descriptor.uuid)
    }

    private fun enableNotificationToCharacteristic(characteristic: BluetoothGattCharacteristic?) {
//        val descriptor = characteristic!!.getDescriptor(UUID.fromString(HRM_DESCRIPTOR_UUID))
        val descriptor = characteristic!!.getDescriptor(UUID.fromString(TX_DESCRIPTOR_UUID))

        gatt!!.setCharacteristicNotification(characteristic, true)
        descriptor.apply {
            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }

        gatt!!.writeDescriptor(descriptor)
    }

    private fun requestReadCharacteristic() {
        val characteristic = gatt!!
            .getService(UUID.fromString(NORDIC_SERVICE_UUID))
            .getCharacteristic(UUID.fromString(TX_CHARACTERISTIC_UUID))
        gatt!!.readCharacteristic(characteristic)
    }

    private fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (UUID.fromString(TX_CHARACTERISTIC_UUID) == characteristic!!.uuid) {
            val data1 = characteristic.value[0].toInt()
            val data2 = characteristic.value[1].toInt()
            Log.i("ECGMonitor", "Value1: $data1, Value2: $data2")

            (context as Activity).runOnUiThread {
                view.notifyBleValue(data1)
            }
        }
    }
}
