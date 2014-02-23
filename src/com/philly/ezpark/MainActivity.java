package com.philly.ezpark;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.philly.ezpark.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false; // not set autohide for now

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    
    private static final String MACHINE_ID = "12F002";
    private static final String URL = "http://ez-park.herokuapp.com/requests.json";

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);
        
        final NumberPicker hourPicker = (NumberPicker) findViewById(R.id.hourPicker);
        final NumberPicker minutePicker = (NumberPicker) findViewById(R.id.minutePicker);
        
        final Button payNowBtn = (Button) findViewById(R.id.btnPayNow);

        hourPicker.setMaxValue(3);
        hourPicker.setMinValue(0);
        
        minutePicker.setMinValue(0);
        
        final String[] displayValues = new String[4];
        displayValues[0] = "0";
        displayValues[1] = "15";
        displayValues[2] = "30";
        displayValues[3] = "45";
        
        minutePicker.setMaxValue(displayValues.length - 1);
        minutePicker.setDisplayedValues(displayValues);
        minutePicker.setValue(1); // set default value as the 2nd element of display values array
        
        /*
         * Set onClick listener for Pay Now button
         */
        payNowBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		int hour, minute, duration;
        		
        		hour = hourPicker.getValue();
        		minute = Integer.parseInt(displayValues[minutePicker.getValue()]); // get minutes
        		
        		duration = hour*60 + minute;
        		
        		new sendData().execute(Integer.valueOf(duration));
        		
        		
        	/*	Toast toast = Toast.makeText(getApplicationContext(), hour + " " + displayValues[minute], Toast.LENGTH_LONG);
        		toast.show();*/
            }
        });
        
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        /*if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }*/
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.btnPayNow).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    
	private class sendData extends AsyncTask<Integer, Void, JSONObject> {
			
		private ProgressDialog dialog;
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
		SimpleDateFormat formatterYYYYMMdd = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
		SimpleDateFormat formatterhhmma = new SimpleDateFormat("hh:mma", Locale.US);
		
		double expireTime;
 
		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(MainActivity.this, "", "Sending data...");
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
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			
		    // Get the layout inflater
		    LayoutInflater inflater = MainActivity.this.getLayoutInflater();
			View view = inflater.inflate(R.layout.payment_succeeded_dialog, null);
		    builder.setView(view);
			
		    builder.setTitle("Payment Succeeded");
		    
			// Add the buttons
			builder.setPositiveButton("Print Receipt", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   Toast toast = Toast.makeText(MainActivity.this, "Thank you for your cooperation!", Toast.LENGTH_LONG);
			        	   toast.show();
			           }
			       });
			builder.setNegativeButton("Done", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   Toast toast = Toast.makeText(MainActivity.this, "Thank you for your cooperation!", Toast.LENGTH_LONG);
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
 }
