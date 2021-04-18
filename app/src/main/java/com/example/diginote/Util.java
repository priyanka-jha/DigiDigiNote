package com.example.diginote;

import android.content.Context;
import android.widget.Toast;

public class Util {

   // private static Context context;

    public static void showToast(Context context, String message) {

        Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();

    }




}
