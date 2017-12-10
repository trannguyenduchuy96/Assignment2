package com.example.admin.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onClickToMaps (View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Main tag", "now running onStart");
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Main tag", "now running onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Main tag", "now running onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("Main tag", "now running onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Main tag", "now running onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Main tag", "now running onDestroy");
    }
}
