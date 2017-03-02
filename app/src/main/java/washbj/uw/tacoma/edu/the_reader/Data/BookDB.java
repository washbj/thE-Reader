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

    public static final int DB_VERSION = 4;
    public static final String DB_NAME = "Books.db";
    private static final String BOOK_TABLE = "Books";
    private BookDBHelper mBookDBHelper;
    private SQLiteDatabase mSQLiteDatabase;


    public BookDB(Context context) {
        mBookDBHelper = new BookDBHelper(
                context, DB_NAME, null, DB_VERSION);
        mSQLiteDatabase = mBookDBHelper.getWritableDatabase();
    }

    /**
     * Inserts the course into the local sqlite table. Returns true if successful, false otherwise.
     * @param name
     * @param page
     * @return true or false
     */
    public boolean insertBook(String name, int page, String book_name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("file_id", name);
        contentValues.put("page", page);
        contentValues.put("book_name", book_name);


        long rowId = mSQLiteDatabase.insert("Books", null, contentValues);
        return rowId != -1;
    }

    /**
     * Checks to se if a book name is already in the table
     * @param file_id the name of the file
     * @return true if the file is already stored
     */
    public boolean CheckIsBookAlreadyInDBorNot(String file_id) {
       // try {
            Cursor cursor = null;
            String sql = "SELECT file_id FROM " + "Books" + " WHERE file_id=" + file_id;
            cursor = mSQLiteDatabase.rawQuery(sql, null);
            cursor.moveToFirst();
            Log.i("CursorCount",cursor.getString(0));
            Log.i("CursorCount", "" +   cursor.getInt(1));
            int count = cursor.getCount();
            Log.i("CursorCount", "" + count);
            cursor.close();
            if (count > 0) {
                //Found
                return true;
            }
       // }catch (Exception e) {
           // Log.e("SQLSearchError", e.toString());
            //return false;
       // }
        //Not Found
        return false;
    }

    public boolean updateBook(String file_id, int page, String book_name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("file_id", file_id);
        contentValues.put("page", page);
        contentValues.put("book_name", book_name);

        long rowId = mSQLiteDatabase.update("Books", contentValues,"file_id=" + file_id, null);
        return rowId != -1;
    }

    public void closeDB() {
        mSQLiteDatabase.close();
    }

    /**
     * Delete all the data from the COURSE_TABLE
     */
    public void deleteBooks() {
        mSQLiteDatabase.delete(BOOK_TABLE, null, null);
    }


    class BookDBHelper extends SQLiteOpenHelper {

        private final String CREATE_BOOK_SQL;

        private final String DROP_BOOK_SQL;

        public BookDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            CREATE_BOOK_SQL = context.getString(R.string.CREATE_BOOK_SQL);
            DROP_BOOK_SQL = context.getString(R.string.DROP_BOOK_SQL);

        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_BOOK_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(DROP_BOOK_SQL);
            onCreate(sqLiteDatabase);
        }
    }


}
