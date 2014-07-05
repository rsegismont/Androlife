/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.rsegismont.androlife.programlist;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;

import com.actionbarsherlock.view.MenuItem;
import com.rsegismont.androlife.R;
import com.rsegismont.androlife.common.Constantes;
import com.rsegismont.androlife.common.SdkUtils;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.common.SharedInformation.DatabaseColumn;
import com.rsegismont.androlife.core.ui.AndrolifeListFragment;
import com.rsegismont.androlife.core.utils.AndrolifeUtils;
import com.rsegismont.androlife.details.ProgrammesDetailActivity;

public class FragmentListProgrammes extends AndrolifeListFragment implements AdapterView.OnItemClickListener,
		LoaderManager.LoaderCallbacks<Cursor> {
	public static final String IS_TODAY = "is_today";
	public static final String IS_SELECTED = "IS_SELECTED";
	public static final String TIME = "time";
	public static final String SELECTED_TIME = "SELECTED_TIME";
	public static final String TYPE = "type";
	private static final String[] columnsSelected = new String[] { SharedInformation.DatabaseColumn.ID.stringValue,
			SharedInformation.DatabaseColumn.TITLE.stringValue, SharedInformation.DatabaseColumn.COLOR.stringValue,
			SharedInformation.DatabaseColumn.SUBTITLE.stringValue, SharedInformation.DatabaseColumn.DATE.stringValue,
			SharedInformation.DatabaseColumn.DATE_UTC.stringValue,
			SharedInformation.DatabaseColumn.SCREENSHOT.stringValue,
			SharedInformation.DatabaseColumn.SCREENSHOT_EMISSION.stringValue };

	private Calendar calendarEvening;
	private Calendar calendarMorning;
	private int mType;

	private void setSelection(long date) {

		int index = -1;

		final Cursor cursor = getCursor();
		final int columnDateIndex = cursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue);
		final int initialPosition = cursor.getPosition();

		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			if (date == cursor.getLong(columnDateIndex)) {
				index = i;
				break;
			}
		}
		if (initialPosition >= 0)
			cursor.moveToPosition(initialPosition);

		final int finalIndex = index;

		try {

			getListView().post(new Runnable() {

				@Override
				public void run() {
					getListView().setItemChecked(finalIndex, true);
					getListView().setSelectionFromTop(finalIndex, 0);

				}
			});
		} catch (Throwable e) {

		}

	}

	public Cursor getCursor() {
		try {
			return ((ProgramListAdapter) getListAdapter()).getCursor();
		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
			return null;
		}
	}

	public void onActivityCreated(Bundle paramBundle) {
		super.onActivityCreated(paramBundle);
		final Loader<Cursor> localLoader = getLoaderManager().getLoader(0);
		if ((localLoader != null) && (!localLoader.isReset())) {
			getLoaderManager().restartLoader(this.mType, null, this);
		} else {
			getLoaderManager().initLoader(this.mType, null, this);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 20:
			switch (resultCode) {
			case Activity.RESULT_OK:
				getLoaderManager().restartLoader(this.mType, null, this);
				break;
			default:
				break;

			}

			break;

		default:
			break;
		}

	}

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		setHasOptionsMenu(true);
		this.mType = getArguments().getInt("type");
		this.calendarMorning = Calendar.getInstance();
		this.calendarMorning.setTimeInMillis(getArguments().getLong("time"));
		this.calendarMorning.set(11, 0);
		this.calendarMorning.set(12, 0);
		this.calendarMorning.set(13, 0);
		this.calendarMorning.set(14, 0);
		this.calendarEvening = Calendar.getInstance();
		this.calendarEvening.setTimeInMillis(getArguments().getLong("time"));
		this.calendarEvening.set(11, 23);
		this.calendarEvening.set(12, 59);
		this.calendarEvening.set(13, 59);
		this.calendarEvening.set(14, 250);
	}

	public Loader<Cursor> onCreateLoader(int paramInt, Bundle paramBundle) {
		try {
			Context localContext = getActivity().getApplicationContext();
			String[] arrayOfString = columnsSelected;
			Calendar[] arrayOfCalendar = new Calendar[2];
			arrayOfCalendar[0] = this.calendarMorning;
			arrayOfCalendar[1] = this.calendarEvening;
			return AndrolifeUtils.getAndrolifeLoader(localContext, paramInt, arrayOfString, arrayOfCalendar);
		} catch (Throwable localThrowable) {
			return null;
		}
	}

	public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
		if (SdkUtils.isTabletLandscape(getActivity())) {
			((ProgramListActivity) getActivity()).setDetailsSelection(paramInt);
		} else {

			Cursor localCursor = getCursor();
			int i = localCursor.getPosition();
			localCursor.moveToPosition(paramInt);
			int j = localCursor.getColumnIndex(SharedInformation.DatabaseColumn.DATE_UTC.stringValue);
			Intent localIntent = new Intent(getActivity(), ProgrammesDetailActivity.class);
			localIntent.putExtra(Constantes.DETAIL_DATE_TIME, localCursor.getLong(j));
			localIntent.putExtra(Constantes.TYPE, this.mType);
			if (i >= 0)
				localCursor.moveToPosition(i);
			startActivity(localIntent);
		}
	}

	public void onLoadFinished(Loader<Cursor> paramLoader, Cursor paramCursor) {
		if (getListAdapter() == null) {
			setListAdapter(new ProgramListAdapter(getAndrolifeActivity(), paramCursor));
		} else {
			((ProgramListAdapter) getListAdapter()).changeCursor(paramCursor);
			((ProgramListAdapter) getListAdapter()).notifyDataSetChanged();
		}

		try {
			final boolean isToday = getArguments().getBoolean(IS_TODAY, false);
			if ((isVisible()) && (isToday)) {
				setSelection(AndrolifeUtils.getCurrentIndex(paramCursor));
			}

			final boolean isSelected = getArguments().getBoolean(IS_SELECTED, false);
			if ((isVisible()) && (isSelected)) {
				setSelection(getArguments().getLong(SELECTED_TIME));
			}
			return;

		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
		}
	}

	public void onLoaderReset(Loader<Cursor> paramLoader) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_list_programmes_now:
			if (getCursor() != null) {
				setSelection(AndrolifeUtils.getCurrentIndex(getCursor()));
			}
			return false;

		case R.id.menu_list_programmes_tonight:
			if (getCursor() != null) {
				setSelection(AndrolifeUtils.getTonightIndex(getCursor()));
			}
			return false;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onViewCreated(View paramView, Bundle paramBundle) {
		super.onViewCreated(paramView, paramBundle);
		getListView().setOnItemClickListener(this);
		if (SdkUtils.isTabletLandscape(getActivity())) {
			getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		} else {
			getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		}

		getListView().setDividerHeight(0);

		getListView().setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_FLING)
					getAndrolifeActivity().getImageDownloader().setPauseWork(true);
				else {
					getAndrolifeActivity().getImageDownloader().setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});

		getListView().setCacheColorHint(getResources().getColor(R.color.background));

	}
}
