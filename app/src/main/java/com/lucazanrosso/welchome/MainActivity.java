package com.lucazanrosso.welchome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    SwitchCompat alarmSwitch;
    AppCompatImageView imageView;
    AppCompatTextView textView;
    SharedPreferences sharedPreferences;
    DatabaseReference mDatabase;
    FirebaseUser user;
    FirebaseJobDispatcher dispatcher;

    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSignInButtons();
    }

    private void setSignInButtons() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        if (sharedPreferences.getBoolean("isSignedIn", false)) {
            setLayout();
        } else {
            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                sharedPreferences.edit().putBoolean("isSignedIn", true).apply();
                setLayout();
            } else if (resultCode == RESULT_CANCELED) {
                finishAffinity();
            } else {
                // Sign in failed, check response for error code
                Toast.makeText(this,"Sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setLayout() {
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(user.getUid());

        alarmSwitch = findViewById(R.id.alarm_switch);
        alarmSwitch.setChecked(sharedPreferences.getBoolean("alarmIsSet", false));
        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mDatabase.child("alarm_is_set").setValue(b);
                sharedPreferences.edit().putBoolean("alarmIsSet", b).apply();
                sharedPreferences.edit().putString("colorSelected", "yellow").apply();
                if (b) {
                    sharedPreferences.edit().putString("textSelected", getResources().getString(R.string.waiting_nodeMCU)).apply();
                } else {
                    mDatabase.child("thief_is_entered").setValue(false);
                    sharedPreferences.edit().putBoolean("thiefIsEntered", false).apply();
                    sharedPreferences.edit().putString("textSelected", getResources().getString(R.string.alarm_deactivated)).apply();
                }
            }
        });

        imageView = findViewById(R.id.alarm_image);
        textView = findViewById(R.id.alarm_text);
        setColor(sharedPreferences.getString("colorSelected", "yellow"));
        textView.setText(sharedPreferences.getString("textSelected", getResources().getString(R.string.alarm_deactivated)));

        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        if (!sharedPreferences.getBoolean("justSignedIn", false)) {
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild("alarm_is_set")) {
                        mDatabase.child("alarm_is_set").setValue(false);
                        mDatabase.child("thief_is_entered").setValue(false);
                        mDatabase.child("verification_code").setValue(0);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            Job myJob = dispatcher.newJobBuilder()
                    .setService(NotificationJobService.class) // the JobService that will be called
                    .setTag("my-unique-tag")        // uniquely identifies the job
                    .build();
            dispatcher.mustSchedule(myJob);
            sharedPreferences.edit().putBoolean("justSignedIn", true).apply();
        }
    }

    public void setColor(String color) {
        switch (color) {
            case "green":
                imageView.setColorFilter(ContextCompat.getColor(this, R.color.green));
                break;
            case "yellow":
                System.out.println("yellow");
                imageView.setColorFilter(ContextCompat.getColor(this, R.color.yellow));
                break;
            case "red":
                imageView.setColorFilter(ContextCompat.getColor(this, R.color.red));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        dispatcher.cancel("my-unique-tag");
        sharedPreferences.edit().putBoolean("isSignedIn", false).apply();
        sharedPreferences.edit().putBoolean("justSignedIn", false).apply();
        setSignInButtons();
        int id = item.getItemId();
        if (id == R.id.action_sign_out) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                        }
                    });
            return true;
        } else if (id == R.id.action_delete_account) {
            AuthUI.getInstance()
                    .delete(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "alarmIsSet":
                alarmSwitch.setChecked(sharedPreferences.getBoolean(key,false));
                break;
            case "colorSelected":
                setColor(sharedPreferences.getString(key, "yellow"));
                break;
            case "textSelected":
                textView.setText(sharedPreferences.getString(key, ""));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }
}
