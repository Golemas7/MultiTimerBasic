package com.offficial.kalisstudiodev.mytimerapp;


import android.database.Cursor;
import android.os.CountDownTimer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.offficial.kalisstudiodev.mytimerapp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.TaskViewHolder> {
    private static final String TAG = "CursorRecyclerViewAdapt";
    private Cursor mCursor;
    private static OnTaskClickListener mListener;

    private static ImageButton mStartButton;

    private static Button mDeleteMultipleButton;

    private List<Task> taskList = new ArrayList<>();    // Used for delete multiple
    private List<View> viewList = new ArrayList<>();    // Used for delete multiple
    private List<CountDownTimer> timerList = new ArrayList<>();     // Used for delete multiple
    private List<TaskViewHolder> holderList = new ArrayList<>();    // Used for delete multiple


    private static ArrayList<TaskViewHolder> previousHolderList;    // MAYBE used for delete button
    private static ArrayList<Integer> timeInTimers;                 // Used in cTimers


    private static ArrayList<Integer> deletedHolderListPlacement;
    private static ArrayList<TaskViewHolder> taskViewHolderList;
    private static ArrayList<Task> tasksList;                       // used for delete single

    private static Integer previousTaksListSize;

    private static Boolean wasDestroyed = true;

    private static Integer firstLoad;


    interface OnTaskClickListener {
        void onEditClick(Task task);
        void onDeleteClick(Task task);
        void saveIsPlayingToDB(Task task, String isPlaying);
        void saveDescriptionToDB(Task task, String string);
        void onFinishTask(Task task);
        void saveCurrentTimeToDB(Task task, Integer integer);
    }


    public CursorRecyclerViewAdapter(Cursor cursor, OnTaskClickListener listener) {

        mCursor = cursor;
        mListener = listener;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timer_list_items, parent, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder holder, int position) {

        // EXPERIMENTAL CODE BELOW ↓↓↓↓↓

        if (previousHolderList == null) {
            previousHolderList = new ArrayList<>();
        }

        if (timeInTimers == null) {
            timeInTimers = new ArrayList<>();
        }

        if (deletedHolderListPlacement == null) {
            deletedHolderListPlacement = new ArrayList<>();
        }

        if (taskViewHolderList == null) {
            taskViewHolderList = new ArrayList<>();
        }

        if (tasksList == null) {
            tasksList = new ArrayList<>();
        }

        if (previousTaksListSize == null) {
            previousTaksListSize = 0;
        }

        // EXPERIMENTAL CODE ABOVE ↑↑↑↑↑



        if((mCursor == null) || (mCursor.getCount() == 0)) {
            // If there are not tasks or this is the first startup, display the instructions

            holder.name.setText("INSTRUCTIONS\n\n\nUse the add Task button in the menu or the floating action button '+' to create new tasks.\n\n\nTo delete a task, press on it until it is deleted or use the menu item remove timer(s) to delete one or several tasks.\n"/*R.string.instructions_heading*/);
            holder.description.setText("");
            holder.time.setText("");
            holder.restartButton.setVisibility(View.GONE);
            holder.startButton.setVisibility(View.GONE);
        } else {
            if(!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("Couldn't move cursor to position " + position);
            }

            final Task task = new Task(mCursor.getLong(mCursor.getColumnIndex(TasksContract.Columns._ID)),
                    mCursor.getString(mCursor.getColumnIndex(TasksContract.Columns.TASKS_NAME)),
                    mCursor.getString(mCursor.getColumnIndex(TasksContract.Columns.TASKS_DESCRIPTION)),
                    mCursor.getInt(mCursor.getColumnIndex(TasksContract.Columns.TASKS_TIME)),
                    mCursor.getInt(mCursor.getColumnIndex(TasksContract.Columns.TASKS_CURRENT_TIME)),
                    mCursor.getString(mCursor.getColumnIndex(TasksContract.Columns.TASK_IS_PLAYING))
            );

            holder.name.setText(task.getName());
            holder.description.setText(task.getDescription());

            // EXPERIMENTAL CODE BELOW ↓↓↓↓↓

            // TODO check the code bellow

            if (previousTaksListSize == 0) {
                firstLoad = 0;
                previousTaksListSize = getItemCount();
            }

            if (previousTaksListSize != getItemCount()) {
                tasksList.clear();
                previousTaksListSize = getItemCount();
            }

            if (tasksList.size() == getItemCount()) {
                tasksList.clear();
            }

            tasksList.add(task);


            Boolean isHolderInList = false;

            for (Iterator<TaskViewHolder> iter = taskViewHolderList.listIterator(); iter.hasNext(); ) {
                TaskViewHolder mTask = iter.next();
                if (mTask == holder) {
                    isHolderInList = true;
                }
            }

            if (!isHolderInList) {
                taskViewHolderList.add(holder);
            }


            holder.restartButton.setVisibility(View.VISIBLE);
            holder.startButton.setVisibility(View.VISIBLE);

            if (!task.getIsPlaying().equals("true")) {

                holder.time.setText(task.getCurrentTime() / 3600 > 1 ?
                        String.format(Locale.getDefault(),"%02d:%02d:%02d", task.getCurrentTime() / 3600, task.getCurrentTime() % 3600 / 60, task.getCurrentTime() % 60) :
                        String.format(Locale.getDefault(),"%02d:%02d", task.getCurrentTime() % 3600 / 60, task.getCurrentTime() % 60));
            }

            if (task.getIsPlaying().equals("true") && !holder.isRunning && holder.cTimer == null && !wasDestroyed) {

                createTimer(holder, task);
                holder.cTimer.start();

                holder.isRunning = true;

                holder.startButton.setVisibility(View.GONE);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.restartButton.setVisibility(View.VISIBLE);

            } else if (task.getIsPlaying().equals("false")){

                holder.startButton.setVisibility(View.VISIBLE);
                holder.pauseButton.setVisibility(View.GONE);
                holder.resumeButton.setVisibility(View.GONE);
                holder.restartButton.setVisibility(View.VISIBLE);
            }



            if (holder.cTimer == null && task.getIsPlaying().equals("true") && holder.isRunning) {

                createTimer(holder, task);

                holder.cTimer.start();

                holder.startButton.setVisibility(View.GONE);
                holder.pauseButton.setVisibility(View.VISIBLE);
                holder.resumeButton.setVisibility(View.GONE);
                holder.restartButton.setVisibility(View.VISIBLE);

            }

            if (task.getIsPlaying().equals("paused")) {
                holder.startButton.setVisibility(View.GONE);
                holder.pauseButton.setVisibility(View.GONE);
                holder.resumeButton.setVisibility(View.VISIBLE);
                holder.restartButton.setVisibility(View.VISIBLE);
            }


            firstLoad++;
            if (firstLoad == getItemCount()) {
                wasDestroyed = false;
                firstLoad = -100000000;
            }

            // EXPERIMENTAL CODE ABOVE ↑↑↑↑↑


            View.OnLongClickListener longItemListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    switch (v.getId()) {
                        case R.id.tli_item:
                            if(mListener != null) {
//
                                // EXPERIMENTAL CODE BELOW ↓↓↓↓↓

                                deletedHolderListPlacement.clear();
                                deletedHolderListPlacement.add(holder.getLayoutPosition());

                                for (Iterator<TaskViewHolder> iter = taskViewHolderList.listIterator(deletedHolderListPlacement.get(0)); iter.hasNext(); ) {
                                    TaskViewHolder mTask = iter.next();

                                    try {

                                        TaskViewHolder next = iter.next();

                                        if (next.isRunning) {

                                            if (mTask.cTimer == null) {

                                            } else {
                                                mTask.cTimer.cancel();
                                                mTask.cTimer.onFinish();
                                                mTask.isRunning = false;
                                            }


                                            next.isRunning = false;

                                            mListener.saveCurrentTimeToDB(tasksList.get(next.getLayoutPosition()), next.currentTime);

                                            next.cTimer.cancel();
                                            next.cTimer.onFinish();
                                            next.cTimer = null;

                                            mTask.isRunning = true;
                                            mListener.saveIsPlayingToDB(tasksList.get(next.getLayoutPosition()), "true");

                                        } else {
                                            if ((next.currentTime != tasksList.get(next.getLayoutPosition()).getTime()) && next.currentTime > 0) {

                                                if (mTask.cTimer != null) {
                                                    mTask.cTimer.cancel();
                                                    mTask.cTimer.onFinish();
                                                    mTask.cTimer = null;

                                                    mTask.isRunning = false;
                                                }


                                                mTask.startButton.setVisibility(View.GONE);
                                                mTask.pauseButton.setVisibility(View.GONE);
                                                mTask.resumeButton.setVisibility(View.VISIBLE);

                                                mTask.time.setText("00:00");

                                            } else {

                                                if (mTask.cTimer != null) {
                                                    mTask.cTimer.cancel();
                                                    mTask.cTimer.onFinish();
                                                    mTask.cTimer = null;
                                                }

                                                mListener.saveIsPlayingToDB(tasksList.get(next.getLayoutPosition()), "false");

                                                mTask.isRunning = false;

                                                mTask.startButton.setVisibility(View.VISIBLE);
                                                mTask.pauseButton.setVisibility(View.GONE);
                                                mTask.resumeButton.setVisibility(View.GONE);
                                            }
                                        }


                                        ((ListIterator<TaskViewHolder>) iter).previous();

                                    } catch (Exception e) {
                                        if (mTask.cTimer != null) {
                                            mTask.cTimer.cancel();
                                            mTask.cTimer.onFinish();
                                            mTask.cTimer = null;

                                            mTask.isRunning = false;
                                        }

                                        mTask.startButton.setVisibility(View.VISIBLE);
                                        mTask.pauseButton.setVisibility(View.GONE);
                                        mTask.resumeButton.setVisibility(View.GONE);
                                    }

                                }



                                if (holder.cTimer != null) {
                                    holder.restartButton.performClick();
                                    holder.cTimer = null;
                                }

                                tasksList.remove(task);

                                // EXPERIMENTAL CODE ABOVE ↑↑↑↑↑




                                mListener.onDeleteClick(task);
                            }

                        default:

                    }

                    return true;
                }
            };

            View.OnClickListener buttonListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//
                    switch(view.getId()) {
                        case R.id.tli_restart:
                            if(mListener != null) {
                                if (holder.cTimer == null) {
                                    createTimer(holder, task);
                                }

                                holder.currentTime = -1;
                                holder.cTimer.cancel();
                                holder.cTimer.onFinish();
                                holder.cTimer  = null;
                                holder.isRunning = false;

                                mListener.saveCurrentTimeToDB(task, task.getTime());
                                mListener.saveIsPlayingToDB(task, "false");

                                holder.isRunning = false;


                                holder.startButton.setVisibility(View.VISIBLE);
                                holder.pauseButton.setVisibility(View.GONE);
                            }
                            break;
                        case R.id.tli_play:
                            if(mListener != null) {

                                createTimer(holder, task);
                                holder.cTimer.start();

                                holder.isRunning = true;
                                mListener.saveIsPlayingToDB(task, "true");

                                holder.startButton.setVisibility(View.GONE);
                                holder.pauseButton.setVisibility(View.VISIBLE);
                                holder.restartButton.setVisibility(View.VISIBLE);
                            }
                            break;
                        case R.id.tli_stop:
                            if (mListener != null) {

                                holder.cTimer.cancel();
                                holder.cTimer.onFinish();
                                holder.cTimer = null;

                                mListener.saveIsPlayingToDB(task, "paused");

                                mListener.saveCurrentTimeToDB(task, holder.currentTime);
                                holder.isRunning = false;

                                holder.pauseButton.setVisibility(View.GONE);
                                holder.resumeButton.setVisibility(View.VISIBLE);
                            }
                            break;
                        case R.id.tli_resume:
                            if (mListener != null) {

                                createTimer(holder, task);
                                holder.cTimer.start();

                                mListener.saveIsPlayingToDB(task, "true");
                                holder.isRunning = true;

                                holder.resumeButton.setVisibility(View.GONE);
                                holder.pauseButton.setVisibility(View.VISIBLE);
                            }
                            break;
                        case R.id.tli_item:
                            if(mListener != null && !MainActivity.DELETE_REQUEST) {
                                mListener.onEditClick(task);
                            } else if (mListener != null && MainActivity.DELETE_REQUEST) {


                                if (!holder.isSelected) {
                                    holder.isSelected = true;
                                    holder.itemView.setBackgroundColor(MainActivity.getContext().getResources().getColor(R.color.colorMarked));

                                    taskList.add(task);
                                    viewList.add(holder.itemView);
                                    timerList.add(holder.cTimer);
                                    holderList.add(holder);

                                    mDeleteMultipleButton = holder.deleteButton;

                                } else {
                                    holder.isSelected = false;
                                    holder.itemView.setBackgroundColor(MainActivity.getContext().getResources().getColor(R.color.colorWhite));

                                    for (Iterator<Task> iter = taskList.listIterator(); iter.hasNext(); ) {
                                        Task mTask = iter.next();
                                        if (mTask == task) {
                                            iter.remove();
                                        }
                                    }

                                    for (Iterator<View> iter = viewList.listIterator(); iter.hasNext(); ) {
                                        View mTask = iter.next();
                                        if (mTask == holder.itemView) {
                                            iter.remove();
                                        }
                                    }

                                    for (Iterator<CountDownTimer> iter = timerList.listIterator(); iter.hasNext(); ) {
                                        CountDownTimer mTask = iter.next();
                                        if (mTask == holder.cTimer) {
                                            iter.remove();
                                        }
                                    }

                                    for (Iterator<TaskViewHolder> iter = holderList.listIterator(); iter.hasNext(); ) {
                                        TaskViewHolder mTask = iter.next();
                                        if (mTask == holder) {
                                            iter.remove();
                                        }
                                    }

                                }

                            }
                            break;
                        case R.id.tli_deleteMultiple:

                            deletedHolderListPlacement.clear();

                            for (int i = 0; i < holderList.size(); i++) {
                                deletedHolderListPlacement.add(holderList.get(i).getLayoutPosition());
                            }

                            Collections.sort(deletedHolderListPlacement);


                            // EXPERIMENTAL CODE BELOW ↓↓↓↓↓

                            if ( deletedHolderListPlacement.size() > 1) {

                                int indx = 0;
                                for (int i = 0; i < deletedHolderListPlacement.size() - 1; i++) {
                                    taskViewHolderList.get(deletedHolderListPlacement.get(i)-indx).item.performLongClick();
                                    indx++;
                                }

                            } else {
                                holder.item.performLongClick();
                            }

                            // EXPERIENTAL CODE ABOVE↑↑↑↑↑↑









                            for (Iterator<TaskViewHolder> iter = taskViewHolderList.listIterator(); iter.hasNext(); ) {
                                TaskViewHolder mTask = iter.next();

                                try {

                                    mTask.currentTime = -1;
                                    mTask.cTimer.cancel();
                                    mTask.cTimer.onFinish();
                                    mTask.cTimer = null;

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                iter.remove();
                            }



                            if (holder.cTimer != null) {
                                holder.currentTime = -1;
                                holder.cTimer.cancel();
                                holder.cTimer.onFinish();
                                holder.cTimer = null;
                            }



                            resetColors(viewList);
                            resetTimers(timerList, holderList);
                            deleteMultiple(taskList);
                            break;
                        default:

                    }
                }
            };


            holder.restartButton.setOnClickListener(buttonListener);
            holder.startButton.setOnClickListener(buttonListener);
            holder.pauseButton.setOnClickListener(buttonListener);
            holder.resumeButton.setOnClickListener(buttonListener);
            holder.deleteButton.setOnClickListener(buttonListener);

            holder.item.setOnClickListener(buttonListener);
            holder.item.setOnLongClickListener(longItemListener);

        }


    }

    @Override
    public int getItemCount() {

        if((mCursor == null) || (mCursor.getCount() == 0)) {
            return 1; // fib, because we populate a single ViewHolder with instructions
        } else {
            return mCursor.getCount();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.
     * The returned old Cursor is <em>not</em> closed.
     *
     * @param newCursor The new cursor to be used
     * @return Returns the previously set Cursor, or null if there wasn't one.
     * If the given new Cursor is the same instance as the previously set
     * Cursor, null is also returned.
     */
    Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }

        final Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if(newCursor != null) {
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            // notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, getItemCount());
        }
        return oldCursor;

    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "TaskViewHolder";

        TextView name = null;
        TextView description = null;
        TextView time = null;
        ImageButton restartButton = null;
        ImageButton startButton = null;
        ImageButton pauseButton = null;
        ImageButton resumeButton = null;
        Button deleteButton = null;
        View item = null;

        // timer specific things
        CountDownTimer cTimer = null;
        int currentTime = 0;

        // isPlaying for each item
        Boolean isRunning = false;

        // select for each item
        Boolean isSelected = false;


        public TaskViewHolder(View itemView) {
            super(itemView);


            this.name = itemView.findViewById(R.id.tli_name);
            this.description = itemView.findViewById(R.id.tli_info);
            this.time  = itemView.findViewById(R.id.tli_timer);
            this.restartButton = itemView.findViewById(R.id.tli_restart);
            this.startButton = itemView.findViewById(R.id.tli_play);
            this.pauseButton = itemView.findViewById(R.id.tli_stop);
            this.resumeButton = itemView.findViewById(R.id.tli_resume);

            this.deleteButton = itemView.findViewById(R.id.tli_deleteMultiple);

            this.item = itemView.findViewById(R.id.tli_item);


        }
    }




    public void createTimer(final  TaskViewHolder holder, final Task task) {

        holder.cTimer = new CountDownTimer(task.getCurrentTime() * 1000, 500) {
            @Override
            public void onTick(long millisUntilFinished) {

                holder.currentTime = ((int) millisUntilFinished) / 500;

                if (holder.currentTime == task.getCurrentTime() * 2) {
                    holder.currentTime -= 1;
                }


                if (holder.currentTime % 2 != 0) {
                    holder.currentTime = holder.currentTime / 2;
                    // If it wants to print the original, make it print the second one


                    holder.time.setText(holder.currentTime / 3600 > 1 ?
                            String.format(Locale.getDefault(), "%02d:%02d:%02d", holder.currentTime / 3600, holder.currentTime % 3600 / 60, holder.currentTime % 60) :
                            String.format(Locale.getDefault(), "%02d:%02d", holder.currentTime % 3600 / 60, holder.currentTime % 60));
                } else {
                    holder.currentTime = holder.currentTime / 2;
                }


                if (timeInTimers.size() <= holder.getLayoutPosition()) {
                    timeInTimers.add(holder.currentTime);
                } else {

                    try {
                        timeInTimers.set(holder.getLayoutPosition(), holder.currentTime);
                    } catch (Exception e) {

                        // if the place does not exist, stop that timer

                        holder.currentTime = -1;
                        holder.cTimer.cancel();
                        holder.cTimer.onFinish();

                        e.printStackTrace();
                    }


                }

            }

            @Override
            public void onFinish() {

                String timeNow =
                        ((Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 9) ? (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) : ("0" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY))) + ":"
                                + (Calendar.getInstance().get(Calendar.MINUTE) > 9 ? Calendar.getInstance().get(Calendar.MINUTE) : "0" + Calendar.getInstance().get(Calendar.MINUTE)) + " "
                                + (Calendar.getInstance().get(Calendar.MONTH) + 1 > 9 ? (Calendar.getInstance().get(Calendar.MONTH) + 1) : "0" + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "/"
                                + (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) > 9 ? Calendar.getInstance().get(Calendar.DAY_OF_MONTH) : "0" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));

                if (holder.currentTime < 1) {

                    holder.startButton.setVisibility(View.VISIBLE);
                    holder.pauseButton.setVisibility(View.GONE);
                    holder.resumeButton.setVisibility(View.GONE);

                    holder.time.setText(task.getCurrentTime() / 3600 > 1 ?
                            String.format(Locale.getDefault(),"%02d:%02d:%02d", task.getCurrentTime() / 3600, task.getCurrentTime() % 3600 / 60, task.getCurrentTime() % 60) :
                            String.format(Locale.getDefault(),"%02d:%02d", task.getCurrentTime() % 3600 / 60, task.getCurrentTime() % 60));

                    if (holder.currentTime == 0) {
                        holder.description.setText("Ended at: " + timeNow);

                        if (!holder.description.getText().toString().equals(task.getDescription())) {
                            mListener.saveDescriptionToDB(task, holder.description.getText().toString());
                        }

                        mStartButton = holder.startButton;
                        mListener.onFinishTask(task);


                        holder.isRunning = false;
                        mListener.saveIsPlayingToDB(task, "false");

                    } else if (holder.currentTime == -1) {

                    }

                } else {
                    mListener.saveCurrentTimeToDB(task, holder.currentTime);
                }
            }

        };
    }



    public static ImageButton getPlayButton() {
        return mStartButton;
    }

    public static Boolean getDeleteMultipleButton() {

        try {

            mDeleteMultipleButton.performClick();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public void deleteMultiple(List<Task> taskList) {

        for (int i = 0; i < taskList.size(); i++) {
            mListener.onDeleteClick(taskList.get(i));
        }
    }

    public void resetColors(List<View> viewList) {

        for (int i = 0; i < viewList.size(); i++) {
            viewList.get(i).setBackgroundColor(MainActivity.getContext().getResources().getColor(R.color.colorWhite));
        }
    }

    public void resetTimers(List<CountDownTimer> timerList, List<TaskViewHolder> holderList) {

        try {
            for (int i = 0; i < holderList.size(); i++) {
                try {
                    holderList.get(i).currentTime = -1;

                }  catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < timerList.size(); i++) {
                try {
                    timerList.get(i).cancel();
                    timerList.get(i).onFinish();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<Task> getTaskList() {
        return taskList;
    }


    public void setTimers() {

    }


    public static void onDestroy() {

        Integer i = 0;

        for (Iterator<Task> iter = tasksList.listIterator(); iter.hasNext(); ) {
            Task mTask = iter.next();
            if (taskViewHolderList.get(i).cTimer != null) {
                taskViewHolderList.get(i).cTimer.cancel();
                taskViewHolderList.get(i).cTimer.onFinish();
                taskViewHolderList.get(i).cTimer = null;
            }
            i++;
        }

        wasDestroyed = true;
        firstLoad = 0;
        taskViewHolderList.clear();
        tasksList.clear();


    }


}



