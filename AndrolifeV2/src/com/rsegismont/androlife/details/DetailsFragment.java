package com.rsegismont.androlife.details;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.rsegismont.androlife.R;
import com.rsegismont.androlife.activities.ProgrammeAbstract;
import com.rsegismont.androlife.application.AndrolifeApplication;
import com.rsegismont.androlife.calendar.CalendarWrapper;
import com.rsegismont.androlife.common.SdkUtils;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.common.SharedInformation.DatabaseColumn;
import com.rsegismont.androlife.core.ui.AndrolifeFragment;
import com.rsegismont.androlife.utils.AndrolifeUtils;
import com.rsegismont.androlife.utils.AndrolifeViewFactory;

public class DetailsFragment extends AndrolifeFragment implements
		LoaderCallbacks<Cursor> {
	public static final String CURSOR = "cursor";
	public static final String TIME = "time";
	private int height;
	private boolean isAvailableOnNO;
	private String mDate;
	private ImageView mImageView;
	private long mTimeEnd = -1L;
	private String noUrl = "";
	private Date startTime = null;
	private ContentValues values;
	private int width;
	private DetailsContentAdapter mAdapter;

	public SimpleDateFormat getDateFormatter() {
		return ((ProgrammeAbstract) getActivity()).detailsHeaderDateFormat;
	}

	public void setupDatas() {

		this.isAvailableOnNO = false;
		this.values = ((ContentValues) getArguments().getParcelable(CURSOR));

		try {

			final long currentTime = System.currentTimeMillis();

			final String endTimeString = values
					.getAsString(DatabaseColumn.NolifeOnlineEnd.stringValue);

			startTime = AndrolifeUtils.stringToDate(values
					.getAsString(DatabaseColumn.NolifeOnlineStart.stringValue),
					getResources().getString(R.string.format_database), true);

			Date endTime;
			if (TextUtils.isEmpty(endTimeString))
				endTime = new Date(currentTime + 1000);
			else
				endTime = AndrolifeUtils
						.stringToDate(
								values.getAsString(DatabaseColumn.NolifeOnlineEnd.stringValue),
								getResources().getString(
										R.string.format_database), true);

			if ((currentTime >= startTime.getTime())
					&& (currentTime <= endTime.getTime())) {
				this.isAvailableOnNO = true;
				this.noUrl = values
						.getAsString(DatabaseColumn.NolifeOnlineURL.stringValue);

				if (TextUtils.isEmpty(noUrl)) {
					this.noUrl = values
							.getAsString(DatabaseColumn.NolifeOnlineExternalURL.stringValue);
				}

			}

		} catch (Throwable e) {

		}

		mDate = getDateFormatter()
				.format(new Date(values
						.getAsLong(DatabaseColumn.DATE_UTC.stringValue)));

	}

	public void setupActionBarMenu(Menu menu) {
		if (isAvailableOnNO) {
			menu.findItem(R.id.detail_viewnolifeonline).setVisible(true);
		} else {
			menu.findItem(R.id.detail_viewnolifeonline).setVisible(false);
		}

		if (values.getAsString(DatabaseColumn.URL.stringValue).length() > 0) {
			menu.findItem(R.id.detail_discuss_forum).setVisible(true);
		} else {
			menu.findItem(R.id.detail_discuss_forum).setVisible(false);
		}
		if (Build.VERSION.SDK_INT > 7
				&& (System.currentTimeMillis() <= values
						.getAsLong(DatabaseColumn.DATE_UTC.stringValue))) {
			menu.findItem(R.id.detail_register_calendar).setVisible(true);
		} else {
			menu.findItem(R.id.detail_register_calendar).setVisible(false);
		}

	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			setHasOptionsMenu(true);
		}
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setupDatas();

		if (SdkUtils.isATablet(getActivity())) {
			this.width = getResources().getDimensionPixelSize(
					R.dimen.details_width);
			this.height = getResources().getDimensionPixelSize(
					R.dimen.details_height);
		} else {
			this.width = getResources().getDisplayMetrics().widthPixels;
			this.height = getResources().getDimensionPixelSize(
					R.dimen.now_height);
		}

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mImageView = null;
		values = null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.detail, menu);
		setupActionBarMenu(menu);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(500, null, this);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle paramBundle) {

		final View view = inflater.inflate(R.layout.androlife_details_fragment,
				container, false);

		final View headerView = inflater.inflate(
				R.layout.androlife_details_header, null, false);

		// Image
		mImageView = (ImageView) headerView
				.findViewById(R.id.androlife_detail_image);

		final ImageView mHdView = (ImageView) headerView
				.findViewById(R.id.androlife_detail_hd);
		final ImageView mCsaView = (ImageView) headerView
				.findViewById(R.id.androlife_detail_csa);
		final TextView mTitleView = (TextView) headerView
				.findViewById(R.id.androlife_detail_title);
		final TextView mDateView = null;

		final TextView mTypeView = (TextView) headerView
				.findViewById(R.id.androlife_detail_type);
		final ListView mListView = (ListView) view
				.findViewById(R.id.detail_content);

		SdkUtils.setTextViewRoboto(mDateView, "Roboto-Regular");

		this.mTimeEnd = getArguments().getLong(TIME);
		if (this.mTimeEnd == -1) {
			this.mTimeEnd = (SdkUtils.ONE_HOUR + this.values.getAsLong(
					SharedInformation.DatabaseColumn.DATE_UTC.stringValue)
					.longValue());
		}

		AndrolifeViewFactory.displayImage(mImageView, values, width, height);
		AndrolifeViewFactory.addCsaView(mCsaView, values
				.getAsString(SharedInformation.DatabaseColumn.CSA.stringValue));
		AndrolifeViewFactory.addHdView(mHdView, values
				.getAsString(SharedInformation.DatabaseColumn.HD.stringValue),
				true);

		final TextView mSubTitleView = (TextView) headerView
				.findViewById(R.id.androlife_detail_subtitle);

		SdkUtils.setTextViewRoboto(mSubTitleView, "Roboto-Regular");
		SdkUtils.setTextViewRoboto(mTitleView, "Roboto-Bold");

		// Type
		mTypeView
				.setText(values
						.getAsString(SharedInformation.DatabaseColumn.TYPE.stringValue));

		mTitleView
				.setText(values
						.getAsString(SharedInformation.DatabaseColumn.TITLE.stringValue));
		final String subTitle = values
				.getAsString(SharedInformation.DatabaseColumn.SUBTITLE.stringValue);
		if (mSubTitleView != null) {
			if (TextUtils.isEmpty(subTitle)) {
				mSubTitleView.setVisibility(View.GONE);
			} else {
				mSubTitleView.setText(subTitle);
			}
		}

		/**
		 * if (mDateView != null) { mDateView.setText(this.mDate); }
		 */

		mListView.addHeaderView(headerView);
		mAdapter = new DetailsContentAdapter((ProgrammeAbstract) getActivity(),
				this.values, R.layout.androlife_details_item,
				this.isAvailableOnNO, this.startTime);
		mListView.setAdapter(mAdapter);
		return mListView;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.detail_viewnolifeonline:

			try {

				startActivity(SdkUtils.prepare_web(getActivity(),
						AndrolifeUtils.getMobileUrl(noUrl)));
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return true;

		case R.id.detail_register_calendar:

			CalendarWrapper.registerCalendarEvent(getActivity(),
					values.getAsString(DatabaseColumn.DESCRIPTION.stringValue),
					"Nolife TV",
					values.getAsString(DatabaseColumn.DETAIL.stringValue),
					values.getAsLong(DatabaseColumn.DATE_UTC.stringValue),
					mTimeEnd);
			return true;

		case R.id.detail_discuss_forum:

			try {
				final Intent intent = SdkUtils.prepare_web(getActivity(),
						values.getAsString(DatabaseColumn.URL.stringValue));
				if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
					startActivity(intent);
				}
			} catch (Throwable e) {
				Log.e("android", e.getMessage(), e);
			}
			return true;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(
				getActivity(),
				SharedInformation.CONTENT_URI_PROGRAMMES,
				null,
				DatabaseColumn.DESCRIPTION.stringValue + " =? AND "
						+ DatabaseColumn.DATE_UTC.stringValue + "<>? AND "
						+ DatabaseColumn.DATE_UTC.stringValue + ">?",
				new String[] {
						values.getAsString(DatabaseColumn.DESCRIPTION.stringValue),
						String.valueOf(values
								.getAsLong(DatabaseColumn.DATE_UTC.stringValue)),
						String.valueOf(System.currentTimeMillis()) },
				SharedInformation.DatabaseColumn.DATE_UTC.stringValue + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if ((cursor != null) && (cursor.getCount() > 0)) {
			if (mAdapter != null) {
				final StringBuilder sb = new StringBuilder();
				final int cursorCount = cursor.getCount();
				for (int i = 0; i < cursorCount; i++) {
					if (cursor.moveToPosition(i)) {
						sb.append(AndrolifeApplication.instance.dateFormatter.format(cursor.getLong(cursor
								.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue))));
						if (i < cursorCount - 1) {
							sb.append("\n");
						}
					}
				}
				mAdapter.addToAdapter("Rediffusions", sb.toString());
				mAdapter.notifyDataSetChanged();
			}

		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}
}