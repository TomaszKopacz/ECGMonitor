package com.tomaszkopacz.ecgmonitor.analysis

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.tomaszkopacz.ecgmonitor.R
import java.io.File


class AnalysisActivity : AppCompatActivity() {

    val file = "04062019053544.csv"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        readEcgFiles()
    }

    private fun readEcgFiles() {
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath + "/ECG"
        Log.d("ECGMonitor", "Path: $path")
        val directory = File(path)
        val files = directory.listFiles()
        for (i in files.indices) {
            Log.d("ECGMonitor", "FileName:" + files[i].name)
        }
    }
}
