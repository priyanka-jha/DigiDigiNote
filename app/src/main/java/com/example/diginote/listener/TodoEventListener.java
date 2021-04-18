package com.example.diginote.listener;


import com.example.diginote.model.TodoModel;

public interface TodoEventListener {


    void onNoteClick(TodoModel todoModel);


    void onNoteLongClick(TodoModel todoModel);


}
