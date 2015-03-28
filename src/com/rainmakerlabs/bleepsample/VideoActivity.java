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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends BleepActivity {
	protected static final String TAG = "VideoActivity";
	Context contextThis = this;
	VideoView videoView;
	MediaController mediaController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "oncreate");
		super.onCreate(savedInstanceState);
		isVideoActivityOpen = true;
		setContentView(R.layout.custom_video_activity);
		
		Intent intent = getIntent();
		String adUrl = intent.getStringExtra("url");
		
		videoView = (VideoView) findViewById(R.id.videoViewer);
	    
		new BackgroundAsyncTask().execute(adUrl);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		isVideoActivityOpen = false;
	}
	
	public class BackgroundAsyncTask extends AsyncTask<String, Uri, Void> {
		Integer track = 0;
		ProgressDialog dialog;
		
		protected void onPreExecute() {
			dialog = new ProgressDialog(contextThis);
			dialog.setMessage("Please wait while we load your video...");
			dialog.setCancelable(true);
			dialog.show();
		}
		
		protected void onProgressUpdate(final Uri... uri) {
			try {
				mediaController=new MediaController(contextThis);
				videoView.setMediaController(mediaController);
				mediaController.setPrevNextListeners(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// next button clicked
					}
				}, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});
				mediaController.show(10000);
				videoView.setVideoURI(uri[0]);
				videoView.requestFocus();
				videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
					public void onPrepared(MediaPlayer arg0) {
						videoView.start();
						dialog.dismiss();
					}
				});
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		protected Void doInBackground(String... params) {
			try {
				Uri uri = Uri.parse(params[0]);
				publishProgress(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
    
}
