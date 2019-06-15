package com.tomaszkopacz.ecgmonitor.analysis

import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.series.PointsGraphSeries
import com.tomaszkopacz.ecgmonitor.R
import com.tomaszkopacz.ecgmonitor.constants.EcgGraphs
import com.tomaszkopacz.ecgmonitor.hrv.HRV
import kotlinx.android.synthetic.main.activity_analysis.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

class AnalysisActivity : AppCompatActivity(), MyRecyclerViewAdapter.ItemListener {

    private val filesList = ArrayList<String>()

    private lateinit var listView: RecyclerView
    private lateinit var listLayout: RecyclerView.LayoutManager
    private lateinit var listAdapter: MyRecyclerViewAdapter

    private var timeArray = DoubleArray(0)
    private var ecgArray = DoubleArray(0)
    private var diff1Array = DoubleArray(0)
    private var diff2Array = DoubleArray(0)

    private var rrArray = DoubleArray(0)
    private var rmssd = 0.0
    private var nn50 = 0
    private var pnn50 = 0.0
    private var sdsd = 0.0
    private var poincarePoints: Array<DataPoint>? = null

    private var ecgSeries: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var diff1Series: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var diff2Series: LineGraphSeries<DataPoint> = LineGraphSeries()
    private var rrSeries: BarGraphSeries<DataPoint> = BarGraphSeries()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        getEcgFiles()

        listView = findViewById(R.id.files_list_view)
        listLayout = LinearLayoutManager(this)
        listAdapter = MyRecyclerViewAdapter(this, filesList, this)

        listView.setHasFixedSize(true)
        listView.layoutManager = listLayout
        listView.adapter = listAdapter

        setGraphs()
    }

    private fun getEcgFiles() {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/ECG"
        Log.d("ECGMonitor", "Path: $path")
        val directory = File(path)
        val files = directory.listFiles()
        for (i in files.indices) {
            Log.i("ECGMonitor", "New file found: ${files[i].name}")
            filesList.add(files[i].name)
        }
    }

    private fun setGraphs() {
        setEcgGraph()
        setDiff1Graph()
        setDiff2Graph()
        setRRGraph()
        setPoincareGraph()
    }

    private fun setEcgGraph() {
        ecgGraph.viewport.isScalable = false
        ecgGraph.viewport.isScrollable = true

        ecgGraph.viewport.setScalableY(false)
        ecgGraph.viewport.setScrollableY(false)

        ecgGraph.viewport.isXAxisBoundsManual = true
        ecgGraph.viewport.setMinX(EcgGraphs.SimulatedEcg.MIN_X)
        ecgGraph.viewport.setMaxX(EcgGraphs.SimulatedEcg.MAX_X)

        ecgGraph.viewport.isYAxisBoundsManual = true
        ecgGraph.viewport.setMinY(EcgGraphs.SimulatedEcg.MIN_ECG_Y)
        ecgGraph.viewport.setMaxY(EcgGraphs.SimulatedEcg.MAX_ECG_Y)

        ecgSeries.color = Color.RED
        ecgGraph.addSeries(ecgSeries)
    }

    private fun setDiff1Graph() {
        diff1Graph.viewport.isScalable = false
        diff1Graph.viewport.isScrollable = true

        diff1Graph.viewport.setScalableY(false)
        diff1Graph.viewport.setScrollableY(false)

        diff1Graph.viewport.isXAxisBoundsManual = true
        diff1Graph.viewport.setMinX(EcgGraphs.SimulatedEcg.MIN_X)
        diff1Graph.viewport.setMaxX(EcgGraphs.SimulatedEcg.MAX_X)

        diff1Graph.viewport.isYAxisBoundsManual = true
        diff1Graph.viewport.setMinY(EcgGraphs.SimulatedEcg.MIN_DIFF1_Y)
        diff1Graph.viewport.setMaxY(EcgGraphs.SimulatedEcg.MAX_DIFF1_Y)

        diff1Graph.addSeries(diff1Series)
    }

    private fun setDiff2Graph() {
        diff2Graph.viewport.isScalable = false
        diff2Graph.viewport.isScrollable = true

        diff2Graph.viewport.setScalableY(false)
        diff2Graph.viewport.setScrollableY(false)

        diff2Graph.viewport.isXAxisBoundsManual = true
        diff2Graph.viewport.setMinX(EcgGraphs.SimulatedEcg.MIN_X)
        diff2Graph.viewport.setMaxX(EcgGraphs.SimulatedEcg.MAX_X)

        diff2Graph.viewport.isYAxisBoundsManual = true
        diff2Graph.viewport.setMinY(EcgGraphs.SimulatedEcg.MIN_DIFF2_Y)
        diff2Graph.viewport.setMaxY(EcgGraphs.SimulatedEcg.MAX_DIFF2_Y)

        diff2Graph.addSeries(diff2Series)
    }

    private fun setRRGraph() {
        rrGraph.viewport.isScalable = false
        rrGraph.viewport.isScrollable = true

        rrGraph.viewport.setScalableY(false)
        rrGraph.viewport.setScrollableY(false)

        rrGraph.viewport.isXAxisBoundsManual = true
        rrGraph.viewport.setMinX(EcgGraphs.SimulatedEcg.MIN_X)
        rrGraph.viewport.setMaxX(EcgGraphs.SimulatedEcg.MAX_X)

        rrGraph.viewport.isYAxisBoundsManual = true
        rrGraph.viewport.setMinY(EcgGraphs.SimulatedEcg.MIN_RR_Y)
        rrGraph.viewport.setMaxY(EcgGraphs.SimulatedEcg.MAX_RR_Y)

        rrGraph.addSeries(rrSeries)
    }

    private fun setPoincareGraph() {
        poincareGraph.gridLabelRenderer.horizontalAxisTitle = "Aktualna wartość RR [s]"
        poincareGraph.gridLabelRenderer.verticalAxisTitle = "Poprzednia wartość RR [s]"
        poincareGraph.viewport.isScalable = false
        poincareGraph.viewport.isScrollable = true
        poincareGraph.viewport.isXAxisBoundsManual = true
        poincareGraph.viewport.isYAxisBoundsManual = true
        poincareGraph.viewport.scrollToEnd()
        poincareGraph.viewport.setMinX(EcgGraphs.SimulatedEcg.MIN_RR_Y)
        poincareGraph.viewport.setMaxX(EcgGraphs.SimulatedEcg.MAX_RR_Y)
        poincareGraph.viewport.setMinY(EcgGraphs.SimulatedEcg.MIN_RR_Y)
        poincareGraph.viewport.setMaxY(EcgGraphs.SimulatedEcg.MAX_RR_Y)
    }

    override fun onItemClicked(view: View?, position: Int) {
        val filename = filesList[position]
        analyseEcgFile(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/ECG", filename))
    }

    private fun analyseEcgFile(file: File?) {
        readEcgFile(file)
        countHRV()
        displayHRVResults()
    }

    private fun readEcgFile(file: File?) {
        var br: BufferedReader? = null

        try {
            br = BufferedReader(FileReader(file))

            var elementCount = 0
            for (it in br.lineSequence())
                elementCount++

            timeArray = DoubleArray(elementCount)
            ecgArray = DoubleArray(elementCount)
            diff1Array = DoubleArray(elementCount)
            diff2Array = DoubleArray(elementCount)

            br = BufferedReader(FileReader(file))
            br.lineSequence().forEachIndexed{ index, element ->
                splitLineToArrays(element, index)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                br?.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    private fun splitLineToArrays(string: String, index: Int) {
        val delimiter = ", "
        val parts = string.split(delimiter)

        timeArray[index] = parts[0].toDouble()
        ecgArray[index] = parts[1].toDouble()
        diff1Array[index] = parts[2].toDouble()
        diff2Array[index] = parts[3].toDouble()
    }

    private fun countHRV() {
        rrArray = HRV.countRR(timeArray, diff1Array)

        rmssd = HRV.countRMSSD(rrArray)
        nn50 = HRV.countNN50(rrArray)
        pnn50 = HRV.countPNN50(rrArray)
        sdsd = HRV.countSDSD(rrArray)

        poincarePoints = HRV.countPoincarePoints(rrArray)
        poincarePoints!!.sortBy { it.x }
    }

    private fun displayHRVResults() {
        for (index in timeArray.indices) {
            addEcgPoint(index)
            addDiff1Point(index)
            addDiff2Point(index)
        }

        for (index in rrArray.indices)
            addRRPoint(index)

        RMSSDView.text = "RMSSD: " + String.format("%.2f", rmssd) + " sek"
        NN50View.text = "NN50: $nn50"
        PNN50View.text = "PNN50: " + String.format("%.2f", pnn50) + " %"
        SDSDView.text = "SDSD: " + String.format("%.2f", sdsd) + " sek"

        addPoincarePoints()
    }

    private fun addEcgPoint(i: Int) {
        val point = DataPoint(timeArray[i], ecgArray[i])
        ecgSeries.appendData(point, true, timeArray.size)
    }

    private fun addDiff1Point(i: Int) {
        val point = DataPoint(timeArray[i], diff1Array[i])
        diff1Series.appendData(point, true, timeArray.size)
    }

    private fun addDiff2Point(i: Int) {
        val point = DataPoint(timeArray[i], diff2Array[i])
        diff2Series.appendData(point, true, timeArray.size)
    }

    private fun addRRPoint(i: Int) {
        val point = DataPoint(i.toDouble(), rrArray[i])
        rrSeries.appendData(point, true, rrArray.size)
    }

    private fun addPoincarePoints() {
        val poincareSeries = PointsGraphSeries(poincarePoints)
        poincareSeries.size = 10f
        poincareGraph.addSeries(poincareSeries)
    }
}
