package com.example.diginote.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.diginote.R;
import com.example.diginote.Util;
import com.example.diginote.database.DatabaseHelper;
import com.example.diginote.databinding.ActivityChecklistBinding;
import com.example.diginote.databinding.ActivitySpeechBinding;
import com.example.diginote.model.TodoModel;
import com.example.diginote.reminder.AlarmBrodcast;
import com.example.diginote.sharedpref.SharedPref;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SpeechActivity extends AppCompatActivity {

    public static final String TAG = SpeechActivity.class.getSimpleName();
    ActivitySpeechBinding activitySpeechBinding;
    private TodoModel todoModel;

    private final int REQ_CODE_SPEECH_INPUT1 = 101;
    private final int REQ_CODE_SPEECH_INPUT2 = 102;

    String title = "" ,text = "", drawName = "", drawText = "";
    private long time;
    int noteId=0;
    private String itemType="speech";

    DatabaseHelper dh;
    SQLiteDatabase db;
    SharedPref sharedPrefren;

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
            Log.e(TAG,e.toString());
        }

        super.onCreate(savedInstanceState);
        activitySpeechBinding =  ActivitySpeechBinding.inflate(getLayoutInflater());
        View view = activitySpeechBinding.getRoot();
        setContentView(view);

        reminderpopup = new Dialog(SpeechActivity.this);

        activitySpeechBinding.edTitle.requestFocus();

        activitySpeechBinding.edTitle.addTextChangedListener(new TextWatcher() {
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

        Toolbar toolbar = activitySpeechBinding.layoutToolbar.toolbar;
        setSupportActionBar(toolbar);

        dh = new DatabaseHelper(this);
        db = dh.getWritableDatabase();

        Intent intent = getIntent();
        noteId = intent.getIntExtra("noteId",0);

        if (noteId!=0){

            todoModel = dh.getNote(noteId);
            activitySpeechBinding.edTitle.setText(todoModel.getTitle());
            activitySpeechBinding.edNote.setText(todoModel.getNote());
        }
        else {

            activitySpeechBinding.edTitle.setFocusable(true);

        }


        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        activitySpeechBinding.btnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    if (activitySpeechBinding.edTitle.hasFocus()) {
                        recordNotes(REQ_CODE_SPEECH_INPUT1);

                    }
                    else if (activitySpeechBinding.edNote.hasFocus()) {
                        recordNotes(REQ_CODE_SPEECH_INPUT2);
                    }
                } catch (Exception e) {
                    Log.e(TAG,e.toString());
                }


            }
        });

    }

    private void recordNotes(int req_code_speech_input1) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Say something");
            try {
                startActivityForResult(intent, req_code_speech_input1);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG,e.toString());
                Util.showToast(getApplicationContext(),"Text to speech not supported on your device");

            }
        }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.addnotes_menu, menu);

        if(activitySpeechBinding.edTitle.getText().length()==0){
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(false);
        }
        else {
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setVisible(true);
        }
        if(noteId==0){
            menu.getItem(2).setVisible(false);
        }
        else {
            menu.getItem(2).setVisible(true);
        }
        return true;

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.menu_save:

                saveNotes();

                return true;

            case R.id.menu_share:

                try {
                    title = activitySpeechBinding.edTitle.getText().toString().trim();
                    text = activitySpeechBinding.edNote.getText().toString().trim();
                    if(title.isEmpty() || text.isEmpty()) {
                        Util.showToast(getApplicationContext(),"Please enter title!");
                    }
                    else {
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        String shareBody = title+": "+text;
                        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                    }
                } catch (Exception e) {
                    Log.e(TAG,e.toString());
                }
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(SpeechActivity.this,
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


                TimePickerDialog timePickerDialog = new TimePickerDialog(SpeechActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SpeechActivity.this, R.layout.reminder_spinner, arraySpinner);
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

        String message = activitySpeechBinding.edTitle.getText().toString();
        if(message.isEmpty()){
            Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show();
        }
        else {
            setAlarm(activitySpeechBinding.edTitle.getText().toString(), selectedAlarmDate, selectedAlarmTime);
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






    private void saveNotes() {

        title = activitySpeechBinding.edTitle.getText().toString().trim();
        text = activitySpeechBinding.edNote.getText().toString().trim();

        try {
            if (title.isEmpty()) {
                Util.showToast(getApplicationContext(),"Please enter title!");

            }
            else {
                time = new Date().getTime(); // get current time;
                if (noteId==0){
                    createNote();
                }
                else {
                    updateNote();
                }
            }
        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }

    }

    private void updateNote() {
        try {
            todoModel.setTitle(title);
            todoModel.setNote(text);
            todoModel.setItemtype(itemType);
            todoModel.setTimestamp(time);
            todoModel.setDrawname(drawName);
            todoModel.setDrawtext(drawText);
            int id = dh.updateNote(todoModel);
            onBackPressed();
        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }
    }

    public void createNote() {

        try {
            long id = dh.insertNote(title,text,time,itemType,drawName,drawText);
            onBackPressed();
        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT1: {
                try {
                    if (resultCode == Activity.RESULT_OK && null != data) {

                        ArrayList<String> result = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        activitySpeechBinding.edTitle.setText(result.get(0));
                        activitySpeechBinding.edNote.requestFocus();
                    }
                } catch (Exception e) {
                    Log.e(TAG,e.toString());
                }
                break;
            }

            case REQ_CODE_SPEECH_INPUT2: {
                try {
                    if (resultCode == RESULT_OK && null != data) {

                        ArrayList<String> result = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        activitySpeechBinding.edNote.setText(result.get(0));

                    }
                } catch (Exception e) {
                    Log.e(TAG,e.toString());
                }
                break;

            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }

}
