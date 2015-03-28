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
import android.graphics.Color;

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
		adlib = new HashMap<String,Bitmap>();
		bleepMainStart();
		if(super.thisBleepService!=null)
			super.thisBleepService.startBleepDiscovery();
	}

	@Override
	protected void onStop() {
		super.onDestroy();
//		super.onStop();
		bleepMainStop();
//		if(super.thisBleepService!=null)
//			super.thisBleepService.stopBleepDiscovery();
//		super.thisBleepService.onDestroy();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		bleepMainDestroy();
	}

	protected void displayStatusMsg(String msg) {
		tvBeaconStatus.setText(msg);
	}

	public static int getDominantColor(Bitmap bitmap) {
		if (null == bitmap) return Color.TRANSPARENT;

		int redBucket = 0;
		int greenBucket = 0;
		int blueBucket = 0;
		int alphaBucket = 0;

		boolean hasAlpha = bitmap.hasAlpha();
		int pixelCount = bitmap.getWidth() * bitmap.getHeight();
		int[] pixels = new int[pixelCount];
		bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

		for (int y = 0, h = bitmap.getHeight(); y < h; y++)
		{
			for (int x = 0, w = bitmap.getWidth(); x < w; x++)
			{
				int color = pixels[x + y * w]; // x + y * width
				redBucket += (color >> 16) & 0xFF; // Color.red
				greenBucket += (color >> 8) & 0xFF; // Color.greed
				blueBucket += (color & 0xFF); // Color.blue
				if (hasAlpha) alphaBucket += (color >>> 24); // Color.alpha
			}
		}

		return Color.argb(
				(hasAlpha) ? (alphaBucket / pixelCount) : 255,
						redBucket / pixelCount,
						greenBucket / pixelCount,
						blueBucket / pixelCount);
	}

}
