package com.lucazanrosso.welchome;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.Toast;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SwitchCompat alarmSwitch;
    SharedPreferences sharedPreferences;

    private static final int RC_SIGN_IN = 123;
    FirebaseUser user;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
//        alarmSwitch = findViewById(R.id.alarm_switch);
//        alarmSwitch.setChecked(sharedPreferences.getBoolean("alarmIsSet", false));
//        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                setDispatcher(b);
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().getCurrentUser();
                mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild(user.getUid())) {
                            mDatabase.child(user.getUid()).child("alarm_is_set").setValue(true);
                            mDatabase.child(user.getUid()).child("thief_is_entered").setValue(false);
                            mDatabase.child(user.getUid()).child("verification_code").setValue(0);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // ...
            } else {
                // Sign in failed, check response for error code
                Toast.makeText(this,"Sign in Failed", Toast.LENGTH_SHORT).show();
            }
        }
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