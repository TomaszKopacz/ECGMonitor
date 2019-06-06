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

    private val timeArray = ArrayList<Double>()
    private val ecgArray = ArrayList<Double>()
    private val diff1Array = ArrayList<Double>()
    private val diff2Array = ArrayList<Double>()

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
            br.lineSequence().forEach {
                splitLineToArrays(it)
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

    private fun splitLineToArrays(it: String) {
        val delimiter = ", "
        val parts = it.split(delimiter)

        timeArray.add(parts[0].toDouble())
        ecgArray.add(parts[1].toDouble())
        diff1Array.add(parts[2].toDouble())
        diff2Array.add(parts[3].toDouble())
    }
}
