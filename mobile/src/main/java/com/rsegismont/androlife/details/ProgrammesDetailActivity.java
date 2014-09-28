package com.rsegismont.androlife.details;

import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.rsegismont.androlife.R;
import com.rsegismont.androlife.activities.ProgrammeAbstract;
import com.rsegismont.androlife.common.Constantes;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.common.SharedInformation.DatabaseColumn;
import com.rsegismont.androlife.common.api.AndrolifeApi9;
import com.rsegismont.androlife.common.utils.Utils;
import com.rsegismont.androlife.core.utils.AndrolifeUtils;
import com.rsegismont.androlife.home.HomeActivity;

import java.util.Calendar;

public class ProgrammesDetailActivity extends ProgrammeAbstract implements LoaderManager.LoaderCallbacks<Cursor> {


	@Override
	public int getActivityOrientation() {
		if(Utils.hasGingerbread()){
			return AndrolifeApi9.getSensorPortraitAttribute();
		}
		return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	}

	public static final String commonDate = SharedInformation.DatabaseColumn.DATE_UTC.stringValue + " BETWEEN ? AND ?";
	private ViewPager myPager;
	public PagerTabStrip tabTitle;

	public boolean firstInit = true;

	private int getCurrentIndex() {
		if (this.mCursor == null) {
			return -1;
		}
		if (this.mCursor.getCount() <= 0) {
			return -1;
		}

		for (int position = Math.max(0, mCursor.getPosition()); position < mCursor.getCount(); position++) {

			if (mCursor.moveToPosition(position)) {
				long dateUtc = this.mCursor.getLong(this.mCursor
						.getColumnIndex(SharedInformation.DatabaseColumn.DATE_UTC.stringValue));
				if (System.currentTimeMillis() - dateUtc >= 0) {
					return position;
				}
			} else {
				break;
			}
		}

		return -1;
	}

	private int getTonightIndex() {
		if (this.mCursor == null) {
			return -1;
		}
		if (this.mCursor.getCount() <= 0) {
			return -1;
		}

		final long currentDateUtc = this.mCursor.getLong(this.mCursor
				.getColumnIndex(SharedInformation.DatabaseColumn.DATE_UTC.stringValue));
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(currentDateUtc);
		calendar.set(Calendar.HOUR_OF_DAY, 19);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		for (int position = Math.max(0, mCursor.getPosition()); position < mCursor.getCount(); position++) {

			if (mCursor.moveToPosition(position)) {
				long candidateDateUtc = this.mCursor.getLong(this.mCursor
						.getColumnIndex(SharedInformation.DatabaseColumn.DATE_UTC.stringValue));
				if (calendar.getTime().getTime() - candidateDateUtc >= 0L) {
					return position;
				}
			} else {
				break;
			}
		}

		return -1;

	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		final int type = getIntent().getIntExtra(Constantes.TYPE, Constantes.CURSOR_FULL);
		getSupportLoaderManager().initLoader(type, null, this);
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);

		setContentView(R.layout.host_activity);
		setTitle(getResources().getString(R.string.app_name));

		switch (type) {

		case Constantes.CURSOR_FULL:
			getSupportActionBar().setSubtitle(getResources().getString(R.string.subtitle_all));
			break;
		case Constantes.CURSOR_QUERY:
			getSupportActionBar().setSubtitle(getResources().getString(R.string.subtitle_query));
			break;
		case Constantes.CURSOR_NEWS:
			getSupportActionBar().setSubtitle(getResources().getString(R.string.subtitle_news));
			break;
		case Constantes.CURSOR_SELECTION:
			getSupportActionBar().setSubtitle(getResources().getString(R.string.subtitle_selection));
			break;
		default:
			break;
		}

		myPager = (ViewPager) findViewById(R.id.host_tab_viewpager);
		tabTitle = (PagerTabStrip) findViewById(R.id.host_tab_title);
		if (tabTitle != null) {
			this.tabTitle.setTabIndicatorColor(getResources().getColor(R.color.androlife_programs_hint_underline));
			this.tabTitle.setTextColor(getResources().getColor(R.color.androlife_programs_hint_text));
		}

	}

	public Loader<Cursor> onCreateLoader(int paramInt, Bundle paramBundle) {

		Calendar morningCalendar = Calendar.getInstance();
		morningCalendar.setTimeInMillis(getIntent().getLongExtra("DETAIL_DATE_INDEX", 0L));
		morningCalendar.add(Calendar.HOUR_OF_DAY, -24);
		Calendar eveningCalendar = Calendar.getInstance();
		eveningCalendar.setTimeInMillis(getIntent().getLongExtra("DETAIL_DATE_INDEX", 0L));
		eveningCalendar.add(Calendar.HOUR_OF_DAY, 24);
		switch (paramInt) {
		default:
			return AndrolifeUtils.getAndrolifeLoader(getApplicationContext(), paramInt, null, new Calendar[] {
					morningCalendar, eveningCalendar });

		case Constantes.CURSOR_QUERY:

			return new CursorLoader(this, SharedInformation.CONTENT_URI_PROGRAMMES, null,
					SharedInformation.DatabaseColumn.DESCRIPTION.stringValue + " LIKE ?", new String[] { "%"
							+ getIntent().getExtras().getString("DETAIL_QUERY") + "%" },
					SharedInformation.DatabaseColumn.DATE_UTC.stringValue + " ASC");
		case Constantes.CURSOR_QUERY_VIEW:
			String str = SharedInformation.DatabaseColumn.DATE_UTC.stringValue + " BETWEEN ? AND ?";
			String[] arrayOfString = new String[2];
			arrayOfString[0] = "" + morningCalendar.getTime().getTime();
			arrayOfString[1] = "" + eveningCalendar.getTime().getTime();
			return new CursorLoader(this, SharedInformation.CONTENT_URI_PROGRAMMES, null, str, arrayOfString,
					SharedInformation.DatabaseColumn.DATE_UTC.stringValue + " ASC");
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	}

	public void onLoadFinished(Loader<Cursor> paramLoader, Cursor cursor) {

		this.mCursor = cursor;
		adapterDetails = new DetailsAdapter(this);
		myPager.setOnPageChangeListener(adapterDetails);
		myPager.setAdapter(adapterDetails);

		final String intentDate = getIntent().getDataString();
		if (intentDate == null) {
			final long initialTime = getIntent().getLongExtra(Constantes.DETAIL_DATE_TIME, 0);
			setPagerSelection(initialTime);
		} else {
			setPagerSelection(Long.valueOf(intentDate));

		}

	}

	public void onLoaderReset(Loader<Cursor> paramLoader) {
	}

	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case R.id.menu_list_programmes_now:
			setPagerSelection(getCurrentIndex());
			return true;
		case R.id.menu_list_programmes_tonight:
			setPagerSelection(getTonightIndex());
			return true;
		case android.R.id.home:
			// ProjectsActivity is my 'home' activity
			startActivityAfterCleanup(HomeActivity.class);
			return true;
		}
		return (super.onOptionsItemSelected(menuItem));
	}

	public void setPagerSelection(long initialTime) {
		final int initialPosition = mCursor.getPosition();
		for (int i = 0; i < mCursor.getCount(); i++) {
			mCursor.moveToPosition(i);
			final long value = mCursor.getLong(mCursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue));
			final int index = i;
			if (initialTime == value) {

				if (myPager.getCurrentItem() == index) {
					if (firstInit == true) {
						firstInit = false;
						adapterDetails.onPageSelected(index);
					}
				} else {
					myPager.setCurrentItem(index, false);
				}

				break;
			}

		}
		if (initialPosition >= 0) {
			mCursor.moveToPosition(initialPosition);
		}
	}
}