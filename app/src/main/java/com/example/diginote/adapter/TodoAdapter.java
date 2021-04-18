package com.example.diginote.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diginote.R;
import com.example.diginote.activities.Base64;
import com.example.diginote.databinding.ActivityMainBinding;
import com.example.diginote.databinding.TodolayoutBinding;
import com.example.diginote.listener.TodoEventListener;
import com.example.diginote.model.TodoModel;
import com.example.diginote.sharedpref.SharedPref;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder>{

    public static final String TAG = TodoAdapter.class.getSimpleName();

    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy  hh:mm aaa", Locale.US);

    private TodoEventListener todoEventListener;

    private ArrayList<TodoModel> todoModelList;
    private Context context;

    private boolean multiCheckMode = false;
    SharedPref sharedPrefren;

    public TodoAdapter() {
    }

    public void setTodoEventListener(TodoEventListener todoEventListener) {
        this.todoEventListener = todoEventListener;
    }

    public TodoAdapter(ArrayList<TodoModel> todoModelList, Context context) {
        this.todoModelList = todoModelList;
        this.context = context;

        sharedPrefren = new SharedPref(context);

    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TodoViewHolder todoViewHolder = null;
        try {

            TodolayoutBinding todolayoutBinding =  TodolayoutBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
            View view = todolayoutBinding.getRoot();
            todoViewHolder = new TodoViewHolder(view);

        } catch (Exception e) {
            Log.e(TAG,e.toString());
        }

        return todoViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {

        final TodoModel todoModel = todoModelList.get(position);

        if (todoModel != null) {

            try {

                if (todoModel.getTitle().equals("")) {
                    holder.todolayoutBinding.textTitle.setVisibility(View.GONE);

                }

                if (todoModel.getNote().equals("")) {
                    holder.todolayoutBinding.textNote.setVisibility(View.GONE);

                }

                if (todoModel.getDrawtext().equals("")) {
                    holder.todolayoutBinding.image.setVisibility(View.GONE);

                }


                holder.todolayoutBinding.textTitle.setText(todoModel.getTitle());

                String date = date(todoModel.getTimestamp());
                holder.todolayoutBinding.textDate.setText(date);

                if(todoModel.getItemtype().equals("checklist")){
                    String note = "", noteText = "";
                    String[] noteTextArray;
                    ArrayList<String> checkArrayList = new ArrayList<>();
                    ArrayList<String> noteCheckArrayList = new ArrayList<>();

                    note = todoModel.getNote();
                    String[] check = note.split(",");
                    for(int i=0; i<check.length; i++){
                        checkArrayList.add(check[i]);
                    }

                    for(int i=0; i<checkArrayList.size(); i++){
                        noteTextArray = checkArrayList.get(i).split(":");
                        noteText = noteTextArray[0];
                        System.out.println("noteText = " + noteText);

                        noteCheckArrayList.add(noteText);
                    }

                    String checkNote = TextUtils.join(", ", noteCheckArrayList);
                    holder.todolayoutBinding.textNote.setText(checkNote);


                }
                else {
                    holder.todolayoutBinding.textNote.setText(todoModel.getNote());
                }


                holder.todolayoutBinding.cardview.setBackgroundColor(Color.TRANSPARENT);   //to set corner background transparent

                String imageText = todoModel.getDrawtext();

                if(imageText != null){
                    byte[] image_str;
                    try {
                        image_str = Base64.decode(imageText);
                        holder.todolayoutBinding.image.setImageBitmap(BitmapFactory.decodeByteArray(image_str, 0, image_str.length));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(context, "Image not found !!! ", Toast.LENGTH_LONG).show();
                }



                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        todoEventListener.onNoteClick(todoModel);
                     }
                });


                 holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        todoEventListener.onNoteLongClick(todoModel);
                        return false;
                    }
                });


                // check checkBox if note selected
                if (multiCheckMode) {
                     // Highlight item if multiMode on

                    if(todoModel.isChecked()) {
                       // holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.black));
                        try {
                            if (sharedPrefren.loadNightModeState() == true) {
                                holder.itemView.setBackgroundColor(Color.WHITE);
                            } else {
                                holder.itemView.setBackgroundColor(Color.BLACK);
                            }
                        } catch (Exception e) {
                            Log.e(TAG,e.toString());
                        }
                    }
                    else {
                        holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                    }
                }
                else
                {
                    // Unhighlight item if multiMode off
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
                }


            } catch (Exception e) {
                Log.e(TAG,e.toString());

            }

        }

    }


    @Override
    public int getItemCount() {
        return todoModelList.size();
    }



    public class TodoViewHolder extends RecyclerView.ViewHolder {

        TodolayoutBinding todolayoutBinding;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            todolayoutBinding =  TodolayoutBinding.bind(itemView);


        }

    }

    public String date(long timestamp) {

        return sdf.format(timestamp);
    }

    private TodoModel getTodoItem(int position) {
        return todoModelList.get(position);
    }

    //to get all checked notes
    public List<TodoModel> getCheckedNotes() {
        List<TodoModel> checkedNotes = new ArrayList<>();
        for (TodoModel n : this.todoModelList) {
            if (n.isChecked())
                checkedNotes.add(n);
        }

        return checkedNotes;
    }

    public void setMultiCheckMode(boolean multiCheckMode) {
        this.multiCheckMode = multiCheckMode;
        notifyDataSetChanged();
    }
}
