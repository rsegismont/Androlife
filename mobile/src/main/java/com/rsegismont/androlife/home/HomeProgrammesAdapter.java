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

package com.rsegismont.androlife.home;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.common.SdkUtils;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.common.SharedInformation.DatabaseColumn;
import com.rsegismont.androlife.core.ui.SkeletonActivity;
import com.rsegismont.androlife.core.utils.AndrolifeUtils;
import com.rsegismont.androlife.core.utils.AndrolifeViewFactory;
import com.rsegismont.androlife.settings.SettingsActivity;

public class HomeProgrammesAdapter extends BaseAdapter {
	private int height;
	private LayoutInflater inflator;
	private SkeletonActivity mContext;
	private int mCurrentPosition;
	private Cursor mCursor;
	// private ImageFetcher mImageDownloader;
	private int mLayout;
	private SparseArray<ContentValues> values = new SparseArray<ContentValues>();
	private int width;

	public HomeProgrammesAdapter(SkeletonActivity activity, Cursor paramCursor, int paramInt1, int paramInt2) {
		changeCursor(paramCursor, paramInt2);
		this.mLayout = paramInt1;
		this.mContext = activity;
		this.inflator = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));

		this.width = getWidth();
		this.height = this.mContext.getResources().getDimensionPixelSize(R.dimen.now_height);
	}

	private int getWidth() {
		final int width = mContext.getResources().getDisplayMetrics().widthPixels;
		if (SdkUtils.isXLargeTablet(mContext)) {
			int diff = 0;
			if (SdkUtils.isTabletLandscape(mContext)) {
				diff = mContext.getResources().getDimensionPixelSize(R.dimen.list_width);
			}
			return ((int) (width - diff) / 2);
		} else {
			return width;
		}
	}

	public void changeCursor(Cursor paramCursor, int paramInt) {
		this.mCursor = paramCursor;
		this.mCurrentPosition = paramInt;
		this.values.clear();
	}

	public int getCount() {
		try {
			int i = Math.min(
					Math.max(this.mCursor.getCount() - this.mCurrentPosition, 0),
					Integer.valueOf(
							PreferenceManager.getDefaultSharedPreferences(this.mContext).getString(
									SettingsActivity.HOME_NUMBERS, "50")).intValue());
			return i;
		} catch (Throwable localThrowable) {
		}
		return 0;
	}

	public int getCurrentPosition() {
		return this.mCurrentPosition;
	}

	public Cursor getCursor() {
		return this.mCursor;
	}

	public long getDateForPosition(int position) {
		ContentValues contentValues = (ContentValues) this.values.get(position);
		if (contentValues == null) {
			if (this.mCursor.moveToPosition(position + this.mCurrentPosition)) {
				contentValues = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(this.mCursor, contentValues);
				this.values.put(position, contentValues);
			} else {
				return System.currentTimeMillis();
			}
		}

		return contentValues.getAsLong(DatabaseColumn.DATE_UTC.stringValue);

	}

	public Object getItem(int paramInt) {
		return Integer.valueOf(paramInt);
	}

	public long getItemId(int paramInt) {
		return paramInt;
	}

	public View getView(int position, View convertView, ViewGroup paramViewGroup) {
		View view = convertView;
		ViewHolder viewHolder = null;
		if (view == null) {
			view = this.inflator.inflate(this.mLayout, paramViewGroup, false);
			viewHolder = new ViewHolder();

			// description
			viewHolder.programme_description = ((TextView) view.findViewById(R.id.home_programme_large_description));
			SdkUtils.setTextViewRoboto(viewHolder.programme_description, "Roboto-Regular");

			viewHolder.mImageView = (ImageView) view.findViewById(R.id.home_programme_large_image);
			viewHolder.programs_color = view.findViewById(R.id.home_programs_color);

			viewHolder.mSubTitleView = ((TextView) view.findViewById(R.id.home_programme_large_subtitle));
			SdkUtils.setTextViewRoboto(viewHolder.mSubTitleView, "Roboto-Medium");

			viewHolder.mCsaView = ((ImageView) view.findViewById(R.id.home_programme_large_csa));
			
			viewHolder.mHdView = ((ImageView) view.findViewById(R.id.home_programme_large_hd));
			view.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		ContentValues contentValues = (ContentValues) this.values.get(position);
		if (contentValues == null) {
			if (this.mCursor.moveToPosition(position + this.mCurrentPosition)) {
				contentValues = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(this.mCursor, contentValues);
				this.values.put(position, contentValues);
			} else {
				return view;
			}
		}

		final int color = AndrolifeUtils.getColor(contentValues
				.getAsString(SharedInformation.DatabaseColumn.COLOR.stringValue));
		viewHolder.programs_color.setBackgroundColor(color);

		final String titleString = contentValues.getAsString(SharedInformation.DatabaseColumn.TITLE.stringValue);
		String dateString = "Maintenant";
		if (position > 0)
			dateString = contentValues.getAsString(SharedInformation.DatabaseColumn.DATE.stringValue).substring(11);
		viewHolder.programme_description.setText(dateString + " : " + titleString);

		viewHolder.mSubTitleView.setVisibility(View.VISIBLE);



		final String subtitleString = contentValues.getAsString(SharedInformation.DatabaseColumn.SUBTITLE.stringValue);

		if (subtitleString.equals("")) {
			viewHolder.mSubTitleView.setVisibility(View.GONE);
		} else {
			viewHolder.mSubTitleView.setVisibility(View.VISIBLE);
			viewHolder.mSubTitleView.setText(subtitleString);
		}

		viewHolder.mImageView.setTag(position);

		AndrolifeViewFactory.displayImage(viewHolder.mImageView, contentValues, width, height);
		AndrolifeViewFactory.addCsaView(viewHolder.mCsaView,
				contentValues.getAsString(SharedInformation.DatabaseColumn.CSA.stringValue));
		AndrolifeViewFactory.addHdView(viewHolder.mHdView,
				contentValues.getAsString(SharedInformation.DatabaseColumn.HD.stringValue),false);

		return view;

	}

	public boolean hasStableIds() {
		return true;
	}

	private static class ViewHolder {
		public ImageView mHdView;
		public ImageView mCsaView;
		public ImageView mImageView;
		public TextView mSubTitleView;
		public TextView programme_description;
		public View programs_color;
	}
}