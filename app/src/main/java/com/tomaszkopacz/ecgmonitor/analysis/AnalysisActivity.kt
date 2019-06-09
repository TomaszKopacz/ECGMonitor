package com.tomaszkopacz.ecgmonitor.analysis

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.tomaszkopacz.ecgmonitor.R
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

    override fun onItemClicked(view: View?, position: Int) {
        val filename = filesList[position]
        readEcgFile(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/ECG", filename))
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

            for (time in timeArray)
                Log.i("ECGMonitor", "Time: $time")

            for (ecg in ecgArray)
                Log.i("ECGMonitor", "ECG: $ecg")

            for (diff1 in diff1Array)
                Log.i("ECGMonitor", "diff1: $diff1")

            for (diff2 in diff2Array)
                Log.i("ECGMonitor", "diff2: $diff2")

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
}
