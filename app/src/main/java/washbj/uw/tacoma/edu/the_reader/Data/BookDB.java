package washbj.uw.tacoma.edu.the_reader.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import washbj.uw.tacoma.edu.the_reader.R;

/**
 * Created by Justin of America on 2/28/2017.
 */

public class BookDB {

    /**
     * Version of the database
     */
    public static final int DB_VERSION = 4;
    /**
     * Name of the database
     */
    public static final String DB_NAME = "Books.db";
    /**
     * Name of the table
     */
    private static final String BOOK_TABLE = "Books";
    /**
     * The used databasehelper
     */
    private BookDBHelper mBookDBHelper;
    /**
     * The writable database
     */
    private SQLiteDatabase mSQLiteDatabase;

    /**
     * Constructor that creates the writable database and the helper
     * @param context the current activity
     */
    public BookDB(Context context) {
        mBookDBHelper = new BookDBHelper(
                context, DB_NAME, null, DB_VERSION);
        mSQLiteDatabase = mBookDBHelper.getWritableDatabase();
    }

    /**
     * Inserts the course into the local sqlite table. Returns true if successful, false otherwise.
     * @param file_id name of file location
     * @param page page number
     * @param book_name name of the book (file name)
     * @return whether the result was succesful
     */
    public boolean insertBook(String file_id, int page, String book_name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("file_id", file_id);
        contentValues.put("page", page);
        contentValues.put("book_name", book_name);


        long rowId = mSQLiteDatabase.insert("Books", null, contentValues);
        return rowId != -1;
    }

    /**
     * Checks to see if a book name is already in the table
     * @param file_id the location of the file
     * @return true if the file is already stored
     */
    public boolean CheckIsBookAlreadyInDBorNot(String file_id) {
       // try {
            Cursor cursor = null;
            String sql = "SELECT file_id FROM " + "Books" + " WHERE file_id=" + " +\"" + file_id + "\"";
            cursor = mSQLiteDatabase.rawQuery(sql, null);
            cursor.moveToFirst();
            int count = cursor.getCount();
            //Log.i("CursorCount", "" + count);
            cursor.close();
        if (count > 0) {
            return true;
        }
        return false;

    }

    /**
     * Checks the page number of the given file location
     * @param file_id the file location
     * @return the page number
     */
    public int CheckPageNumber(String file_id) {
        // try {
        Cursor cursor = null;
        String sql = "SELECT page FROM " + "Books" + " WHERE file_id=" + " +\"" + file_id + "\"";
        cursor = mSQLiteDatabase.rawQuery(sql, null);
        cursor.moveToFirst();
        //cursor.move(1);
        int page = cursor.getInt(cursor.getColumnIndex("page"));
        Log.i("CheckPageNumber", "" + page);
        cursor.close();
        return page;

    }

    /**
     * Updates the values for the book, mainly the page
     * @param file_id the file location
     * @param page the page number
     * @param book_name the book name (file name)
     * @return
     */
    public boolean updateBook(String file_id, int page, String book_name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("file_id", file_id);
        contentValues.put("page", page);
        contentValues.put("book_name", book_name);

        long rowId = mSQLiteDatabase.update("Books", contentValues,"file_id=" + "\"" + file_id + "\"", null);
        return rowId != -1;
    }

    /**
     * closes the database
     */
    public void closeDB() {
        mSQLiteDatabase.close();
    }

    /**
     * Delete all the data from the BOOK_TABLE
     */
    public void deleteBooks() {
        mSQLiteDatabase.delete(BOOK_TABLE, null, null);
    }

    /**
     * Class to get the database started
     */
    class BookDBHelper extends SQLiteOpenHelper {

        /**
         * Code to create the table
         */
        private final String CREATE_BOOK_SQL;

        /**
         * Code to delete the table
         */
        private final String DROP_BOOK_SQL;

        /**
         * Creates the helper and initializes the SQL code with the strings from values
         * @param context
         * @param name
         * @param factory
         * @param version
         */
        public BookDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            CREATE_BOOK_SQL = context.getString(R.string.CREATE_BOOK_SQL);
            DROP_BOOK_SQL = context.getString(R.string.DROP_BOOK_SQL);

        }

        /**
         * Creates the SQL table
         * @param sqLiteDatabase
         */
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_BOOK_SQL);
        }

        /**
         * Creates the SQL table and deletes the current
         * @param sqLiteDatabase
         * @param i
         * @param i1
         */
        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(DROP_BOOK_SQL);
            onCreate(sqLiteDatabase);
        }
    }


}
