package com.example.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class QRCode extends Activity implements AsyncResponse {
	static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	private String token = "";
	private TextView result;
	private String statword = "";
	tokenSender ts = new tokenSender(null);
	
	class tokenSender extends AsyncTask<String, Void, String> {
		public AsyncResponse delegate=null;
		
		@Override
		protected String doInBackground(String... params) {
			Log.d("Token code: ", token);
			JSONObject jsonobj = new JSONObject();
			try {
				jsonobj.put("nim", "13512052");
				jsonobj.put("token", token);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String tes = jsonobj.toString();
			Log.d("JSON obj: ", tes);
			
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httppostreq = new HttpPost("http://167.205.32.46/pbd/api/catch");
			
			try {
				StringEntity se = new StringEntity(jsonobj.toString());
				httppostreq.setEntity(se);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			HttpResponse httpresponse = null;
			try {
				httpresponse = httpclient.execute(httppostreq);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String responseString = "";
			
			ByteArrayOutputStream out;
			try {
				out = new ByteArrayOutputStream();
				httpresponse.getEntity().writeTo(out);
				responseString = out.toString();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return responseString;
		}
		
		 @Override
	        protected void onPostExecute(String result) {
	        	super.onPostExecute(result);
	        	delegate.processFinish(result);
		 }
		 

		public tokenSender(AsyncResponse delegate) {
			this.delegate = delegate;
		}
	}
	
	public void processFinish(String output) {
		Log.d("Response from Async: ", (String) output);
		result = (TextView) findViewById(R.id.httpResult);
		result.setText("JSON returned: " + (String) output);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set the main content layout of the Activity
		setContentView(R.layout.activity_qrcode);
		ts.delegate = this;
	}
	
	@Override
	public void onResume() {
	super.onResume();
	}
	
	@Override
	public void onPause() {
	super.onPause();
	}
	
	@Override
	public void onDestroy() {
	super.onDestroy();
	}
	
	//product qr code mode
	public void scanQR(View v) {
		try {
			//start the scanning activity from the com.google.zxing.client.android.SCAN intent
			Intent intent = new Intent(ACTION_SCAN);
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException anfe) {
			//on catch, show the download dialog
			showDialog(QRCode.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
		}
	}

	//alert dialog for downloadDialog
	private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
		AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
		downloadDialog.setTitle(title);
		downloadDialog.setMessage(message);
		downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				try {
					act.startActivity(intent);
				} catch (ActivityNotFoundException anfe) {

				}
			}
		});
		downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
			}
		});
		return downloadDialog.show();
	}

	//on ActivityResult method
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				//get the extras that are returned from the intent
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
				toast.show();
				token = contents;
				
				ts.execute();
				
			}
		}
	}
}
