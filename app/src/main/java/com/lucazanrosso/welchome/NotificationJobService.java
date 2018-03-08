package com.lucazanrosso.welchome;

import android.content.Context;
import android.content.SharedPreferences;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationJobService extends JobService{

    SharedPreferences sharedPreferences;
    boolean alarmIsWorking;
    boolean thiefIsEntered;
    int verificationCode = -1;

    @Override
    public boolean onStartJob(JobParameters job) {

        sharedPreferences = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        this.alarmIsWorking = sharedPreferences.getBoolean("alarmIsWorking", false);
        this.thiefIsEntered = sharedPreferences.getBoolean("thiefIsEntered", false);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("alarm_is_working").setValue(true);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean alarmIsWorkingDB = dataSnapshot.child("alarm_is_working").getValue(Boolean.class);
                boolean thiefIsEnteredDB = dataSnapshot.child("thief_is_entered").getValue(Boolean.class);
                int verificationCodeDB = dataSnapshot.child("alarm_is_working").getValue(Integer.class);
                if (alarmIsWorking != alarmIsWorkingDB) {
                    if (alarmIsWorkingDB) {

                    } else {

                    }
                    sharedPreferences.edit().putBoolean("alarmIsWorking", alarmIsWorkingDB).apply();
                }
                if (thiefIsEntered != thiefIsEnteredDB) {
                    if (thiefIsEnteredDB) {

                    } else {

                    }
                    sharedPreferences.edit().putBoolean("thiefIsEntered", alarmIsWorkingDB).apply();
                }
                if (verificationCode == verificationCodeDB) {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return true; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; // Answers the question: "Should this job be retried?"
    }

}
