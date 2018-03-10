package com.lucazanrosso.welchome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean("isSignedIn", false)) {
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
            Job myJob = dispatcher.newJobBuilder()
                    .setService(NotificationJobService.class) // the JobService that will be called
                    .setTag("my-unique-tag")        // uniquely identifies the job
                    .build();
            dispatcher.mustSchedule(myJob);
            sharedPreferences.edit().putBoolean("isSignedIn", true).apply();
        }
    }
}