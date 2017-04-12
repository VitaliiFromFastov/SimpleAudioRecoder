package com.example.admin.simpleaudiorecoder;


import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.admin.simpleaudiorecoder.RecorderFragment.PATH;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerFragment extends Fragment implements VoiceAdapter.ListItemClickListener,
        MediaPlayer.OnCompletionListener,AudioManager.OnAudioFocusChangeListener,SeekBar.OnSeekBarChangeListener {


    private ImageView mPlayImageView;
    private ImageView mPauseImageView;
    private SeekBar mSeekBar;
    private Chronometer mChronometer;
    private RecyclerView mRecyclerView;
    private TextView mVoiceNameTextView;
    private TextView mVoiceDurationTextView;
    private MediaPlayer mPlayer;
    private VoiceAdapter mAdapter;
    private List<Voice> mVoiceList;
    private AudioManager mAudioManager;
    private String mSourceFilename;
    private long timeWhenStopped;
       private int mNumberOfNoteColumns;

    public PlayerFragment() {
        // Required empty public constructor
    }

    /**
     * Prior to Android N there was a platform bug that could cause
     * setUserVisibleHint to bring a fragment up to the started state before its
     * FragmentTransaction had been committed.
     * As some apps relied on this behavior, it is preserved for apps that declare
     * a targetSdkVersion of 23 or lower.
     **/
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {

            if (isVisibleToUser) {

                mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);

                determineScreenSize();


                mVoiceList = new ArrayList<>();
                displayFileInfo();
                //manage Adapter

                GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), mNumberOfNoteColumns);
                mRecyclerView.setLayoutManager(gridLayoutManager);
                mAdapter = new VoiceAdapter(mVoiceList, this);

                mRecyclerView.setAdapter(mAdapter);
            }


            // If we are becoming invisible, then...
            else {
                if (mPlayer != null && mPlayer.isPlaying()) {
                    pauseMediaPlayer();
                }
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_player, container, false);

        mPlayImageView = (ImageView) rootView.findViewById(R.id.play_image_view);
        mPauseImageView = (ImageView) rootView.findViewById(R.id.pause_image_view);
        mChronometer = (Chronometer) rootView.findViewById(R.id.player_chronometer);
        mVoiceDurationTextView = (TextView) rootView.findViewById(R.id.voice_duration_text_view);
        mVoiceNameTextView = (TextView) rootView.findViewById(R.id.voice_name_text_view);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.player_seek_bar);





        mPlayImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mSourceFilename != null) {
                    startPlayer(PATH + mSourceFilename);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.toast_pick_file), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mPauseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseMediaPlayer();
            }
        });


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_menu_item) {

            if (mSourceFilename != null) {

                deleteVoiceDialog();

            } else {
                Toast.makeText(getActivity(), getString(R.string.toast_pick_file), Toast.LENGTH_SHORT).show();
            }
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    @Override
    public void onListItemClick(String fileName) {
        mSourceFilename = fileName;
        releaseMediaPlayer();
        startPlayer(PATH + mSourceFilename);


    }

    @Override
    public void onAudioFocusChange(int audiofocusChange) {
        switch (audiofocusChange) {

            case AudioManager.AUDIOFOCUS_LOSS:
                if (mPlayer.isPlaying()) {
                    mPlayer.stop();
                    releaseMediaPlayer();
                    showPlayImageView();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mPlayer.isPlaying()) {
                    pauseMediaPlayer();
                    showPlayImageView();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mPlayer.isPlaying()) {
                    mPlayer.setVolume(0.2f, 0.2f);
                }
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mPlayer != null && !mPlayer.isPlaying()) {
                    resumeMediaPlayer();
                    showPauseImageView();
                    mPlayer.setVolume(1.0f, 1.0f);

                } else if (mPlayer == null) {
                    startPlayer(PATH + mSourceFilename);
                }
                break;
        }

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        timeWhenStopped = 0;
        showPlayImageView();
        mSeekBar.setProgress(0);
        releaseMediaPlayer();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }


    public void displayFileInfo() {

        File file = new File(PATH);
        for (File child : file.listFiles()) {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(String.valueOf(child));
            String titleStr = child.getName();

            String dateStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);

            Date inputDate = null;
            try {
                inputDate = new SimpleDateFormat("yyyyMMdd'T'HHmmss").parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }


            String formattedDate = new SimpleDateFormat("dd MMM yyyy").format(inputDate);

            String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationMilliseconds = Long.parseLong(durationStr);
            String formattedDuration = convertDuration(durationMilliseconds);


            Voice newVoiceFile = new Voice(titleStr, formattedDate, formattedDuration);

            mVoiceList.add(0, newVoiceFile);





        }


    }

    private String convertDuration(long duration) {

        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void startPlayer(String dataSource) {
        if (mPlayer != null && !mPlayer.isPlaying()) {
            resumeMediaPlayer();
        } else if (mPlayer == null) {

            if (!successfullyRetrievedAudioFocus()) {
                mAudioManager.requestAudioFocus(this,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            } else if (successfullyRetrievedAudioFocus()) {

                mPlayer = new MediaPlayer();

                try {
                    mPlayer.setDataSource(dataSource);
                } catch (IOException e) {
                    Log.e("PlayerActivity", "mPlayer.setDataSource failed");
                }
                mPlayer.setVolume(1.0f, 1.0f);

                try {
                    mPlayer.prepare();
                } catch (IOException e) {
                    Log.e("PlayerActivity", "mPlayer.prepare failed");
                }
                mPlayer.setOnCompletionListener(this);

                initSeekBar();

                showPauseImageView();

                //start Chronometer
                timeWhenStopped = 0;
                mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                mChronometer.start();
                // mVoiceDurationTextView.setText(DateUtils.formatElapsedTime(mPlayer.getDuration()/1000));
                mVoiceDurationTextView.setText(convertDuration(mPlayer.getDuration()));
                mVoiceNameTextView.setText(mSourceFilename);

                mPlayer.start();

                //keep screen on
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            }

        }
    }

    private void pauseMediaPlayer() {
        mPlayer.pause();
        showPlayImageView();
        timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
        mChronometer.stop();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void resumeMediaPlayer() {
        mPlayer.start();
        showPauseImageView();
        mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        mChronometer.start();

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


    }

    private void initSeekBar() {


        Runnable runnable = new Runnable() {
            @Override
            public void run() {


                int voiceDuration = mPlayer.getDuration();
                mSeekBar.setMax(voiceDuration);
                int mCurrentPosition = 0;

                while (mPlayer != null && mCurrentPosition < voiceDuration) {


                    try {
                        Thread.sleep(300);
                        mCurrentPosition = mPlayer.getCurrentPosition();
                    } catch (InterruptedException e) {
                        Log.e("PlayerActivity", "Thread.sleep() faild");
                        return;
                    } catch (Exception e) {
                        Log.e("PlayerActivity", "Exception was caught");
                        return;
                    }

                    mSeekBar.setProgress(mCurrentPosition);

                }
            }
        };
        new Thread(runnable).start();
        mSeekBar.setOnSeekBarChangeListener(this);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean inputFromUser) {

        if (inputFromUser) {
            if (mPlayer != null) {
                mPlayer.seekTo(progress);

                mChronometer.setBase(SystemClock.elapsedRealtime() - mPlayer.getCurrentPosition());
            } else {
                startPlayer(PATH + mSourceFilename);
                mPlayer.seekTo(progress);

            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private boolean successfullyRetrievedAudioFocus() {
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        int result = mAudioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }

    private void showPlayImageView() {
        mPauseImageView.setVisibility(View.INVISIBLE);
        mPlayImageView.setVisibility(View.VISIBLE);

    }

    private void showPauseImageView() {
        mPlayImageView.setVisibility(View.INVISIBLE);
        mPauseImageView.setVisibility(View.VISIBLE);
    }

    private void releaseMediaPlayer() {
        showPlayImageView();
        mSeekBar.setProgress(0);
        timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        timeWhenStopped = 0;
        if (mPlayer != null) {

            mPlayer.release();

            mPlayer = null;

            mAudioManager.abandonAudioFocus(this);
        }
    }

    private void deleteVoiceDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle(getString(R.string.delete_voice_title));
        alert.setMessage(getString(R.string.delete_voice_message));
        alert.setPositiveButton(getString(R.string.delete_voice), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // continue with delete

                new File(PATH + mSourceFilename).delete();

                mVoiceList.clear();
                mAdapter.notifyDataSetChanged();
                mVoiceNameTextView.setText(R.string.pick_a_voice_file);
                mVoiceDurationTextView.setText("00:00:00");
                mSourceFilename = null;
                releaseMediaPlayer();
                displayFileInfo();


            }
        });
        alert.setNegativeButton(getString(R.string.cancel_deleting), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // close dialog
                dialog.cancel();
            }
        });
        alert.show();
    }

    // method to determine Screen size of a user's device
    public void determineScreenSize() {

        int smallestDp = getResources().getConfiguration().smallestScreenWidthDp;

        if (smallestDp >= 720) {
            // For 10” tablets (720dp wide and bigger)
            mNumberOfNoteColumns = 3;

        } else if (smallestDp >= 600 && smallestDp < 720) {
            // For 7” tablets (600dp wide and bigger)
            mNumberOfNoteColumns = 2;
        } else {
            //for handsets
            mNumberOfNoteColumns = 1;
        }

    }

}




