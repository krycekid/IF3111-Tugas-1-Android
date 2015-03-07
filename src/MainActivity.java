package com.example.test;

/* Import for Compass */
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;



/* Import for JSON*/
import org.json.*;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
/* Import for Maps */
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;

public class MainActivity extends FragmentActivity implements SensorEventListener {
		
	/* HTTP Request */
	private TextView data;
	private ImageView img;
	private HTTPRequestGet hrg = new HTTPRequestGet();
	private String statword = ""; 
	
	/* Map */
	private GoogleMap map;
	
	/* Compass */
	private ImageView mPointer;
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;
	
    /* Process for Jerry Location */
    
    class HTTPRequestGet extends AsyncTask<String, Void, String> {
    	private double latitude;
    	private double longitude;
    	private LatLng jerry_position;
    	
    	@Override
        protected String doInBackground(String... urls) {
    		HttpClient httpclient = new DefaultHttpClient();
    		HttpResponse response;
    		String responseString = null;
    		try {
    			response = httpclient.execute(new HttpGet(urls[0]));
    			StatusLine statusLine = response.getStatusLine();
    		if(statusLine.getStatusCode() == HttpStatus.SC_OK){
    			ByteArrayOutputStream out;
    			out = new ByteArrayOutputStream();
    			response.getEntity().writeTo(out);
    			responseString = out.toString();
    			out.close();
    		} else{
    			//Closes the connection.
    			response.getEntity().getContent().close();
    			throw new IOException(statusLine.getReasonPhrase());
    			}
    		} catch (ClientProtocolException e) {
    			//TODO Handle problems..
    		} catch (IOException e) {
    			//TODO Handle problems..
    		}
    		
    		
    		return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
        	
	    	try {
	    		JSONObject jObject = new JSONObject(result);
	    		latitude = jObject.getDouble("lat");
	    		longitude = jObject.getDouble("long");
	    		jerry_position = new LatLng(latitude,longitude);
	    	} catch (JSONException e) {
	    		// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	data = (TextView) findViewById(R.id.dummy2);
        	data.setText("Jerry's Latitude: " + jerry_position.latitude + "\nJerry's Longitude: "
        			+ jerry_position.longitude
        			);
        	if (map != null) {
        		map.addMarker(new MarkerOptions().position(jerry_position).title("Jerry"));
        		moveCameraTo(jerry_position.latitude, jerry_position.longitude);
        	}
        }
    }
    
	/* Activities */
    public void moveCameraTo(double latitude, double longitude) {
    	map.moveCamera(CameraUpdateFactory.newLatLngZoom(
        new LatLng(latitude, longitude), 0));

        CameraPosition cameraPosition = new CameraPosition.Builder()
        .target(new LatLng(latitude, longitude))      // Sets the center of the map to location user
        .zoom(15)                   // Sets the zoom
        .bearing(0)                // Sets the orientation of the camera to east
        .tilt(0)                   // Sets the tilt of the camera to 30 degrees
        .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        hrg.execute("http://167.205.32.46/pbd/api/track?nim=13512052");
        
        /* Compass Initialization */
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	    mPointer = (ImageView) findViewById(R.id.pointer);
	    
        /* Map Initialization */
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        
        /* Map process */
        if (map!=null){
        	map.setMyLocationEnabled(true);
        	map.getUiSettings().setRotateGesturesEnabled(false);
        	map.getUiSettings().setCompassEnabled(false);
        	
        	LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
        	
            if (location != null)
            {
            	/* Write marker to current position */
            	LatLng curPos = new LatLng(location.getLatitude(),location.getLongitude());
            	map.addMarker(new MarkerOptions().position(curPos).title("Tom"));
            }
        }
    }
    
    protected void onResume() {
	    super.onResume();
	    map.setMyLocationEnabled(true);
	    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
	}
	 
	protected void onPause() {
	    super.onPause();
	    map.setMyLocationEnabled(false);
	    mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
	}
    
    @Override
	public void onSensorChanged(SensorEvent event) {
    	/* Compass Process */
		if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            RotateAnimation ra = new RotateAnimation(
            		mCurrentDegree, 
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f, 
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
     
            ra.setDuration(250);
     
            ra.setFillAfter(true);
     
            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
        }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	public void onClickTest(View v) {
		Intent intent = new Intent(this, QRCode.class);
		if (intent != null) {
			startActivity(intent);
		}
	}
}