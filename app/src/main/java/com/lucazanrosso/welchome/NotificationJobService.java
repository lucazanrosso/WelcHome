package com.lucazanrosso.welchome;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationJobService extends JobService{

    SharedPreferences sharedPreferences;
    boolean alarmIsSet;
    boolean thiefIsEntered;
    int verificationCode = -1;

    FirebaseUser user;

    @Override
    public boolean onStartJob(JobParameters job) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.alarmIsSet = sharedPreferences.getBoolean("alarmIsSet", false);
        this.thiefIsEntered = sharedPreferences.getBoolean("thiefIsEntered", false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(user.getUid());

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean alarmIsSetDB = dataSnapshot.child("alarm_is_set").getValue(Boolean.class);
                boolean thiefIsEnteredDB = dataSnapshot.child("thief_is_entered").getValue(Boolean.class);
                int verificationCodeDB = dataSnapshot.child("verification_code").getValue(Integer.class);
                sharedPreferences.edit().putBoolean("alarmIsSet", alarmIsSetDB).apply();

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
