package com.example.diginote.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.example.diginote.databinding.ActivityAddImageBinding;
import com.example.diginote.model.TodoModel;
import com.example.diginote.reminder.AlarmBrodcast;
import com.example.diginote.sharedpref.SharedPref;

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


public class AddImageActivity extends AppCompatActivity {

    public static final String TAG = AddImageActivity.class.getSimpleName();
    ActivityAddImageBinding activityAddImageBinding;
    private TodoModel todoModel;

    public int CLICK_PICTURE = 1;
    public final static int BROWSE_PICTURE =2;

    Uri imageUri;

    File imageFolder,sdCard;

    private Bitmap mBitmap;
    String title = "", note = "", imageText = "", imageName = "", imageName1 = "";
    String imageType = "";
    private String itemType="image";
    long time;
    int noteId = 0;

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


        activityAddImageBinding =  ActivityAddImageBinding.inflate(getLayoutInflater());
        View view = activityAddImageBinding.getRoot();
        setContentView(view);

        reminderpopup = new Dialog(AddImageActivity.this);

        activityAddImageBinding.edTitle.addTextChangedListener(new TextWatcher() {
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

        Toolbar toolbar = activityAddImageBinding.layoutToolbar.toolbar;
        setSupportActionBar(toolbar);

        dh = new DatabaseHelper(this);
        db = dh.getWritableDatabase();

        Intent intent = getIntent();
        noteId = intent.getIntExtra("noteId", 0);

        if (noteId != 0) {

            todoModel = dh.getNote(noteId);

            activityAddImageBinding.edTitle.setVisibility(View.VISIBLE);
            activityAddImageBinding.edNote.setVisibility(View.VISIBLE);

            activityAddImageBinding.edTitle.setText(todoModel.getTitle());
            activityAddImageBinding.edNote.setText(todoModel.getNote());

            imageName = todoModel.getDrawname();
            imageText = todoModel.getDrawtext();


            String imageText = todoModel.getDrawtext();

            if (imageText != null) {
                byte[] image_str;
                try {
                    image_str = Base64.decode(imageText);
                    activityAddImageBinding.image.setImageBitmap(BitmapFactory.decodeByteArray(image_str, 0, image_str.length));
                    mBitmap = BitmapFactory.decodeByteArray(image_str, 0, image_str.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Image not found !!! ", Toast.LENGTH_LONG).show();
            }



        }
        else {

            activityAddImageBinding.edTitle.setVisibility(View.GONE);
            activityAddImageBinding.edNote.setVisibility(View.GONE);

            imageType = getIntent().getStringExtra("imageType");

            try {
                activityAddImageBinding.image.setImageResource(0);
                activityAddImageBinding.image.setImageDrawable(null);
                imageText = "";

            } catch (Exception e) {
                System.out.println("Exception set image==" + e.toString());
            }

            if (imageType.equals("camera")) {


                clickImage();

            }
            else if(imageType.equals("gallery")) {

                browseImage();
            }


        }




    }

    private void clickImage() {

        try{

            String currentdatetime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
            currentdatetime = currentdatetime.replace("/", "");
            currentdatetime = currentdatetime.replace(":", "");
            currentdatetime = currentdatetime.replace(" ", "");
            imageName = "Image_"+currentdatetime+".jpg";

            System.out.println("imageName = " + imageName);

            createImagePath();


            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(Build.VERSION.SDK_INT > 21) {

                File newFile = new File(imageFolder, imageName);

                imageUri =  FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName()+".provider", newFile);


            } else {

                imageUri = Uri.fromFile(new File(sdCard.getPath() + "/DigiNote/" + imageName));   //for api<22

            }

            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(cameraIntent, CLICK_PICTURE);


        }catch(Exception e)

        {
            Toast.makeText(getApplicationContext(), "Please allow the application to access photos,media and files on your device", Toast.LENGTH_SHORT).show();
            System.out.println("permission error=="+e.toString());
        }
    }

    private void createImagePath() {

        if(Build.VERSION.SDK_INT > 21) {

            imageFolder = new File(getApplicationContext().getCacheDir(), "diginoteimages");

           // File newFile = new File(cachePath, imageName);

        }
        else {

            sdCard = Environment.getExternalStorageDirectory();
            imageFolder = new File(sdCard.getPath() + "/DigiNote/");
            // imageFolder = new File(sdCard.getAbsolutePath() + "/.DigiNote/" );   //to hide folder


        }

        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }

        System.out.println("file = " + imageFolder);


    }



    private void browseImage() {

        String currentdatetime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        currentdatetime = currentdatetime.replace("/", "");
        currentdatetime = currentdatetime.replace(":", "");
        currentdatetime = currentdatetime.replace(" ", "");
        imageName = "Image_"+currentdatetime+".jpg";

        System.out.println("imageName = " + imageName);

        createImagePath();

        Intent Gal = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(Gal, BROWSE_PICTURE);

    }

/*    @Override
    protected void onPause() {
        super.onPause();

        System.out.println("method onPause = " );
       // image_folder_delete();

    }

    @Override
    protected void onResume() {
        super.onResume();

        System.out.println("method onResume = " );
       // image_folder_delete();

    }
*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{

			if (data == null) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
			} else

            if (requestCode == BROWSE_PICTURE && resultCode == RESULT_OK && data != null) {

                Uri selectedImage = data.getData();

                System.out.println("selectedImage = " + selectedImage);

                String[] filePathColumn = { MediaStore.MediaColumns.DATA };
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                System.out.println("picturePath = " + picturePath);
                cursor.close();


               // activityAddImageBinding.image.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(picturePath, bounds);
                if (bounds.outWidth == -1) { // TODO: Error
                }
                int width = bounds.outWidth;
                int height = bounds.outHeight;
                System.out.println("IMG H:"+height+" W:"+width);
                try{
                    if ((width >= 800) ) {
                        BitmapFactory.Options options=new BitmapFactory.Options();
                        double scaleFactor=width/ 800d;
                        int scaleFact=(int) Math.round(scaleFactor);
                        double newHeight=height/scaleFactor;
                        int tempHeight=(int) Math.round(newHeight);
                        int compressFact = 100/scaleFact;
                        System.out.println("IMG tempH:"+tempHeight+" W:"+800+" scaleFact:"+scaleFact+" compressFact:"+compressFact);
                        options.inSampleSize = scaleFact;
                        Bitmap bitmap=BitmapFactory.decodeFile(picturePath,options);
                        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 800, tempHeight, true);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        scaled.compress(Bitmap.CompressFormat.PNG, compressFact, stream); //compress to which format you want.

                        //save bitmap in local cache storage to share
                        Bitmap convertedImage = getResizedBitmap(bitmap, 800);

                        try {
                            FileOutputStream out = new FileOutputStream(imageFolder);
                            convertedImage.compress(Bitmap.CompressFormat.JPEG, 90, out);

                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
                        convertedImage.compress(Bitmap.CompressFormat.PNG,100, baos);
                        byte [] b=baos.toByteArray();
                        imageText =Base64.encodeBytes(b);

                        mBitmap = convertedImage;
                        ////


                        activityAddImageBinding.image.setImageBitmap(scaled);
                        activityAddImageBinding.edTitle.setVisibility(View.VISIBLE);
                        activityAddImageBinding.edNote.setVisibility(View.VISIBLE);
                        byte [] byte_arr = stream.toByteArray();
                        imageText = Base64.encodeBytes(byte_arr);
                        // btn_save.setVisibility(View.VISIBLE);
                    }
                    else{
                        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 60, stream); //compress to which format you want.
                        activityAddImageBinding.image.setImageBitmap(bitmap);
                        activityAddImageBinding.edTitle.setVisibility(View.VISIBLE);
                        activityAddImageBinding.edNote.setVisibility(View.VISIBLE);
                        byte [] byte_arr = stream.toByteArray();
                        imageText = Base64.encodeBytes(byte_arr);
                        //  btn_save.setVisibility(View.VISIBLE);
                    }
                }
                catch(Exception e) {
                    Log.e("", "ROME parse error ui185: " + e.toString());
                }
                catch(Error e2) {
                    Log.e("", "ROME parse error ui188: " + e2.toString());
                    Toast.makeText(getApplicationContext(), "Image size exceed!!!", Toast.LENGTH_LONG).show();
                }
            }

            else if(requestCode == CLICK_PICTURE && resultCode == RESULT_OK && imageUri != null) {
                System.out.println("convertedImage = " + imageUri);

                Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
                Bitmap convertedImage = getResizedBitmap(photo, 800);

                try {
                    FileOutputStream out = new FileOutputStream(imageFolder);
                    convertedImage.compress(Bitmap.CompressFormat.JPEG, 90, out);

                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos=new  ByteArrayOutputStream();
                convertedImage.compress(Bitmap.CompressFormat.PNG,100, baos);
                byte [] b=baos.toByteArray();
                imageText =Base64.encodeBytes(b);

              //  imageUri = Uri.fromFile(new File(sdCard.getPath() + "/DigiNote/" + imageName));

               // System.out.println("imageUri = " + imageUri);
                mBitmap = convertedImage;
                activityAddImageBinding.image.setImageBitmap(convertedImage);
                activityAddImageBinding.edTitle.setVisibility(View.VISIBLE);
                activityAddImageBinding.edNote.setVisibility(View.VISIBLE);

            }



        }catch(Exception e)
        {
            System.out.println("Exception image"+e.toString());
        }
        catch(Error e2) {
            System.out.println("Exception image parse"+e2.toString());
            Toast.makeText(getApplicationContext(), "Image size exceed!!!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
      //  super.onBackPressed();

        //  image_folder_delete();

        /*if(activityAddImageBinding.edTitle.getVisibility() == View.VISIBLE &&
                activityAddImageBinding.edNote.getVisibility() == View.VISIBLE) {

            saveImage();
        }
        else {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }*/

        saveImage();

    }

    private void saveImage() {

        title = activityAddImageBinding.edTitle.getText().toString().trim();
        note = activityAddImageBinding.edNote.getText().toString().trim();


        try {
            if (title.isEmpty()) {
                Util.showToast(getApplicationContext(), "Please enter title!");

            } else {
                time = new Date().getTime(); // get current time;

                if (noteId == 0) {
                    createNote(title,note);
                } else {
                    updateNote(title, note);
                }


            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }





    }

    private void updateNote(String title, String note) {
        try {
            todoModel.setTitle(title);
            todoModel.setNote(note);
            todoModel.setItemtype(itemType);
            todoModel.setTimestamp(time);
            todoModel.setDrawname(imageName);
            todoModel.setDrawtext(imageText);
            int id = dh.updateNote(todoModel);

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            //  onBackPressed();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void createNote(String title, String note) {

        try {
            long id = dh.insertNote(title, note, time, itemType, imageName, imageText);
            // onBackPressed();

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }


    }


    public void image_folder_delete(){
       // String filename2 = "Image_1.jpg";
        // String filename1 = ".nomedia";
       // System.out.println("filename = " + filename2);

     //   createImagePath();

        if(imageFolder.exists()){
            imageFolder.delete();
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.addimage_menu, menu);

        if(activityAddImageBinding.edTitle.getText().length()==0){
            menu.getItem(0).setVisible(false);

        }
        else {
            menu.getItem(0).setVisible(true);

        }
        if(noteId==0){
            menu.getItem(1).setVisible(false);
        }
        else {
            menu.getItem(1).setVisible(true);
        }
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {


            case R.id.menu_share:
                try {
                    shareData();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddImageActivity.this,
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


                TimePickerDialog timePickerDialog = new TimePickerDialog(AddImageActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddImageActivity.this, R.layout.reminder_spinner, arraySpinner);
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

        String message = activityAddImageBinding.edTitle.getText().toString();
        if(message.isEmpty()){
            Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show();
        }
        else {
            setAlarm(activityAddImageBinding.edTitle.getText().toString(), selectedAlarmDate, selectedAlarmTime);
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



    private void shareData() throws IOException {

        try {
            title = activityAddImageBinding.edTitle.getText().toString().trim();
            note = activityAddImageBinding.edNote.getText().toString().trim();
            String shareBody = title+": "+note;


            if(title.isEmpty() || note.isEmpty()) {
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
            file_share = new File(cachePath, imageName);

        }
        else {

             File sdCard = Environment.getExternalStorageDirectory();
             file_share = new File(sdCard.getPath() + "/DigiNote/" + imageName);

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
}
