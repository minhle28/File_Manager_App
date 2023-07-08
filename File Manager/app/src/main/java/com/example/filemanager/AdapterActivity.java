package com.example.filemanager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;

import android.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import androidx.recyclerview.widget.RecyclerView;

public class AdapterActivity extends RecyclerView.Adapter<AdapterActivity.ViewHolder> {
    private Context context;
    private File[] filesAndFolders;
    private File mRootFile;
    private StoreCallBack mCallBack;
    private AlertDialog.Builder builder;

    private File mCurrentSelectedFile;

    public AdapterActivity(Context context, File rootFile, File[] filesAndFolders, StoreCallBack callBack, AlertDialog.Builder B) {
        this.context = context;
        this.filesAndFolders = filesAndFolders;
        Arrays.sort(this.filesAndFolders);
        this.builder = B;
        this.mRootFile = rootFile;
        this.mCallBack = callBack;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterActivity.ViewHolder holder, int position) {
        File selectedFile = filesAndFolders[position];
        holder.textView.setText(selectedFile.getName());

        if (selectedFile.isDirectory()) {
            holder.imageView.setImageResource(R.drawable.baseline_folder_24);
        } else {
            holder.imageView.setImageResource(R.drawable.baseline_insert_drive_file_24);
        }

        holder.itemView.setOnClickListener(view -> {
            if (selectedFile.isDirectory()) {
                /*Intent intent = new Intent(context, StorageActivity.class);
                intent.putExtra("path", selectedFile.getAbsolutePath());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);*/
                mRootFile = selectedFile;
                //update data
                updateData();
            } else {
                // Open the file
                try {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    String type = "image/*";
                    intent.setDataAndType(Uri.parse(selectedFile.getAbsolutePath()), type);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Cannot open the file", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.itemView.setOnLongClickListener(view -> {

            PopupMenu popupMenu = new PopupMenu(context, view, Gravity.END);
            popupMenu.getMenu().add("RENAME");
            popupMenu.getMenu().add("MOVE");
            popupMenu.getMenu().add("DELETE");

            popupMenu.setOnMenuItemClickListener(item -> {

                if (item.getTitle().equals("RENAME")) {
                    // Set up the input
                    final EditText input = new EditText(view.getContext());
                    input.setInputType(InputType.TYPE_CLASS_TEXT);

                    //Popup alertdialog for rename
                    builder.setTitle("Rename")
                            .setMessage("Enter the new name:")
                            .setView(input)
                            .setPositiveButton("OK", (dialog, which) -> {
                                String newName = input.getText().toString().trim();
                                if (!TextUtils.isEmpty(newName)) {
                                    File newFile = new File(selectedFile.getParent(), newName);
                                    if (selectedFile.renameTo(newFile)) {
                                        Toast.makeText(context, "File Renamed", Toast.LENGTH_SHORT).show();
                                        //update data
                                        updateData();

                                    } else {
                                        //try to copy new file
                                        try {
                                            Utilities.copyFile(selectedFile, newFile);
                                            selectedFile.delete();
                                            Toast.makeText(context, "File Renamed", Toast.LENGTH_SHORT).show();
                                            //update data
                                            updateData();
                                        } catch (IOException e) {
                                            Toast.makeText(context, "Failed to Rename", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else if (item.getTitle().equals("MOVE")) {
                    mCallBack.startChooseDestination();
                    //update list just show folders
                    showFolderOnly();
                    mCurrentSelectedFile = selectedFile;
                } else if (item.getTitle().equals("DELETE")) {
                    boolean deleted = selectedFile.delete();
                    if (deleted) {
                        Toast.makeText(context, "DELETED", Toast.LENGTH_SHORT).show();
                        //view.setVisibility(View.GONE);
                        //update data
                        updateData();
                    }
                }
                return true;
            });

            popupMenu.show();
            return true;
        });
    }

    public boolean goBack(File rootFile) {
        if (rootFile.getAbsolutePath().equals(mRootFile.getAbsolutePath())) {
            //can not go back -> close StorageActivity
            return false;
        } else {
            mRootFile = mRootFile.getParentFile();
            updateData();
            return true;
        }
    }

    public void moveFileToSelectedFolder() {
        //selected folder is mRootFile, and selected file is mCurrentSelectedFile
        //try to copy new file
        try {
            File destinationFile = new File(mRootFile, mCurrentSelectedFile.getName());
            Utilities.copyFile(mCurrentSelectedFile, destinationFile);
            mCurrentSelectedFile.delete();
            Toast.makeText(context, "File moved", Toast.LENGTH_SHORT).show();
            //update data
            updateData();
        } catch (IOException e) {
            Toast.makeText(context, "Failed to move file", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateData() {
        //update data
        filesAndFolders = mRootFile.listFiles();
        Arrays.sort(filesAndFolders);
        notifyDataSetChanged();
    }

    private void showFolderOnly() {
        filesAndFolders = mRootFile.listFiles(file -> file.isDirectory());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filesAndFolders.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.file_name_text_view);
            imageView = itemView.findViewById(R.id.icon_view);
        }
    }
}