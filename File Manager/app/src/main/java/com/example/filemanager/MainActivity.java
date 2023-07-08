package com.example.filemanager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSION_STORAGE = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton storageBtn = findViewById(R.id.storage_button);

        storageBtn.setOnClickListener(v -> {
            //Check permission allowed or not
            if (checkPermission()) {
                startActivity(new Intent(MainActivity.this, StorageActivity.class)
                        .putExtra("path", Environment.getExternalStorageDirectory().getPath()));
            } else {
                requestPermission();
            }
        });
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }else{
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
            startActivity(intent);
        }else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Storage permission is required, please allow from settings", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_STORAGE);
            }
        }
    }
}