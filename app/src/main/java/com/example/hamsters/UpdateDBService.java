package com.example.hamsters;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;


public class UpdateDBService extends Service
{
    final Handler handler = new Handler();

    SQLiteDatabase database;
    final DBHelper dbHelper = new DBHelper(this);

    int arRow = 0;
    int numberRow = 0;
    int countBytes = 0;

    Bitmap.Config conf = Bitmap.Config.ARGB_8888;
    Bitmap bm = Bitmap.createBitmap(200, 200, conf);

    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void onCreate()
    {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        database       = dbHelper.getWritableDatabase();
        arRow = 0;
        numberRow = dbHelper.dbCountRows();

        handler.post(new Runnable() {
            @Override
            public void run() {
                updateDB();
                handler.postDelayed(this, 2000);
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy()
    {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);

        dbHelper.close();
        database.close();
    }

    private void updateDB() {
        if (arRow == 0 || numberRow == 0) numberRow = dbHelper.dbCountRows();
        if (arRow >= numberRow - 1) arRow = -1;
        arRow++;
        if (numberRow > 1) countBytes = dbHelper.dbGetHamster(arRow).bytes.length;


        if (countBytes == 4) {
            final Hamster hamster;
            hamster = dbHelper.dbGetHamster(arRow);

            Picasso.with(getApplicationContext()).load(hamster.httpimage)
                    //.resize(300, 300)
                    //.memoryPolicy(MemoryPolicy.NO_CACHE )
                    //.networkPolicy(NetworkPolicy.NO_CACHE)
                    //.skipMemoryCache()
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            bm = bitmap;

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            if (bm.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                                hamster.bytes = stream.toByteArray();
                                dbHelper.dbUpdateHamster(arRow, hamster.bytes);
                            }
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable)
                        {
                            bm = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.camera200x200);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            if (bm.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                                hamster.bytes = stream.toByteArray();
                                dbHelper.dbUpdateHamster(arRow, hamster.bytes);
                            }
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
        }
    }

}
