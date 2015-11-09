package com.nkc.metercheck.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.util.Log;

import com.nkc.metercheck.model.Meter;
import com.nkc.metercheck.model.Room;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jumpon-pc on 4/11/2558.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    //Logcat tag
    private static final String LOG = DatabaseHelper.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "meterDorm";

    // Table Names
    private static final String TABLE_ROOM = "room";
    private static final String TABLE_METER = "meter";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // Room Table - column names
    private static final String KEY_DORM_ID = "dorm_id";
    private static final String KEY_CAPACITY = "capacity";
    private static final String KEY_TOILET = "toilet";
    private static final String KEY_ROOM_TYPE = "room_type";
    private static final String KEY_ROOM_STATUS = "room_status";

    // Meter Table - column names
    private static final String KEY_MONTHS = "months";
    private static final String KEY_TERMS = "terms";
    private static final String KEY_YEARS = "years";
    private static final String KEY_ROOM_ID = "room_id";
    private static final String KEY_METER_START = "meter_start";
    private static final String KEY_METER_END = "meter_end";
    private static final String KEY_PAY_TYPE = "pay_type";

    // Table Create Statements
    // Room table create statement
    private static final String CREATE_TABLE_ROOM = "CREATE TABLE "
            + TABLE_ROOM + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ROOM_ID + " TEXT," + KEY_DORM_ID
            + " INTEGER," + KEY_CAPACITY + " INTEGER," + KEY_TOILET
            + " INTEGER," + KEY_ROOM_TYPE + " TEXT," + KEY_ROOM_STATUS
            + " INTEGER" + ")";

    // Meter table create statement
    private static final String CREATE_TABLE_METER = "CREATE TABLE "
            + TABLE_METER + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ROOM_ID
            + " TEXT," + KEY_MONTHS + " INTEGER," + KEY_TERMS
            + " INTEGER," + KEY_YEARS + " TEXT," + KEY_METER_START + " TEXT," + KEY_METER_END
            + " TEXT," + KEY_PAY_TYPE + " INTEGER," + KEY_CREATED_AT + " DATETIME)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_ROOM);
        db.execSQL(CREATE_TABLE_METER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_METER);

        // create new tables
        onCreate(db);
    }

    // ------------------------ "Room" table methods ----------------//
    public void createRoom(String room_id, Integer dorm_id, Integer capacity, Integer toilet, String room_type, Integer room_status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ROOM_ID, room_id);
        values.put(KEY_DORM_ID, dorm_id);
        values.put(KEY_CAPACITY, capacity);
        values.put(KEY_TOILET, toilet);
        values.put(KEY_ROOM_TYPE, room_type);
        values.put(KEY_ROOM_STATUS, room_status);

        // Inserting Row
        long id = db.insert(TABLE_ROOM, null, values);

        Log.d(LOG, "New Room inserted into sqlite: " + id);
    }

    // ตรวจสอบข้อมูลห้อง
    public int checkRoom(String room_id) {

        String query = "SELECT * FROM " + TABLE_ROOM + " WHERE " + KEY_ROOM_ID + "='" + room_id + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    // ------------------------ "Meter" table methods ----------------//
    public void createMeter(String room_id, Integer months, Integer terms, String years, String meter_start, String meter_end, Integer pay_type) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ROOM_ID, room_id);
        values.put(KEY_MONTHS, months);
        values.put(KEY_TERMS, terms);
        values.put(KEY_YEARS, years);
        values.put(KEY_METER_START, meter_start);
        values.put(KEY_METER_END, meter_end);
        values.put(KEY_PAY_TYPE, pay_type);
        values.put(KEY_CREATED_AT, getDateTime());

        // Inserting Row
        long id = db.insert(TABLE_METER, null, values);

        Log.d(LOG, "New Meter inserted into sqlite: " + id);
    }

    // Check meter
    public int chkMeter(String room_id, Integer months, Integer terms, String Years) {

        String query = "SELECT * FROM " + TABLE_METER + " WHERE " + KEY_ROOM_ID
                + "='" + room_id + "' AND " + KEY_MONTHS + "=" + months
                + " AND " + KEY_TERMS + "=" + terms + " AND " + KEY_YEARS
                + "='" + Years + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    // Check meter
    public String lastMeter(String room_id, Integer months, Integer terms, String Years) {
        String meter = "0.00";
        String query = "SELECT * FROM " + TABLE_METER + " WHERE " + KEY_ROOM_ID
                + "='" + room_id + "' AND " + KEY_MONTHS + "=" + (months - 1)
                + " AND " + KEY_TERMS + "=" + terms + " AND " + KEY_YEARS
                + "='" + Years + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            meter = cursor.getString(cursor.getColumnIndex(KEY_METER_END));
        }

        cursor.close();

        return meter;
    }

    // Check room
    public int chkRoom(String room_id) {

        String query = "SELECT * FROM " + TABLE_ROOM + " WHERE " + KEY_ROOM_ID + "='" + room_id + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    // getting all rooms
    public List<Room> getAllRooms(Integer month, Integer term, String year, String search) {
        ArrayList<Room> rooms = new ArrayList<Room>();
        String selectQuery = "SELECT " + TABLE_ROOM + "." + KEY_ROOM_ID + ", " + TABLE_METER + "." + KEY_METER_START + ", "
                + TABLE_METER + "." + KEY_METER_END + " FROM " + TABLE_ROOM + " LEFT JOIN " + TABLE_METER
                + " ON " + TABLE_ROOM + "." + KEY_ROOM_ID + "=" + TABLE_METER + "." + KEY_ROOM_ID
                + " WHERE (" + TABLE_METER + "." + KEY_MONTHS + "=" + month + " OR " + TABLE_METER + "." + KEY_MONTHS + " IS NULL) "
                + "AND (" + TABLE_METER + "." + KEY_TERMS + "=" + term + " OR " + TABLE_METER + "." + KEY_TERMS + " IS NULL " + ") "
                + "AND (" + TABLE_METER + "." + KEY_YEARS + "='" + year + "' OR " + TABLE_METER + "." + KEY_YEARS + " IS NULL)";

        if (search != "") {
            selectQuery += " AND " + TABLE_METER + "." + KEY_ROOM_ID + " LIKE '" + search + "'";
        }

        Log.i("selectQuery", selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Room r = new Room();
                r.setRoomId(c.getString(c.getColumnIndex(KEY_ROOM_ID)));
                r.setMeterStart(c.getString(c.getColumnIndex(KEY_METER_START)));
                r.setMeterEnd(c.getString(c.getColumnIndex(KEY_METER_END)));

                rooms.add(r);
            } while (c.moveToNext());
        }
        this.closeDB();
        return rooms;
    }

    // getting all rooms
    public List<Meter> getAllMeter(Integer month, Integer term, String year) {
        ArrayList<Meter> meters = new ArrayList<Meter>();
        String selectQuery = "SELECT * FROM " + TABLE_METER + " WHERE " + KEY_MONTHS + "=" + month
                + " AND " + KEY_TERMS + "=" + term + " AND " + KEY_YEARS + "='" + year + "'";

        Log.i("selectQuery", selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Meter m = new Meter();
                m.setRoomId(c.getString(c.getColumnIndex(KEY_ROOM_ID)));
                m.setMonths(Integer.valueOf(c.getString(c.getColumnIndex(KEY_MONTHS))));
                m.setTerms(Integer.valueOf(c.getString(c.getColumnIndex(KEY_TERMS))));
                m.setYears(c.getString(c.getColumnIndex(KEY_YEARS)));
                m.setMeterStart(c.getString(c.getColumnIndex(KEY_METER_START)));
                m.setMeterEnd(c.getString(c.getColumnIndex(KEY_METER_END)));
                m.setPayType(Integer.valueOf(c.getString(c.getColumnIndex(KEY_PAY_TYPE))));
                m.setCreate(c.getString(c.getColumnIndex(KEY_CREATED_AT)));

                meters.add(m);
            } while (c.moveToNext());
        }
        this.closeDB();
        return meters;
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    /**
     * get datetime
     */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
