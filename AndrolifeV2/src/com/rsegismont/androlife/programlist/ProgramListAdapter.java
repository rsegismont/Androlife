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
package com.rsegismont.androlife.programlist;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.common.SdkUtils;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.core.ui.SkeletonActivity;
import com.rsegismont.androlife.utils.AndrolifeUtils;
import com.rsegismont.androlife.utils.AndrolifeViewFactory;

/**
 * Adapter of Program list
 * 
 * @author Romain Segismont
 * @version 1.0
 * @since 1.0
 * 
 */
public class ProgramListAdapter extends CursorAdapter {

	private int height;
	private LayoutInflater inflator;
	private SkeletonActivity mContext;
	private int mLayout;
	private int width;

	/**
	 * Constructor of {@link ProgramListAdapter}
	 * 
	 * @param context
	 *            {@link Context} of the current {@link SkeletonActivity}
	 * @param cursor
	 *            {@link Cursor} that will provide all the data to display
	 */
	public ProgramListAdapter(SkeletonActivity context, Cursor cursor) {
		super(context, cursor, 0);
		mCursor = cursor;

		inflator = ((LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE));
		mLayout = R.layout.list_programmes_item;

		mContext = context;
		width = mContext.getResources().getDimensionPixelSize(R.dimen.list_image_width);
		height = mContext.getResources().getDimensionPixelSize(R.dimen.list_image_height);
	}

	@Override
	public void bindView(View convertView, Context paramContext, Cursor paramCursor) {
		ViewHolder holder = (ViewHolder) convertView.getTag();
		if (holder == null) {
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.list_programmes_image);
			holder.colorImage = (ImageView) convertView.findViewById(R.id.list_programmes_color);
			holder.content = (TextView) convertView.findViewById(R.id.list_programmes_data);
			holder.content2 = (TextView) convertView.findViewById(R.id.list_programmes_data2);
			SdkUtils.setTextViewRoboto(holder.content, "Roboto-BoldCondensed");
			convertView.setTag(holder);
		}

		if (mCursor != null) {

			final int color = AndrolifeUtils.getColor(this.mCursor.getString(this.mCursor
					.getColumnIndex(SharedInformation.DatabaseColumn.COLOR.stringValue)));
			holder.colorImage.setBackgroundColor(color);

			final String date = this.mCursor.getString(this.mCursor
					.getColumnIndex(SharedInformation.DatabaseColumn.DATE.stringValue));
			final String title = this.mCursor.getString(this.mCursor
					.getColumnIndex(SharedInformation.DatabaseColumn.TITLE.stringValue));

			holder.content.setText(date.substring(11) + " : " + title);
			holder.content2.setText(this.mCursor.getString(this.mCursor
					.getColumnIndex(SharedInformation.DatabaseColumn.SUBTITLE.stringValue)));

			AndrolifeViewFactory.displayImage( holder.image, mCursor, width, height, title);
		}

	}

	@Override
	public int getCount() {
		try {
			return Math.max(this.mCursor.getCount(), 0);
		} catch (Throwable error) {
			error.printStackTrace();
			return 0;
		}
	}

	@Override
	public Cursor getCursor() {
		return this.mCursor;
	}

	@Override
	public Object getItem(int paramInt) {
		return Integer.valueOf(paramInt);
	}

	@Override
	public long getItemId(int paramInt) {
		return paramInt;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View newView(Context paramContext, Cursor paramCursor, ViewGroup paramViewGroup) {
		return inflator.inflate(mLayout, paramViewGroup, false);
	}

	/**
	 * View Holder for {@link ProgramListAdapter} item
	 * 
	 * @author Romain Segismont
	 * @version 1.0
	 * @since 1.0
	 * 
	 */
	private static class ViewHolder {
		ImageView colorImage;
		TextView content;
		TextView content2;
		ImageView image;
	}
}