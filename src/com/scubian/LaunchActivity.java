package com.scubian;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class LaunchActivity extends ListActivity {
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;

	private static final int INSERT_ID = Menu.FIRST;
	private static final int ABOUT_ID = Menu.FIRST + 1;
	private static final int HELP_ID = Menu.FIRST + 2;
	private static final int PREFERENCE_ID = Menu.FIRST + 3;
	private static final int DELETE_ID = Menu.FIRST + 4;

	private TaskDbAdapter mDbHelper;
	private Cursor mNotesCursor;

	/**
	 * Preferences
	 */
	private boolean startUpInstructionsPref;
	private String orderByPref;
	private int distanceRangePref;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.e("On Create","Entered");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Update preferences
		getPrefs();
		
		// Database for setting task details
		mDbHelper = new TaskDbAdapter(this);
		mDbHelper.open();

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Log.w("instructions:", "To IF-"+ extras.toString());
			
			ArrayList<String> task_row_ids = extras.getStringArrayList("task_ids");

			fillData(task_row_ids);
		} else {
			Log.w("instructions:", "To ELSE-"+startUpInstructionsPref);

			if(startUpInstructionsPref){
				// Showing dialog on start up
				AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
				// set the message to display
				alertbox.setTitle("Information");
				alertbox.setMessage("Enable GPS and Internet service to proceed.\nPress Menu button to add task and change preferences.");
	
				// add a neutral button to the alert box and assign a click listener
				alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
					// click listener on the alert box
					public void onClick(DialogInterface arg0, int arg1) {
						// the button was clicked
					}
				});
				
				// show it
				alertbox.show();
			}

			fillData(null);
		}

		registerForContextMenu(getListView());
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
//		getPrefs();
		Log.e("On Restored","Entered");
		
		onCreate(savedInstanceState);
		//super.onRestoreInstanceState(savedInstanceState);
	}
	
	private void fillData(ArrayList<String> task_row_ids) {
		// Get all of the rows from the database and create the item list
		if (task_row_ids != null) {
			long[] rowIds = new long[task_row_ids.size()];

			int current = 0;
			for (String rid : task_row_ids) {
				rowIds[current] = Long.parseLong(rid);
				current++;
			}

			mNotesCursor = mDbHelper.fetchTasksWithIds(rowIds, orderByPref);
		} else {
			mNotesCursor = mDbHelper.fetchAllTask(orderByPref);
		}

		startManagingCursor(mNotesCursor);

		// Create an array to specify the fields we want to display in the list
		String[] from = new String[] { TaskDbAdapter.KEY_TASK, TaskDbAdapter.KEY_NOTES, TaskDbAdapter.KEY_STATUS  };

		// and an array of the fields we want to bind those fields to (in this case just text1)
		int[] to = new int[] { R.id.title,R.id.note,R.id.status };

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter task = new SimpleCursorAdapter(this, R.layout.task_row, mNotesCursor, from, to);
		setListAdapter(task);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_ID, 1, R.string.menu_insert);
		menu.add(0, ABOUT_ID, 2, "About");
		menu.add(0, HELP_ID, 3, "Help");
		menu.add(0, PREFERENCE_ID, 4, "Preferences");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case INSERT_ID:
			createTask();
			return true;
		case ABOUT_ID:
			Intent i = new Intent(this, About.class);
			startActivity(i);
			return true;
		case HELP_ID:
			Intent i1 = new Intent(this, Help.class);
			startActivity(i1);
			return true;
		case PREFERENCE_ID:
			Intent settingsActivity = new Intent(getBaseContext(),
					Preferences.class);
			startActivity(settingsActivity);
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			mDbHelper.deleteTask(info.id);
			fillData(null);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void createTask() {
		Intent i = new Intent(this, QuickAdd.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//super.onListItemClick(l, v, position, id);
		Cursor c = mNotesCursor;
		c.moveToPosition(position);
		
		Intent i = new Intent(this, QuickAdd.class);
		i.putExtra(TaskDbAdapter.KEY_ROWID, id);
		
		i.putExtra(TaskDbAdapter.KEY_TASK, c.getString(c
				.getColumnIndexOrThrow(TaskDbAdapter.KEY_TASK)));
		i.putExtra(TaskDbAdapter.KEY_LATITUDE, c.getString(c
				.getColumnIndexOrThrow(TaskDbAdapter.KEY_LATITUDE)));

		i.putExtra(TaskDbAdapter.KEY_LONGITUDE, c.getString(c
				.getColumnIndexOrThrow(TaskDbAdapter.KEY_LONGITUDE)));
		i.putExtra(TaskDbAdapter.KEY_TAGS, c.getString(c
				.getColumnIndexOrThrow(TaskDbAdapter.KEY_TAGS)));
		i.putExtra(TaskDbAdapter.KEY_NOTES, c.getString(c
				.getColumnIndexOrThrow(TaskDbAdapter.KEY_NOTES)));
		
		i.putExtra(TaskDbAdapter.KEY_PRIORITY, c.getInt(c
				.getColumnIndexOrThrow(TaskDbAdapter.KEY_PRIORITY)));
		i.putExtra(TaskDbAdapter.KEY_REPEAT, c.getInt(c
				.getColumnIndexOrThrow(TaskDbAdapter.KEY_REPEAT)));
		
		
		startActivityForResult(i, ACTIVITY_EDIT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
		
		try {
			Bundle extras = intent.getExtras();

			if(extras != null){
				switch (requestCode) {
				case ACTIVITY_CREATE:
					String task = extras.getString(TaskDbAdapter.KEY_TASK);
					String latitude = extras.getString(TaskDbAdapter.KEY_LATITUDE);
					String longitude = extras
							.getString(TaskDbAdapter.KEY_LONGITUDE);
					String tag = extras.getString(TaskDbAdapter.KEY_TAGS);
					String note = extras.getString(TaskDbAdapter.KEY_NOTES);
					
					int priority = extras.getInt(TaskDbAdapter.KEY_PRIORITY);
					int repeat = extras.getInt(TaskDbAdapter.KEY_REPEAT);
	
					mDbHelper.createTask(task, latitude, longitude, tag, note,
							priority, repeat);
					fillData(null);
					break;
				case ACTIVITY_EDIT:
					Long rowId = extras.getLong(TaskDbAdapter.KEY_ROWID);
					if (rowId != null) {
						String editTask = extras.getString(TaskDbAdapter.KEY_TASK);
	
						String editLatitude = extras
								.getString(TaskDbAdapter.KEY_LATITUDE);
						String editLongitude = extras
								.getString(TaskDbAdapter.KEY_LONGITUDE);
						String editTags = extras.getString(TaskDbAdapter.KEY_TAGS);
						String editNotes = extras
								.getString(TaskDbAdapter.KEY_NOTES);
						int editPriority = extras.getInt(TaskDbAdapter.KEY_PRIORITY);
						int editRepeat = extras.getInt(TaskDbAdapter.KEY_REPEAT);
	
						mDbHelper.updateNote(rowId, editTask, editLatitude,
								editLongitude, editTags, editNotes, editPriority,
								editRepeat);
					}
					fillData(null);
					break;
				}
			}
		} catch (Exception e) {
			Log.e("LaunchActivity",e.toString());
		}
	}

	private void getPrefs() {
		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		startUpInstructionsPref = prefs.getBoolean("startUpInstructionsPref", true);
		orderByPref = prefs.getString("orderByPref", "default");
		distanceRangePref = Integer.parseInt(prefs.getString("distanceRangePref", "2"));
		
		Log.w("Preferences:", "In Get Prefs:"+ startUpInstructionsPref +"|"+orderByPref+"|"+distanceRangePref);
	}
/*
	@Override
	protected void onStart(){
		getPrefs();

		Log.w("instructions:", "In Onstart-"+startUpInstructionsPref);
		
		super.onStart();
	}
*/
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}