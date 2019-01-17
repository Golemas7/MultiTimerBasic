package com.offficial.kalisstudiodev.mytimerapp;

import java.io.Serializable;

class Task implements Serializable {
    public static final long serialVersionUID = 20161120L;

    private long m_Id;
    private final String mName;
    private final String mDescription;
    private final int mTime;
    private int mCurrentTime;
    private final String mIsPlaying;

    public Task(long id, String name, String description, int time, int currentTime, String isPlaying) {
        this.m_Id = id;
        mName = name;
        mDescription = description;
        mTime = time;
        mCurrentTime = currentTime;
        mIsPlaying = isPlaying;
    }

    public long getId() {
        return m_Id;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public int getTime() {
        return mTime;
    }

    public int getCurrentTime() {
        return mCurrentTime;
    }

    public void setCurrentTime(int currentTime) {
        mCurrentTime = currentTime;
    }

    public void setId(long id) {
        this.m_Id = id;
    }

    public String getIsPlaying() {
        return mIsPlaying;
    }

    @Override
    public String toString() {
        return "Task{" +
                "m_Id=" + m_Id +
                ", mName='" + mName + '\'' +
                ", mDescription='" + mDescription + '\'' +
                ", mTime=" + mTime +
                ", mCurrentTime=" + mCurrentTime +
                ", mIsPlaying=" + mIsPlaying +
                '}';
    }


}