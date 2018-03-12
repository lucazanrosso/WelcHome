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
    int verificationCode;
    CountDownTimer countDownTimer;

    Context context;

    FirebaseUser user;
    DatabaseReference mDatabase;

    @Override
    public boolean onStartJob(JobParameters job) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        countDownTimer = new CountDownTimer(10000, 5000) {
            @Override
            public void onTick(long l) {
//                System.out.println(l/1000);
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
        mDatabase = FirebaseDatabase.getInstance().getReference().child(user.getUid());
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                verificationCode = dataSnapshot.child("verification_code").getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean alarmIsSetDB = dataSnapshot.child("alarm_is_set").getValue(Boolean.class);
                boolean thiefIsEnteredDB = dataSnapshot.child("thief_is_entered").getValue(Boolean.class);
                int verificationCodeDB = dataSnapshot.child("verification_code").getValue(Integer.class);

//                System.out.println(alarmIsSetDB);
//                System.out.println(thiefIsEnteredDB);
//                System.out.println(verificationCodeDB);
//                System.out.println(verificationCode);

                if (alarmIsSetDB) {
                    if (thiefIsEnteredDB && !sharedPreferences.getBoolean("thiefIsEntered", false)) {
                        setNotification("Alarm!", "A thief entered in your home");
                        sharedPreferences.edit().putBoolean("thiefIsEntered", true).apply();
                        sharedPreferences.edit().putString("colorSelected", "red").apply();
                        sharedPreferences.edit().putString("textSelected", context.getResources().getString(R.string.thief_is_entered)).apply();
                    } else if (verificationCode != verificationCodeDB) {
                        if (sharedPreferences.getString("colorSelected", "yellow").equals("yellow")) {
                            sharedPreferences.edit().putString("colorSelected", "green").apply();
                            sharedPreferences.edit().putString("textSelected", context.getResources().getString(R.string.its_all_ok)).apply();
                        }
                        if (sharedPreferences.getBoolean("nodeMCUProblems", false)) {
                            sharedPreferences.edit().putBoolean("nodeMCUProblems", false).apply();
                        }
                    }
                    if (!sharedPreferences.getBoolean("alarmIsSet", false))
                        sharedPreferences.edit().putBoolean("alarmIsSet", true).apply();
                    countDownTimer.start();
                    verificationCode = verificationCodeDB;
                } else {
                    countDownTimer.cancel();
                    setNotification("Attention!", " Someone has deactivated the alarm");
                    sharedPreferences.edit().putBoolean("alarmIsSet", false).apply();
                    sharedPreferences.edit().putString("colorSelected", "yellow").apply();
                    sharedPreferences.edit().putString("textSelected", context.getResources().getString(R.string.alarm_deactivated)).apply();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                countDownTimer.cancel();
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
