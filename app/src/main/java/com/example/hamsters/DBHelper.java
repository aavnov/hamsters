package com.example.hamsters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DBHelper extends SQLiteOpenHelper
{
    /** Database version */
    public static final int     DATABASE_VERSION   = 1;
    /** Database name */
    public static final  String DATABASE_NAME      = "dbHamster";

    // Table name
    public static final  String TABLE_NAME_HAMSTER  = "hamster";
    // Column name
    /** Field 0 of the table TABLE_HAMSTER, which is the primary key      */
    public static final String HAMSTER_COLUMN_ID     = "_id";
    /** Field 1 of the table TABLE_HAMSTER, stores the name of the hamster  */
    public static final String HAMSTER_COLUMN_TITLE   = "title";
    /** Field 2 of the table TABLE_HAMSTER, stores the text of the hamster  */
    public static final String HAMSTER_COLUMN_DESCRIPTION   = "description";
    /** Field 3 of the table TABLE_HAMSTER, stores the http of the image hamster */
    public static final String HAMSTER_COLUMN_HTTPIMAGE  = "httpimage";
    /** Field 4 of the table TABLE_HAMSTER, stores the pixel of the image hamster  */
    public static final String HAMSTER_COLUMN_IMAGE  = "image";
    /** Field 5 of the table TABLE_HAMSTER, stores the pinned of the hamster  */
    public static final String HAMSTER_COLUMN_PINNED  = "pinned";


    // скрипт создания Таблицы
    private static final String TABLE_CREATE_HAMSTER = "create table "
            + TABLE_NAME_HAMSTER         + "("
            + HAMSTER_COLUMN_ID          + " integer primary key, "
            + HAMSTER_COLUMN_TITLE       + " TEXT, "
            + HAMSTER_COLUMN_DESCRIPTION + " TEXT, "
            + HAMSTER_COLUMN_HTTPIMAGE   + " TEXT, "
            + HAMSTER_COLUMN_IMAGE       + " BLOB, "
            + HAMSTER_COLUMN_PINNED      + " boolean " + ")";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase database)
    {
        database.execSQL(TABLE_CREATE_HAMSTER);
    }


    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {}


    public void dbPutHamster(Integer _id, Hamster hamster)
    {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(HAMSTER_COLUMN_ID,          _id);
        values.put(HAMSTER_COLUMN_TITLE,       hamster.title);
        values.put(HAMSTER_COLUMN_DESCRIPTION, hamster.description);
        values.put(HAMSTER_COLUMN_HTTPIMAGE,   hamster.httpimage);
        values.put(HAMSTER_COLUMN_IMAGE,       hamster.bytes);
        values.put(HAMSTER_COLUMN_PINNED,     (hamster.pinned) ? 1 : 0);

        database.insert(TABLE_NAME_HAMSTER, null, values);
    }


    public Cursor getAllData(String  strSearch)
    {
        String where = " UPPER(title) LIKE ? OR UPPER(description) LIKE ? ";
        String[] whereArgs = {"%"+strSearch.toUpperCase()+"%", "%"+strSearch.toUpperCase()+"%" };
        String orderBy = " CASE WHEN "
                + HAMSTER_COLUMN_PINNED
                + " = 1 THEN 1 ELSE 2 END, "
                +" CASE WHEN "
                + HAMSTER_COLUMN_TITLE
                + " LIKE '%Случайный хомяк%' THEN 3 ELSE 2 END ";

        SQLiteDatabase database = this.getReadableDatabase();
        return database.query(TABLE_NAME_HAMSTER, null, where, whereArgs, null, null, orderBy);
    }


    public Cursor getAllData() {
        String orderBy = " CASE WHEN "
                + HAMSTER_COLUMN_PINNED
                + " = 1 THEN 1 ELSE 2 END, "
                +" CASE WHEN "
                + HAMSTER_COLUMN_TITLE
                + " LIKE '%Случайный хомяк%' THEN 3 ELSE 2 END ";

        SQLiteDatabase database = this.getReadableDatabase();
        return database.query(TABLE_NAME_HAMSTER, null, null, null, null, null, orderBy);
    }


    public Hamster dbGetHamster(Integer _id)
    {
        Hamster hamster = new Hamster();
        SQLiteDatabase database = this.getReadableDatabase();
        String [] columns = {HAMSTER_COLUMN_TITLE,
                HAMSTER_COLUMN_DESCRIPTION,
                HAMSTER_COLUMN_HTTPIMAGE,
                HAMSTER_COLUMN_IMAGE,
                HAMSTER_COLUMN_PINNED};
        Cursor cursor = database.query(TABLE_NAME_HAMSTER, columns, " _id = ?",
                new String[] { _id.toString() },
                null,
                null,
                null,
                null);

        if (cursor != null) cursor.moveToFirst();

        hamster.title = cursor.getString(0);
        hamster.description = cursor.getString(1);
        hamster.httpimage = cursor.getString(2);
        hamster.bytes  = cursor.getBlob(3);
        hamster.pinned = (cursor.getInt(4) == 1) ? true : false;

        cursor.close();
        return hamster;
    }


    private static final String COUNT_ROWS = "SELECT count(*) FROM " + TABLE_NAME_HAMSTER;


    public int dbCountRows()
    {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor mCount= database.rawQuery(COUNT_ROWS, null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();

        return count;
    }


    public void dbUpdateHamster(Integer _id, byte[] bytes)
    {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues newValues = new ContentValues();
        newValues.put(DBHelper.HAMSTER_COLUMN_IMAGE, bytes);

        String where = DBHelper.HAMSTER_COLUMN_ID + " = ? ";
        String[] whereArgs = new String[]{_id.toString()};

        database.update(DBHelper.TABLE_NAME_HAMSTER, newValues, where, whereArgs);
    }
    

    public void dbDeleteAll()
    {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(DBHelper.TABLE_NAME_HAMSTER, null, null);
    }
}
