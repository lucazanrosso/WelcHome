package com.lucazanrosso.welchome;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;

public class MainActivity extends AppCompatActivity {

    SwitchCompat alarmSwitch;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        alarmSwitch = findViewById(R.id.alarm_switch);
        alarmSwitch.setChecked(sharedPreferences.getBoolean("alarmIsSet", false));
        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setDispatcher(b);
            }
        });
    }

    private void setDispatcher(boolean alarmIsSet) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        if (alarmIsSet) {
            Job myJob = dispatcher.newJobBuilder()
                    .setService(NotificationJobService.class) // the JobService that will be called
                    .setTag("my-unique-tag")        // uniquely identifies the job
                    .build();
            dispatcher.mustSchedule(myJob);
            sharedPreferences.edit().putBoolean("alarmIsSet", true).apply();
        } else {
            dispatcher.cancel("my-unique-tag");
            sharedPreferences.edit().putBoolean("alarmIsSet", false).apply();
        }
    }
}