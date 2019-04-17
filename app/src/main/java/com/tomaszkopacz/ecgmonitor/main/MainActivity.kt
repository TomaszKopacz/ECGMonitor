package com.tomaszkopacz.ecgmonitor.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.tomaszkopacz.ecgmonitor.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_ENABLE_BLE = 100
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        initBle()

        setObservers()
        setListeners()
    }

    private fun initBle() {
        val bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bleAdapter = bleManager.adapter
        val bleHandler = Handler(Handler.Callback { true })

        if (bleAdapter.isEnabled)
            viewModel.init(bleAdapter, bleHandler)
        else
            makeBleEnableIntent()
    }

    private fun setObservers() {
        viewModel.device.observe(this, Observer {
            Log.i("ECGMonitor", "Device discovered: " + it!!.name)
        })
    }

    private fun setListeners() {
        startBleScanButton.setOnClickListener {
            viewModel.scanBleDevice()
        }
    }

    private fun makeBleEnableIntent() {
        val enableBleIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBleIntent, REQUEST_ENABLE_BLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            REQUEST_ENABLE_BLE -> {
                initBle()
            }
        }
    }
}
