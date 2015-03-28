/*
 * Copyright (C) 2014 Rainmaker Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.rainmakerlabs.bleepsample;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Bitmap;

public class MainActivity extends BleepActivity {
	
	public static LinearLayout myGallery;
	public static int gal_size = 0;
	public static HashMap<String,Bitmap> adlib = new HashMap<String,Bitmap>();
	
	protected static final String TAG = "MainActivity";
	private TextView tvBeaconStatus;
	
	protected void onCreateForBleep() {
		bleepSplashCreate();
	}

	protected void doAfterServiceConnection() {
		bleepSplashAfterServiceConnection();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "oncreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		myGallery = (LinearLayout)findViewById(R.id.mygallery);
		
		tvBeaconStatus = (TextView) findViewById(R.id.tvStatus);
		tvBeaconStatus.setText("Waiting to detect beacons...");
		
		if (isCustomisable) {
			RelativeLayout thislayout = (RelativeLayout) findViewById(R.id.mainLayout);
			Button optBtn = new Button(this);
			optBtn.setText("Options");
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
			         280,
			         RelativeLayout.LayoutParams.WRAP_CONTENT);
			         lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			optBtn.setLayoutParams(lp);
			optBtn.setOnClickListener(new Button.OnClickListener() {
				@Override
			    public void onClick(View v) {
					Intent stIntent = new Intent(MainActivity.this, OptionsActivity.class);
					if (thisBleepService != null)
						thisBleepService.pauseBleepDiscovery();
					startActivity(stIntent);
			    }
			});
			thislayout.addView(optBtn);
		}
	}
    
	@Override
	protected void onStart() {
		super.onStart();
		bleepMainStart();
		if(super.thisBleepService!=null)
			super.thisBleepService.startBleepDiscovery();
	}

	@Override
	protected void onStop() {
		super.onStop();
		bleepMainStop();
		if(super.thisBleepService!=null)
			super.thisBleepService.stopBleepDiscovery();
	}
	
    @Override
	protected void onDestroy() {
		if(super.thisBleepService!=null)
			super.thisBleepService.stopBleepDiscovery();
		super.onDestroy();
		bleepMainDestroy();
	}
   
    protected void displayStatusMsg(String msg) {
    		tvBeaconStatus.setText(msg);
    }
    
}
