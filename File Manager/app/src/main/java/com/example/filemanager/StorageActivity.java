package com.example.filemanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class StorageActivity extends AppCompatActivity implements StoreCallBack {
    private RecyclerView recyclerView;
    private TextView noFilesText;

    private AdapterActivity mAdapter;
    private File mRootFile;

    private MenuItem mMenuSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        recyclerView = findViewById(R.id.recycler_view);
        noFilesText = findViewById(R.id.empty_text);

        String path = getIntent().getStringExtra("path");
        mRootFile = new File(path);
        File[] filesAndFolders = mRootFile.listFiles();

        if (filesAndFolders == null || filesAndFolders.length == 0) {
            noFilesText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noFilesText.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            setupRecyclerView(mRootFile, filesAndFolders);
        }
    }

    private void setupRecyclerView(File rootFile, File[] filesAndFolders) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        mAdapter = new AdapterActivity(getApplicationContext(), rootFile, filesAndFolders, this, builder);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void startChooseDestination() {
        mMenuSelect.setVisible(true);
        updateTitle(getString(R.string.title_start_choose_destination));
    }

    private void updateTitle(String newTitle) {
        getSupportActionBar().setTitle(newTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        mMenuSelect = menu.findItem(R.id.menu_select);
        mMenuSelect.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_select:{
                mAdapter.moveFileToSelectedFolder();
                //hide menu select
                mMenuSelect.setVisible(false);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(mAdapter != null){
            if(!mAdapter.goBack(mRootFile)){
                super.onBackPressed();
            }
        }
    }
}
