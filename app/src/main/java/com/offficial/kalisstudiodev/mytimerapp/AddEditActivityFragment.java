package com.offficial.kalisstudiodev.mytimerapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.offficial.kalisstudiodev.mytimerapp.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddEditActivityFragment extends Fragment {

    private static final String TAG = "AddEditActivityFragment";

    Activity mActivity;

    private CursorRecyclerViewAdapter mAdapter; // add adapter reference

    public enum FragmentEditMode { EDIT, ADD }
    private FragmentEditMode mMode;

    private EditText mNameTextView;
    private EditText mSetTimeHoursTextView;
    private EditText mSetTimeMinutesTextView;
    private EditText mSetTimeSecondsTextView;
    private TextView mDescriptionTextView;
    private static Button mStartButton;

    public AddEditActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);

        mNameTextView = view.findViewById(R.id.addedit_name);
        mSetTimeHoursTextView = view.findViewById(R.id.addedit_task_hours);
        mSetTimeMinutesTextView = view.findViewById(R.id.addedit_task_minutes);
        mSetTimeSecondsTextView = view.findViewById(R.id.addedit_task_seconds);
        mDescriptionTextView = view.findViewById(R.id.addedit_task_info);
        mStartButton = view.findViewById(R.id.addedit_start);


        Bundle arguments = getActivity().getIntent().getExtras();  // The line change later

        final Task task;
        if(arguments != null) {

            task = (Task) arguments.getSerializable(Task.class.getSimpleName());
            if(task != null) {

                mNameTextView.setText(task.getName());
                mDescriptionTextView.setText(task.getDescription());

                String hoursXXX = "";
                String minutesXX = "";
                String secondsXX = "";

                int time = task.getTime();
                int hh = time/3600;
                int mm = time%3600/60;
                int ss = time%60;


                if (hh <= 99) {
                    hoursXXX += 0;
                }
                if (hh <= 9) {
                    hoursXXX += 0;
                }
                if (mm <= 9) {
                    minutesXX += 0;
                }
                if (ss <= 9) {
                    secondsXX += 0;
                }


                mSetTimeHoursTextView.setText(
                        hoursXXX + String.valueOf(hh)
                );
                mSetTimeMinutesTextView.setText(
                        minutesXX + String.valueOf(mm)
                );
                mSetTimeSecondsTextView.setText(
                        secondsXX + String.valueOf(ss)
                );

                mMode = FragmentEditMode.EDIT;
            } else {
                // No task, so we must be adding a new task, and not editing an  existing one
                mMode = FragmentEditMode.ADD;
            }
        } else {
            task = null;
            mNameTextView.setText("Task " + (MainActivityFragment.getItemCount()+1));
            mSetTimeMinutesTextView.setText("01");
            mMode = FragmentEditMode.ADD;
        }



        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update the database if at least one field has changed.
                // - There's no need to hit the database unless this has happened.


                int hh;     // to save repeated conversions to int. (Hours)
                int mm;     // to save repeated conversions to int. (Minutes)
                int ss;     // to save repeated conversions to int. (Seconds)
                int totalTime;

                if (Integer.parseInt(mSetTimeHoursTextView.getText().toString()) > 0) {
                    hh = Integer.parseInt(mSetTimeHoursTextView.getText().toString());
                } else {
                    hh = 0;
                }

                if (Integer.parseInt(mSetTimeMinutesTextView.getText().toString()) > 0) {
                    mm = Integer.parseInt(mSetTimeMinutesTextView.getText().toString());
                } else {
                    mm = 0;
                }

                if (Integer.parseInt(mSetTimeSecondsTextView.getText().toString()) > 0) {
                    ss = Integer.parseInt(mSetTimeSecondsTextView.getText().toString());
                } else {
                    ss = 0;
                }

                totalTime = (hh * 3600) + (mm * 60) + ss;

                ContentResolver contentResolver = getActivity().getContentResolver();
                ContentValues values = new ContentValues();

                switch (mMode) {
                    case EDIT:
                        if (!mNameTextView.getText().toString().equals(task.getName())) {
                            values.put(TasksContract.Columns.TASKS_NAME, mNameTextView.getText().toString());
                        }
                        if (!mDescriptionTextView.getText().toString().equals(task.getDescription())) {
                            values.put(TasksContract.Columns.TASKS_DESCRIPTION, mDescriptionTextView.getText().toString());
                        }
                        if (totalTime != task.getTime() || totalTime == 0) {
                            if (totalTime == 0) {
                                totalTime = 1;
                            }
                            values.put(TasksContract.Columns.TASKS_TIME, totalTime);
                            if (task.getIsPlaying().equals("false")) {
                                values.put(TasksContract.Columns.TASKS_CURRENT_TIME, totalTime);
                            }
                        }
                        if (values.size() != 0) {

                            contentResolver.update(TasksContract.buildTaskUri(task.getId()), values, null, null);
                        }
                        break;
                    case ADD:
                        if (mNameTextView.length() > 0) {


                            String f = "false";

                            values.put(TasksContract.Columns.TASKS_NAME, mNameTextView.getText().toString());
                            values.put(TasksContract.Columns.TASKS_DESCRIPTION, mDescriptionTextView.getText().toString());
                            values.put(TasksContract.Columns.TASKS_TIME, totalTime);
                            values.put(TasksContract.Columns.TASKS_CURRENT_TIME, totalTime);
                            values.put(TasksContract.Columns.TASK_IS_PLAYING, f);
                            contentResolver.insert(TasksContract.CONTENT_URI, values);
                        }
                        break;
                }

                getActivity().finish();

            }

        });


        view.findViewById(R.id.content_add_edit).setFocusableInTouchMode(true);
        view.findViewById(R.id.content_add_edit).requestFocus();
        view.findViewById(R.id.content_add_edit).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {

                    getActivity().finish();
                    return true;
                }
                return false;
            }
        });


        return view;
    }



    public Activity getRequiredActivity() {
        mActivity = getActivity();
        return mActivity;
    }


}
