package com.example.hamsters;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.os.Handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    ImageButton btnFind;

    Intent updateDBServiceIntent = new Intent();

    ExpandableListView expandableListView;

//    SQLiteDatabase database;
    final DBHelper sql = new DBHelper(this);

    private Cursor          mCursor;
    private SimpleCursorTreeAdapter mAdapter;
    Boolean isExpanded = false;

    final Handler handler = new Handler();

    private EditText etSearch;

    Long idDbHamster = new Long(0);

    Intent shareIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Не забудь отправить фото хомячка другу", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //направляем друзьям понравившееся фото
                Hamster hamster = sql.dbGetHamster((int) (long) idDbHamster);

                ImageView ivImage = (ImageView) findViewById(R.id.ivImg);
                if(isExpanded) {

                    Uri bmpUri = getLocalBitmapUri(ivImage);
                    if (bmpUri != null) {

                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, hamster.title);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                        shareIntent.setType("image/*");

                        startActivity(Intent.createChooser(shareIntent, "Поделится фото через"));
                    } else {
                       //
                    }
                }
            }
        });


        btnFind = (ImageButton) findViewById(R.id.button);
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String strSearch = etSearch.getText().toString();
                    mCursor = sql.getAllData(strSearch);
                    mAdapter.changeCursor(mCursor);
            }
        });


        etSearch = (EditText) findViewById(R.id.etSearch);
        etSearch.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String strSearch = etSearch.getText().toString();
                    mCursor = sql.getAllData(strSearch);
                    mAdapter.changeCursor(mCursor);
                    return true;
                }
                return false;
            }
        });


        expandableListView = (ExpandableListView) findViewById(R.id.exp_list);
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        final int groupPosition, long id) {
                idDbHamster = id;

                return false;
            }
        });
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                isExpanded = true;
            }
        });
        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                isExpanded = false;
            }
        });

        sql.dbDeleteAll();

        invokeWS();

        updateDBServiceIntent = new Intent(getApplicationContext(), UpdateDBService.class);
        // Стартуем UdateDBService для обновления рисунков
        if(!IsMyServiceRunning(UpdateDBService.class))
            getApplicationContext().startService(updateDBServiceIntent);
    }


    public void onDestroy()
    {
        super.onDestroy();
        updateDBServiceIntent = new Intent(getApplicationContext(), UpdateDBService.class);
        // Останавливаем UdateDBService для обновления рисунков
        if(IsMyServiceRunning(UpdateDBService.class))
            getApplicationContext().stopService(updateDBServiceIntent);
        sql.dbDeleteAll();
    }


    public void invokeWS()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://unrealmojo.com/porn/test3/" ,new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonHamster = jsonArray.getJSONObject(i);
                        final Hamster hamster = new Hamster();
                        try {
                            hamster.title = jsonHamster.getString("title");
                        } catch (JSONException e) {
                            break;
                        }
                        try {
                            hamster.description = jsonHamster.getString("description");
                        } catch (JSONException e) {
                            break;
                        }
                        try{
                            hamster.httpimage   = jsonHamster.getString("image");
                        } catch (JSONException e) {
                            break;
                        }
                        try {
                            hamster.pinned = jsonHamster.getBoolean("pinned");
                        } catch (JSONException e) {
                            hamster.pinned = false;
                        }

                        byte[] bytes  = new byte[]{ (byte) 0x65, (byte)0x10, (byte)0xf3, (byte)0x29};
                        hamster.bytes = bytes;

                        //загружаем всю информацию о хомячках в БД
                        sql.dbPutHamster(i, hamster);
                    }


                    mCursor = sql.getAllData();

                    String[] groupFrom = { DBHelper.HAMSTER_COLUMN_TITLE };
                    int[] groupTo = { R.id.tvTitle };

                    String[] childFrom = { DBHelper.HAMSTER_COLUMN_DESCRIPTION, DBHelper.HAMSTER_COLUMN_IMAGE };
                    int[] childTo = { R.id.tvDescription, R.id.ivImg };

                    mAdapter = new ExpListViewAdapter
                            (getApplicationContext(), mCursor,
                            R.layout.item_group, groupFrom, groupTo,
                            R.layout.item_child, childFrom, childTo);
                    mAdapter.setViewBinder(new HamsterViewBinder());
                    expandableListView.setAdapter(mAdapter);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Произошла ошибка. Недокументированный ответ сервера JSON.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // When Http response code is '404'
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Запрашиваемый ресурс не найден", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Проблемы серверной стороны", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(getApplicationContext(), "Неожиданная проблема (Устройство не соединено с интернетом или удаленный сервер вышел из строя)", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public boolean IsMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private Uri getLocalBitmapUri(ImageView imageView)
    {
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }

        Uri bmpUri = null;
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar itemhamster clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Об альбоме")
                .setMessage(R.string.alert_title_texthelp_create)
                .setCancelable(false)
                .setNegativeButton("Закрыть",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }
}
