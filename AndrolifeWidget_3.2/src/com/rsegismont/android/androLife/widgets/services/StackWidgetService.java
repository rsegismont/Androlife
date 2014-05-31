/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.rsegismont.android.androLife.widgets.services;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.rsegismont.android.androLife.widgets.R;
import com.rsegismont.android.androLife.widgets.utils.WidgetsUtils;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.common.SharedInformation.DatabaseColumn;
import com.squareup.picasso.Picasso;

public class StackWidgetService extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
	}

	private class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
		private static final int mCount = 10;

		private Context mContext;
		private int mAppWidgetId;
		private Cursor mCursor;
		private RemoteTarget target;

		private SimpleDateFormat dateFormatter, timeFormatter;

		public StackRemoteViewsFactory(Context context, Intent intent) {
			mContext = context;
			mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			dateFormatter = new SimpleDateFormat(getString(R.string.androlife_date_formater), Locale.FRANCE);
			timeFormatter = new SimpleDateFormat(getString(R.string.androlife_time_formater), Locale.FRANCE);
		}

		public void onCreate() {
			// In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
			// for example downloading or creating content etc, should be deferred to onDataSetChanged()
			// or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.

			Log.e("DEBUG", "onCreate=");

			// We sleep for 3 seconds here to show how the empty view appears in the interim.
			// The empty view is set in the StackWidgetProvider and should be a sibling of the
			// collection view.
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void onDestroy() {

		}

		public int getCount() {
			return mCount;
		}

		public RemoteViews makeFakeImage(RemoteViews rv) {
			rv.setImageViewBitmap(R.id.widget_image,
					BitmapFactory.decodeResource(mContext.getResources(), R.drawable.toutsuite));
			rv.setTextViewText(R.id.widget_itemDate, "Aucune ");
			rv.setTextViewText(R.id.widget_itemTitle, "Sans titre");
			return rv;
		}

		public RemoteViews getViewAt(int position) {
			// position will always range from 0 to getCount() - 1.

			// We construct a remote views item based on our widget item xml file, and set the
			// text based on the position.
			final RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_item);

			if (mCursor != null) {
				if (!mCursor.moveToPosition(position)) {
					return makeFakeImage(rv);
				}
			} else {
				return makeFakeImage(rv);
			}

			target = new RemoteTarget(rv);

			final String title = mCursor.getString(mCursor.getColumnIndex(DatabaseColumn.DESCRIPTION.stringValue));

			final long date = mCursor.getLong(mCursor
					.getColumnIndex(SharedInformation.DatabaseColumn.DATE_UTC.stringValue));

			final StringBuilder sb = new StringBuilder();
			sb.append(WidgetsUtils.getDisplayDate(mContext, dateFormatter, date));
			sb.append(" ");
			sb.append(timeFormatter.format(date));

			rv.setTextViewText(R.id.widget_itemDate, sb.toString());
			rv.setTextViewText(R.id.widget_itemTitle, title);

			final String imgUrl = mCursor.getString(mCursor.getColumnIndex(DatabaseColumn.SCREENSHOT.stringValue));

			final String imgUrlAdd = mCursor.getString(mCursor
					.getColumnIndex(DatabaseColumn.SCREENSHOT_EMISSION.stringValue));

			if (!TextUtils.isEmpty(imgUrlAdd)) {
				Picasso.with(mContext).load(imgUrlAdd).error(R.drawable.toutsuite).into(target);
				// rv.setImageViewBitmap(R.id.widget_image, AndrolifeUtils.loadImage(
				// mContext, imgUrlAdd, R.drawable.toutsuite));
			} else {
				Picasso.with(mContext).load(imgUrl).error(R.drawable.toutsuite).into(target);
			}

			// Next, we set a fill-intent which will be used to fill-in the pending intent template
			// which is set on the collection view in StackWidgetProvider.
			Bundle extras = new Bundle();
			extras.putInt(StackWidgetProvider.EXTRA_ITEM, position);
			Intent fillInIntent = new Intent();
			fillInIntent.putExtras(extras);
			rv.setOnClickFillInIntent(R.id.widget_intent, fillInIntent);

			// You can do heaving lifting in here, synchronously. For example, if you need to
			// process an image, fetch something from the network, etc., it is ok to do it here,
			// synchronously. A loading view will show up in lieu of the actual contents in the
			// interim.
			try {
				System.out.println("Loading view " + position);
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Return the remote views object.
			return rv;
		}

		public RemoteViews getLoadingView() {
			// You can create a custom loading view (for instance when getViewAt() is slow.) If you
			// return null here, you will get the default loading view.
			return null;
		}

		public int getViewTypeCount() {
			return 1;
		}

		public long getItemId(int position) {
			return position;
		}

		public boolean hasStableIds() {
			return true;
		}

		public void createCursor() {

			try {

				if (mCursor != null) {
					mCursor.close();
					mCursor = null;
				}

				String date = "";

				final Cursor tmpCursor = mContext.getContentResolver().query(SharedInformation.CONTENT_URI_PROGRAMMES,
						null, DatabaseColumn.DATE_UTC.stringValue + "<= ?",
						new String[] { "" + System.currentTimeMillis() }, DatabaseColumn.DATE.stringValue + " ASC");

				Log.e("DEBUG", "tmpCursor=" + tmpCursor.getCount());

				if (tmpCursor.getCount() > 0) {
					tmpCursor.moveToLast();
					date = tmpCursor.getString(tmpCursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue));

				} else
					date = "" + System.currentTimeMillis();

				tmpCursor.close();

				mCursor = mContext.getContentResolver().query(SharedInformation.CONTENT_URI_PROGRAMMES, null,
						DatabaseColumn.DATE_UTC.stringValue + ">=?", new String[] { date },
						DatabaseColumn.DATE.stringValue + " ASC");
			} catch (Throwable e) {

			}

		}

		public void onDataSetChanged() {

			Log.w("androlife", "datasetchanged");

			createCursor();
			// This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
			// on the collection view corresponding to this factory. You can do heaving lifting in
			// here, synchronously. For example, if you need to process an image, fetch something
			// from the network, etc., it is ok to do it here, synchronously. The widget will remain
			// in its current state while work is being done here, so you don't need to worry about
			// locking up the widget.
		}
	}
}
