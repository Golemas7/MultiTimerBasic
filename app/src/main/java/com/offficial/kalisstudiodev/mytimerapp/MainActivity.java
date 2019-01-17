package com.offficial.kalisstudiodev.mytimerapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.offficial.kalisstudiodev.mytimerapp.BuildConfig;
import com.offficial.kalisstudiodev.mytimerapp.R;

public class MainActivity extends AppCompatActivity implements CursorRecyclerViewAdapter.OnTaskClickListener, AppDialog.DialogEvents {

    private static final String TAG = "MainActivity";

    // Whether or not thje activity is in 2-pane mode
    // i.e. running in landscape on a tablet
    private boolean mTwoPane = false;

    public static final int DIALOG_ID_DELETE = 1;
    public static final int DIALOG_ID_CANCEL_EDIT = 2;

    private AlertDialog mDialog = null;         // module scope because we need to dismiss it in onStop
    // e.g. when orientation changes) to avoid memory leaks.

    public static boolean DELETE_REQUEST = false;

    private static Context mContext;

    private CursorRecyclerViewAdapter cursorRecyclerViewAdapter;

    private String mState = null;

    private static final String ADD_EDIT_FRAGMENT = "AddEditFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskEditRequest(null);
            }
        });

        DELETE_REQUEST = false;

        mContext = getApplicationContext();

        // SplashScreen
        Intent detailIntent = new Intent(this, SplashScreenActivity.class);
        startActivity(detailIntent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.


        // inflate menu from xml
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.menumain_confirm).setVisible(false);


        if (mState =="HIDE_MENU")
        {
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setVisible(false);
            }

            menu.findItem(R.id.menumain_confirm).setVisible(true);

        }


        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.menumain_add_timer:
                taskEditRequest(null);
                break;
            case R.id.menumain_remove_timer:
                DELETE_REQUEST = true;
                mState = "HIDE_MENU"; // setting state
                invalidateOptionsMenu(); // now onCreateOptionsMenu(...) is called again

                break;
            case R.id.menumain_settings:
                try {
                    settings();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.menumain_show_about:
                showAboutDialog();
                break;
            case R.id.menumain_quit:
                finish();
                System.exit(0);
                break;
            case R.id.menumain_confirm:
                deleteEntries();
                DELETE_REQUEST = false;
                mState = "";
                invalidateOptionsMenu();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    public void showAboutDialog() {
        @SuppressLint("InflateParams") View messageView = getLayoutInflater().inflate(R.layout.about, null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);

        builder.setView(messageView);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        });

        mDialog = builder.create();
        mDialog.setCanceledOnTouchOutside(true);

        TextView tv = messageView.findViewById(R.id.about_version);
        tv.setText("v" + BuildConfig.VERSION_NAME);

        mDialog.show();

    }



    @Override
    public void onPositiveDialogResult(int dialogId, Bundle args) {
        switch(dialogId) {
            case DIALOG_ID_DELETE:
                Long taskId = args.getLong("TaskId");
                if(BuildConfig.DEBUG && taskId == 0) throw new AssertionError("Task ID is zero");
                getContentResolver().delete(TasksContract.buildTaskUri(taskId), null, null);
                break;
            case DIALOG_ID_CANCEL_EDIT:
                // no action required
                break;

        }
    }

    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {

        switch(dialogId) {
            case DIALOG_ID_DELETE:
                // no action required
                break;
            case DIALOG_ID_CANCEL_EDIT:
                finish();
                break;
        }
    }

    @Override
    public void onDialogCancelled(int dialogId) {

    }


    @Override
    public void onEditClick(Task task) {
        taskEditRequest(task);
    }

    @Override
    public void onDeleteClick(Task task) {
        getContentResolver().delete(TasksContract.buildTaskUri(task.getId()), null, null);
    }

    @Override
    public void saveDescriptionToDB(Task task, String string) {
        ContentValues values = new ContentValues();
        values.put(TasksContract.Columns.TASKS_DESCRIPTION, string);
        getContentResolver().update(TasksContract.buildTaskUri(task.getId()), values, null, null);
    }

    @Override
    public void saveCurrentTimeToDB(Task task, Integer integer) {
        ContentValues values = new ContentValues();
        values.put(TasksContract.Columns.TASKS_CURRENT_TIME, integer);
        getContentResolver().update(TasksContract.buildTaskUri(task.getId()), values, null, null);
    }

    @Override
    public void saveIsPlayingToDB(Task task, String isPlaying) {
        ContentValues values = new ContentValues();
        values.put(TasksContract.Columns.TASK_IS_PLAYING, isPlaying);
        getContentResolver().update(TasksContract.buildTaskUri(task.getId()), values, null, null);
    }

    @Override
    public void onFinishTask(Task task) {
        taskFinishRequest(task);
    }

    private void taskEditRequest(Task task) {

        if(mTwoPane) {

        } else {

            // in single-pane mode, start the detail activity for the selected item Id.
            Intent detailIntent = new Intent(this, AddEditActivity.class);
            if(task != null) { // editing a task
                detailIntent.putExtra(Task.class.getSimpleName(), task);
                startActivity(detailIntent);
            } else { // adding a new task
                startActivity(detailIntent);
            }
        }
    }

    private void taskFinishRequest(Task task) {

        if(mTwoPane) {

        } else {
            // in single-pane mode, start the detail activity for the selected item Id.
            Intent detailIntent = new Intent(this, FinishTimerActivity.class);
            if(task != null) { // editing a task
                detailIntent.putExtra(Task.class.getSimpleName(), task);
                startActivity(detailIntent);
            } else { // adding a new task
                startActivity(detailIntent);
            }
        }
    }

    private void settings() {

        Intent detailIntent = new Intent(this, SettingsActivity.class);
        detailIntent.putExtra( SettingsActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.NotificationPreferenceFragment.class.getName() );
        detailIntent.putExtra( SettingsActivity.EXTRA_NO_HEADERS, true );

        startActivity(detailIntent);

    }


    private Cursor mCursor;
    private CursorRecyclerViewAdapter.OnTaskClickListener mListener;

    public void deleteEntries() {
        CursorRecyclerViewAdapter.getDeleteMultipleButton();
    }




    @Override
    public void onDestroy() {
        CursorRecyclerViewAdapter.onDestroy();
        super.onDestroy();
    }

    public boolean getDeleteRequest() {
        return DELETE_REQUEST;
    }

    public void setDeleteRequest(boolean value) {
        DELETE_REQUEST = value;
    }

    public static Context getContext() {
        return mContext;
    }

}
