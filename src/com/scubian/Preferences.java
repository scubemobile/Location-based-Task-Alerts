package com.scubian;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = new Intent(this, LocationService.class);
		
		addPreferencesFromResource(R.xml.preferences);
		
		final Preference servicePref = (Preference) findPreference("servicePref");
		final Preference gpsPref = (Preference) findPreference("gpsPref");
		
		gpsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));
				
				return true;
			}
		});
		
		// Get the custom preference
		SharedPreferences customSharedPreference = getSharedPreferences("servicePref", Activity.MODE_PRIVATE);
		boolean serviceStarted = customSharedPreference.getBoolean("servicePref", false);
		
		if(serviceStarted){
			servicePref.setTitle("Stop Service");
			servicePref.setSummary("Service Running");
		}else{
			servicePref.setTitle("Start Service");
			servicePref.setSummary("Service Stopped");
		}
		
		servicePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				SharedPreferences customSharedPreference = getSharedPreferences("servicePref", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = customSharedPreference.edit();
				boolean serviceStarted = customSharedPreference.getBoolean("servicePref", false);
				
				serviceStarted = !serviceStarted;
				
				editor.putBoolean("servicePref", serviceStarted);
				editor.commit();
				
				if(serviceStarted){
					startService(intent);
					
					servicePref.setTitle("Stop Service");
					servicePref.setSummary("Service Running");
					
					Toast.makeText(getBaseContext(), "Service Started..", Toast.LENGTH_SHORT).show();
				}else{
					stopService(intent);

					servicePref.setTitle("Start Service");
					servicePref.setSummary("Service Stopped");
					
					Toast.makeText(getBaseContext(), "Service Stopped..", Toast.LENGTH_SHORT).show();
				}
				
				return true;
			}
		});
	}
}