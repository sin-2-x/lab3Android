package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DbHandler extends SQLiteOpenHelper {

    //Название базы данных
    public static final String DATABASE_NAME = "softwareDB.db";

    // полный путь к базе данных
    private static String DB_PATH;

    //Строковые занчения для составления запросов таблиц
    public static final class TABLE_SOFTWARE{
        public static final String Name = "program";
        public static final String KEY_ID = "id";
        public static final String KEY_NAME = "name";
        public static final String KEY_DESCRIPTION = "description";
        public static final String KEY_ID_MANUFACTURER = "id_manufacturer";
        public static final String KEY_ID_LICENSE = "id_license";

    }
    public static final class TABLE_MANUFACTURER{
        public static final String Name = "manufacturer";
        public static final String KEY_ID = "id";
        public static final String KEY_NAME = "name";

    }
    public static final String TABLE_LICENSE = "license";

    //Соединение с базой данных
    private SQLiteDatabase db;
    private final Context myContext;


    public DbHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.myContext=context;
        assert context != null;
        DB_PATH = String.valueOf(context.getDatabasePath(DATABASE_NAME));
        create_db();
    }

    //Проверка существования бд
    void create_db(){
        File file = new File(DB_PATH);
        if (!file.exists()) {
            //получаем локальную бд как поток
            try(InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
                // Открываем пустую бд
                OutputStream myOutput = Files.newOutputStream(Paths.get(DB_PATH))) {

                // побайтово копируем данные
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.flush();
            }
            catch(IOException ex){
                Log.d("DatabaseHelper", ex.getMessage());
            }
        }
    }

    //Открытие соединения базы данных
    private void open() {
        db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }
    //Закрытие соединения базы данных
    public void close() {
        db.close();
    }

    //Запрос добавленя новой записи в бд
    public void insert(String table, ContentValues cv) {
        open();
        db.insert(table, null, cv);
        db.close();
    }

    //Запрос обновленя данных в бд
    public void update(String table, ContentValues cv, String w) {
        open();
        db.update(table, cv, w,null);
        db.close();
    }

    //Запрос удаления из базы данных
    public void delete(String table, String where) {
        open();
        db.delete(table, where,null);
        db.close();
    }

    //Запрос данных из базы, ТРЕБУЕТ ЗАКРЫТИЯ СОЕДИНЕНИЯ
    public Cursor exe(String q){
        open();
        Cursor mCursor= db.rawQuery(q, null);
        return mCursor;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}