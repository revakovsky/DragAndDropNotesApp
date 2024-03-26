package com.revakovsky.dragdropnotesapp.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.revakovsky.dragdropnotesapp.models.NoteItem

class DatabaseHandler(
    context: Context,
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "NotesDatabase"
        private const val TABLE_NAME = "NotesTable"

        private const val KEY_ID = "_id"
        private const val KEY_TITLE = "title"
        private const val KEY_NOTE = "note"
        private const val KEY_TIMESTAMP = "timeStamp"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val dbRequest = ("CREATE TABLE "
                + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_NOTE + " TEXT,"
                + KEY_TIMESTAMP + " TEXT)"
                )
        db?.execSQL(dbRequest)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addNote(noteItem: NoteItem): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(KEY_TITLE, noteItem.title)
            put(KEY_TIMESTAMP, noteItem.timeStamp)
            put(KEY_NOTE, noteItem.note)
            put(KEY_ID, noteItem.id)
        }
        val addingStatus = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return addingStatus
    }

    fun editNote(note: String, title: String, id: Int): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(KEY_TITLE, title)
            put(KEY_NOTE, note)
        }
        val editingStatus = db.update(TABLE_NAME, contentValues, "_id=$id", null)
        db.close()
        return editingStatus
    }

    @SuppressLint("Range")
    fun getNotes(): ArrayList<NoteItem> {
        val db = this.readableDatabase
        val selectQuery = "SELECT  * FROM $TABLE_NAME"
        val noteList: ArrayList<NoteItem> = ArrayList()

        try {
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val note = NoteItem(
                        id = cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        title = cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        note = cursor.getString(cursor.getColumnIndex(KEY_NOTE)),
                        timeStamp = cursor.getString(cursor.getColumnIndex(KEY_TIMESTAMP))
                    )
                    noteList.add(note)
                } while (cursor.moveToNext())
            }
            cursor.close()
            return noteList

        } catch (e: SQLiteException) {
            e.printStackTrace()
            return noteList
        } finally {
            db.close()
        }
    }

    fun deleteNote(id: Int): Int {
        val db = this.writableDatabase
        val deletedStatus = db.delete(TABLE_NAME, "$KEY_ID=$id", null)
        db.close()
        return deletedStatus
    }

}