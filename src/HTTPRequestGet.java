package com.example.test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

public class HTTPRequestGet extends AsyncTask<String, Void, String> {
	private String jsonString = "";
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
		jsonString = responseString;
		Log.d("wew",responseString);
		return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
    	super.onPostExecute(result);
    	
    	
    }
}