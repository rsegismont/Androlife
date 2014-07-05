/* The MIT License (MIT)
 *
 * Copyright (c) 2013 Romain Segismont
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package com.rsegismont.androlife.core.utils;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.application.AndrolifeApplication;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.core.ui.SkeletonActivity;
import com.rsegismont.androlife.settings.SettingsActivity;

/**
 * Utilit class used to store common task (such as image loading)
 * 
 * @author Romain Segismont
 * @version 1.0
 * @since 1.0
 * 
 */
public class AndrolifeViewFactory {

	public static final int HD_ON = 1;
	public static final int HD_OFF = 0;

	/** Constant for J-Music */
	private static final String JMUSIC = "J-music";

	/**
	 * Setup the csa logo into an {@link ImageView}
	 * 
	 * @param imageView
	 *            the {@link ImageView} to set
	 * @param csaValue
	 *            the minimum age required for a program
	 */
	public static void addCsaView(ImageView imageView, String csaValue) {
		try {
			if (!TextUtils.isEmpty(csaValue)) {
				switch (Integer.valueOf(csaValue).intValue()) {
				default:
					imageView.setImageResource(0);
					break;

				// 10 year and more
				case 10:
					imageView.setImageResource(R.drawable.ic_detail_csa10);
					break;

				// 12 year and more
				case 12:
					imageView.setImageResource(R.drawable.ic_detail_csa12);
					break;

				// 16 year and more
				case 16:
					imageView.setImageResource(R.drawable.ic_detail_csa16);
					break;
				}
			} else {
				imageView.setImageResource(0);
			}
		} catch (Throwable error) {
			error.printStackTrace();
			imageView.setImageResource(0);
		}
	}
	
	public static void addVersionView(TextView textView){
		try {
			textView.setText(textView.getContext().getString(R.string.androlife_about_version, textView.getContext().getPackageManager()
					.getPackageInfo(textView.getContext().getPackageName(), 0).versionName));

		} catch (PackageManager.NameNotFoundException nameNotFoundException) {
			nameNotFoundException.printStackTrace();
		}

	}

	/**
	 * Setup the HD logo into an {@link ImageView}
	 * 
	 * @param imageView
	 *            the {@link ImageView} to set
	 * 
	 */
	public static void addHdView(ImageView imageView, String hdString, boolean changeVisibility) {
		try {
			final int hdInt = Integer.valueOf(hdString);
			if (hdInt == HD_ON) {
				if (changeVisibility) {
					imageView.setVisibility(View.VISIBLE);
				}
				imageView.setImageResource(R.drawable.androlife_logo_hd);
			} else {
				if (changeVisibility) {
					imageView.setVisibility(View.GONE);
				}
				imageView.setImageResource(0);
			}
		} catch (Throwable error) {
			error.printStackTrace();
			if (changeVisibility) {
				imageView.setVisibility(View.GONE);
			}
			imageView.setImageResource(0);
		}
	}

	/**
	 * Display an image into an {@link ImageView}
	 * 
	 * @param activity
	 *            current {@link SkeletonActivity}
	 * @param firstImage
	 *            the first image to test and display if not null
	 * @param secondImage
	 *            the second image to test and display if not null and if firstImage was null
	 * @param imageView
	 *            the {@link ImageView} where to set the image
	 * @param width
	 *            width requested for the image to display
	 * @param height
	 *            height requested for the image to display
	 */
	private static void displayImage(String firstImage, String secondImage, ImageView imageView, int width, int height) {

		try {
			if (TextUtils.isEmpty(firstImage) == false) {
				AndrolifeApplication.instance.mImageDownloader.loadImage(firstImage, imageView, width, height);

				// Picasso.with(imageView.getContext()).load(firstImage).placeholder(R.drawable.toutsuite).resize(width,
				// height).centerInside().into(imageView);
			} else {
				if (TextUtils.isEmpty(secondImage) == false) {
					// Picasso.with(imageView.getContext()).load(secondImage).placeholder(R.drawable.toutsuite).resize(width,
					// height).centerInside().into(imageView);
					AndrolifeApplication.instance.mImageDownloader.loadImage(secondImage, imageView, width, height);
				} else {
					loadDefaultImage(imageView, width, height);
				}
			}

		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	private static void loadDefaultImage(ImageView imageView, int width, int height) {
		// Picasso.with(imageView.getContext()).load(R.drawable.toutsuite).placeholder(R.drawable.toutsuite).fit().into(imageView);
		AndrolifeApplication.instance.mImageDownloader.loadImage(R.drawable.toutsuite, imageView, width, height);
	}

	/**
	 * 
	 * @param activity
	 *            current {@link SkeletonActivity}
	 * @param imageView
	 *            the {@link ImageView} where to set the image
	 * @param values
	 *            where to extract images to display
	 * @param width
	 *            width requested for the image to display
	 * @param height
	 *            height requested for the image to display
	 */
	public static void displayImage(ImageView imageView, ContentValues values, int width, int height) {
		if (AndrolifeApplication.instance.preferencesManager.getBoolean(SettingsActivity.PERFORMANCES_IMAGES, true)) {
			final String screenShotEmission = values
					.getAsString(SharedInformation.DatabaseColumn.SCREENSHOT_EMISSION.stringValue);
			final String screenshot = values.getAsString(SharedInformation.DatabaseColumn.SCREENSHOT.stringValue);

			displayImage(screenShotEmission, screenshot, imageView, width, height);
		} else {
			loadDefaultImage(imageView, width, height);
		}
	}

	/**
	 * 
	 * @param activity
	 *            current {@link SkeletonActivity}
	 * @param imageView
	 *            the {@link ImageView} where to set the image
	 * @param values
	 *            where to extract images to display
	 * @param width
	 *            width requested for the image to display
	 * @param height
	 *            height requested for the image to display
	 * @param title
	 *            title of the program to display
	 */
	public static void displayImage(ImageView imageView, Cursor values, int width, int height, String title) {
		if (AndrolifeApplication.instance.preferencesManager.getBoolean(SettingsActivity.PERFORMANCES_IMAGES, true)) {

			final String screenShotEmission = values.getString(values
					.getColumnIndex(SharedInformation.DatabaseColumn.SCREENSHOT_EMISSION.stringValue));
			final String screenshot = values.getString(values
					.getColumnIndex(SharedInformation.DatabaseColumn.SCREENSHOT.stringValue));

			if (title.equals(JMUSIC)) {
				displayImage(screenShotEmission, screenshot, imageView, width, height);
			} else {
				displayImage(screenshot, screenShotEmission, imageView, width, height);
			}

		} else {
			loadDefaultImage(imageView, width, height);
		}
	}

}
