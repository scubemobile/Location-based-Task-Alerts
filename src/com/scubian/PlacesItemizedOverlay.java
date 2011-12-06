package com.scubian;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class PlacesItemizedOverlay extends ItemizedOverlay {
	private Context context;
	private ArrayList items = new ArrayList();

	private double latitude;
	private double longitude;
	
	public PlacesItemizedOverlay(Context aContext, Drawable marker) {
		super(boundCenterBottom(marker));
		context = aContext;
	}

	public void addOverlayItem(OverlayItem item) {
		// as we need only one item always
		if(items.size() > 0){
			items.remove(0);
		}
		
		items.add(item);
		populate();
	}
	
	public double[] getMarkerPosition(){
		double point[] = new double[2];
		
		point[0] = this.latitude;
		point[1] = this.longitude;
		
		return point;
	}

	public void updateMarker(){
		
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return (OverlayItem) items.get(i);
	}

	@Override
	public int size() {
		return items.size();
	}

	public boolean onTouchEvent(android.view.MotionEvent event, MapView mapView){
		if (event.getAction() == 1) {
			GeoPoint p = mapView.getProjection().fromPixels((int) event.getX(),
					(int) event.getY());
			
			items.remove(0);
			
			this.addOverlayItem(new OverlayItem(new GeoPoint(
				(int) (p.getLatitudeE6()),
				(int) (p.getLongitudeE6())), null,null));
			
			this.longitude = p.getLongitudeE6()/1E6;
			this.latitude = p.getLatitudeE6()/1E6;
			
			Toast.makeText(context,
					p.getLatitudeE6() / 1E6 + "," + p.getLongitudeE6() / 1E6,
					Toast.LENGTH_SHORT).show();
		}
		return false;
	}
}