package com.example.diginote.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.diginote.model.TodoModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "todo_db";


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(TodoModel.CREATE_TABLE);
        System.out.println("db created = " + db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TodoModel.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public long insertNote(String title, String text, long time, String itemtype, String drawname, String drawtext) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(TodoModel.COLUMN_TITLE, title);
        values.put(TodoModel.COLUMN_TEXT, text);
        values.put(TodoModel.COLUMN_TIMESTAMP, time);
        values.put(TodoModel.COLUMN_ITEMTYPE, itemtype);
        values.put(TodoModel.COLUMN_DRAWNAME, drawname);
        values.put(TodoModel.COLUMN_DRAW, drawtext);

        // insert row
        long id = db.insert(TodoModel.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public int getNotesCount() {
        String countQuery = "SELECT  * FROM " + TodoModel.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public List<TodoModel> getAllNotes() {
        List<TodoModel> notes = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TodoModel.TABLE_NAME + " ORDER BY " +
                TodoModel.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TodoModel note = new TodoModel();
                note.setId(cursor.getInt(cursor.getColumnIndex(TodoModel.COLUMN_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TITLE)));
                note.setNote(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TEXT)));
                note.setItemtype(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_ITEMTYPE)));
               // note.setTimestamp(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TIMESTAMP)));
                note.setTimestamp(Long.parseLong(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TIMESTAMP))));
                note.setDrawname(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_DRAWNAME)));
                note.setDrawtext(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_DRAW)));

                notes.add(note);


            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return notes;
    }

    public List<TodoModel> getAllNotesDateCreated() {
        List<TodoModel> notes = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TodoModel.TABLE_NAME + " ORDER BY " +
                TodoModel.COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TodoModel note = new TodoModel();
                note.setId(cursor.getInt(cursor.getColumnIndex(TodoModel.COLUMN_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TITLE)));
                note.setNote(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TEXT)));
                note.setItemtype(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_ITEMTYPE)));
                // note.setTimestamp(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TIMESTAMP)));
                note.setTimestamp(Long.parseLong(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TIMESTAMP))));
                note.setDrawname(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_DRAWNAME)));
                note.setDrawtext(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_DRAW)));

                notes.add(note);



            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        //
        // return notes list
        return notes;
    }

    public List<TodoModel> getAllNotesAlphabetically() {
        List<TodoModel> notes = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TodoModel.TABLE_NAME + " ORDER BY " +
                TodoModel.COLUMN_TITLE + " COLLATE NOCASE ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TodoModel note = new TodoModel();
                note.setId(cursor.getInt(cursor.getColumnIndex(TodoModel.COLUMN_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TITLE)));
                note.setNote(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TEXT)));
                note.setItemtype(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_ITEMTYPE)));
                // note.setTimestamp(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TIMESTAMP)));
                note.setTimestamp(Long.parseLong(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TIMESTAMP))));
                note.setDrawname(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_DRAWNAME)));
                note.setDrawtext(cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_DRAW)));

                notes.add(note);

                System.out.println("title.."+cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TITLE)));


            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return notes;
    }

    public TodoModel getNote(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TodoModel.TABLE_NAME,
                new String[]{TodoModel.COLUMN_ID, TodoModel.COLUMN_TITLE,TodoModel.COLUMN_TEXT, TodoModel.COLUMN_TIMESTAMP, TodoModel.COLUMN_ITEMTYPE, TodoModel.COLUMN_DRAWNAME, TodoModel.COLUMN_DRAW},
                TodoModel.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
       /* TodoModel note = new TodoModel(
                cursor.getInt(cursor.getColumnIndex(TodoModel.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TEXT))),
                cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TIMESTAMP)),
                cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_ITEMTYPE)));
*/

       TodoModel note = new TodoModel(
               cursor.getInt(cursor.getColumnIndex(TodoModel.COLUMN_ID)),
               cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TITLE)),
               cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_TEXT)),
               cursor.getLong(cursor.getColumnIndex(TodoModel.COLUMN_TIMESTAMP)),
               cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_ITEMTYPE)),
               cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_DRAWNAME)),
               cursor.getString(cursor.getColumnIndex(TodoModel.COLUMN_DRAW))
       );
        // close the db connection
        cursor.close();

        return note;
    }



    public int updateNote(TodoModel note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TodoModel.COLUMN_TITLE, note.getTitle());
        values.put(TodoModel.COLUMN_TEXT, note.getNote());
        values.put(TodoModel.COLUMN_TIMESTAMP, note.getTimestamp());
        values.put(TodoModel.COLUMN_ITEMTYPE, note.getItemtype());
        values.put(TodoModel.COLUMN_DRAWNAME, note.getDrawname());
        values.put(TodoModel.COLUMN_DRAW, note.getDrawtext());

        // updating row
        return db.update(TodoModel.TABLE_NAME, values, TodoModel.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
    }

    public void deleteNote(TodoModel note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TodoModel.TABLE_NAME, TodoModel.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
        db.close();
    }


}
