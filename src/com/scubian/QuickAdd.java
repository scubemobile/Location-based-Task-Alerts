package com.scubian;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class QuickAdd extends Activity {
	private EditText mTask;
	private EditText mLatitude;
	private EditText mLongitude;
	private EditText mTag;
	private EditText mNotes;
	private Spinner mPriority;
	private Spinner mRepeat;
	
	private String array_spinner[];
	private String array_spinner1[];
	private Long mRowId;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quick_add);
		setTitle(R.string.quick_add);

		mTask = (EditText) findViewById(R.id.task);
		mLatitude = (EditText) findViewById(R.id.latitude);
		mLongitude = (EditText) findViewById(R.id.longitude);
		mTag = (EditText) findViewById(R.id.tag);
		mNotes = (EditText) findViewById(R.id.note);
		
		mPriority = (Spinner) findViewById(R.id.Spinner02);
		mRepeat = (Spinner) findViewById(R.id.Spinner01);
		
		// Pressing map button
		Button map = (Button) findViewById(R.id.map);
		map.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(view.getContext(), Maptask.class);
				startActivityForResult(intent, 1);
			}
		});
		Button confirmButton = (Button) findViewById(R.id.confirm);

		// Spinner for setting Priority
		array_spinner = new String[3];
		array_spinner[0] = "High";
		array_spinner[1] = "Medium";
		array_spinner[2] = "Low";
		
		Spinner s = (Spinner) findViewById(R.id.Spinner02);
		ArrayAdapter adapter = new ArrayAdapter(this,
				android.R.layout.simple_spinner_item, array_spinner);
		s.setAdapter(adapter);

		// Spinner for Repeat task
		array_spinner1 = new String[4];
		array_spinner1[0] = "Never";
		array_spinner1[1] = "Daily";
		array_spinner1[2] = "Weekly";
		array_spinner1[3] = "Forever";
		
		Spinner s1 = (Spinner) findViewById(R.id.Spinner01);
		ArrayAdapter adapter1 = new ArrayAdapter(this,
				android.R.layout.simple_spinner_item, array_spinner1);
		s1.setAdapter(adapter1);

		mRowId = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String task = extras.getString(TaskDbAdapter.KEY_TASK);
			String latitude = extras.getString(TaskDbAdapter.KEY_LATITUDE);
			String longitude = extras.getString(TaskDbAdapter.KEY_LONGITUDE);
			String tag = extras.getString(TaskDbAdapter.KEY_TAGS);
			String note = extras.getString(TaskDbAdapter.KEY_NOTES);
			
			int prio = extras.getInt(TaskDbAdapter.KEY_PRIORITY,0);
			int rep = extras.getInt(TaskDbAdapter.KEY_REPEAT,0);
			
			mRowId = extras.getLong(TaskDbAdapter.KEY_ROWID);

			if (task != null) {
				mTask.setText(task);
			}
			if (latitude != null) {
				mLatitude.setText(latitude);
			}
			if (longitude != null) {
				mLongitude.setText(longitude);
			}
			if (tag != null) {
				mTag.setText(tag);
			}
			if (note != null) {
				mNotes.setText(note);
			}
			
			mPriority.setSelection(prio);
			mRepeat.setSelection(rep);
		}
		
		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Bundle bundle = new Bundle();

				bundle.putString(TaskDbAdapter.KEY_TASK, mTask.getText()
						.toString());
				bundle.putString(TaskDbAdapter.KEY_LATITUDE, mLatitude
						.getText().toString());
				bundle.putString(TaskDbAdapter.KEY_LONGITUDE, mLongitude
						.getText().toString());
				bundle.putString(TaskDbAdapter.KEY_TAGS, mTag.getText()
						.toString());
				bundle.putString(TaskDbAdapter.KEY_NOTES, mNotes.getText()
						.toString());
				bundle.putInt(TaskDbAdapter.KEY_PRIORITY, mPriority.getSelectedItemPosition());
				bundle.putInt(TaskDbAdapter.KEY_REPEAT, mRepeat.getSelectedItemPosition());

				Log.w("confirmClick",""+ mRepeat.getSelectedItemPosition());
				
				if (mRowId != null) {
					bundle.putLong(TaskDbAdapter.KEY_ROWID, mRowId);
				}

				Intent mIntent = new Intent();
				mIntent.putExtras(bundle);
				setResult(RESULT_OK, mIntent);
				finish();
			}
		});
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		try {
			Bundle extras = intent.getExtras();

			double lat = extras.getDouble("lat"); 
			double lon = extras.getDouble("lon");
			
			mLatitude.setText(""+lat);
			mLongitude.setText(""+lon);
		}catch(Exception e){
			Log.e("Quick add - activity result", e.toString());
		}
	}
}
