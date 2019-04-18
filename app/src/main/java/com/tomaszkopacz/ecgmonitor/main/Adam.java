package com.tomaszkopacz.ecgmonitor.main;

import android.os.Handler;

public class Adam {

    private Boolean isRecording = false;

    private static final int RECORD_PERIOD = 10000;

    public void onBLECharacteristicReceived() {
        Handler handler = new Handler();

        // stop record after 10 sec
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopRecording();
            }
        }, RECORD_PERIOD);

        // start record and play sound
        startRecording();
        playSound();
    }

    private void playSound(){

    }

    private void startRecording() {

        //myRecorder.startRecording();

        isRecording = true;
        processData();
    }

    private void stopRecording() {
        //myRecorder.stopRecording()
        isRecording = false;
    }

    private void processData() {
        while (isRecording){
            //read audio, write file
        }
    }
}
