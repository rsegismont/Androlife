/* The MIT License (MIT)
 *
 * Copyright (c) 2014 Romain Segismont
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

package com.rsegismont.androlife.home;

import java.util.Calendar;
import java.util.List;

import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.actionbarsherlock.view.MenuItem;
import com.rsegismont.androlife.R;
import com.rsegismont.androlife.common.SdkUtils;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.core.download.DownloadXmlNolifeTask;
import com.rsegismont.androlife.core.ui.SwipeActivity;
import com.rsegismont.androlife.core.utils.AndrolifeUtils;

public class HomeActivity extends SwipeActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String PREFIX_TAG_PROGRAMMES = "prefix_programmes";
	private static final String FAKE_TAG = "FAKE_TAG";
	private static String[] columnsSelected;

	static {
		columnsSelected = new String[9];
		columnsSelected[0] = SharedInformation.DatabaseColumn.TITLE.stringValue;
		columnsSelected[1] = SharedInformation.DatabaseColumn.COLOR.stringValue;
		columnsSelected[2] = SharedInformation.DatabaseColumn.SUBTITLE.stringValue;
		columnsSelected[3] = SharedInformation.DatabaseColumn.CSA.stringValue;
		columnsSelected[4] = SharedInformation.DatabaseColumn.DATE.stringValue;
		columnsSelected[5] = SharedInformation.DatabaseColumn.DATE_UTC.stringValue;
		columnsSelected[6] = SharedInformation.DatabaseColumn.SCREENSHOT.stringValue;
		columnsSelected[7] = SharedInformation.DatabaseColumn.SCREENSHOT_EMISSION.stringValue;
		columnsSelected[8] = SharedInformation.DatabaseColumn.HD.stringValue;
	}

	private UpdateTaskNeeded mUpdateTask;
	private DownloadXmlNolifeTask mDownloadTask;

	private HomeProgrammesGridFragment homeGridFragment;
	private HomeProgrammesAdapter homeGridFragmentAdapter;

	private static Handler mHandler = new Handler();
	private int currentPosition = 0;
	private Cursor mCursor;

	private Runnable mUpdate = new Runnable() {

		@Override
		public void run() {
			updateFragmentProgrammes(false);
		}
	};
	public int mHorizontalMargin, mVerticalMargin;

	private void setCurrentPosition(boolean isFirstUpdate) {
		if (this.mCursor == null) {
			return;
		}

		if (isFirstUpdate) {
			this.currentPosition = 0;
		}

		for (int i = Math.max(0, this.currentPosition);; i++) {
			if ((i >= this.mCursor.getCount()) || (!this.mCursor.moveToPosition(i))) {
				break;
			}
			long l = this.mCursor.getLong(this.mCursor
					.getColumnIndex(SharedInformation.DatabaseColumn.DATE_UTC.stringValue));
			if (System.currentTimeMillis() - l < 0L) {
				this.currentPosition = (i - 1);
				break;
			}
		}

	}

	private void setupTimer() {
		if (mCursor == null) {
			return;
		}

		mHandler.removeCallbacks(mUpdate);
		if (mCursor.moveToPosition(this.currentPosition)) {
			if (mCursor.moveToNext() == true) {
				long l = this.mCursor.getLong(mCursor
						.getColumnIndex(SharedInformation.DatabaseColumn.DATE_UTC.stringValue));
				mHandler.postDelayed(mUpdate, l - System.currentTimeMillis());
			}
		}
	}

	private void initMenuFragment() {
		FragmentTransaction menuFragmentTransaction = getSupportFragmentManager().beginTransaction();
		menuFragmentTransaction.replace(R.id.left_drawer, new HomeMenuFragment());
		menuFragmentTransaction.commit();
	}

	private void setupUi() {
		final int orientation = getResources().getConfiguration().orientation;
		final boolean isTablet = SdkUtils.isATablet(getApplicationContext());
		setContentView(R.layout.androlife_navigation);

		View leftDrawer;

		if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		if ((orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) || (!isTablet)) {
			getSupportActionBar().setSubtitle(R.string.home_menu_closed);
			leftDrawer = findViewById(R.id.left_drawer);

			if (isTablet) {
				leftDrawer.getLayoutParams().width = (1 * this.width / 2);
			} else {
				leftDrawer.getLayoutParams().width = (3 * this.width / 4);
			}

			this.mDrawerLayout = ((DrawerLayout) findViewById(R.id.drawer_layout));
			this.mDrawerLayout.setDrawerShadow(getResources().getDrawable(R.drawable.drawer_shadow), Gravity.LEFT);
			this.mDrawerToggle = new ActionBarDrawerToggle(this, this.mDrawerLayout, R.drawable.ic_drawer,
					R.string.home_menu_open, R.string.home_menu_closed);
			this.mDrawerLayout.setDrawerListener(new DrawerListener() {

				private int progressState = 0;

				@Override
				public void onDrawerStateChanged(int arg0) {

				}

				@Override
				public void onDrawerSlide(View arg0, float arg1) {
					if ((arg1 >= 0.5f) && (this.progressState == 0)) {
						this.progressState = 1;
						getSupportActionBar().setSubtitle(R.string.home_menu_open);
					} else if ((arg1 < 0.5f) && (this.progressState == 1)) {
						this.progressState = 0;
						getSupportActionBar().setSubtitle(R.string.home_menu_closed);
					}
				}

				@Override
				public void onDrawerOpened(View arg0) {
					this.progressState = 1;
				}

				@Override
				public void onDrawerClosed(View arg0) {
					this.progressState = 0;
				}
			});

		}

		mHorizontalMargin = getResources().getDimensionPixelSize(R.dimen.common_margin_horizontal);
		mVerticalMargin = getResources().getDimensionPixelSize(R.dimen.common_margin_vertical);

		initMenuFragment();

		if ((LinearLayout) findViewById(R.id.main_frag_list) != null) {
			homeGridFragment = new HomeProgrammesGridFragment();
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.main_frag_list, homeGridFragment, PREFIX_TAG_PROGRAMMES);
			transaction.commit();

			if (homeGridFragmentAdapter != null) {
				onLoadFinishedInternal();
			}
		}

	}

	private void updateFragmentProgrammes(boolean initialUpdate) {

		if (this.mCursor != null) {
			setCurrentPosition(initialUpdate);

			if (homeGridFragment.adapter == null) {
				homeGridFragmentAdapter = new HomeProgrammesAdapter(this, this.mCursor, R.layout.home_programs_item,
						this.currentPosition);
				homeGridFragment.setAdapter(homeGridFragmentAdapter);

			} else {
				homeGridFragment.adapter.changeCursor(this.mCursor, this.currentPosition);
				homeGridFragment.adapter.notifyDataSetChanged();
			}

		}

	}

	public void displayDialogFragment(int title, int ok, int cancel) {
		HomeAlertDialogFragment.newInstance(this, title, ok, cancel).show(getSupportFragmentManager(), "dialog");
	}

	public void download() {
		if (mDownloadTask != null) {
			mDownloadTask.cancel(true);
		}
		mDownloadTask = new DownloadXmlNolifeTask(this);
		mDownloadTask.execute();
	}

	public void isUpdateNeeded() {
		if (mUpdateTask != null) {
			mUpdateTask.cancel(true);
		}
		mUpdateTask = new UpdateTaskNeeded(this);
		mUpdateTask.execute();
	}

	public void onBackPressed() {
		if ((SdkUtils.isXLargeTablet(getApplicationContext())) || (SdkUtils.isTabletLandscape(getApplicationContext()))) {
			displayDialogFragment(R.string.alert_dialog_exit_title, R.string.alert_dialog_exit,
					R.string.alert_dialog_exit_cancel);
		} else {
			if (!this.mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
				displayDialogFragment(R.string.alert_dialog_exit_title, R.string.alert_dialog_exit,
						R.string.alert_dialog_exit_cancel);
			} else {
				this.mDrawerLayout.closeDrawers();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void onCreate(Bundle savedInstance) {

		super.onCreate(savedInstance);
		try {
			if (savedInstance == null) {
				getPackageManager().getPackageInfo(AndrolifeUtils.androlifeCore, 0);
				displayDialogFragment(R.string.alert_dialog_core_title, R.string.alert_dialog_core_ok,
						R.string.alert_dialog_exit);

			}

		} catch (Throwable error) {
			isUpdateNeeded();
		}

		getSupportLoaderManager().initLoader(0, null, this);
		setupUi();

	}

	public Loader<Cursor> onCreateLoader(int paramInt, Bundle paramBundle) {
		return AndrolifeUtils.getAndrolifeLoader(getApplicationContext(), 0, columnsSelected, new Calendar[0]);
	}

	@Override
	public List<Integer> getInflatedMenus() {
		final List<Integer> menus = super.getInflatedMenus();
		menus.add(R.menu.home);
		return menus;
	}

	private void onLoadFinishedInternal() {
		if (homeGridFragment != null) {
			if ((this.mCursor != null) && (!this.mCursor.isClosed()) && (this.mCursor.getCount() > 0)) {
				setupTimer();
				updateFragmentProgrammes(true);

			}
		}
	}

	public void onLoadFinished(Loader<Cursor> paramLoader, Cursor paramCursor) {

		try {
			this.mCursor = paramCursor;
			onLoadFinishedInternal();
		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
		}
	}

	public void onLoaderReset(Loader<Cursor> paramLoader) {
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(FAKE_TAG, FAKE_TAG);
	}

	public boolean onOptionsItemSelected(MenuItem menuItem) {

		switch (menuItem.getItemId()) {
		case R.id.menu_refresh:
			download();
			return true;
		case R.id.menu_exit:
			finish();
			return true;
		case R.id.menu_search:
			onSearchRequested();
			return true;
		default:
			return super.onOptionsItemSelected(menuItem);
		}

	}

	private static class UpdateTaskNeeded extends AsyncTask<Void, Void, Boolean> {

		private HomeActivity activity;

		public UpdateTaskNeeded(HomeActivity activity) {
			this.activity = activity;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			final ContentResolver localContentResolver = activity.getContentResolver();
			final Uri uri = SharedInformation.CONTENT_URI_PROGRAMMES;
			final String dateUTC = SharedInformation.DatabaseColumn.DATE_UTC.stringValue + ">=?";
			final String[] arrayOfString = new String[] { "" + System.currentTimeMillis() };

			final Cursor cursor = localContentResolver.query(uri, null, dateUTC, arrayOfString,
					SharedInformation.DatabaseColumn.DATE_UTC.stringValue + " ASC");

			if (cursor.getCount() == 0) {
				cursor.close();
				return true;
			} else {
				if (!cursor.moveToLast()) {
					cursor.close();
					return true;
				} else {
					cursor.close();
					return false;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result && (!isCancelled())) {
				activity.download();
			}
		}

	}

	public void onStart() {
		super.onStart();
		setupTimer();
	}

	public void onStop() {
		super.onStop();
		mHandler.removeCallbacks(this.mUpdate);
		// mDrawerLayout.closeDrawers();
	}
}