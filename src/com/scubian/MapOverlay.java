package com.scubian;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

class MapOverlay extends com.google.android.maps.Overlay {
	protected Context context;

	public MapOverlay(Context context) {
		this.context = context;
	}

	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		// ...
		
		return true;
	}
	
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		// ---when user lifts his finger---
		if (event.getAction() == 1) {
			GeoPoint p = mapView.getProjection().fromPixels((int) event.getX(),
					(int) event.getY());
			Toast.makeText(context,
					p.getLatitudeE6() / 1E6 + "," + p.getLongitudeE6() / 1E6,
					Toast.LENGTH_SHORT).show();
		}
		return false;
	}
}