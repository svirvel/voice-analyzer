package com.alicenilsson.voiceanalyzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RecordingActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private boolean isRecording = false;

    private Button recordButton;
    private Button cancelButton;

    private MediaRecorder recorder = null;

    private TextView timeTextView;

    private Date recordingStartDate;

    private Timer timer;

    // Set max recording time to 5 minutes (in ms)
    private int maxRecordingTime = 5*30*1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        fileName = getExternalFilesDir("recordings").getAbsolutePath();


        fileName += "/audiorecordtest.3gp";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        recordButton = findViewById(R.id.recordButton);
        cancelButton = findViewById(R.id.cancelButton);

        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

            if(isRecording) {
                stopRecording();
                // Proceed to analyze recording
            }
            else
            {
                startRecording();
                isRecording = true;
                recordButton.setText("Stop");
            }

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            if(isRecording) {
                stopRecording();
                // Delete recording
            }
            finish();
            }
        });

        timeTextView = findViewById(R.id.timeTextView);

    }

    private void startRecording() {

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setMaxDuration(maxRecordingTime);

        try {
            recorder.prepare();
        } catch (IOException e) {

        }

        recorder.start();

        recordingStartDate = new Date();

        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        int elapsedSeconds = (int) (((new Date().getTime()) - recordingStartDate.getTime()) / 1000L);
                        int seconds = elapsedSeconds % 60;
                        int minutes = elapsedSeconds / 60;

                        String timeString = String.format("%02d:%02d", minutes,seconds);

                        timeTextView.setText(timeString);

                        if(elapsedSeconds >= (maxRecordingTime / 1000)) {
                            Toast.makeText(getApplicationContext(), "Max recording time reached", Toast.LENGTH_SHORT).show();
                            stopRecording();
                        }

                    }
                });
            }
        };

        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask,0,1000);
    }

    private void stopRecording() {

        if(recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
        }

        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if(!permissionToRecordAccepted) {
            finish();
        }

    }

    @Override
    public void onStop() {

        super.onStop();

        if (isRecording) {
            stopRecording();
        }
    }
}
