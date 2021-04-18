package com.example.diginote.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.diginote.R;
import com.example.diginote.adapter.TodoAdapter;
import com.example.diginote.callback.MainActionModeCallback;
import com.example.diginote.database.DatabaseHelper;
import com.example.diginote.databinding.ActivityMainBinding;
import com.example.diginote.listener.TodoEventListener;
import com.example.diginote.model.TodoModel;
import com.example.diginote.sharedpref.SharedPref;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TodoEventListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    ActivityMainBinding activityMainBinding;

    TodoAdapter todoAdapter;
    ArrayList<TodoModel> todoModelArrayList;

    SharedPref sharedPrefren;
    DatabaseHelper dh;

    String[] sort_list;

    private MainActionModeCallback mainActionModeCallback;
    private int checkedCount = 0;

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

        activityMainBinding =  ActivityMainBinding.inflate(getLayoutInflater());
        View view = activityMainBinding.getRoot();
        setContentView(view);

        Toolbar toolbar = activityMainBinding.layoutToolbar.toolbar;
        setSupportActionBar(toolbar);

        dh = new DatabaseHelper(this);

        todoModelArrayList = new ArrayList<>();

        showNote();

        activityMainBinding.textNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(), AddNotesActivity.class);
                startActivity(i);


            }
        });


        activityMainBinding.btnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(),SpeechActivity.class);
                startActivity(i);

            }
        });

        activityMainBinding.btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ChecklistActivity.class);
                startActivity(i);
            }
        });

        activityMainBinding.btnBrush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),DrawActivity.class);
                startActivity(i);
            }
        });


        activityMainBinding.btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestCameraAndStoragePermission();

            }
        });

    }


    private void requestCameraAndStoragePermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {

                            final AlertDialog.Builder alertadd = new AlertDialog.Builder(MainActivity.this);
                            LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                            final View v = factory.inflate(R.layout.image_dialog, null);
                            alertadd.setView(v);

                            LinearLayout llCamera = v.findViewById(R.id.ll_camera);
                            LinearLayout llGallery = v.findViewById(R.id.ll_gallery);

                            final AlertDialog show = alertadd.show();

                            llCamera.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent i = new Intent(getApplicationContext(), AddImageActivity.class);
                                    i.putExtra("imageType","camera");
                                    startActivity(i);
                                    show.dismiss();
                                }
                            });

                            llGallery.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent i = new Intent(getApplicationContext(), AddImageActivity.class);
                                    i.putExtra("imageType","gallery");
                                    startActivity(i);
                                    show.dismiss();
                                }
                            });

                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }


                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

        private void showSettingsDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Need Permissions");
            builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
            builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    openSettings();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();

        }

    private void openSettings() {

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void showNote() {

        try {
            if (dh.getNotesCount() > 0) {
                activityMainBinding.toDoEmptyView.setVisibility(View.GONE);
                activityMainBinding.noteRecyclerView.setVisibility(View.VISIBLE);

                showAllNotes();

            } else {
                activityMainBinding.toDoEmptyView.setVisibility(View.VISIBLE);
                activityMainBinding.noteRecyclerView.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }


    }

    private void showAllNotes() {
        try {

                todoModelArrayList.clear();
                todoModelArrayList.addAll(dh.getAllNotes());
                todoAdapter = new TodoAdapter(todoModelArrayList, getApplicationContext());
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                activityMainBinding.noteRecyclerView.setLayoutManager(linearLayoutManager);
                activityMainBinding.noteRecyclerView.setItemAnimator(new DefaultItemAnimator());
                todoAdapter.setTodoEventListener(this);
                activityMainBinding.noteRecyclerView.setAdapter(todoAdapter);
                todoAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todolist_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:

                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.menu_share:

                try {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    StringBuilder sb = new StringBuilder();
                    sb.append("Hey,I am using this simple and awesome DigiNote app just check it out.");
                    sb.append("https://play.google.com/store/apps/details?id=" + getPackageName());
                    //sharingIntent.addFlags(activityfl.ClearWhenTaskReset);
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "DigiNote");
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                    startActivity(Intent.createChooser(sharingIntent, "DigiNote"));
                } catch (Exception e) {
                    Log.e(TAG,e.toString());
                }
                return true;

            case R.id.menu_sort:
                sort_list = new String[]{"Alphabetically","Date modified","Date Created"};
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setTitle(R.string.sort).setIcon(R.drawable.sort);
                //-1 means no items are checked when dialog appears first
                alertDialogBuilder.setSingleChoiceItems(sort_list, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i)
                        {
                            case 0:
                                loadNotesAlphabetically();
                                break;

                            case  1:
                                loadNotesDateMod();
                                break;

                            case 2:
                                loadNotesDateCreated();
                                break;
                        }
                        dialog.dismiss();
                    }
                });

                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void loadNotesAlphabetically() {

        todoModelArrayList.clear();
        todoModelArrayList.addAll(dh.getAllNotesAlphabetically());
        todoAdapter = new TodoAdapter(todoModelArrayList, getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        activityMainBinding.noteRecyclerView.setLayoutManager(linearLayoutManager);
        activityMainBinding.noteRecyclerView.setItemAnimator(new DefaultItemAnimator());
        todoAdapter.setTodoEventListener(this);
        activityMainBinding.noteRecyclerView.setAdapter(todoAdapter);
        todoAdapter.notifyDataSetChanged();

    }



    public void loadNotesDateMod() {

        todoModelArrayList.clear();
        todoModelArrayList.addAll(dh.getAllNotes());
        todoAdapter = new TodoAdapter(todoModelArrayList, getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        activityMainBinding.noteRecyclerView.setLayoutManager(linearLayoutManager);
        activityMainBinding.noteRecyclerView.setItemAnimator(new DefaultItemAnimator());
        todoAdapter.setTodoEventListener(this);
        activityMainBinding.noteRecyclerView.setAdapter(todoAdapter);
        todoAdapter.notifyDataSetChanged();

    }

    public void loadNotesDateCreated() {


        todoModelArrayList.clear();
        todoModelArrayList.addAll(dh.getAllNotesDateCreated());
        todoAdapter = new TodoAdapter(todoModelArrayList, getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        activityMainBinding.noteRecyclerView.setLayoutManager(linearLayoutManager);
        activityMainBinding.noteRecyclerView.setItemAnimator(new DefaultItemAnimator());
        todoAdapter.setTodoEventListener(this);
        activityMainBinding.noteRecyclerView.setAdapter(todoAdapter);
        todoAdapter.notifyDataSetChanged();

    }


    @Override
    public void onNoteClick(TodoModel todoModel) {

        String itemType = todoModel.getItemtype();

        if (itemType.equals("note")) {
            Intent i = new Intent(getApplicationContext(), AddNotesActivity.class);
            i.putExtra("noteId", todoModel.getId());
            startActivity(i);
        }
        else if (itemType.equals("speech")) {
            Intent i = new Intent(getApplicationContext(), SpeechActivity.class);
            i.putExtra("noteId", todoModel.getId());
            startActivity(i);
        }
        else if (itemType.equals("checklist")) {
            Intent i = new Intent(getApplicationContext(), ChecklistActivity.class);
            i.putExtra("noteId", todoModel.getId());
            startActivity(i);
        }
        else if (itemType.equals("drawing")) {
            Intent i = new Intent(getApplicationContext(), DrawActivity.class);
            i.putExtra("noteId", todoModel.getId());
            startActivity(i);
        }
        else if (itemType.equals("image")) {
            Intent i = new Intent(getApplicationContext(), AddImageActivity.class);
            i.putExtra("noteId", todoModel.getId());
            startActivity(i);
        }

    }

    @Override
    public void onNoteLongClick(TodoModel todoModel) {

        todoModel.setChecked(true);
        checkedCount = 1;

        todoAdapter.setMultiCheckMode(true);

        todoAdapter.setTodoEventListener(new TodoEventListener() {
            @Override
            public void onNoteClick(TodoModel todoModel) {

                todoModel.setChecked(!todoModel.isChecked());
                if (todoModel.isChecked())
                    checkedCount++;
                else
                    checkedCount--;
                if (checkedCount > 1) {
                    mainActionModeCallback.changeShareItemVisible(false);
                } else mainActionModeCallback.changeShareItemVisible(true);

                if (checkedCount == 0) {
                    //  finish multi select mode wen checked count =0
                    mainActionModeCallback.getAction().finish();
                }


                mainActionModeCallback.setCount(checkedCount+"/"+todoModelArrayList.size());
                todoAdapter.notifyDataSetChanged();

            }

            @Override
            public void onNoteLongClick(TodoModel todoModel) {

            }
        });



        mainActionModeCallback = new MainActionModeCallback(){
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_delete_notes){
                    onDeleteMultiNotes();
                }
                else if (item.getItemId() == R.id.action_share_note){
                    onShareNote();
                }
                mode.finish();
                return false;

            }
        };
        //startaction mode
        startActionMode(mainActionModeCallback);
        mainActionModeCallback.setCount(checkedCount+"/"+todoModelArrayList.size());





    }


    private void onShareNote() {

        TodoModel note = todoAdapter.getCheckedNotes().get(0);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        String notetext = note.getTitle()+": "+note.getNote() + "\n\n Created on : " +
                todoAdapter.date(note.getTimestamp()) + "\n  By :" +
                getString(R.string.app_name);
        share.putExtra(Intent.EXTRA_TEXT, notetext);
        startActivity(share);


    }

    private void onDeleteMultiNotes() {

        final List<TodoModel> checkedNotes = todoAdapter.getCheckedNotes();
        if (checkedNotes.size() != 0) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(R.string.app_name).setMessage("Are you sure you want to delete?").
                    setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            for ( TodoModel note : checkedNotes) {
                                dh.deleteNote(note);
                            }
                            Toast.makeText(getApplicationContext(), checkedNotes.size() + " Note(s) Delete successfully !", Toast.LENGTH_SHORT).show();
                            // refresh Notes
                            loadNotesAlphabetically();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();



        } else Toast.makeText(getApplicationContext(), "No Note(s) selected", Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);

        todoAdapter.setMultiCheckMode(false);
        todoAdapter.setTodoEventListener(this);

    }
}
