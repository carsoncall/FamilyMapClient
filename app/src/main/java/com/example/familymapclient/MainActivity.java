package com.example.familymapclient;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.familymapclient.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import Result.LoginResult;

public class MainActivity extends AppCompatActivity implements LoginFragment.Listener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private LoginFragment loginFragment;
    private MapFragment mapFragment;
    private Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        loginFragment = (LoginFragment)fragmentManager.findFragmentById(R.id.fragmentFrameLayout);

        if (loginFragment == null) {
            loginFragment = new LoginFragment();
            loginFragment.registerListener(this);

            fragmentManager.beginTransaction()
                    .add(R.id.fragmentFrameLayout, loginFragment)
                    .commit();
        } else {
            if (loginFragment instanceof LoginFragment) {
                ((LoginFragment) loginFragment).registerListener(this);
            }
        }


//        //Creating the settings button and activity
//        settingsButton = findViewById(R.id.idBtnSettings);
//        settingsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
//                startActivity(i);
//            }
//        });
    }

    @Override
    public void notifyDone() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = new MapFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentFrameLayout, fragment)
                .commit();
    }
}