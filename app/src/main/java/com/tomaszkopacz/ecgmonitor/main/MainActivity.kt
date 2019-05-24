package com.tomaszkopacz.ecgmonitor.main

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.tomaszkopacz.ecgmonitor.ble.BleClient
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), BLEView {

    private var bleClient: BleClient = BleClient()

    private var ecgSeries: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var diff1Series: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var diff2Series: LineGraphSeries<DataPoint> = LineGraphSeries()

    private var timePointer: Double = 0.0
    private var lastEcgValue: Int = 0
    private var lastDiff1Value: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.tomaszkopacz.ecgmonitor.R.layout.activity_main)

        bleClient.onCreate(this, this)
        setGraphs()
        setListeners()
    }

    private fun setGraphs() {
        setEcgGraph()
        setDiff1Graph()
        setDiff2Graph()
    }

    private fun setEcgGraph() {
        ecgGraph.title = "EKG"

        ecgGraph.viewport.isScalable = false
        ecgGraph.viewport.isScrollable = true

        ecgGraph.viewport.setScalableY(false)
        ecgGraph.viewport.setScrollableY(false)

        ecgGraph.viewport.isXAxisBoundsManual = true
        ecgGraph.viewport.setMinX(0.toDouble())
        ecgGraph.viewport.setMaxX(10.toDouble())

        ecgGraph.viewport.isYAxisBoundsManual = true
        ecgGraph.viewport.setMinY(450.toDouble())
        ecgGraph.viewport.setMaxY(900.toDouble())

        ecgSeries.color = Color.RED
        ecgGraph.addSeries(ecgSeries)
    }

    private fun setDiff1Graph() {
        diff1Graph.title = "Pierwsza pochodna"

        diff1Graph.viewport.isScalable = false
        diff1Graph.viewport.isScrollable = true

        diff1Graph.viewport.setScalableY(false)
        diff1Graph.viewport.setScrollableY(false)

        diff1Graph.viewport.isXAxisBoundsManual = true
        diff1Graph.viewport.setMinX(0.toDouble())
        diff1Graph.viewport.setMaxX(10.toDouble())

        diff1Graph.viewport.isYAxisBoundsManual = true
        diff1Graph.viewport.setMinY((-500).toDouble())
        diff1Graph.viewport.setMaxY(500.toDouble())

        diff1Graph.addSeries(diff1Series)
    }

    private fun setDiff2Graph() {
        diff2Graph.title = "Druga pochodna"

        diff2Graph.viewport.isScalable = false
        diff2Graph.viewport.isScrollable = true

        diff2Graph.viewport.setScalableY(false)
        diff2Graph.viewport.setScrollableY(false)

        diff2Graph.viewport.isXAxisBoundsManual = true
        diff2Graph.viewport.setMinX(0.toDouble())
        diff2Graph.viewport.setMaxX(10.toDouble())

        diff2Graph.viewport.isYAxisBoundsManual = true
        diff2Graph.viewport.setMinY((-500).toDouble())
        diff2Graph.viewport.setMaxY(500.toDouble())

        diff2Graph.addSeries(diff2Series)
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

    var startTime = (-1).toLong()

    override fun notifyBleValue(value: Int) {

        if (startTime == (-1).toLong()) {
            startTime = System.currentTimeMillis()
        }

        val currentTime = (System.currentTimeMillis() - startTime) * 0.001

        val point = DataPoint(currentTime, value.toDouble())
        ecgSeries.appendData(point, true, 1000)

        val currentDiff1 = (value - lastEcgValue).toDouble()
        val diff1Point = DataPoint(currentTime, currentDiff1)
        diff1Series.appendData(diff1Point, true, 1000)

        val currentDiff2 = (currentDiff1 - lastDiff1Value)
        val diff2Point = DataPoint(currentTime, currentDiff2)
        diff2Series.appendData(diff2Point, true, 1000)

        lastEcgValue = value
        lastDiff1Value = currentDiff1.toInt()
        timePointer++
    }
}
