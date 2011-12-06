package com.scubian;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class LocationService<LbsGeocodingActivity> extends Service {
	public static LaunchActivity MAIN_ACTIVITY;
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in
	// Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in
	// Milliseconds
	protected LocationManager locationManager;

	// main activity here
	public static void setMainActivity(LaunchActivity activity) {
		MAIN_ACTIVITY = activity;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.e("Service", "onBind");
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (MAIN_ACTIVITY != null) {
			// Background Service creation
			Log.e("Service", "onCreate");
			Toast.makeText(this, "Service created", Toast.LENGTH_SHORT).show();

			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATES,
					MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
					new MyLocationListener());
			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			// Getting current location
			if (location != null) {
				String message = String.format("Current Location \n Longitude: %1$s \n Latitude: %2$s",location.getLongitude(), location.getLatitude());
				Toast.makeText(LocationService.this, message, Toast.LENGTH_LONG).show();
				
				double lon = location.getLongitude();
				double lat = location.getLatitude();

				// Checking if any task nearby
				tasknearby(lon, lat);
			} else {
				Log.e("Location loading", "Failed");
			}
		}else{
			
		}
	}

	public int onStartCommand(Intent intent, int startid) {
		Toast.makeText(this, "Service started...", Toast.LENGTH_LONG).show();
		Log.e("Service", "onStart");
		return START_STICKY;
	}

	private class MyLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {
			String message = String.format(
					"New Location \n Longitude: %1$s \n Latitude: %2$s",
					location.getLongitude(), location.getLatitude());
			
			Log.e("Service", message);
			
			Toast.makeText(LocationService.this, message, Toast.LENGTH_LONG)
					.show();
			double lng = location.getLongitude();
			double lat = location.getLatitude();
			// Calling function to invoke task nearby
			tasknearby(lng, lat);

		}

		public void onStatusChanged(String s, int i, Bundle b) {
			Toast.makeText(LocationService.this, "Provider status changed",
					Toast.LENGTH_LONG).show();
		}

		public void onProviderDisabled(String s) {
			Toast
					.makeText(
							LocationService.this,
							"Provider disabled by the user. GPS turned off Home>Settings>Location and security >Check-Use GPS satellite",
							Toast.LENGTH_LONG).show();
		}

		public void onProviderEnabled(String s) {
			Toast.makeText(LocationService.this,
					"Provider enabled by the user. GPS turned on",
					Toast.LENGTH_LONG).show();

		}

	}

	// Database entry checking
	// @SuppressWarnings("null")
	public void tasknearby(double longitude1, double latitude1) {

		Toast.makeText(LocationService.this, "finding task nearby!!!",
				Toast.LENGTH_LONG).show();

		TaskDbAdapter mDbHelper = new TaskDbAdapter(this);
		mDbHelper.open();
		Cursor cur = mDbHelper.fetchAllTask(null);

		if (cur.getCount() > 0) {
			Log.e("taskNearBy:", "few records found");

			ArrayList<String> rowids = new ArrayList<String>();
			
			cur.moveToFirst();
			while (cur.isAfterLast() == false) {
				long rowid = Long.parseLong(cur.getString(cur.getColumnIndex(TaskDbAdapter.KEY_ROWID)));
				
				double lat2 = Double.parseDouble(cur.getString(cur.getColumnIndex(TaskDbAdapter.KEY_LATITUDE)));
				double lon2 = Double.parseDouble(cur.getString(cur.getColumnIndex(TaskDbAdapter.KEY_LONGITUDE)));
				
				float results[] = new float[3];
				Location.distanceBetween(latitude1, longitude1, lat2, lon2, results);
				float dist = results[0];
				
				if (dist <= 2.0) {
					rowids.add(""+rowid);
				}

				cur.moveToNext();
			}
			
			taskNotification(rowids);
		} else {
			Log.e("taskNearBy:", "no records found");
		}

		cur.close();
	}

	private void taskNotification(ArrayList<String> task_row_ids) {
		// for task nearby notification
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager1 = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.notification;
		CharSequence tickerText = "Task nearby!!";
		long when = System.currentTimeMillis();
		Notification notification1 = new Notification(icon, tickerText, when);
		Context context = getApplicationContext();
		CharSequence contentTitle = "Task notification";
		CharSequence contentText = "Complete your task here!!!!";
		
		Intent notifyIntent = new Intent(Intent.ACTION_MAIN);
		notifyIntent.putExtra("task_ids", task_row_ids);

		// Database cursor passed
		notifyIntent.setClass(getApplicationContext(), LaunchActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
						| Notification.FLAG_AUTO_CANCEL);
		
		notification1.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		final int HELLO_ID = 1;
		mNotificationManager1.notify(HELLO_ID, notification1);
	}

	public void onDestroy() {
		super.onDestroy();
		if (MAIN_ACTIVITY != null) {
			Log.e("Service", "onDestroy");
		}
	}
}