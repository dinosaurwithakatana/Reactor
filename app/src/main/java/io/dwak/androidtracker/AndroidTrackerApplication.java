package io.dwak.androidtracker;

import android.app.Application;

import io.dwak.tracker.Tracker;

/**
 * Created by vishnu on 12/17/14.
 */
public class AndroidTrackerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Tracker.init();
    }
}