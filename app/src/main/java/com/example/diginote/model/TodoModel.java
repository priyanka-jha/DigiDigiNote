package com.example.diginote.model;

public class TodoModel {

    public static final String TABLE_NAME = "todolisttable";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_ITEMTYPE = "itemtype";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_DRAWNAME = "drawname";
    public static final String COLUMN_DRAW = "drawtext";
    /*public static final String COLUMN_DRAWNAME = "drawname";
    public static final String COLUMN_DRAW = "drawtext";*/


    private int id;
    private String title;
    private String note;
    private long timestamp;
    private String itemtype;
    private String drawname;
    private String drawtext;

    private boolean checked = false;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TITLE + " TEXT,"
                    + COLUMN_TEXT + " TEXT,"
                    + COLUMN_ITEMTYPE + " TEXT,"
                    + COLUMN_TIMESTAMP + " TEXT,"
                    + COLUMN_DRAWNAME + " TEXT,"
                    + COLUMN_DRAW + " TEXT"
                    + ")";

    public TodoModel() {
    }

    public TodoModel(int id, String title, String note, long timestamp, String itemtype) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.timestamp = timestamp;
        this.itemtype = itemtype;
    }

    public TodoModel(int id, String title, String note, long timestamp, String itemtype, String drawname, String drawtext) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.timestamp = timestamp;
        this.itemtype = itemtype;
        this.drawname = drawname;
        this.drawtext = drawtext;
    }

    public String getDrawname() {
        return drawname;
    }

    public void setDrawname(String drawname) {
        this.drawname = drawname;
    }

    public String getDrawtext() {
        return drawtext;
    }

    public void setDrawtext(String drawtext) {
        this.drawtext = drawtext;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getItemtype() {
        return itemtype;
    }

    public void setItemtype(String itemtype) {
        this.itemtype = itemtype;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
