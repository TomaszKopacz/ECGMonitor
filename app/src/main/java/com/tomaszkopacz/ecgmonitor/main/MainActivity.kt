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
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_ENABLE_BLE = 100

        private const val SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
        private const val RX_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"
        private const val TX_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
    }

    private var ble: BLE? = null
    private var device: BluetoothDevice? = null
    private var gatt: BluetoothGatt? = null

    // when activity starts running
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

                    // when services are discovered log their uuids
                    Log.i("ECGMonitor", "SERVICE DISCOVERED")
                    for (service in gatt!!.services)
                        Log.i("ECGMonitor", "service: " + service.uuid)

                    // get our service (ECG)
                    val service = gatt.getService(UUID.fromString(SERVICE_UUID))

                    // get TX characteristic (transmission)
                    val characteristic =
                        service.getCharacteristic(UUID.fromString(TX_UUID))

                    // get descriptor of characteristic
                    val descriptor =
                        characteristic.getDescriptor(UUID.fromString("I dont know that uuid"))

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

        }

        // when new value obtained
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {

            // print value bytes
            val bleValue = characteristic!!.value
            Log.i("ECGMonitor", "Value: $bleValue")

            // convert bytes to number, i don't know if works
            //val bb = ByteBuffer.wrap(byteArray)
            //bb.order(ByteOrder.LITTLE_ENDIAN)
            //while (bb.hasRemaining()) {
            //    val v = bb.short
            //    /* Do something with v... */
            //}
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
