package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SoftwareRecyclerViewAdapter.ItemClickListener{
    //Класс для работы с базой данных
    DbHandler dbHandler;
    //Массив объектов представления
    ArrayList<Soft> softlist = new ArrayList<Soft>();
    //Диалоговое окно для удаления
    BottomSheetDialog deleteDialog;
    //Выбранное программное обеспечения
    Soft selectedSoft;
    //Разметка списка програмного обеспечения
    RecyclerView softlv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deleteDialog = new BottomSheetDialog (this);
        softlv = findViewById(R.id.sowtwareResiclerView);
        dbHandler = new DbHandler(this);
        View arrowBack = findViewById(R.id.arrowBack);
        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowPopup(view, false);
            }
        });
        fillList();
    }

    @Override
    public void onItemClick(View view, int position) {

        selectedSoft = softlist.get(position);
        goToCurrentSoftware(view);
    }
    @Override
    public boolean onLongItemClick(View view, int position) {
        selectedSoft = softlist.get(position);
        ShowPopup(view, true);
        return true;
    }

    private void fillList(){
        //Инициализация массивов
        getData();
        //Инициализация коллекции имен в списке программного обеспечения
        //String[]  listviewItemTitles = new String[softlist.size()];
        //Resources res = getResources();
        //f

        //Заполенение коллекции элементов отображения с помощью адаптера
        SoftwareRecyclerViewAdapter adapter = new SoftwareRecyclerViewAdapter(MainActivity.this, softlist);
        softlv.setLayoutManager(new LinearLayoutManager(this));
        adapter.setClickListener(this);
        softlv.setAdapter(adapter);
    }
    private void getData() {
        //Запрос списка программного обеспечения в базе данных
        Cursor mCursor = dbHandler.exe("SELECT program.id, program.name, manufacturer.name, license.type, program.description  " +
                "from program\n" +
                "JOIN manufacturer on program.id_manufacturer = manufacturer.id\n"+
                "JOIN license on program.id_license = license.id");
        //Очистка старых данных
        softlist.clear();
        //Заполнение коллекциии программного обеспечения
        for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            softlist.add(new Soft(mCursor.getLong(0),
                    mCursor.getString(1),
                    mCursor.getString(2),
                    mCursor.getString(3),
                    mCursor.getString(4)));
        }
        dbHandler.close();
    }
    //Класс для отображения данных в списке
    public static class Soft {
        public Soft(long id, String n, String m, String license, String description){
            this.id = id;
            this.name = n;
            this.manufacturer = m;
            this.license = license;
            this.description = description;
        }
        public long id;
        public String name;
        public String license;
        public String manufacturer;
        public String description;

    }
    //Обработчик события закрытия диалогового окна
    ActivityResultLauncher<Intent> mResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> fillList());

    // Метод для перехода на страницу редактирования программы
    public void goToCurrentSoftware(View view) {
        Intent myIntent = new Intent(MainActivity.this, AddActivity.class);
        //Заполнение информации о выбранном элементе
        if(selectedSoft !=null){
            myIntent.putExtra("id", selectedSoft.id);
        }
        selectedSoft = null;
        //Запуск диалогового окна
        mResult.launch(myIntent);
    }

    //Метод для показа диалогового окна удаления
    @SuppressLint("SetTextI18n")
    public void ShowPopup(View v, boolean single) {
        TextView dialogText;
        Button deleteBtn;
        deleteDialog.setContentView(R.layout.popup);
        dialogText =(TextView) deleteDialog.findViewById(R.id.SoftwareTextView);
        //Заполнение текста запроса на удаление
        if (dialogText != null && single) {
            dialogText.setText("Объект \"" + selectedSoft.name +"\" будет удален" );
        }
        else{

            dialogText.setText("Очистить базу данных?" );
        }
        //Инициализация кнопки удаления
        deleteBtn = (Button) deleteDialog.findViewById(R.id.DeleteButton);

        assert deleteBtn != null;
        //Регистрация события нажатия на кнопку
        deleteBtn.setOnClickListener(v1 -> {
            if(single)
                deleteSoftware((int) selectedSoft.id);
            else
                for (Soft soft:softlist                     ) {
                    deleteSoftware((int) soft.id);
                }
            selectedSoft = null;
            //Прекращение диалога
            deleteDialog.dismiss();
            //Обновление списка программного обеспечения
            fillList();
        });
        Objects.requireNonNull(deleteDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteDialog.show();
    }

    private  void deleteSoftware(int id){
        //Удаление объекта ПО
        dbHandler.delete(DbHandler.TABLE_SOFTWARE.Name, DbHandler.TABLE_SOFTWARE.KEY_ID+" = "+ id);
        //Очистка бд от остаточных данных
        dbHandler.delete(DbHandler.TABLE_MANUFACTURER.Name,
                DbHandler.TABLE_MANUFACTURER.KEY_ID+
                        " in ( SELECT id from manufacturer Where id > 1 " +
                        "EXCEPT " +
                        "SELECT id_manufacturer from program)");

    }
}