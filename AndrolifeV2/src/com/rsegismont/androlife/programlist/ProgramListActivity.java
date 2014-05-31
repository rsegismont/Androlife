package com.rsegismont.androlife.programlist;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.MenuItem;
import com.rsegismont.androlife.R;
import com.rsegismont.androlife.activities.ProgrammeAbstract;
import com.rsegismont.androlife.common.Constantes;
import com.rsegismont.androlife.common.SdkUtils;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.common.SharedInformation.DatabaseColumn;
import com.rsegismont.androlife.details.DetailsAdapter;
import com.rsegismont.androlife.home.HomeActivity;
import com.rsegismont.androlife.utils.AndrolifeUtils;

public class ProgramListActivity extends ProgrammeAbstract implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String AUJOURDHUI = "Aujourd'hui";

	private static final String SAVED_TAB = "SAVED_TAB";
	private static final int SAVED_NOW = -1;
	private static final int SAVED_NONE = -2;

	private static final String[] columnsPhone = new String[] { SharedInformation.DatabaseColumn.DATE.stringValue,
			SharedInformation.DatabaseColumn.DATE_UTC.stringValue };

	private static final String[] columnsTablettes = new String[] { SharedInformation.DatabaseColumn.TITLE.stringValue,
			SharedInformation.DatabaseColumn.SUBTITLE.stringValue,
			SharedInformation.DatabaseColumn.DESCRIPTION.stringValue,
			SharedInformation.DatabaseColumn.COLOR.stringValue,
			SharedInformation.DatabaseColumn.NolifeOnlineStart.stringValue,
			SharedInformation.DatabaseColumn.NolifeOnlineEnd.stringValue,
			SharedInformation.DatabaseColumn.NolifeOnlineURL.stringValue,
			SharedInformation.DatabaseColumn.NolifeOnlineShowDate.stringValue,
			SharedInformation.DatabaseColumn.NolifeOnlineExternalURL.stringValue,
			SharedInformation.DatabaseColumn.HD.stringValue, SharedInformation.DatabaseColumn.CSA.stringValue,
			SharedInformation.DatabaseColumn.COLOR.stringValue, SharedInformation.DatabaseColumn.DETAIL.stringValue,
			SharedInformation.DatabaseColumn.TYPE.stringValue, SharedInformation.DatabaseColumn.URL.stringValue,
			SharedInformation.DatabaseColumn.DATE.stringValue, SharedInformation.DatabaseColumn.DATE_UTC.stringValue,
			SharedInformation.DatabaseColumn.SCREENSHOT.stringValue,
			SharedInformation.DatabaseColumn.SCREENSHOT_EMISSION.stringValue };
	private AdapterList adapterList;
	private Calendar calendarEvening;
	private Calendar calendarMorning;
	public SparseArray<FragmentListProgrammes> currentListFragment = new SparseArray<FragmentListProgrammes>();
	public int currentTabPosition = 0;

	private boolean isNowToUpdate = false;
	private int mType;
	private ViewPager pagerDetails;
	private ViewPager pagerList;
	private AdapterTask adapterTask;
	private boolean isSelected = false;

	private void initTabs(final int newPosition) {

		switch (this.mType) {
		case Constantes.CURSOR_FULL:
			if (newPosition != SAVED_NONE) {
				setTitle(getResources().getString(R.string.program_list_title));
				getSupportActionBar().setSubtitle(getResources().getString(R.string.subtitle_all));
			}
			break;
		case Constantes.CURSOR_SELECTION:
			if (newPosition != SAVED_NONE) {
				setTitle(getResources().getString(R.string.program_list_title));
				getSupportActionBar().setSubtitle(getResources().getString(R.string.subtitle_selection));
			}
			break;
		case Constantes.CURSOR_NEWS:
			if (newPosition != SAVED_NONE) {
				setTitle(getResources().getString(R.string.program_list_title));
				getSupportActionBar().setSubtitle(getResources().getString(R.string.subtitle_news));
			}
			break;
		default:
			break;
		}

		launchAdapterTask(newPosition);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(SAVED_TAB, currentTabPosition);
	}

	private void setDetailsSelection(long date) {
		int index = -1;

		final int columnDateIndex = mCursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue);
		final int initialPosition = mCursor.getPosition();

		for (int i = 0; i < mCursor.getCount(); i++) {
			mCursor.moveToPosition(i);
			if (date == mCursor.getLong(columnDateIndex)) {
				index = i;
				break;
			}
		}
		if (initialPosition >= 0)
			mCursor.moveToPosition(initialPosition);

		setDetailsSelection(index);
	}

	public void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent) {
		super.onActivityResult(paramInt1, paramInt2, paramIntent);
		switch (paramInt1) {
		default:
			break;
		case 20:
			switch (paramInt2) {
			default:
				break;
			case Activity.RESULT_OK:
				initTabs(SAVED_NONE);
				if (SdkUtils.isTabletLandscape(getApplicationContext()))
					getSupportLoaderManager().restartLoader(this.mType, null, this);
				break;
			}
			break;
		}

	}

	@Override
	protected void onDestroy() {
		getSupportLoaderManager().destroyLoader(mType);
		super.onDestroy();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		setContentView(R.layout.list_programmes);

		pagerList = (ViewPager) findViewById(R.id.programm_list_viewpager);

		calendarMorning = Calendar.getInstance();
		calendarEvening = Calendar.getInstance();

		mType = getIntent().getIntExtra(Constantes.TYPE, Constantes.CURSOR_FULL);

		int newPosition = SAVED_NOW;
		if (savedInstanceState != null) {
			newPosition = savedInstanceState.getInt(SAVED_TAB, SAVED_NOW);
		} else {
			if (getIntent().hasExtra(Constantes.DETAIL_DATE_TIME)) {
				isSelected = true;
			} else {
				isNowToUpdate = true;
			}

		}
		initTabs(newPosition);

		if (SdkUtils.isTabletLandscape(getApplicationContext())) {
			pagerDetails = (ViewPager) findViewById(R.id.host_tab_viewpager);
			final PagerTabStrip tabTitle = (PagerTabStrip) findViewById(R.id.host_tab_title);
			if (tabTitle != null) {
				tabTitle.setTabIndicatorColor(getResources().getColor(R.color.androlife_programs_hint_underline));
				tabTitle.setTextColor(getResources().getColor(R.color.androlife_programs_hint_text));
			}

			getSupportLoaderManager().initLoader(mType, null, this);

		}

	}

	public Loader<Cursor> onCreateLoader(int paramInt, Bundle paramBundle) {

		Context localContext = getApplicationContext();
		String[] arrayOfString = columnsTablettes;
		Calendar[] arrayOfCalendar = new Calendar[2];
		arrayOfCalendar[0] = this.calendarMorning;
		arrayOfCalendar[1] = this.calendarEvening;
		return AndrolifeUtils.getAndrolifeLoader(localContext, paramInt, arrayOfString, arrayOfCalendar);

	}

	public void onDetailsSwiped(int position) {
		if (this.currentListFragment.get(this.currentTabPosition) != null) {
			try {
				final ListView listViewProgrammesFragment = ((FragmentListProgrammes) currentListFragment
						.get(currentTabPosition)).getListView();
				int mid = (int) (listViewProgrammesFragment.getLastVisiblePosition() - listViewProgrammesFragment
						.getFirstVisiblePosition()) / 2;
				listViewProgrammesFragment.setItemChecked(position, true);
				listViewProgrammesFragment.setSelection(Math.max(0, position - mid));

			} catch (Throwable localThrowable) {

			}
		}
	}

	public void onLoadFinished(Loader<Cursor> paramLoader, Cursor cursor) {
		if (cursor == null)
			return;

		this.mCursor = cursor;

		if (SdkUtils.isTabletLandscape(this)) {

			if (adapterDetails == null) {
				adapterDetails = new DetailsAdapter(this);
				pagerDetails.setOnPageChangeListener(adapterDetails);
			} else {
				adapterDetails.clear();
				adapterDetails.notifyDataSetChanged();
			}
			pagerDetails.setAdapter(adapterDetails);

			if (isNowToUpdate) {
				isNowToUpdate = false;
				setDetailsSelection(AndrolifeUtils.getCurrentIndex(mCursor));
			}

			try {
				if (isSelected && (adapterList.selectedIndex == getSupportActionBar().getSelectedNavigationIndex())) {
					isSelected = false;

					setDetailsSelection(getIntent().getExtras().getLong(Constantes.DETAIL_DATE_TIME));

				}
			} catch (Throwable e) {

			}
		}

	}

	public void onLoaderReset(Loader<Cursor> paramLoader) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case R.id.menu_list_programmes_now:
			if (setListNow()) {
				if (SdkUtils.isTabletLandscape(getApplicationContext())) {
					setDetailsSelection(AndrolifeUtils.getCurrentIndex(mCursor));
				}
			}
			isNowToUpdate = true;
			return false;

		case R.id.menu_list_programmes_tonight:

			if (SdkUtils.isTabletLandscape(getApplicationContext()))
				setDetailsSelection(AndrolifeUtils.getTonightIndex(mCursor));
			return false;

		case android.R.id.home:
			startActivityAfterCleanup(HomeActivity.class);
			return true;
		}
		return (super.onOptionsItemSelected(menuItem));
	}

	public void setDetailsSelection(int paramInt) {
		if (this.pagerDetails != null) {
			this.adapterDetails.ignoreSelected = true;
			this.pagerDetails.setCurrentItem(paramInt, false);
		}
	}

	public boolean setListNow() {
		int i = getSupportActionBar().getSelectedNavigationIndex();
		for (int j = 0; j < adapterList.titlesDisplayed.size(); j++) {

			if (((String) this.adapterList.titlesDisplayed.get(j)).equals(AUJOURDHUI)) {
				getSupportActionBar().setSelectedNavigationItem(j);
				if (i == j) {
					this.pagerList.requestFocus();
					return true;
				}
			}
		}

		return false;
	}

	private void launchAdapterTask(int newSelection) {
		if (adapterTask != null) {
			adapterTask.cancel(true);
		}
		adapterTask = new AdapterTask(this, newSelection);
		adapterTask.execute();
	}

	private static class AdapterTask extends AsyncTask<Void, Void, Cursor> {

		private WeakReference<ProgramListActivity> listActivity;
		private int newPosition;

		public AdapterTask(ProgramListActivity listActivity, int newPosition) {
			this.listActivity = new WeakReference<ProgramListActivity>(listActivity);
			this.newPosition = newPosition;
		}

		@Override
		protected Cursor doInBackground(Void... params) {
			final ProgramListActivity activity = listActivity.get();
			if (activity != null) {
				return AndrolifeUtils.getAndrolifeCursor(SharedInformation.CONTENT_URI_PROGRAMMES,
						activity.getApplicationContext(), activity.mType, columnsPhone, new Calendar[0]);
			}
			return null;

		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			if (result != null && (!isCancelled())) {
				final ProgramListActivity activity = listActivity.get();
				if (activity != null) {
					activity.adapterList = new ProgramListActivity.AdapterList(activity, result);
					activity.pagerList.setOnPageChangeListener(activity.adapterList);
					activity.pagerList.setAdapter(activity.adapterList);

					if (activity.adapterList.selectedIndex != -1) {
						newPosition = activity.adapterList.selectedIndex;
					}

					if (newPosition == SAVED_NOW) {
						activity.setListNow();
					} else if (newPosition >= 0) {
						activity.getSupportActionBar().setSelectedNavigationItem(newPosition);
					}
				}
			}

		}

	}

	private static class AdapterList extends FragmentStatePagerAdapter implements ActionBar.TabListener,
			ViewPager.OnPageChangeListener {
		public List<Long> titles = new LinkedList<Long>();
		public List<String> titlesDisplayed = new LinkedList<String>();
		private WeakReference<ProgramListActivity> pgList;
		private int selectedIndex = -1;
		private long selectedTime = -1;

		public AdapterList(ProgramListActivity pgList, Cursor cursor) {
			super(pgList.getSupportFragmentManager());

			final SimpleDateFormat dateFormat = new SimpleDateFormat(pgList.getString(R.string.androlife_time_tabs),
					Locale.getDefault());

			this.pgList = new WeakReference<ProgramListActivity>(pgList);
			if (pgList.getIntent().hasExtra(Constantes.DETAIL_DATE_TIME)) {
				selectedTime = pgList.getIntent().getExtras().getLong(Constantes.DETAIL_DATE_TIME);
			}

			String previous = "";
			for (int i = 0; i < cursor.getCount(); i++) {

				cursor.moveToPosition(i);
				final long time = cursor.getLong(cursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue));
				final String date = cursor.getString(cursor.getColumnIndex(DatabaseColumn.DATE.stringValue)).substring(
						0, 10);

				if (!previous.equals(date)) {
					previous = date;

					titles.add(time);
					if (AndrolifeUtils.isToday(time))
						titlesDisplayed.add(AUJOURDHUI);
					else
						titlesDisplayed.add(dateFormat.format(new Date(time)));

					final Tab tab = pgList.getSupportActionBar().newTab()
							.setText(titlesDisplayed.get(titlesDisplayed.size() - 1)).setTabListener(this);
					pgList.getSupportActionBar().addTab(tab);

				}

				if (selectedTime != -1) {
					if (time == selectedTime) {
						selectedIndex = titlesDisplayed.size() - 1;
					}
				}
			}

		}

		public void destroyItem(ViewGroup paramViewGroup, int paramInt, Object paramObject) {
			super.destroyItem(paramViewGroup, paramInt, paramObject);
			final ProgramListActivity activity = pgList.get();
			if (activity != null) {
				activity.currentListFragment.remove(paramInt);
			}
		}

		public int getCount() {
			return this.titles.size();
		}

		public Fragment getItem(int position) {

			FragmentListProgrammes listProgrammesFragment = new FragmentListProgrammes();
			final ProgramListActivity activity = pgList.get();
			if (activity != null) {

				Bundle bundle = new Bundle();
				bundle.putInt(FragmentListProgrammes.TYPE, activity.mType);
				bundle.putLong(FragmentListProgrammes.TIME, ((Long) titles.get(position)).longValue());
				if (selectedIndex != -1) {
					bundle.putBoolean(FragmentListProgrammes.IS_SELECTED, selectedIndex == position);
					bundle.putLong(FragmentListProgrammes.SELECTED_TIME, selectedTime);
				} else {
					bundle.putBoolean(FragmentListProgrammes.IS_TODAY,
							((String) titlesDisplayed.get(position)).equals(AUJOURDHUI));
				}

				listProgrammesFragment.setArguments(bundle);
				activity.currentListFragment.put(position, listProgrammesFragment);
			}
			return listProgrammesFragment;
		}

		public void onPageScrollStateChanged(int paramInt) {
		}

		public void onPageScrolled(int paramInt1, float paramFloat, int paramInt2) {
		}

		public void onPageSelected(int paramInt) {
			final ProgramListActivity activity = pgList.get();
			if (activity != null) {
				activity.getSupportActionBar().setSelectedNavigationItem(paramInt);
			}
		}

		public void onTabReselected(ActionBar.Tab paramTab, FragmentTransaction paramFragmentTransaction) {
		}

		public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
			final ProgramListActivity activity = pgList.get();
			if (activity != null) {
				activity.currentTabPosition = tab.getPosition();

				activity.pagerList.setCurrentItem(tab.getPosition(), false);

				activity.calendarMorning.setTimeInMillis(titles.get(tab.getPosition()));
				activity.calendarMorning.set(Calendar.HOUR_OF_DAY, 0);
				activity.calendarMorning.set(Calendar.MINUTE, 0);
				activity.calendarMorning.set(Calendar.SECOND, 0);
				activity.calendarMorning.set(Calendar.MILLISECOND, 0);

				activity.calendarEvening.setTimeInMillis(titles.get(tab.getPosition()));
				activity.calendarEvening.set(Calendar.HOUR_OF_DAY, 23);
				activity.calendarEvening.set(Calendar.MINUTE, 59);
				activity.calendarEvening.set(Calendar.SECOND, 59);
				activity.calendarEvening.set(Calendar.MILLISECOND, 250);
				if (SdkUtils.isTabletLandscape(activity.getApplicationContext())) {
					activity.getSupportLoaderManager().restartLoader(activity.mType, null, activity);
				}
			}
		}

		public void onTabUnselected(ActionBar.Tab paramTab, FragmentTransaction paramFragmentTransaction) {
		}
	}

	@Override
	public List<Integer> getInflatedMenus() {
		final List<Integer> toReturn = super.getInflatedMenus();
		toReturn.add(R.menu.programm_list);
		return toReturn;
	}

}