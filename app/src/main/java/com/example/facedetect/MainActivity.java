package com.example.facedetect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.facedetect.helpers.ImageHelperActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startOps(View view) {
        Intent intent = new Intent(this, ImageHelperActivity.class);
        startActivity(intent);
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
    }
}