package com.lucazanrosso.welchome;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
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
    boolean thiefIsEntered;
    int verificationCode = -1;
    CountDownTimer countDownTimer;

    Context context;

    FirebaseUser user;

    @Override
    public boolean onStartJob(JobParameters job) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        countDownTimer = new CountDownTimer(10000, 5000) {
            @Override
            public void onTick(long l) {
                System.out.println(l/1000);
            }

            @Override
            public void onFinish() {
                setNotification("Attention!", "There are some problems with your nodeMCU");
                sharedPreferences.edit().putBoolean("nodeMCUProblems", true).apply();
                sharedPreferences.edit().putString("colorSelected", "yellow").apply();
                sharedPreferences.edit().putString("textSelected", context.getResources().getString(R.string.nodeMCU_problems)).apply();
            }
        };

        context = getApplicationContext();

        user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(user.getUid());

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean alarmIsSetDB = dataSnapshot.child("alarm_is_set").getValue(Boolean.class);
                sharedPreferences.edit().putBoolean("alarmIsSet", alarmIsSetDB).apply();
                thiefIsEntered = sharedPreferences.getBoolean("thiefIsEntered", false);
                boolean thiefIsEnteredDB = dataSnapshot.child("thief_is_entered").getValue(Boolean.class);
                int verificationCodeDB = dataSnapshot.child("verification_code").getValue(Integer.class);
                if (alarmIsSetDB) {
                    if (thiefIsEnteredDB && !thiefIsEntered) {
                        setNotification("Alarm!", "A thief entered in your home");
                        sharedPreferences.edit().putBoolean("thiefIsEntered", true).apply();
                        sharedPreferences.edit().putString("colorSelected", "red").apply();
                        sharedPreferences.edit().putString("textSelected", context.getResources().getString(R.string.thief_is_entered)).apply();
                    } else if (verificationCode != verificationCodeDB) {
                        countDownTimer.start();
                        if (sharedPreferences.getString("colorSelected", "yellow").equals("yellow")) {
                            sharedPreferences.edit().putString("colorSelected", "green").apply();
                            sharedPreferences.edit().putString("textSelected", context.getResources().getString(R.string.its_all_ok)).apply();
                        }
                        if (sharedPreferences.getBoolean("nodeMCUProblems", false)) {
                            sharedPreferences.edit().putBoolean("nodeMCUProblems", false).apply();
                        }
                    }
                    if (!sharedPreferences.getBoolean("alarmIsSet", false)) {
                        sharedPreferences.edit().putBoolean("alarmIsSet", true).apply();
                        countDownTimer.start();
                    }
                    verificationCode = verificationCodeDB;
                } else {
                    if (verificationCodeDB != -1) {
                        countDownTimer.cancel();
                        setNotification("Attention!", " Someone has deactivated the alarm");
                        sharedPreferences.edit().putBoolean("alarmIsSer", false).apply();
                        sharedPreferences.edit().putString("colorSelected", "yellow").apply();
                        sharedPreferences.edit().putString("textSelected", context.getResources().getString(R.string.alarm_deactivated)).apply();
                        verificationCode = -1;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });

        return true; // Answers the question: "Is there still work going on?"
    }

    public void setNotification(String title, String text) {
        Intent notificationIntent = new Intent(context, MyNotification.class);
        notificationIntent.putExtra("notification_title", title);
        notificationIntent.putExtra("notification_text", text);
        context.sendBroadcast(notificationIntent);
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; // Answers the question: "Should this job be retried?"
    }

}
