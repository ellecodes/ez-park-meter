package com.philly.ezpark;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SendData extends AsyncTask<Integer, Void, JSONObject> {
	
    private static final String MACHINE_ID = "12F002";
    private static final String URL = "http://ez-park.herokuapp.com/requests.json";
	
	private ProgressDialog dialog;
	
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
	SimpleDateFormat formatterYYYYMMdd = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
	SimpleDateFormat formatterhhmma = new SimpleDateFormat("hh:mma", Locale.US);
	
	double expireTime;
	private Activity context;
	
	public SendData(Activity context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		dialog = ProgressDialog.show(context, "", "Please wait. Processing...");
		dialog.setCancelable(false);
	}
	@Override
	protected JSONObject doInBackground(Integer... duration) {
		JSONObject result = null;
		MessageController mc = new MessageController();
		
		Date date = new Date();
		String url = URL + "?request[meter_id]=" + MACHINE_ID + "&request[paytime]=" + Uri.encode(formatter.format(date.getTime())) + "&request[duration]=" + duration[0].intValue();
		
		// Compute expire time in mili seconds used in the response dialog
		expireTime = date.getTime() + duration[0]*60*1000; 
		
		result = mc.post(null, null, url);
		
		return result;
	}
	
	@Override
	protected void onPostExecute(JSONObject result) {
		dialog.dismiss();
		
		// Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
	    // Get the layout inflater
	    LayoutInflater inflater = context.getLayoutInflater();
		View view = inflater.inflate(R.layout.payment_succeeded_dialog, null);
	    builder.setView(view);
		
	    builder.setTitle("Payment Succeeded");
	    
		// Add the buttons
		builder.setPositiveButton("Print Receipt", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   Toast toast = Toast.makeText(context, "Thank you for your cooperation!", Toast.LENGTH_LONG);
		        	   toast.show();
		           }
		       });
		builder.setNegativeButton("Done", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   Toast toast = Toast.makeText(context, "Thank you for your cooperation!", Toast.LENGTH_LONG);
		               toast.show();
		           }
		       });

		// Get the AlertDialog from create()
		AlertDialog responseDialog = builder.create();
		
		// Update text views in the dialog
		TextView dateTxtView = (TextView) view.findViewById(R.id.dateTxtView);
		TextView expTextView = (TextView) view.findViewById(R.id.expTimeTxtView);
		TextView purchaseTimeTxtView = (TextView) view.findViewById(R.id.purchaseTimeTxtView);
		TextView machineTxtView = (TextView) view.findViewById(R.id.machineTxtView);
		
		if (expireTime > 0) {
			dateTxtView.setText(formatterYYYYMMdd.format(expireTime));
			expTextView.setText(formatterhhmma.format(expireTime));	
		}
		
		/*
		 *  Will use data from POST response eventually
		 */
		String purchaseTime = "";
		Date date = new Date();
		
		try {
			purchaseTime = result.getString("created_at");
			date = formatterYYYYMMdd.parse(purchaseTime);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		purchaseTimeTxtView.setText(formatterhhmma.format(date.getTime()));
		machineTxtView.setText(MACHINE_ID);
		
		responseDialog.show();
	}
}
