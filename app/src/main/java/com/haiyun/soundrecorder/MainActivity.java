package com.haiyun.soundrecorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NewRecordingListener {

    private final List<RecordingsList> recordingsLists = new ArrayList<>();
    private RecyclerView recordingsRecyclerView;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recordingsRecyclerView = findViewById(R.id.recordingsRecyclerView);
        final FloatingActionButton newRecording = findViewById(R.id.newRecording);

        recordingsRecyclerView.setHasFixedSize(true);
//        MainActivity mainActivity = this;
        recordingsRecyclerView.setLayoutManager(new LinearLayoutManager( this));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Permissions denied. You might want to inform the user about the importance of permissions for your app's functionality.");

            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_AUDIO}, 20);

            Log.w(TAG, "Request action finished.");
        } else {
            Log.d(TAG, "Permission already granted.");
            getRecordings(recordingsRecyclerView);
        }

        newRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                RecordingDialog recordingDialog = new RecordingDialog(MainActivity.this, MainActivity.this);
                recordingDialog.setCancelable(false);
                recordingDialog.show();

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getRecordings(recordingsRecyclerView);
        } else {
            Toast.makeText(this, "Please give memory permissions", Toast.LENGTH_SHORT).show();
        }
    }

    private void getRecordings(RecyclerView recyclerView) {
        //2024-01-07 01:23:02 - to rest
        //2024-01-07 09:16:43 - start

        // Check sound recording list
        // if yes, the list them
        // if not, just show no recordings yet

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/SoundRecorder");

        if (file.exists()) {
            File[] recordings = file.listFiles();

            // show the list
            //for (int i=0; i<recordings.length; i++) {
            for (File recording : recordings) {
                final String getFileName = recording.getName();

                RecordingsList recordingsList = new RecordingsList(getFileName, "0", recording.getAbsolutePath());
                recordingsLists.add(recordingsList);
            }

            Log.e(TAG, String.valueOf(recyclerView));
            recyclerView.setAdapter(new RecordingsAdapter(MainActivity.this, recordingsLists));

        } else {
            Toast.makeText(this, "No Recordings Available", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onNewRecord() {
        recordingsLists.clear();
        getRecordings(recordingsRecyclerView);
    }
}