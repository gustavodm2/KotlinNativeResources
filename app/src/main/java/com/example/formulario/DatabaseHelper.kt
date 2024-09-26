package com.example.formulario

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "Formulario.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "user_data"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_COMMENT = "comment"
        const val COLUMN_PHOTO_PATH = "photo_path"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_NAME TEXT," +
                "$COLUMN_EMAIL TEXT," +
                "$COLUMN_COMMENT TEXT," +
                "$COLUMN_PHOTO_PATH TEXT)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertData(name: String, email: String, comment: String, photoPath: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_EMAIL, email)
            put(COLUMN_COMMENT, comment)
            put(COLUMN_PHOTO_PATH, photoPath)
        }
        return db.insert(TABLE_NAME, null, contentValues)
    }

    fun getAllData(): List<Map<String, String>> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        val dataList = mutableListOf<Map<String, String>>()

        if (cursor.moveToFirst()) {
            do {
                val data = mapOf(
                    COLUMN_NAME to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    COLUMN_EMAIL to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    COLUMN_COMMENT to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT)),
                    COLUMN_PHOTO_PATH to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_PATH))
                )
                dataList.add(data)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return dataList
    }

}

