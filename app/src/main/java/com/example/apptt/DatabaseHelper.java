package com.example.apptt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "login.db";
    public static final String TABLE_NAME = "users";
    public static final String Col1 = "ID";
    public static final String Col2 = "NOMBRE";
    public static final String Col3 = "APELLIDO";
    public static final String Col4 = "CORREO";
    public static final String Col5 = "CONTRASENA";
    public static final String Col6 = "TELEFONO";
    private static final String TAG = "DatabaseHelper";

    public DatabaseHelper(Context context){
        super(context,DATABASE_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_NAME+" (ID INTEGER PRIMARY KEY AUTOINCREMENT, NOMBRE TEXT, APELLIDO TEXT, CORREO TEXT, CONTRASENA TEXT, TELEFONO TEXT)");
        Log.d(TAG,"Base de datos creada con los tabla de usuarios");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertUser(String nombre, String apellido, String correo, String contrasena, String telefono){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Col2,nombre);
        contentValues.put(Col3,apellido);
        contentValues.put(Col4,correo);
        contentValues.put(Col5,contrasena);
        contentValues.put(Col6,telefono);
        long result = db.insert(TABLE_NAME,null, contentValues);
        return result != -1;
    }

    public boolean checkMailExists(String correo){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ TABLE_NAME +" WHERE CORREO = ?",new String[]{correo});
        return cursor.getCount() > 0;
    }

    public boolean CheckUser(String correo, String contrasena){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE CORREO = ? AND CONTRASENA = ?",new String[]{correo,contrasena});
        if (cursor.getCount() > 0 ){
            return true;
        }
        return false;
    }

    public String RecoveryPassword(String correo){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT CONTRASENA FROM "+TABLE_NAME+" WHERE CORREO = ?",new String[]{correo});
        if (cursor.moveToFirst()){
            return cursor.getString(0);
        }else{
            return null;
        }
    }
}
