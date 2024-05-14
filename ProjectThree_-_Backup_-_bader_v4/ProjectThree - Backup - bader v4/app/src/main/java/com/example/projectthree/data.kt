package com.example.projectthree

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class data (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1)  {
    companion object  {
        const val DATABASE_NAME = "register.db"
        const val TABLE_NAME = "registration"
        const val COL_1 = "ID"
        const val COL_2 = "Name"
        const val COL_3 = "Phone"
        const val COL_4 = "Gmail"
        const val COL_5 = "Password"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_NAME (ID INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT,Phone TEXT,Gmail TEXT,Password TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}