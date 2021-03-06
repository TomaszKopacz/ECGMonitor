package com.tomaszkopacz.ecgmonitor.main

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.tomaszkopacz.ecgmonitor.R
import com.tomaszkopacz.ecgmonitor.analysis.AnalysisActivity
import com.tomaszkopacz.ecgmonitor.ble.BleClient
import com.tomaszkopacz.ecgmonitor.ble.BleNordicClient
import com.tomaszkopacz.ecgmonitor.constants.EcgGraphs
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), BLEView {

    private var bleClient: BleClient = BleNordicClient()

    private var ecgSeries: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var diff1Series: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var diff2Series: LineGraphSeries<DataPoint> = LineGraphSeries()

    private var currentFile: File? = null
    private var MAX_ARRAY_SIZE = 100
    private var ecgData = Array(MAX_ARRAY_SIZE) {DoubleArray(4)}

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId

        if(id == R.id.action_analysis) {
            val intent = Intent(this, AnalysisActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
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
        ecgGraph.viewport.setMinX(EcgGraphs.RealEcg.MIN_X)
        ecgGraph.viewport.setMaxX(EcgGraphs.RealEcg.MAX_X)

        ecgGraph.viewport.isYAxisBoundsManual = true
        ecgGraph.viewport.setMinY(EcgGraphs.RealEcg.MIN_ECG_Y)
        ecgGraph.viewport.setMaxY(EcgGraphs.RealEcg.MAX_ECG_Y)

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
        diff1Graph.viewport.setMinX(EcgGraphs.RealEcg.MIN_X)
        diff1Graph.viewport.setMaxX(EcgGraphs.RealEcg.MAX_X)

        diff1Graph.viewport.isYAxisBoundsManual = true
        diff1Graph.viewport.setMinY(EcgGraphs.RealEcg.MIN_DIFF1_Y)
        diff1Graph.viewport.setMaxY(EcgGraphs.RealEcg.MAX_DIFF1_Y)

        diff1Graph.addSeries(diff1Series)
    }

    private fun setDiff2Graph() {
        diff2Graph.title = "Druga pochodna"

        diff2Graph.viewport.isScalable = false
        diff2Graph.viewport.isScrollable = true

        diff2Graph.viewport.setScalableY(false)
        diff2Graph.viewport.setScrollableY(false)

        diff2Graph.viewport.isXAxisBoundsManual = true
        diff2Graph.viewport.setMinX(EcgGraphs.RealEcg.MIN_X)
        diff2Graph.viewport.setMaxX(EcgGraphs.RealEcg.MAX_X)

        diff2Graph.viewport.isYAxisBoundsManual = true
        diff2Graph.viewport.setMinY(EcgGraphs.RealEcg.MIN_DIFF2_Y)
        diff2Graph.viewport.setMaxY(EcgGraphs.RealEcg.MAX_DIFF2_Y)

        diff2Graph.addSeries(diff2Series)
    }

    private fun setListeners() {
        startBleScanButton.setOnClickListener {
            scanBleDevice()
        }

        connectDeviceButton.setOnClickListener {
            connectBleDevice()
        }

        stopBleButton.setOnClickListener {

        }
    }

    private fun scanBleDevice() {
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

        if (connected)
            createFile()
    }

    private fun createFile() {
        if (isExternalStorageWritable()) {
            currentFile = getFileExternalDirectory()
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }

    private fun getFileExternalDirectory(): File {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/ECG"

        val album = File(path)

        if (!album.exists())
            album.mkdirs()

        val sdf = SimpleDateFormat("ddMMyyyyhhmmss", Locale.getDefault())

        val filename = sdf.format(Date()) + ".csv"
        val file = File(path, filename)

        file.createNewFile()

        Log.i("ECGMonitor", "New file created at: ${file.absolutePath}")
        return file
    }

    var startTime = (-1).toLong()

    var index = 0
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

        ecgData[index] = doubleArrayOf(currentTime, value.toDouble(), currentDiff1, currentDiff2)

        if (index == MAX_ARRAY_SIZE - 1) {
            val fos = FileOutputStream(currentFile, true)
            val pw = PrintWriter(fos)

            for (sample in ecgData) {
                pw.appendln(sample.joinToString())
            }

            index = 0
            ecgData = Array(MAX_ARRAY_SIZE) {DoubleArray(4)}

            pw.close()
            fos.close()

        } else {
            index++
        }

        lastEcgValue = value
        lastDiff1Value = currentDiff1.toInt()
        timePointer++
    }
}
