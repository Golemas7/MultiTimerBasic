package com.offficial.kalisstudiodev.mytimerapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.offficial.kalisstudiodev.mytimerapp.R;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A placeholder fragment containing a simple view.
 */
public class FinishTimerActivityFragment extends Fragment {

    private static final String TAG = "FinishTimerActivityFrag";

    private int STORAGE_PERMISSION_CODE = 1;



    private TextView mNameTextView;
    private TextView mTimeTextView;
    private TextView mOverdueTextView;

    private ImageButton mPlayButton;
    private ImageButton mDeleteButton;
    private Button mStopButton;

    long startTime, timeInMilliseconds = 0;
    Handler handlerForTimer = new Handler();

    public FinishTimerActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        final View view = inflater.inflate(R.layout.fragment_finish_timer, container, false);


        mNameTextView = view.findViewById(R.id.fti_name);
        mTimeTextView = view.findViewById(R.id.fti_time);
        mOverdueTextView = view.findViewById(R.id.fti_overtime);

        mPlayButton = view.findViewById(R.id.fti_play);
        mDeleteButton = view.findViewById(R.id.fti_delete);
        mStopButton = view.findViewById(R.id.fti_stop);


        Bundle arguments = getActivity().getIntent().getExtras();  // The line change later

        final Task task;
        if (arguments != null) {


            task = (Task) arguments.getSerializable(Task.class.getSimpleName());
            if (task != null) {

                mNameTextView.setText(task.getName());

                mTimeTextView.setText(task.getTime() / 3600 > 1 ?
                        String.format(Locale.getDefault(), "%02d:%02d:%02d", task.getTime() / 3600, task.getTime() % 3600 / 60, task.getTime() % 60) :
                        String.format(Locale.getDefault(), "%02d:%02d", task.getTime() % 3600 / 60, task.getTime() % 60));

            } else {
                // No task, so we must be adding a new task, and not editing an  existing one
            }
        } else {
            task = null;
        }


        // Start vibrating
        final Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 500, 500, 1000, 500};

        // Check if the user has't turned off vibrations
        SharedPreferences mPreferences  = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        Boolean vibrations = mPreferences.getBoolean("notifications_new_message_vibrate", true);
        try {
            if (vibrations) {
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    v.vibrate(pattern, 0);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }


        // Start playing sounds
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        String sound = mPreferences.getString("notifications_new_message_ringtone", "Morning.ogg");

        if (ContextCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

        } else {
            requestStoragePermission();
        }






        String soundFile = mPreferences.getString("notifications_new_message_ringtone", "");

        final MediaPlayer r =
                MediaPlayer.create(getContext(), Uri.parse(soundFile));


        // Check if the user has't turned off alarm sounds in the app
        Boolean sounds = mPreferences.getBoolean("notifications_new_message", true);

        try {
            if (sounds &&
                    ContextCompat.checkSelfPermission(this.getContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    ) {
                r.setLooping(true);
                r.start();

            }

        } catch(Exception e) {
            e.printStackTrace();
        }




        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update the database if at least one field has changed.
                // - There's no need to hit the database unless this has happened.

                // Delete the task
                ContentResolver contentResolver = getActivity().getContentResolver();
//                ContentValues values = new ContentValues();

                switch (view.getId()) {
                    case R.id.fti_delete:
                        try {
                            r.stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            v.cancel();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        contentResolver.delete(TasksContract.buildTaskUri(task.getId()), null, null);
                        break;
                    case R.id.fti_stop:
                        stop(view);
                        try {
                            r.stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            v.cancel();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        getActivity().finish();
                        break;
                    case R.id.fti_play:
                        CursorRecyclerViewAdapter.getPlayButton().performClick();
                        stop(view);
                        try {
                            r.stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            v.cancel();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        getActivity().finish();
                        // Play the timer
                        break;
                    default:

                }

                try {
                    r.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    v.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stop(view);
                getActivity().finish();

            }

        };

        view.findViewById(R.id.fti_play).setOnClickListener(buttonListener);
        view.findViewById(R.id.fti_stop).setOnClickListener(buttonListener);
        view.findViewById(R.id.fti_delete).setOnClickListener(buttonListener);


        // handle back button
        view.findViewById(R.id.content_finish_timer).setFocusableInTouchMode(true);
        view.findViewById(R.id.content_finish_timer).requestFocus();
        view.findViewById(R.id.content_finish_timer).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View mView, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {

                    stop(view);
                    r.stop();
                    v.cancel();
                    getActivity().finish();
                    return true;
                }

                return false;
            }
        });


        // Start a timer
        start(view);


        return view;
    }

    // Timer related functions

    public static String getDateFromMillis(long d) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(d);
    }

    public void start(View v) {
        startTime = SystemClock.uptimeMillis();
        handlerForTimer.postDelayed(updateTimerThread, 0);
    }

    public void stop(View v) {
        handlerForTimer.removeCallbacks(updateTimerThread);
    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            mOverdueTextView.setText("+" + getDateFromMillis(timeInMilliseconds));
            handlerForTimer.postDelayed(this, 1000);
        }
    };

    @Override
    public void onStop() {
        mStopButton.performClick();
        super.onStop();
    }



    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale((FinishTimerActivity)getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this.getContext())
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because of this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((FinishTimerActivity)getActivity(),
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions((FinishTimerActivity)getActivity(),
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

}
