package com.example.admin.simpleaudiorecoder;


import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.widget.Toast.makeText;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecorderFragment extends Fragment {

    private static final String LOG_TAG = "RecorderFragment";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 101;
    private MediaRecorder mRecorder;
    private boolean permissionToRecordAccepted;
    private boolean permissionToStoreAccepted;
    private Chronometer mChronometer;
    private ImageView mStartRecordingImage;
    private ImageView mStopRecordingImage;
    private TextView mTextView;
    public static final String PATH="/sdcard/SimpleAudioRecorder/";
    long timeWhenStopped = 0;
    String saveName;

    public RecorderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
               View rootView = inflater.inflate(R.layout.activity_recorder, container, false);

        requestPermission();
        mTextView = (TextView) rootView.findViewById(R.id.text_under_button);
        mStartRecordingImage =(ImageView) rootView.findViewById(R.id.start_record_image);
        mStopRecordingImage = (ImageView) rootView.findViewById(R.id.stop_record_image);
        mChronometer = (Chronometer) rootView.findViewById(R.id.recorder_chronometer);
        mStartRecordingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextView.setText(getString(R.string.stop_recording_and_save));
                mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                mChronometer.start();
                startRecording();
            }
        });

        mStopRecordingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
                timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
                mChronometer.stop();

            }
        });

//make directory
        boolean exists = (new File(PATH)).exists();
        if (!exists){new File(PATH).mkdirs();}


        return rootView;

    }

    private String  incrementNameIndex(){
        int num = 1;
         saveName = getString(R.string.voice_name)+ num  ;
        File file = new File(PATH, saveName);
        while(file.exists()) {
            saveName = getString(R.string.voice_name) + (num++);
            file = new File(PATH, saveName);
        }
        return saveName;
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(getActivity(), new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_RECORD_AUDIO_PERMISSION);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                permissionToStoreAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted && !permissionToStoreAccepted ) {getActivity().finish();}

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getActivity(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void startRecording() {


        if (checkPermission()) {
            if (isExternalStorageWritable()) {
                setVisibilityStopRecording();



                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setOutputFile(PATH + incrementNameIndex());
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                try {
                    mRecorder.prepare();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "mRecorder.prepare() failed");
                }

                mRecorder.start();

                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



            }
            else {
                makeText(getActivity(),getString(R.string.memory_is_busy),Toast.LENGTH_SHORT).show();
            }
        }
        else {
            requestPermission();
        }
    }

    private void stopRecording() {

        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            setVisibilityStartRecording();
            mTextView.setText(getString(R.string.start_recording));
            timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenStopped = 0;
           Toast toast = Toast.makeText(getActivity(), saveName + getString(R.string.new_voice_file_created), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM,0,0);
           toast .show();
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        }

    }

    private void setVisibilityStartRecording(){
        mStopRecordingImage.setVisibility(View.INVISIBLE);
        mStartRecordingImage.setVisibility(View.VISIBLE);
    }

    private void setVisibilityStopRecording(){
        mStartRecordingImage.setVisibility(View.INVISIBLE);
        mStopRecordingImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        mTextView.setText(getString(R.string.start_recording));
        setVisibilityStartRecording();
    }

    @Override
    public void onStop() {

        stopRecording();
        super.onStop();

    }
/** Prior to Android N there was a platform bug that could cause
 * setUserVisibleHint to bring a fragment up to the started state before its
 * FragmentTransaction had been committed.
 * As some apps relied on this behavior, it is preserved for apps that declare
 * a targetSdkVersion of 23 or lower.**/
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

            if (!isVisibleToUser) {
               stopRecording();
            }
        }
    }

