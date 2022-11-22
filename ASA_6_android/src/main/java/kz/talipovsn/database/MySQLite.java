package kz.talipovsn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class MySQLite extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 4; // НОМЕР ВЕРСИИ БАЗЫ ДАННЫХ И ТАБЛИЦ !

    static final String DATABASE_NAME = "TV"; // Имя базы данных

    static final String TABLE_NAME = "televizory"; // Имя таблицы
    static final String ID = "id"; // Поле с ID
    static final String NAME = "name"; // Поле с наименованием организации
    static final String NAME_LC = "name_model"; // // Поле с наименованием организации в нижнем регистре
    static final String PRICE = "price";// Поле с телефонным номером
    static final String DIAGONAL= "diagonaltv";
    static final String COLOR= "color";


    static final String ASSETS_FILE_NAME = "televizory.txt"; // Имя файла из ресурсов с данными для БД
    static final String DATA_SEPARATOR = "|"; // Разделитель данных в файле ресурсов с телефонами

    private Context context; // Контекст приложения

    public MySQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Метод создания базы данных и таблиц в ней
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY,"
                + NAME + " TEXT,"
                + NAME_LC + " TEXT,"
                + DIAGONAL + " TEXT,"
                + COLOR + " TEXT,"
                + PRICE + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        System.out.println(CREATE_CONTACTS_TABLE);
        loadDataFromAsset(context, ASSETS_FILE_NAME,  db);
    }

    // Метод при обновлении структуры базы данных и/или таблиц в ней
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        System.out.println("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Добавление нового контакта в БД
    public void addData(SQLiteDatabase db, String name, String price,String diagonaltv,String color) {
        ContentValues values = new ContentValues();
        values.put(NAME, name);
        values.put(NAME_LC, name.toLowerCase());
        values.put(PRICE, price);
        values.put(DIAGONAL,diagonaltv);
        values.put(COLOR,color);
        db.insert(TABLE_NAME, null, values);
    }

    // Добавление записей в базу данных из файла ресурсов
    public void loadDataFromAsset(Context context, String fileName, SQLiteDatabase db) {
        BufferedReader in = null;

        try {
            // Открываем поток для работы с файлом с исходными данными
            InputStream is = context.getAssets().open(fileName);
            // Открываем буфер обмена для потока работы с файлом с исходными данными
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            while ((str = in.readLine()) != null) { // Читаем строку из файла
                String strTrim = str.trim(); // Убираем у строки пробелы с концов
                if (!strTrim.equals("")) { // Если строка не пустая, то
                    StringTokenizer st = new StringTokenizer(strTrim, DATA_SEPARATOR); // Нарезаем ее на части
                    String name = st.nextToken().trim(); // Извлекаем из строки название организации без пробелов на концах
                    String phoneNumber = st.nextToken().trim();// Извлекаем из строки номер организации без пробелов на концах
                    String diagonaltv = st.nextToken().trim();
                    String color = st.nextToken().trim();
                    addData( db,name, phoneNumber, diagonaltv,color); // Добавляем название и телефон в базу данных
                }
            }

        // Обработчики ошибок
        } catch (IOException ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

    }

    // Получение значений данных из БД в виде строки с фильтром
    public String getData(String filter, Spinner spinner) {

        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + NAME; // Переменная для SQL-запроса
        long idSpin = spinner.getSelectedItemId();

        if (idSpin==0) {


            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE ("
                    + NAME_LC + " LIKE '%" + filter.toLowerCase() + "%'"
                    + " OR " + PRICE + " LIKE '%" + filter + "%'"
                    + " OR " + DIAGONAL + " LIKE '%" + filter + "%'"
                    + " OR " + COLOR + " LIKE '%" + filter + "%'"
                    + ")";
        }
        else if (idSpin == 1) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE ("
                + NAME + " LIKE '%" + filter.toLowerCase() + "%'"+")";
        }
        else if (idSpin == 2) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE ("
                    + PRICE + " LIKE '%" + filter + "%'"+")";
        }
        else if (idSpin == 3) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE ("
                    + DIAGONAL + " LIKE '%" + filter + "%'" + ")";
        }

//        else if (idSpin == 4) {
//                selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE ("
//                        + COLOR + " LIKE '%" + filter + "%'"+")";
//        }


        SQLiteDatabase db = this.getReadableDatabase(); // Доступ к БД
        Cursor cursor = db.rawQuery(selectQuery, null); // Выполнение SQL-запроса

        StringBuilder data = new StringBuilder(); // Переменная для формирования данных из запроса

        int num = 0;
        if (cursor.moveToFirst()) { // Если есть хоть одна запись, то
            do { // Цикл по всем записям результата запроса
                int n = cursor.getColumnIndex(NAME);
                int t = cursor.getColumnIndex(PRICE);
                int d = cursor.getColumnIndex(DIAGONAL);
                int c = cursor.getColumnIndex(COLOR);
                String name = cursor.getString(n); // Чтение названия организации
                String Price = cursor.getString(t);// Чтение телефонного номера
                String diagonaltv = cursor.getString(d);
                String color = cursor.getString(c);
                data.append(String.valueOf(++num) + ") " + name +": " + color+":  " + diagonaltv + "\n" + Price + "\n");
            } while (cursor.moveToNext()); // Цикл пока есть следующая запись
        }
        return data.toString(); // Возвращение результата
    }

}