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
import com.rainmakerlabs.bleepsample.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

public class AdDialog {

	public static int howManyAdDialogsShowing;
	static Dialog onlyDialog;
	protected static String onlyAdUrl;

	//public static void showAdsDialog(Activity content, int adsId) {
	public static void showAdsDialog(Context context, Bitmap adsId, String adUrl, String adAspect) {
		final Dialog dialog = new Dialog(context, R.style.AppTheme);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.custom_ads_dialog);
		dialog.setCanceledOnTouchOutside(true);
		ImageView btnClose = (ImageView) dialog.findViewById(R.id.imgCADClose);
		ImageView btnAds = (ImageView) dialog.findViewById(R.id.imgCADAds);
		//btnAds.setImageResource(adsId);
		btnAds.setImageBitmap(adsId);
		if (adAspect.equalsIgnoreCase("scale to fill"))
			btnAds.setScaleType(ImageView.ScaleType.FIT_XY);
		else if (adAspect.equalsIgnoreCase("aspect fit"))
			btnAds.setScaleType(ImageView.ScaleType.FIT_CENTER);
		else if (adAspect.equalsIgnoreCase("aspect fill"))
			btnAds.setScaleType(ImageView.ScaleType.CENTER_CROP);
		btnClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeDialog(dialog);
			}
		});

		btnAds.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeDialog(dialog);
			}
		});

		onlyAdUrl = adUrl;
		onlyDialog = dialog;
		dialog.show();
		howManyAdDialogsShowing += 1;

	}
	
	public static void closeOnlyAdDialog() {
		closeDialog(onlyDialog);
	}
	
	public static String getCurrentAdUrl() {
		return onlyAdUrl;
	}
	
	public static void closeDialog(Dialog dialog) {
		dialog.dismiss();
		howManyAdDialogsShowing -= 1;
		onlyAdUrl = "";
		onlyDialog = null;
	}

}
