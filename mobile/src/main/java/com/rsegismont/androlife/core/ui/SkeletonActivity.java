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

package com.rsegismont.androlife.core.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.application.AndrolifeApplication;
import com.rsegismont.androlife.common.SdkUtils;
import com.rsegismont.androlife.common.utils.ImageCacher;
import com.rsegismont.androlife.common.utils.ImageFetcher;
import com.rsegismont.androlife.common.utils.Utils;
import com.rsegismont.androlife.core.utils.ActivityEffectsHelper;
import com.rsegismont.androlife.core.utils.ActivityEffectsHelper.EffectListener;
import com.rsegismont.androlife.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all Androlife Activities
 * 
 * @author Romain Segismont
 * @version 1.0
 * @since 1.0
 * 
 */
public abstract class SkeletonActivity extends FragmentActivity implements EffectListener {
	protected AndrolifeApplication androlifeApplication;
	protected int height = 0;
	protected int width = 0;
	public SharedPreferences preferencesManager;
	protected Menu mMenu;

	@Override
	public void onTransitionEffect(int enterAnim, int exitAnim) {
		overridePendingTransition(enterAnim, exitAnim);
	}

	/**
	 * Method to override to return the requested orientation of the activity
	 * 
	 * @return {@link ActivityInfo#screenOrientation} attribute
	 */
	protected int getActivityOrientation() {
		if (!SdkUtils.isATablet(this)) {
			return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		} else {
			return Integer.valueOf(
					PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(
							SettingsActivity.GENERAL_ORIENTATION, "" + ActivityInfo.SCREEN_ORIENTATION_SENSOR))
					.intValue();
		}

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		ActivityEffectsHelper.onBackPressed(androlifeApplication, this);
	}

	/**
	 * Setup width and height of the device
	 * 
	 */
	private void setupDimensions() {
		final DisplayMetrics localDisplayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
		this.height = localDisplayMetrics.heightPixels;
		this.width = localDisplayMetrics.widthPixels;
	}

	public boolean isTranslucentActivity() {
		return false;
	}

	@Override
	protected void onCreate(Bundle paramBundle) {

		super.onCreate(paramBundle);
		this.preferencesManager = PreferenceManager.getDefaultSharedPreferences(this);

		if (isTranslucentActivity() && Utils.hasKitKat()) {
			setTheme(R.style.Androlife_Theme_Transparent);
		}

		setupDimensions();

        //noinspection ResourceType
        setRequestedOrientation(getActivityOrientation());
		this.androlifeApplication = ((AndrolifeApplication) getApplication());
		androlifeApplication.destroyCpt = (1 + androlifeApplication.destroyCpt);

		if (getImageDownloader() == null) {
			initImageDownloader();
		}

	}

	private void initImageDownloader() {
		ImageCacher.ImageCacheParams localImageCacheParams = new ImageCacher.ImageCacheParams(this, "thumbs");
		localImageCacheParams.setMemCacheSizePercent(0.5F);
		this.androlifeApplication.setImageDownloader(new ImageFetcher(this));

		getImageDownloader().setImageFadeIn(true);
		getImageDownloader().setLoadingImage(R.drawable.toutsuite);
		getImageDownloader().addImageCache(localImageCacheParams);
	}

	public ImageFetcher getImageDownloader() {
		return androlifeApplication.mImageDownloader;
	}

	public Menu getMenu() {
		return mMenu;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		for (int menuId : getInflatedMenus()) {
			inflater.inflate(menuId, menu);
		}

		mMenu = menu;

		return true;
	}

	public List<Integer> getInflatedMenus() {
		final List<Integer> toReturn = new ArrayList<Integer>();
		toReturn.add(R.menu.skeleton);
		return toReturn;
	}

	protected void onDestroy() {

		super.onDestroy();
		try {
			androlifeApplication.destroyCpt = Math.max(0, (-1 + androlifeApplication.destroyCpt));
			if (androlifeApplication.destroyCpt == 0) {
				getImageDownloader().closeCache();
				androlifeApplication.setImageDownloader(null);
			}
			androlifeApplication = null;
			preferencesManager = null;
		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_list_programmes_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		final IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(AndrolifeApplication.instance.mConnReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(AndrolifeApplication.instance.mConnReceiver);
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {

			if (androlifeApplication.pauseCpt == 0) {
				androlifeApplication.mImageDownloader.setPauseWork(false);
				androlifeApplication.mImageDownloader.setExitTasksEarly(false);
				// Picasso.with(getApplicationContext()).setDebugging(true);
			}

			androlifeApplication.pauseCpt = (1 + androlifeApplication.pauseCpt);
		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		try {

			androlifeApplication.pauseCpt = Math.max(0, (-1 + androlifeApplication.pauseCpt));
			if (this.androlifeApplication.pauseCpt == 0) {
				// Picasso.with(getApplicationContext()).getSnapshot().dump();
				androlifeApplication.mImageDownloader.setPauseWork(true);
				androlifeApplication.mImageDownloader.setExitTasksEarly(true);
				androlifeApplication.mImageDownloader.flushCache();
			}
		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
		}
	}

	@Override
	public void startActivity(Intent paramIntent) {
		super.startActivity(paramIntent);
		ActivityEffectsHelper.startActivity(androlifeApplication, this);

	}

	/**
	 * Launch an activity with {@link Intent#FLAG_ACTIVITY_CLEAR_TOP} flag
	 * 
	 * @param classToLaunch
	 *            the Class of the activity to launch
	 */
	protected void startActivityAfterCleanup(Class<?> classToLaunch) {
		final Intent newIntent = new Intent(getApplicationContext(), classToLaunch);
		newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(newIntent);
	}
}