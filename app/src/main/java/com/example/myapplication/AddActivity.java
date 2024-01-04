package com.example.myapplication;

import static android.app.PendingIntent.getActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AddActivity extends AppCompatActivity {
    //Класс для работы с базой данных
    DbHandler databaseHelper;

    //Коллекция типов лицензии
    Map<String,Long> licenseValues = new HashMap<String,Long>();
    //Коллекция производителей программного обеспечения
    Map<String,Long> manufacturerValues = new HashMap<String,Long>();

    //Поле ввода названия пограммного обеспечения
    TextView name;
    //Поле выбора поизводителя программного обеспечения
    EditSpinner manufactorer;
    //Поле выбора лицензии
    Spinner spinner;
    //Поле описания программного обесечения
    TextView description;

    //Идентификатор программного обеспечения из базы данных
    Long id = null;
    Toast toast;
    @SuppressLint("StaticFieldLeak")
    private static Context c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Получение значений из родительского окна
        Bundle arguments = getIntent().getExtras();
        if(arguments != null){
            //Заполенение идентификатора программного обеспечения
            id = arguments.getLong("id");
        }
        //Инициализация базы данных
        databaseHelper = new DbHandler(this);
        //Применение разметки
        setContentView(R.layout.activity_add);
        //Заполнение списков производителей и лицензий
        getAllData();
        //Инициализация полей ввода
        initViews();
        c = this;

        View arrowBack = findViewById(R.id.arrowBack);
        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goHome();
            }
        });

    }

    private void initViews() {
        //Привязка полей ввода разметки к переменным класса
        name = findViewById(R.id.NameTextView);
        spinner = (Spinner) findViewById(R.id.LicencySpinner);
        manufactorer = (EditSpinner) findViewById(R.id.ManufacturerSpinner);
        description = findViewById(R.id.DescriptionTextView);

        // Заполенение списка разметки производителей
        manufactorer.setAdapter(new ArrayAdapter<>(this,
                R.layout.spinner_list,
                manufacturerValues.keySet().toArray()));

        // Заполенение списка лицензий
        spinner.setAdapter(new ArrayAdapter<>(this,
                R.layout.spinner_list,
                licenseValues.keySet().toArray()));

        //Если вбырен элемент
        if(id!=null) {
            //Получаем информацию из базы данных
            Cursor mCursor = databaseHelper.exe("SELECT program.id, program.name, manufacturer.name, license.type, program.description from program \n" +
                    "JOIN manufacturer on program.id_manufacturer = manufacturer.id \n" +
                    "JOIN license on program.id_license = license.id \n" +
                    "WHERE program.id = " + id);
            mCursor.moveToFirst();
            //Заполнение полей разметки получанными значениями
            name.setText(mCursor.getString(1));
            manufactorer.setText(mCursor.getString(2));
            description.setText(mCursor.getString(4));
            licenseValues.get(mCursor.getString(3));
            spinner.setSelection(Objects.requireNonNull(licenseValues.get(mCursor.getString(3))).intValue()-1);
            databaseHelper.close();
            }
    }

    //Инициализирует коллекции лицензий и производителей программного обеспечения
    private void getAllData() {
        //Заполнение списка лицензий
        getData(DbHandler.TABLE_LICENSE, licenseValues);
        //Заполенение списка производителей программного обеспечения
        getData(DbHandler.TABLE_MANUFACTURER.Name, manufacturerValues);

    }

    private void getData(String table, Map<String,Long> map) {
        Cursor mCursor = databaseHelper.exe("select * from "+ table);
        map.clear();
        for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            map.put(mCursor.getString(1), mCursor.getLong(0));

        }
        databaseHelper.close();
    }

    //Сохранение данных в базу данных
    public void save(View view) {
        //Если не заполено поле названия прогрммного обеспечения
        //Сохранение не происходит
            if(name.getText().length() == 0){
                //вывод сообщения
                //Activity activity = getActivity();
                CustomDialogFragment dialog = new CustomDialogFragment();
                dialog.show(getSupportFragmentManager(), "custom");
                return;
            }
        //Создание нового сета данных для сохранения
        ContentValues cv = new ContentValues();

        //Если поле производителя не заполнено
        String manufactorerName = manufactorer.getEditableText().toString();
        if (manufactorerName.length()==0) {
            //Присвоить значение по умолчанию
            manufactorerName = "Не указан";
        }
        //Если произодтель явлется новым в базе данных
        if(!manufacturerValues.containsValue(manufactorerName)){
            cv.put(DbHandler.TABLE_MANUFACTURER.KEY_NAME, manufactorerName);
            //Создать запись о производителе
            databaseHelper.insert(DbHandler.TABLE_MANUFACTURER.Name,cv);
            getData(DbHandler.TABLE_MANUFACTURER.Name, manufacturerValues);
            cv = new ContentValues();
        }
        //Инициализация значений программного обеспечения
        cv.put(DbHandler.TABLE_SOFTWARE.KEY_NAME, name.getText().toString());
        cv.put(DbHandler.TABLE_SOFTWARE.KEY_DESCRIPTION, description.getText().toString());
        Set<String> t = licenseValues.keySet();
        String[] tt = (Arrays.copyOf(t.toArray(), t.size(), String[].class));
        String y = tt[spinner.getSelectedItemPosition()];
        cv.put(DbHandler.TABLE_SOFTWARE.KEY_ID_LICENSE, licenseValues.get(y));
        cv.put(DbHandler.TABLE_SOFTWARE.KEY_ID_MANUFACTURER, manufacturerValues.get(manufactorerName));

        //Если был выбран объект для редактирования
        if (id != null) {
            //обновляем данные в бд
            databaseHelper.update(DbHandler.TABLE_SOFTWARE.Name, cv, DbHandler.TABLE_SOFTWARE.KEY_ID + "=" + id);
            databaseHelper.delete(DbHandler.TABLE_MANUFACTURER.Name, DbHandler.TABLE_MANUFACTURER.KEY_ID+ " in ( SELECT id from manufacturer " +
                    "EXCEPT " +
                    "SELECT id_manufacturer from program)");
        } else {
            //создаем запись в бд
            databaseHelper.insert(DbHandler.TABLE_SOFTWARE.Name,cv);
        }
        //Взврат на начальный экран
        goHome();
    }
    //Возвращает на экран родителя
    private void goHome(){
        // переход к главной activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    //Скрывает клавиатуру при касании неуправляемых элементов
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() ==  MotionEvent.ACTION_DOWN)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
