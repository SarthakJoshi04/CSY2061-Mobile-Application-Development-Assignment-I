package com.example.notesapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Define the NotesDatabaseHelper class which extends SQLiteOpenHelper
class NotesDatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    // Called when the database is created for the first time
    override fun onCreate(db: SQLiteDatabase) {
        // Define the SQL command to create the notes table
        val createTable = """
            CREATE TABLE $TABLE_NOTES(
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_TITLE TEXT NOT NULL,
            $COLUMN_CONTENT TEXT NOT NULL
            )
            """.trimIndent()
        // Execute the SQL command to create the table
        db.execSQL(createTable)
    }

    // Called when the database needs to be upgraded
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop the old table if it exists
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        // Recreate the table
        onCreate(db)
    }

    // Method to add a new note to the database
    fun addNote(title: String, content: String): Long {
        val db = this.writableDatabase
        // Create a new set of values to insert
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_CONTENT, content)
        }
        // Insert the new row and return the row ID of the newly inserted row
        return db.insert(TABLE_NOTES, null, values)
    }

    // Method to edit an existing note in the database
    fun editNote(id: Long, title: String, content: String): Int {
        val db = this.writableDatabase
        // Create a new set of values to update
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_CONTENT, content)
        }
        // Update the specified row and return the number of rows affected
        return db.update(TABLE_NOTES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    // Method to delete a note from the database
    fun deleteNote(id: Long): Int {
        val db = this.writableDatabase
        // Delete the specified row and return the number of rows affected
        return db.delete(TABLE_NOTES, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    // Method to retrieve all notes from the database
    fun getAllNotes(): Cursor {
        val db = this.readableDatabase
        // Query the database and return a cursor over the result set
        return db.query(
            TABLE_NOTES,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_ID DESC"
        )
    }

    // Companion object to hold constants for the database name, version, and table/column names
    companion object{
        const val DATABASE_NAME = "notes.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NOTES = "notes"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_CONTENT = "content"
    }
}
