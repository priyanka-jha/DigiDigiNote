package com.example.diginote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.diginote.R;
import com.example.diginote.databinding.ActivitySettingsBinding;
import com.example.diginote.sharedpref.SharedPref;

public class SettingsActivity extends AppCompatActivity {


    SharedPref sharedPrefren;
    public static final String TAG = SettingsActivity.class.getSimpleName();

    ActivitySettingsBinding activitySettingsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPrefren = new SharedPref(getApplicationContext());

        try {
            if (sharedPrefren.loadNightModeState() == true) {
                setTheme(R.style.DarkTheme);
            } else {
                setTheme(R.style.AppTheme);
            }
        } catch (Exception e) {
            Log.e(TAG,e.toString());

        }

        super.onCreate(savedInstanceState);


        activitySettingsBinding =  ActivitySettingsBinding.inflate(getLayoutInflater());
        View view = activitySettingsBinding.getRoot();
        setContentView(view);

        Toolbar toolbar = activitySettingsBinding.layoutToolbar.toolbar;
        setSupportActionBar(toolbar);


        try {
            if (sharedPrefren.loadNightModeState() == true) {
                activitySettingsBinding.switchmode.setChecked(true);
            }
        } catch (Exception e) {
            Log.e(TAG,e.toString());

        }

        activitySettingsBinding.switchmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        sharedPrefren.setNightModeState(true);
                        restartApp();
                    } catch (Exception e) {
                        Log.e(TAG,e.toString());

                    }
                } else {
                    try {
                        sharedPrefren.setNightModeState(false);
                        restartApp();
                    } catch (Exception e) {
                        Log.e(TAG,e.toString());

                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (sharedPrefren.loadNightModeState() == true) {
                setTheme(R.style.DarkTheme);
            } else {
                setTheme(R.style.AppTheme);
            }
        } catch (Exception e) {
            Log.e(TAG,e.toString());

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void restartApp() {

        try {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG,e.toString());

        }

    }


}
