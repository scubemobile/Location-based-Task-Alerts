package com.scubian;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class Maptask extends MapActivity {
	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	
	private GestureDetector gestureDetector;

	private PlacesItemizedOverlay placesItemizedOverlay;

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.map); // bind the layout to the activity

		/*
		activityContext = this.getBaseContext();
		gestureDetector = new GestureDetector((OnGestureListener) activityContext);
		gestureDetector.setOnDoubleTapListener((OnDoubleTapListener) activityContext);
		*/
		// create a map view
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setStreetView(true);

		mapView.invalidate();

		mapController = mapView.getController();
		mapController.setZoom(14); // Zoom 1 is world view
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, new GeoUpdateHandler());
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 1, R.string.go_back);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.w("onmenuitemselected", "entered");
		
		switch (item.getItemId()) {
			case 1:
				Intent mIntent = new Intent();
				
				double point[] = placesItemizedOverlay.getMarkerPosition();
				
				mIntent.putExtra("lat", point[0]);
				mIntent.putExtra("lon", point[1]);
				
				Log.w("onmenuitemselected", "entered case 1 ("+point[0]+","+point[1]+")");
				
				setResult(RESULT_OK, mIntent);
				finish();
			return true;
			default:
				Log.w("onmenuitemselected", "to default");
		}

		return super.onMenuItemSelected(featureId, item);
	}

/*
	public Maptask(Context aContext, AttributeSet attrs) {
		// super(aContext, attrs);
		context = aContext;

		gestureDetector = new GestureDetector((OnGestureListener) context);
		gestureDetector.setOnDoubleTapListener((OnDoubleTapListener) context);
	}
*/
	
	// Override the onTouchEvent() method to intercept events and pass them
	// to the GestureDetector. If the GestureDetector doesn't handle the event,
	// propagate it up to the MapView.
	public boolean onTouchEvent(MotionEvent ev) {
		if (this.gestureDetector.onTouchEvent(ev))
			return true;
		else
			return super.onTouchEvent(ev);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void onStart() {
		super.onStart();
		initialiseOverlays();
	}

	private void initialiseOverlays() {
		// Create an ItemizedOverlay to display a list of markers
		Drawable defaultMarker = getResources().getDrawable(R.drawable.location);
		placesItemizedOverlay = new PlacesItemizedOverlay(this, defaultMarker);

		//@todo need to get this location from GPS as default location
		placesItemizedOverlay.addOverlayItem(new OverlayItem(new GeoPoint(
				(int) (40.748963847316034 * 1E6),
				(int) (-73.96807193756104 * 1E6)), "UN", "United Nations"));

		// Add the overlays to the map
		mapView.getOverlays().add(placesItemizedOverlay);
	}

	public class GeoUpdateHandler implements LocationListener {
		public void onLocationChanged(Location location) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
			mapController.animateTo(point); // mapController.setCenter(point);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	/**
	 * Methods required by OnDoubleTapListener
	 **/
	public boolean onDoubleTap(MotionEvent e) {
		GeoPoint p = mapView.getProjection().fromPixels((int) e.getX(),
				(int) e.getY());

		placesItemizedOverlay.addOverlayItem(new OverlayItem(new GeoPoint(
				(int) (p.getLatitudeE6()),
				(int) (p.getLongitudeE6())), null, null));

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("Double Tap");
		dialog.setMessage("Location: " + p.getLatitudeE6() + ", "
				+ p.getLongitudeE6());
		dialog.show();

		return true;
	}

	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}

	/**
	 * Methods required by OnGestureListener
	 **/
	public boolean onDown(MotionEvent e) {
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	public void onLongPress(MotionEvent e) {
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	public void onShowPress(MotionEvent e) {
	}

	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
}
