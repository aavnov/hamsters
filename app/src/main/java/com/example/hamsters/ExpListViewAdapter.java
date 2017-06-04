package com.example.hamsters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.SimpleCursorTreeAdapter;


public class ExpListViewAdapter extends SimpleCursorTreeAdapter {

    DBHelper dbHelper;
    SQLiteDatabase database;
    Context context ;

    public ExpListViewAdapter(Context context, Cursor cursor,    int groupLayout,
                              String[] groupFrom, int[] groupTo, int childLayout,
                              String[] childFrom, int[] childTo)
    {
        super(context, cursor, groupLayout, groupFrom, groupTo,
                               childLayout, childFrom, childTo);
        this.context = context;
    }


    protected Cursor getChildrenCursor(Cursor groupCursor)
    {

        int idColumn = groupCursor.getColumnIndex(DBHelper.HAMSTER_COLUMN_ID);
        return this.getItem(groupCursor.getInt(idColumn));
    }


    public Cursor getItem(long itemID)
    {
        dbHelper = new DBHelper(context);
        database       = dbHelper.getWritableDatabase();

        return database.query(DBHelper.TABLE_NAME_HAMSTER, null, DBHelper.HAMSTER_COLUMN_ID + " = "
                + itemID, null, null, null, null);
    }

}
