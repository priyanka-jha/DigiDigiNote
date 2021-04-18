package com.example.diginote.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.diginote.R;
import com.example.diginote.Util;
import com.example.diginote.database.DatabaseHelper;
import com.example.diginote.databinding.ActivityAddNotesBinding;
import com.example.diginote.databinding.ActivityChecklistBinding;
import com.example.diginote.databinding.ChecklistItemBinding;
import com.example.diginote.model.TodoModel;
import com.example.diginote.reminder.AlarmBrodcast;
import com.example.diginote.sharedpref.SharedPref;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChecklistActivity extends AppCompatActivity {

    public static final String TAG = ChecklistActivity.class.getSimpleName();
    ActivityChecklistBinding activityChecklistBinding;
    private TodoModel todoModel;

    String title = "" ,text = "", drawName = "", drawText = "";
    private String itemType="checklist";
    String checkNote;

    long time;
    int noteId=0;

    DatabaseHelper dh;
    SQLiteDatabase db;
    SharedPref sharedPrefren;

    ArrayList<Item> itemlist;
    ItemsListAdapter arrayAdapter;

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


        activityChecklistBinding =  ActivityChecklistBinding.inflate(getLayoutInflater());
        View view = activityChecklistBinding.getRoot();
        setContentView(view);

        reminderpopup = new Dialog(ChecklistActivity.this);

        itemlist = new ArrayList<Item>();
        activityChecklistBinding.edTitle.requestFocus();

        activityChecklistBinding.edTitle.addTextChangedListener(new TextWatcher() {
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

        Toolbar toolbar = activityChecklistBinding.layoutToolbar.toolbar;
        setSupportActionBar(toolbar);
        dh = new DatabaseHelper(this);
        db = dh.getWritableDatabase();

        Intent intent = getIntent();
        noteId = intent.getIntExtra("noteId",0);

        if (noteId!=0){

            todoModel = dh.getNote(noteId);
            activityChecklistBinding.edTitle.setText(todoModel.getTitle());
            activityChecklistBinding.edTitle.setSelection(activityChecklistBinding.edTitle.getText().length());
            checkNote = todoModel.getNote();

            setList(checkNote);


        }
        else {

            activityChecklistBinding.edTitle.setFocusable(true);

        }


        activityChecklistBinding.imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item_data = activityChecklistBinding.edText.getText().toString();
                Item item = new Item(item_data, false);
                itemlist.add(item);
                arrayAdapter = new ItemsListAdapter(getApplicationContext(), itemlist);
                activityChecklistBinding.checkItemsList.setAdapter(arrayAdapter);
                activityChecklistBinding.edText.setText("");
                arrayAdapter.notifyDataSetChanged();

                //listitems.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
        });


    }

    private void setList(String checkNote) {

        String[] checkValueArray;
        String checkValue="";
        Boolean isChecked=false;
        ArrayList<String> checkArrayList = new ArrayList<>();

        String[] check = checkNote.split(",");
        for (int i = 0; i < check.length; i++)

        {

            checkArrayList.add(check[i]);

        }

        for(int i=0; i<checkArrayList.size(); i++){

            checkValueArray = checkArrayList.get(i).split(":");
            checkValue = checkValueArray[0];
            isChecked = Boolean.valueOf(checkValueArray[1]);


            Item item = new Item(checkValue, isChecked);
            itemlist.add(item);
            arrayAdapter = new ItemsListAdapter(getApplicationContext(), itemlist);
            activityChecklistBinding.checkItemsList.setAdapter(arrayAdapter);
            activityChecklistBinding.edText.setText("");
            arrayAdapter.notifyDataSetChanged();

        }



    }

    public class Item {
        boolean checked;
        String ItemString;

        Item(String t, boolean b) {
            ItemString = t;
            checked = b;
        }

        public Item() {

        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public String getItemString() {
            return ItemString;
        }

        public void setItemString(String itemString) {
            ItemString = itemString;
        }
    }

    static class ViewHolder {

        CheckBox checkBox;
        EditText text;
        ImageView cancelitem;

    }


    public class ItemsListAdapter extends BaseAdapter {


        private Context context;
        private List<Item> list;

        ItemsListAdapter(Context c, List<Item> l) {
            context = c;
            list = l;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public boolean isChecked(int position) {
            return list.get(position).checked;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            // reuse views
            ViewHolder viewHolder = new ViewHolder();

            if (rowView == null) {
                LayoutInflater inflater = (ChecklistActivity.this).getLayoutInflater();
                rowView = inflater.inflate(R.layout.checklist_item, null);

                viewHolder.checkBox = (CheckBox) rowView.findViewById(R.id.check_item);
                viewHolder.text = (EditText) rowView.findViewById(R.id.item_data);
                viewHolder.cancelitem = (ImageView) rowView.findViewById(R.id.cancelitem);
                rowView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) rowView.getTag();
            }

            viewHolder.checkBox.setChecked(list.get(position).checked);
            boolean newState = !list.get(position).isChecked();
            list.get(position).checked = newState;

            if (newState== false)
            {

                viewHolder.text.setPaintFlags( viewHolder.text.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

            }
            if (newState == true) {

                viewHolder.text.setPaintFlags(viewHolder.text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            final String itemStr = list.get(position).ItemString;
            viewHolder.text.setText(itemStr);

            viewHolder.checkBox.setTag(position);

            final ViewHolder finalViewHolder = viewHolder;
            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean newState = !list.get(position).isChecked();
                    list.get(position).checked = newState;


                    if (newState== false)
                    {

                        finalViewHolder.text.setPaintFlags( finalViewHolder.text.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

                    }
                    if (newState == true) {

                        finalViewHolder.text.setPaintFlags(finalViewHolder.text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                }
            });

            viewHolder.checkBox.setChecked(isChecked(position));
            viewHolder.cancelitem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemlist.remove(position);
                    arrayAdapter.notifyDataSetChanged();

                }
            });

            return rowView;
        }
    }

    private void saveNote() {


        title = activityChecklistBinding.edTitle.getText().toString().trim();

        try {
            if (title.isEmpty()) {
                Util.showToast(getApplicationContext(),"Please enter title!");

            }
            else {
                time = new Date().getTime(); // get current time;

                if (noteId==0){
                    createCheckList();
                }
                else {
                    updateCheckList();
                }



            }
        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }

    }

    private void updateCheckList() {
        ArrayList<String> noteArrayList = new ArrayList<>();
        noteArrayList.clear();

        title = activityChecklistBinding.edTitle.getText().toString().trim();

        for(int i=0; i<itemlist.size(); i++) {

            String note = itemlist.get(i).ItemString+ ":"+ itemlist.get(i).checked;
            noteArrayList.add(note);
        }

        String note1 =  TextUtils.join(",", noteArrayList);
        System.out.println("note = " +" " + note1);

        try {
            todoModel.setTitle(title);
            todoModel.setNote(note1);
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

    private void createCheckList() {

        ArrayList<String> noteArrayList = new ArrayList<>();
        noteArrayList.clear();

        title = activityChecklistBinding.edTitle.getText().toString().trim();


        for(int i=0; i<itemlist.size(); i++) {

            String note = itemlist.get(i).ItemString+ ":"+ itemlist.get(i).checked;
            noteArrayList.add(note);
        }

        String note1 =  TextUtils.join(",", noteArrayList);
        System.out.println("note = " +" " + note1);

        try {
            // callBroadCast();
            long id = dh.insertNote(title,note1,time,itemType,drawName,drawText);
            onBackPressed();


        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addnotes_menu, menu);
        if(activityChecklistBinding.edTitle.getText().length()==0){
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

                saveNote();
                //createCheckList();
                return true;

            case R.id.menu_share:

                try {
                    title = activityChecklistBinding.edTitle.getText().toString().trim();

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

                DatePickerDialog datePickerDialog = new DatePickerDialog(ChecklistActivity.this,
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


                TimePickerDialog timePickerDialog = new TimePickerDialog(ChecklistActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ChecklistActivity.this, R.layout.reminder_spinner, arraySpinner);
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

        String message = activityChecklistBinding.edTitle.getText().toString();
        if(message.isEmpty()){
            Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show();
        }
        else {
            setAlarm(activityChecklistBinding.edTitle.getText().toString(), selectedAlarmDate, selectedAlarmTime);
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



    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }
}
