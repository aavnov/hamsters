package com.example.hamsters;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class HamsterViewBinder implements SimpleCursorTreeAdapter.ViewBinder
{
    Context context;

    public boolean setViewValue(View view, Cursor cursor, int columnIndex)
    {
        switch (view.getId()) {
            // GroupLayout
            case R.id.tvTitle:
                TextView tv = (TextView) view;
                int titleColumnIndex = cursor.getColumnIndex(DBHelper.HAMSTER_COLUMN_TITLE);
                if( titleColumnIndex == -1) return false;

                if(columnIndex == titleColumnIndex) {
                    String nameGroup = cursor.getString(titleColumnIndex);
                    tv.setText(nameGroup);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    tv.setTextColor(Color.BLACK);
                }
                return true;
            // ChildLayout
            case R.id.tvDescription:
                TextView tv1 = (TextView) view;
                int descriptionColumnIndex = cursor.getColumnIndex(DBHelper.HAMSTER_COLUMN_DESCRIPTION);
                if( descriptionColumnIndex == -1) return false;
                String description;

                if(columnIndex == descriptionColumnIndex) {
                    description = cursor.getString(descriptionColumnIndex);
                    tv1.setText(description);
                    tv1.setTextColor(Color.GRAY);
                }
                return true;
            case R.id.ivImg:
                int imageColumnIndex = cursor.getColumnIndex(DBHelper.HAMSTER_COLUMN_IMAGE);
                if( imageColumnIndex == -1) return false;

                if(columnIndex == imageColumnIndex) {
                    byte[] iconByteArray = cursor.getBlob(columnIndex);
                    String http = cursor.getString(3);
                    Bitmap iconBitmap = BitmapFactory.decodeByteArray(iconByteArray, 0, iconByteArray.length);

                    // Set the bitmap.
                    final ImageView iconImageView = (ImageView) view;
                    if (iconByteArray.length == 4) {
                            Picasso.with(context).load(http)
                                    //.resize(300, 300)
                                    .error(R.drawable.camera200x200)
                                    .into(iconImageView);
                    }
                    else
                        iconImageView.setImageBitmap(iconBitmap);
                }
                return true;
        }
        return false;
    }
}
