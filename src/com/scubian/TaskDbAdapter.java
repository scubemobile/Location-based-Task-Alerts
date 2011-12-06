package com.scubian;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TaskDbAdapter {
	public static final String KEY_ROWID = "_id";
	public static final String KEY_TASK = "task";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_TAGS = "tag";
	public static final String KEY_NOTES = "note";
	public static final String KEY_PRIORITY = "priority";
	public static final String KEY_REPEAT = "repeat";
	public static final String KEY_STATUS = "status";

	private static final String TAG = "TaskDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "locationApp";
	private static final String DATABASE_TABLE = "quickadd";
	private static final int DATABASE_VERSION = 2;

	// Database creation SQL statement
	/*
	 * private static final String DATABASE_CREATE =
	 * "create table notes (_id integer primary key autoincrement, " +
	 * "task text not null, tag text not null);"; //din't know if we have to
	 * include latitude,longitude,notes text not null here
	 */
	private static final String DATABASE_CREATE = "create table "
			+ DATABASE_TABLE + " (" + KEY_ROWID
			+ " integer primary key autoincrement, " + KEY_TASK
			+ " text not null, " + KEY_NOTES + " text not null, " + KEY_TAGS
			+ " text not null, " + KEY_LATITUDE + " text not null, "
			+ KEY_LONGITUDE + " text not null, " + KEY_PRIORITY + " integer, "
			+ KEY_REPEAT + " integer, " + KEY_STATUS + " integer);";
	// din't know if we have to include latitude,longitude,notes text not null
	// here

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS IN todolist");
			onCreate(db);
		}
	}

	// Constructor - takes the context to allow the database to be
	// opened/created @param ctx the Context within which to work

	public TaskDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	// Open the notes database. If it cannot be opened, try to create a new
	// instance of the database. If it cannot be created, throw an exception to
	// signal the failure @return this (self reference, allowing this to be
	// chained in initialization call)@throws SQLException if the database could
	// be neither opened or created//

	public TaskDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long createTask(String task, String latitude, String longitude,
			String tag, String note, int priority, int repeat) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TASK, task);
		initialValues.put(KEY_LATITUDE, latitude);
		initialValues.put(KEY_LONGITUDE, longitude);
		initialValues.put(KEY_TAGS, tag);
		initialValues.put(KEY_NOTES, note);
		initialValues.put(KEY_PRIORITY, priority);
		initialValues.put(KEY_REPEAT, repeat);
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	// Delete the note with the given rowId @param rowId id of note to delete
	// @return true if deleted, false otherwise//

	public boolean deleteTask(long rowId) {
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllTask(String orderBy) {
		String order_by = "";
		Log.w("db Order by:-", orderBy);
		
		if(orderBy.toLowerCase().equals("status")){
			Log.w("db Order by:>", ""+1);
			order_by = this.KEY_STATUS;
		}else if(orderBy.toLowerCase().equals("priority")){
			Log.w("db Order by:>", ""+2);
			order_by = this.KEY_PRIORITY;
		}else{// ie., orderByPref = 'default'
			Log.w("db Order by:>", ""+3);
			order_by = null;
		}

		Log.w("db Order by:", ""+order_by);
		
		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TASK,
				KEY_LATITUDE, KEY_LONGITUDE, KEY_TAGS, KEY_NOTES, KEY_STATUS, KEY_PRIORITY,
				KEY_REPEAT }, null, null, null, null, order_by);
	}

	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchTasksWithIds(long[] rowIds,String order) throws SQLException {
		String order_by = "";
		
		if(order.equals("status")){
			order_by = TaskDbAdapter.KEY_STATUS;
		}else if(order.equals("prioriy")){
			order_by = this.KEY_PRIORITY;
		}else{// ie., orderByPref = 'default'
			order_by = null;
		}
		
		String where = "";
		
		for (long l : rowIds) {
			if(where != ""){
				where += " OR ";
			}
			
			where += KEY_ROWID + "=" + l;
		}
		
		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_TASK, KEY_LATITUDE, KEY_LONGITUDE, KEY_NOTES, KEY_STATUS, KEY_PRIORITY,
				KEY_REPEAT }, where, null, null, null, order_by,
				null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		
		return mCursor;
	}
	
	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchTask(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_TASK, KEY_LATITUDE, KEY_LONGITUDE, KEY_NOTES, KEY_STATUS, KEY_PRIORITY,
				KEY_REPEAT }, KEY_ROWID + "=" + rowId, null, null, null, null,
				null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		
		return mCursor;
	}

	/**
	 * Update the note using the details provided. The note to be updated is
	 * specified using the rowId, and it is altered to use the title and body
	 * values passed in
	 */
	public boolean updateNote(long rowId, String task, String latitude,
			String longitude, String tag, String note, int priority, int repeat) {
		ContentValues args = new ContentValues();
		args.put(KEY_TASK, task);
		args.put(KEY_LATITUDE, latitude);
		args.put(KEY_LONGITUDE, longitude);
		args.put(KEY_TAGS, tag);
		args.put(KEY_NOTES, note);
		args.put(KEY_PRIORITY, priority);
		args.put(KEY_REPEAT, repeat);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean markAsDone(long rowId) {
		ContentValues args = new ContentValues();

		args.put(KEY_STATUS, 1);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean markAsNotDone(long rowId) {
		ContentValues args = new ContentValues();

		args.put(KEY_STATUS, 0);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}