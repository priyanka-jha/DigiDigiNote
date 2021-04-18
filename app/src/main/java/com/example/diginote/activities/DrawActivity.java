package com.example.diginote.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.divyanshu.draw.activity.DrawingActivity;
import com.example.diginote.R;
import com.example.diginote.Util;
import com.example.diginote.database.DatabaseHelper;
import com.example.diginote.databinding.ActivityDrawBinding;
import com.example.diginote.databinding.ActivitySpeechBinding;
import com.example.diginote.helper.ColorHelper;
import com.example.diginote.helper.FileHelper;
import com.example.diginote.model.TodoModel;
import com.example.diginote.reminder.AlarmBrodcast;
import com.example.diginote.sharedpref.SharedPref;
import com.rm.freedrawview.FreeDrawSerializableState;
import com.rm.freedrawview.FreeDrawView;
import com.rm.freedrawview.PathDrawnListener;
import com.rm.freedrawview.PathRedoUndoCountChangeListener;
import com.rm.freedrawview.ResizeBehaviour;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DrawActivity extends AppCompatActivity {

    public static final String TAG = DrawingActivity.class.getSimpleName();
    ActivityDrawBinding activityDrawBinding;
    private TodoModel todoModel;

    String title = "" ,text = "", drawName = "", drawText = "";
    private String itemType = "drawing";
    long time;
    int noteId = 0;

    DatabaseHelper dh;
    SQLiteDatabase db;
    SharedPref sharedPrefren;

    FreeDrawView mSignatureView;

    private static final int THICKNESS_STEP = 2;
    private static final int THICKNESS_MAX = 80;
    private static final int THICKNESS_MIN = 15;

    private static final int ALPHA_STEP = 1;
    private static final int ALPHA_MAX = 255;
    private static final int ALPHA_MIN = 0;

    private Menu mMenu;

    public int count = 1;
    private Bitmap mBitmap;

    int undoCount1 = 0, redoCount1 = 0;
    File imageFolder;
    File sdCard;

    ImageView close;
    Button btnsave;
    TextView remindertitle,message, tvCurrentTime, tvCurrentDate;
    Dialog reminderpopup;
    Spinner repeatreminder;

    private int mYear,mMonth,mDay;
    String selectedAlarmDate = "", timeToNotify = "", selectedAlarmTime = "";

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
            Log.e(TAG, e.toString());
        }

        super.onCreate(savedInstanceState);

        activityDrawBinding = ActivityDrawBinding.inflate(getLayoutInflater());
        View view = activityDrawBinding.getRoot();
        setContentView(view);

        reminderpopup = new Dialog(DrawActivity.this);

        Toolbar toolbar = activityDrawBinding.layoutToolbar.toolbar;
        setSupportActionBar(toolbar);

        activityDrawBinding.edTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                invalidateOptionsMenu();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        mSignatureView = activityDrawBinding.drawView;


        dh = new DatabaseHelper(this);
        db = dh.getWritableDatabase();

        Intent intent = getIntent();
        noteId = intent.getIntExtra("noteId", 0);

        if (noteId != 0) {

            todoModel = dh.getNote(noteId);

            activityDrawBinding.edTitle.setVisibility(View.VISIBLE);
            activityDrawBinding.edNote.setVisibility(View.VISIBLE);

            activityDrawBinding.edTitle.setText(todoModel.getTitle());
            activityDrawBinding.edNote.setText(todoModel.getNote());

            drawName = todoModel.getDrawname();

            if (todoModel.getDrawtext().equals(" ")) {


            } else {
                mSignatureView.setVisibility(View.GONE);
                activityDrawBinding.layoutOption.setVisibility(View.GONE);
                activityDrawBinding.imgScreen.setVisibility(View.VISIBLE);

                String imageText = todoModel.getDrawtext();

                if (imageText != null) {
                    byte[] image_str;
                    try {
                        image_str = Base64.decode(imageText);
                        mBitmap = BitmapFactory.decodeByteArray(image_str, 0, image_str.length);
                        activityDrawBinding.imgScreen.setImageBitmap(BitmapFactory.decodeByteArray(image_str, 0, image_str.length));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Image not found !!! ", Toast.LENGTH_LONG).show();
                }

              /*  mMenu.findItem(R.id.menu_save).setVisible(false);
                mMenu.findItem(R.id.menu_delete).setVisible(false);
                mMenu.findItem(R.id.menu_undo).setVisible(false);
                mMenu.findItem(R.id.menu_redo).setVisible(false);
*/
                activityDrawBinding.edTitle.setVisibility(View.VISIBLE);
                activityDrawBinding.edNote.setVisibility(View.VISIBLE);

                activityDrawBinding.edTitle.setText(todoModel.getTitle());
                activityDrawBinding.edNote.setText(todoModel.getNote());


            }
        } else {

            activityDrawBinding.edTitle.setFocusable(true);

        }


        //  mSignatureView = activityDrawBinding.drawView;

        // Setup the View
        mSignatureView.setPaintColor(Color.BLACK);
        mSignatureView.setPaintWidthPx(getApplicationContext().getResources().getDimensionPixelSize(R.dimen.signature_paint_width));
        //mSignatureView.setPaintWidthPx(12);
        mSignatureView.setPaintWidthDp(getApplicationContext().getResources().getDimension(R.dimen.signature_paint_width));
        //mSignatureView.setPaintWidthDp(6);
        mSignatureView.setPaintAlpha(255);// from 0 to 255
        mSignatureView.setResizeBehaviour(ResizeBehaviour.CROP);// Must be one of ResizeBehaviour
        // values;

        ////////////

        activityDrawBinding.layoutOpacity1.setVisibility(View.GONE);
        activityDrawBinding.layoutWidth1.setVisibility(View.GONE);

        ////////////


        // This listener will be notified every time the path done and undone count changes
        mSignatureView.setPathRedoUndoCountChangeListener(new PathRedoUndoCountChangeListener() {
            @Override
            public void onUndoCountChanged(int undoCount) {
                // The undoCount is the number of the paths that can be undone

                undoCount1 = undoCount;

                invalidateOptionsMenu();


            }

            @Override
            public void onRedoCountChanged(int redoCount) {
                // The redoCount is the number of path removed that can be redrawn
                redoCount1 = redoCount;

                invalidateOptionsMenu();


            }
        });
        // This listener will be notified every time a new path has been drawn
        mSignatureView.setOnPathDrawnListener(new PathDrawnListener() {
            @Override
            public void onNewPathDrawn() {
                // The user has finished drawing a path
            }

            @Override
            public void onPathStart() {
                // The user has started drawing a path
            }
        });


        activityDrawBinding.btnOpacity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*if (activityDrawBinding.layoutStroke.getVisibility() == View.GONE) {

                    activityDrawBinding.layoutStroke.setVisibility(View.VISIBLE);
                    activityDrawBinding.layoutWidth1.setVisibility(View.GONE);
                    activityDrawBinding.layoutOpacity1.setVisibility(View.VISIBLE);

                    *//*if (activityDrawBinding.layoutOpacity1.getVisibility() == View.GONE) {

                        activityDrawBinding.layoutOpacity1.setVisibility(View.VISIBLE);

                    }
                    else {

                        activityDrawBinding.layoutOpacity1.setVisibility(View.GONE);

                    }*//*
                }
                else {

                    activityDrawBinding.layoutStroke.setVisibility(View.GONE);

                }
*/

                activityDrawBinding.layoutColor1.setVisibility(View.GONE);
                activityDrawBinding.layoutWidth1.setVisibility(View.GONE);
                if (activityDrawBinding.layoutOpacity1.getVisibility() == View.GONE) {
                    activityDrawBinding.layoutOpacity1.setVisibility(View.VISIBLE);
                } else {
                    activityDrawBinding.layoutOpacity1.setVisibility(View.GONE);

                }


            }
        });

        activityDrawBinding.btnWidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/*

                System.out.println("width visible");

                if (activityDrawBinding.layoutStroke.getVisibility() == View.GONE) {

                    System.out.println("width visible1");


                    activityDrawBinding.layoutStroke.setVisibility(View.VISIBLE);
                    activityDrawBinding.layoutOpacity1.setVisibility(View.GONE);
                    activityDrawBinding.layoutWidth1.setVisibility(View.VISIBLE);


*/
/*                    if( activityDrawBinding.layoutWidth1.getVisibility() == View.GONE) {

                        System.out.println("width visible2");


                        activityDrawBinding.layoutWidth1.setVisibility(View.VISIBLE);

                    }
                    else {
                        System.out.println("width visible3");

                        activityDrawBinding.layoutWidth1.setVisibility(View.GONE);

                    }*//*

                }
                else {
                    System.out.println("width visible4");

                    activityDrawBinding.layoutStroke.setVisibility(View.GONE);

                }
*/


                activityDrawBinding.layoutColor1.setVisibility(View.GONE);
                activityDrawBinding.layoutOpacity1.setVisibility(View.GONE);
                if (activityDrawBinding.layoutWidth1.getVisibility() == View.GONE) {
                    activityDrawBinding.layoutWidth1.setVisibility(View.VISIBLE);
                } else {
                    activityDrawBinding.layoutWidth1.setVisibility(View.GONE);

                }


            }
        });

        activityDrawBinding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                System.out.println("btn all clicked");
                 mSignatureView.undoAll();

               /* mSignatureView.clearDrawAndHistory();
                FileHelper.deleteSavedStateFile(getApplicationContext());*/
            }
        });

        activityDrawBinding.btnChangeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // changeColor();
                activityDrawBinding.layoutWidth1.setVisibility(View.GONE);
                activityDrawBinding.layoutOpacity1.setVisibility(View.GONE);
                if (activityDrawBinding.layoutColor1.getVisibility() == View.GONE) {
                    activityDrawBinding.layoutColor1.setVisibility(View.VISIBLE);

                } else {
                    activityDrawBinding.layoutColor1.setVisibility(View.GONE);

                }


            }
        });

        activityDrawBinding.layoutColor.imageColorBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //int color = Color.parseColor("#000000");

                int color = ResourcesCompat.getColor(getResources(), R.color.color_black, null);

                mSignatureView.setPaintColor(color);
                scaleColorView(activityDrawBinding.layoutColor.imageColorBlack);
            }
        });

        activityDrawBinding.layoutColor.imageColorRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // int color = Color.parseColor("#b0120a");

                int color = ResourcesCompat.getColor(getResources(), R.color.color_red, null);


                mSignatureView.setPaintColor(color);
                scaleColorView(activityDrawBinding.layoutColor.imageColorRed);
            }
        });

        activityDrawBinding.layoutColor.imageColorYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  int color = Color.parseColor("#FFEB3B");

                int color = ResourcesCompat.getColor(getResources(), R.color.color_yellow, null);


                mSignatureView.setPaintColor(color);
                scaleColorView(activityDrawBinding.layoutColor.imageColorYellow);
            }
        });

        activityDrawBinding.layoutColor.imageColorGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //   int color = Color.parseColor("#00C853");
                // changeColor(color);

                int color = ResourcesCompat.getColor(getResources(), R.color.color_green, null);


                mSignatureView.setPaintColor(color);
                scaleColorView(activityDrawBinding.layoutColor.imageColorGreen);

            }
        });

        activityDrawBinding.layoutColor.imageColorBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  int color = Color.parseColor("#00B0FF");

                int color = ResourcesCompat.getColor(getResources(), R.color.color_blue, null);


                mSignatureView.setPaintColor(color);
                scaleColorView(activityDrawBinding.layoutColor.imageColorBlue);
            }
        });

        activityDrawBinding.layoutColor.imageColorPink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  int color = Color.parseColor("#ad1457");

                int color = ResourcesCompat.getColor(getResources(), R.color.color_pink, null);


                mSignatureView.setPaintColor(color);
                scaleColorView(activityDrawBinding.layoutColor.imageColorPink);
            }
        });

        activityDrawBinding.layoutColor.imageColorBrown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //   int color = Color.parseColor("#8D6E63");

                int color = ResourcesCompat.getColor(getResources(), R.color.color_brown, null);


                mSignatureView.setPaintColor(color);
                scaleColorView(activityDrawBinding.layoutColor.imageColorBrown);
            }
        });

        activityDrawBinding.layoutColor.imageColorOrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // int color = Color.parseColor("#ff6f00");

                int color = ResourcesCompat.getColor(getResources(), R.color.color_orange, null);


                mSignatureView.setPaintColor(color);
                scaleColorView(activityDrawBinding.layoutColor.imageColorOrange);
            }
        });

        activityDrawBinding.layoutColor.imageColorPurple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // int color = Color.parseColor("#3d5afe");

                int color = ResourcesCompat.getColor(getResources(), R.color.color_purple, null);


                mSignatureView.setPaintColor(color);
                scaleColorView(activityDrawBinding.layoutColor.imageColorPurple);
            }
        });


        activityDrawBinding.layoutColor.imageColorIndigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  int color = Color.parseColor("#3d5afe");

                int color = ResourcesCompat.getColor(getResources(), R.color.color_indigo, null);


                mSignatureView.setPaintColor(color);
                scaleColorView(activityDrawBinding.layoutColor.imageColorIndigo);
            }
        });


        if (savedInstanceState == null) {

            showLoadingSpinner();

            // Restore the previous saved state
            FileHelper.getSavedStoreFromFile(this,
                    new FileHelper.StateExtractorInterface() {
                        @Override
                        public void onStateExtracted(FreeDrawSerializableState state) {
                            if (state != null) {
                                mSignatureView.restoreStateFromSerializable(state);
                            }

                            hideLoadingSpinner();
                        }

                        @Override
                        public void onStateExtractionError() {
                            hideLoadingSpinner();
                        }
                    });
        }


        activityDrawBinding.sliderAlpha1.setMax((ALPHA_MAX - ALPHA_MIN) / ALPHA_STEP);
        int alphaProgress = ((mSignatureView.getPaintAlpha() - ALPHA_MIN) / ALPHA_STEP);
        activityDrawBinding.sliderAlpha1.setProgress(alphaProgress);
        activityDrawBinding.sliderAlpha1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                mSignatureView.setPaintAlpha(ALPHA_MIN + (progress * ALPHA_STEP));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        activityDrawBinding.sliderThickness1.setMax((THICKNESS_MAX - THICKNESS_MIN) / THICKNESS_STEP);
        int thicknessProgress = (int)
                ((mSignatureView.getPaintWidth() - THICKNESS_MIN) / THICKNESS_STEP);
        activityDrawBinding.sliderThickness1.setProgress(thicknessProgress);
        activityDrawBinding.sliderThickness1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                System.out.println("seekBar = " + seekBar);
                mSignatureView.setPaintWidthPx(THICKNESS_MIN + (progress * THICKNESS_STEP));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //  activityDrawBinding.sideView.setBackgroundColor(mSignatureView.getPaintColor());


    }

    private void changeColor() {
        int color = ColorHelper.getRandomMaterialColor(this);
        System.out.println("color = " + color);
        mSignatureView.setPaintColor(color);

        //activityDrawBinding.sideView.setBackgroundColor(mSignatureView.getPaintColor());

        scaleColorView(activityDrawBinding.layoutColor.imageColorGreen);
    }

    private void changeColor(int color) {

        mSignatureView.setPaintColor(color);
        scaleColorView(activityDrawBinding.layoutColor.imageColorGreen);

    }

    private void scaleColorView(ImageView imageView) {

        //reset scale of all views
        activityDrawBinding.layoutColor.imageColorBlack.setScaleX(1f);
        activityDrawBinding.layoutColor.imageColorBlack.setScaleY(1f);

        activityDrawBinding.layoutColor.imageColorRed.setScaleX(1f);
        activityDrawBinding.layoutColor.imageColorRed.setScaleY(1f);

        activityDrawBinding.layoutColor.imageColorYellow.setScaleX(1f);
        activityDrawBinding.layoutColor.imageColorYellow.setScaleY(1f);

        activityDrawBinding.layoutColor.imageColorGreen.setScaleX(1f);
        activityDrawBinding.layoutColor.imageColorGreen.setScaleY(1f);

        activityDrawBinding.layoutColor.imageColorBlue.setScaleX(1f);
        activityDrawBinding.layoutColor.imageColorBlue.setScaleY(1f);

        activityDrawBinding.layoutColor.imageColorPink.setScaleX(1f);
        activityDrawBinding.layoutColor.imageColorPink.setScaleY(1f);

        activityDrawBinding.layoutColor.imageColorBrown.setScaleX(1f);
        activityDrawBinding.layoutColor.imageColorBrown.setScaleY(1f);

        activityDrawBinding.layoutColor.imageColorOrange.setScaleX(1f);
        activityDrawBinding.layoutColor.imageColorOrange.setScaleY(1f);

        activityDrawBinding.layoutColor.imageColorPurple.setScaleX(1f);
        activityDrawBinding.layoutColor.imageColorPurple.setScaleY(1f);

        activityDrawBinding.layoutColor.imageColorIndigo.setScaleX(1f);
        activityDrawBinding.layoutColor.imageColorIndigo.setScaleY(1f);

        //set scale of selected view
        imageView.setScaleX(1.5f);
        imageView.setScaleY(1.5f);
    }


    private void showLoadingSpinner() {

        TransitionManager.beginDelayedTransition(activityDrawBinding.root);
        activityDrawBinding.progress.setVisibility(View.VISIBLE);
    }

    private void hideLoadingSpinner() {

        activityDrawBinding.progress.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.draw_menu, menu);

        mMenu = menu;



        if (redoCount1 == 0 & undoCount1 == 0) {
            menu.findItem(R.id.menu_save).setVisible(false);
        } else {
            menu.findItem(R.id.menu_save).setVisible(true);

        }

        if (noteId == 0) {

           /* if (activityDrawBinding.imgScreen.getVisibility() == View.VISIBLE) {

                menu.findItem(R.id.menu_delete).setVisible(true);

            } else {
                menu.findItem(R.id.menu_delete).setVisible(false);
            }*/

            if (redoCount1 == 0 & undoCount1 == 0) {
                menu.findItem(R.id.menu_delete).setVisible(false);
            } else {
                menu.findItem(R.id.menu_delete).setVisible(true);

            }



        }
        else {

            menu.findItem(R.id.menu_delete).setVisible(false);


        }



        if (undoCount1 > 0) {
            menu.findItem(R.id.menu_undo).setVisible(true);

        } else {
            menu.findItem(R.id.menu_undo).setVisible(false);

        }

        if (redoCount1 > 0) {
            menu.findItem(R.id.menu_redo).setVisible(true);

        } else {
            menu.findItem(R.id.menu_redo).setVisible(false);

        }

        if(activityDrawBinding.edTitle.getText().length()==0){
            menu.findItem(R.id.menu_share).setVisible(false);
        }
        else {
            menu.findItem(R.id.menu_share).setVisible(true);
        }
        if(noteId==0){
            menu.findItem(R.id.menu_reminder).setVisible(false);
        }
        else {
            menu.findItem(R.id.menu_reminder).setVisible(true);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_save:

                takeAndShowScreenshot();

                return true;

            case R.id.menu_delete:


                if (activityDrawBinding.imgScreen.getVisibility() == View.VISIBLE) {

                    mSignatureView.clearDrawAndHistory();
                    FileHelper.deleteSavedStateFile(this);

                    activityDrawBinding.layoutOption.setVisibility(View.VISIBLE);


                    activityDrawBinding.edTitle.setVisibility(View.GONE);
                    activityDrawBinding.edNote.setVisibility(View.GONE);

                    activityDrawBinding.imgScreen.setImageBitmap(null);
                    activityDrawBinding.imgScreen.setVisibility(View.GONE);

                    mSignatureView.setVisibility(View.VISIBLE);

                }
                else {

                    mSignatureView.clearDrawAndHistory();
                    FileHelper.deleteSavedStateFile(this);


                }



               // deleteDrawing();

                return true;

            case R.id.menu_share:

                try {
                    shareData();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;

            case R.id.menu_undo:
                mSignatureView.undoLast();
                return true;


            case R.id.menu_redo:
                mSignatureView.redoLast();
                return true;

            case android.R.id.home:

                onBackPressed();
                return true;

            case R.id.menu_reminder:
                showPopUp();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void showPopUp() {


        reminderpopup.setContentView(R.layout.reminder_popup);
        close = (ImageView) reminderpopup.findViewById(R.id.close);
        remindertitle = (TextView) reminderpopup.findViewById(R.id.remindertitle);
        message = (TextView) reminderpopup.findViewById(R.id.message);
        tvCurrentTime = (TextView) reminderpopup.findViewById(R.id.currenttime);
        tvCurrentDate = (TextView) reminderpopup.findViewById(R.id.currentdate);
        repeatreminder = (Spinner) reminderpopup.findViewById(R.id.repeatreminder);
        btnsave = (Button) reminderpopup.findViewById(R.id.btnsave);

        String[] arraySpinner = new String[] {"No repeat", "Daily", "Weekly", "Monthly", "Yearly"};

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reminderpopup.dismiss();
            }
        });


        Calendar c = Calendar.getInstance();
        // SimpleDateFormat df = new SimpleDateFormat("dd MMM, yyyy");
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        selectedAlarmDate = df.format(c.getTime());

        tvCurrentDate.setText(selectedAlarmDate);

        tvCurrentDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(DrawActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                               /* calYear = year;
                                calMonth = monthOfYear;
                                calDay = dayOfMonth;

                                final Calendar calendar = Calendar.getInstance();
                                calendar.set(year, monthOfYear, dayOfMonth);
                                current_date = df.format(calendar.getTime());

                                tvCurrentDate.setText(current_date);*/

                                selectedAlarmDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                                tvCurrentDate.setText(selectedAlarmDate);



                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });


        //Display current time
        Calendar c1 = Calendar.getInstance();
        SimpleDateFormat df1 = new SimpleDateFormat("hh:mm a");
        selectedAlarmTime = df1.format(c1.getTime());
        System.out.println("current_time = " + selectedAlarmTime);



       /* SimpleDateFormat sdf1 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        current_time = sdf1.format(new Date());
*/


        tvCurrentTime.setText(selectedAlarmTime);

        tvCurrentTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Calendar calendar = Calendar.getInstance();
                final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                final int currentMinute = calendar.get(Calendar.MINUTE);


                TimePickerDialog timePickerDialog = new TimePickerDialog(DrawActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {


                      /*  if (hourOfDay > 12) {
                            hourOfDay -= 12;
                            amPm = "PM";
                        } else if (hourOfDay == 0) {
                            hourOfDay += 12;
                            amPm = "AM";
                        } else if (hourOfDay == 12){
                            amPm = "PM";
                        }else{
                            amPm = "AM";
                        }



                        calHour = hourOfDay;
                        calMinute = minute;
                        calSec = 0;
*/
                        timeToNotify = hourOfDay + ":" + minute;
                        selectedAlarmTime = FormatTime(hourOfDay, minute);
                        tvCurrentTime.setText(selectedAlarmTime);

                       /* current_time = String.format("%02d:%02d", hourOfDay, minute ) + " "+amPm;
                        tvCurrentTime.setText(current_time);*/
                    }
                },currentHour,currentMinute,false);
                timePickerDialog.show();
            }


        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(DrawActivity.this, R.layout.reminder_spinner, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatreminder.setAdapter(adapter);

        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveReminder();

            }
        });
        reminderpopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        reminderpopup.setCancelable(false);
        reminderpopup.show();
    }

    public String FormatTime(int hour, int minute) {

        String time;
        time = "";
        String formattedMinute;

        if (minute / 10 == 0) {
            formattedMinute = "0" + minute;
        } else {
            formattedMinute = "" + minute;
        }


        if (hour == 0) {
            time = "12" + ":" + formattedMinute + " AM";
        } else if (hour < 12) {
            time = hour + ":" + formattedMinute + " AM";
        } else if (hour == 12) {
            time = "12" + ":" + formattedMinute + " PM";
        } else {
            int temp = hour - 12;
            time = temp + ":" + formattedMinute + " PM";
        }


        return time;
    }


    private void saveReminder() {

        reminderpopup.dismiss();

        String message = activityDrawBinding.edTitle.getText().toString();
        if(message.isEmpty()){
            Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show();
        }
        else {
            setAlarm(activityDrawBinding.edTitle.getText().toString(), selectedAlarmDate, selectedAlarmTime);
        }

    }

    private void setAlarm(String text, String date, String time) {

        Toast.makeText(this, "Alarm set", Toast.LENGTH_SHORT).show();
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), AlarmBrodcast.class);
        intent.putExtra("event", text);
        intent.putExtra("time", date);
        intent.putExtra("date", time);
        intent.putExtra("noteId", noteId);
        intent.putExtra("itemType", itemType);

        System.out.println("text   = " + text);
        System.out.println("text   1= " + date);
        System.out.println("text   2= " + time);
        System.out.println("text   3= " + noteId);
        System.out.println("text   4= " + itemType);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String dateandtime = date + " " + timeToNotify;
        DateFormat formatter = new SimpleDateFormat("d-M-yyyy hh:mm");
        try {
            Date date1 = formatter.parse(dateandtime);
            am.set(AlarmManager.RTC_WAKEUP, date1.getTime(), pendingIntent);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        finish();

    }



    private void deleteDrawing() {

        if (noteId == 0) {

            mSignatureView.clearDrawAndHistory();
            FileHelper.deleteSavedStateFile(this);

            Intent i = new Intent(getApplicationContext(), DrawActivity.class);
            i.putExtra("noteId", 0);
            startActivity(i);


        } else {
          //  mSignatureView.clearDrawAndHistory();
          //  FileHelper.deleteSavedStateFile(this);

            Intent i = new Intent(getApplicationContext(), DrawActivity.class);
            i.putExtra("noteId", noteId);
            startActivity(i);


        }


    }


    @Override
    public void onBackPressed() {
        // super.onBackPressed();


        if (activityDrawBinding.imgScreen.getVisibility() == View.VISIBLE) {
            /*mMenu.findItem(R.id.menu_save).setVisible(true);
            mMenu.findItem(R.id.menu_delete).setVisible(true);
            mMenu.findItem(R.id.menu_undo).setVisible(true);
            mMenu.findItem(R.id.menu_redo).setVisible(true);
            activityDrawBinding.layoutOption.setVisibility(View.VISIBLE);


            activityDrawBinding.edTitle.setVisibility(View.GONE);
            activityDrawBinding.edNote.setVisibility(View.GONE);

            activityDrawBinding.imgScreen.setImageBitmap(null);
            activityDrawBinding.imgScreen.setVisibility(View.GONE);

            mSignatureView.setVisibility(View.VISIBLE);*/

           if (noteId == 0) {

                saveDrawing(mBitmap);


            }
             else {


               String imageText = todoModel.getDrawtext();

               if(imageText != null){
                   byte[] image_str;
                   try {
                       image_str = Base64.decode(imageText);
                       Bitmap bitmapUpdate = BitmapFactory.decodeByteArray(image_str, 0, image_str.length);
                       saveDrawing(bitmapUpdate);

                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
               else{
                   Toast.makeText(getApplicationContext(), "Image not found !!! ", Toast.LENGTH_LONG).show();
               }

            }


        } else {
           // super.onBackPressed();

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }

    }

    private void takeAndShowScreenshot() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // This will take a screenshot of the current drawn content of the view
        mSignatureView.getDrawScreenshot(new FreeDrawView.DrawCreatorListener() {
            @Override
            public void onDrawCreated(Bitmap draw) {
                // The draw Bitmap is the drawn content of the View
                mBitmap = draw;
                //   Log.i("log_tag", "mBitmap11111: " + mBitmap);

                //  activityDrawBinding.sideView.setVisibility(View.GONE);
                mSignatureView.setVisibility(View.GONE);
                activityDrawBinding.layoutOption.setVisibility(View.GONE);

                mMenu.findItem(R.id.menu_save).setVisible(false);
                // mMenu.findItem(R.id.menu_delete).setVisible(false);
                mMenu.findItem(R.id.menu_delete).setVisible(true);
                mMenu.findItem(R.id.menu_undo).setVisible(false);
                mMenu.findItem(R.id.menu_redo).setVisible(false);

                activityDrawBinding.edTitle.setVisibility(View.VISIBLE);
                activityDrawBinding.edNote.setVisibility(View.VISIBLE);

                activityDrawBinding.imgScreen.setVisibility(View.VISIBLE);

                activityDrawBinding.imgScreen.setImageBitmap(draw);




                //  saveDrawing(draw);

            }


            @Override
            public void onDrawCreationError() {
                // Something went wrong creating the bitmap, should never
                // happen unless the async task has been canceled

                Toast.makeText(getApplicationContext(), "Error, cannot create bitmap", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void createImagePath() {


        if(Build.VERSION.SDK_INT > 21) {

            imageFolder = new File(getApplicationContext().getCacheDir(), "diginoteimages");

        }
        else {

            sdCard = Environment.getExternalStorageDirectory();
            imageFolder = new File(sdCard.getPath() + "/DigiNote/");


        }

        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }

        System.out.println("file = " + imageFolder);


    }



    private void saveDrawing(Bitmap draw) {


       // String uniqueId = getTodaysDate() + "_" + getCurrentTime() + "_" + Math.random();
      //  drawName = uniqueId + ".png";


        String currentdatetime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        currentdatetime = currentdatetime.replace("/", "");
        currentdatetime = currentdatetime.replace(":", "");
        currentdatetime = currentdatetime.replace(" ", "");

        drawName = "Draw_"+currentdatetime+".png";

        System.out.println("imagedrawNameName = " + drawName);

        createImagePath();


        File newFile = new File(imageFolder, drawName);

        Uri imageUri =  FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName()+".provider", newFile);

        System.out.println("imageUri = " + imageUri);

        try {
            FileOutputStream out = new FileOutputStream(imageFolder);
            draw.compress(Bitmap.CompressFormat.JPEG, 90, out);

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        draw.compress(Bitmap.CompressFormat.PNG, 100, out);

        byte[] byte_arr = out.toByteArray();

        drawText = Base64.encodeBytes(byte_arr);

        saveData(drawName, drawText);


    }

    private void saveData(String drawName, String drawText) {

        title = activityDrawBinding.edTitle.getText().toString().trim();
        text = activityDrawBinding.edNote.getText().toString().trim();

        try {
            if (title.isEmpty()) {
                Util.showToast(getApplicationContext(), "Please enter title!");

            } else {
                time = new Date().getTime(); // get current time;

                if (noteId == 0) {
                    createNote(drawName, drawText);
                } else {
                    updateNote(drawName, drawText);
                }


            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

       /* byte[] drawByte;
        try {

            drawByte = Base64.decode(drawText);
            System.out.println("image set2.."+drawText);

            activityDrawBinding.imgScreen.setImageBitmap(BitmapFactory.decodeByteArray(drawByte, 0, drawByte.length));
            System.out.println("image set.."+drawByte.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
*/

    }

    private void updateNote(String drawName, String drawText) {

        try {
            todoModel.setTitle(title);
            todoModel.setNote(text);
            todoModel.setItemtype(itemType);
            todoModel.setTimestamp(time);
            todoModel.setDrawname(drawName);
            todoModel.setDrawtext(drawText);
            int id = dh.updateNote(todoModel);

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            //  onBackPressed();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }


    }

    private void createNote(String drawName, String drawText) {

        try {
            long id = dh.insertNote(title, text, time, itemType, drawName, drawText);
            // onBackPressed();

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }


    }

    private String getTodaysDate() {

        final Calendar c = Calendar.getInstance();
        int todaysDate = (c.get(Calendar.YEAR) * 10000) +
                ((c.get(Calendar.MONTH) + 1) * 100) +
                (c.get(Calendar.DAY_OF_MONTH));
        Log.w("DATE:", String.valueOf(todaysDate));
        return (String.valueOf(todaysDate));

    }

    private String getCurrentTime() {

        final Calendar c = Calendar.getInstance();
        int currentTime = (c.get(Calendar.HOUR_OF_DAY) * 10000) +
                (c.get(Calendar.MINUTE) * 100) +
                (c.get(Calendar.SECOND));
        Log.w("TIME:", String.valueOf(currentTime));
        return (String.valueOf(currentTime));

    }


    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause = ");
        // FileHelper.saveStateIntoFile(this, mSignatureView.getCurrentViewStateAsSerializable(), null);


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        System.out.println("savedInstanceState = " + savedInstanceState);

        // activityDrawBinding.sideView.setBackgroundColor(mSignatureView.getPaintColor());
    }


    private void shareData() throws IOException {

        try {
            title = activityDrawBinding.edTitle.getText().toString().trim();
            text = activityDrawBinding.edNote.getText().toString().trim();
            String shareBody = title+": "+text;


            if(title.isEmpty() || text.isEmpty()) {
                Util.showToast(getApplicationContext(),"Please enter title!");
            }
            else {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                sharingIntent.setType("image/*");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, getBitmapFromView(mBitmap));
                startActivity(Intent.createChooser(sharingIntent, "Share Via"));
            }
        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }


    }

    private Uri getBitmapFromView(Bitmap mBitmap) throws IOException {
        Uri bmpUri = null;
        File file_share = null;
        File cachePath = null;

        try {


            if(Build.VERSION.SDK_INT > 21) {

                cachePath = new File(getApplicationContext().getCacheDir(), "diginoteimages");
                file_share = new File(cachePath, drawName);

            }
            else {

                File sdCard = Environment.getExternalStorageDirectory();
                file_share = new File(sdCard.getPath() + "/DigiNote/" + drawName);

            }

            FileOutputStream out = new FileOutputStream(file_share);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();


            if(Build.VERSION.SDK_INT > 21) {

                bmpUri =  FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName()+".provider", file_share);
                System.out.println("bmpUri = " + bmpUri);

            } else {

                bmpUri = Uri.fromFile(file_share);

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;

    }

    /*public void deleteImage() {
        DatabaseHelper dbh = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase db = dbh.getWritableDatabase();
        Cursor cursor = db.rawQuery("Delete drawname,drawtext from todolisttable where id=" + noteId + " ;", null);
        String sch_status = "", permittype = "";
            if(cursor.moveToFirst())

        {
            do {
                sch_status = cursor.getString(0);
                permittype = cursor.getString(1);
            } while (cursor.moveToNext());
            db.close();
        }
    }*/
}
