package com.haiyun.soundrecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class RecordingDialog extends Dialog {

    private final Context context;
    private boolean isRecording = false;
    private ImageView recordBtn;
    private TextView timerTV;
    private MediaRecorder recorder;
    private File recordingPath = null;
    private NewRecordingListener newRecordingListener;
    private Timer timer;

    private int hour = 0, min = 0, sec = 0;

    private static final String TAG = "MediaRecorder";
    public RecordingDialog(@NonNull Context context, NewRecordingListener newRecordingListener) {
        super(context);
        this.context = context;
        this.newRecordingListener = newRecordingListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_dialog_layout);

        timerTV = findViewById(R.id.timer);
        recordBtn = findViewById(R.id.recordBtn);

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "1. Permissions denied. You might want to inform the user about the importance of permissions for your app's functionality.");
            ActivityCompat.requestPermissions((Activity) context, new String[] {android.Manifest.permission.RECORD_AUDIO}, 20);
            Log.w(TAG, "1. Request action finished.");
        } else {
            Log.i(TAG, "1. Permission already granted.");

        }

        recordingPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/SoundRecorder");

        timer = new Timer();

        if (!recordingPath.exists()) {
            recordingPath.mkdirs();
        }

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "2. Permissions denied. You might want to inform the user about the importance of permissions for your app's functionality.");
                    Toast.makeText(context, "Please give mic permissions", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions((Activity) context, new String[] {android.Manifest.permission.RECORD_AUDIO}, 20);
                    Log.w(TAG, "2. Request action finished.");
                } else {
                    Log.i(TAG, "2. Permission already granted.");
                   if (!isRecording) {
                       startRecording();
                   } else {
                       stopRecording();
                   }
                }

            }
        });
    }

    private void startRecording() {
        String fileName = System.currentTimeMillis() + ".mp3";
        isRecording = true;
        recordBtn.setImageResource(R.drawable.stop_recording_icon);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordingPath.getAbsolutePath() + "/" + fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
            Log.e(TAG, String.valueOf(e));
            Toast.makeText(context, "Something went wrong!!!", Toast.LENGTH_SHORT).show();
        }

        recorder.start();

        // start timer
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ((Activity)context).runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        String hourTxt = String.valueOf(hour);
                        String minTxt = String.valueOf(min);
                        String secTxt = String.valueOf(sec);

                        if(hourTxt.length() == 1) {
                            hourTxt = "0" + hourTxt;
                        }

                        if(minTxt.length() == 1) {
                            minTxt = "0" + minTxt;
                        }

                        if(secTxt.length() == 1) {
                            secTxt = "0" + secTxt;
                        }

                        timerTV.setText(hourTxt + ":" + minTxt + ":" + secTxt);

                        sec++;

                        if (sec == 60) {
                            min++;
                            sec = 0;
                        }

                        if (min == 60) {
                            hour++;
                            min = 0;
                        }

                    }
                });
            }
        }, 1000, 1000);


    }

    private void stopRecording() {
        isRecording = false;
        recordBtn.setImageResource(R.drawable.recording_icon);
        Toast.makeText(context, "Recording saved to sdcard/Music/SoundRecord", Toast.LENGTH_SHORT).show();

        timer.purge();
        timer.cancel();

        recorder.stop();
        recorder.release();
        recorder = null;


        dismiss();
        newRecordingListener.onNewRecord();

    }

}
