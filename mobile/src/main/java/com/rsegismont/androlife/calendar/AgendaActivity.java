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

package com.rsegismont.androlife.calendar;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.rsegismont.androlife.R;
import com.rsegismont.androlife.core.ui.SkeletonActivity;
import com.rsegismont.androlife.home.HomeActivity;

/**
 * Class that will display a confirmation screen on devices before ICS ( because we're using private apis to insert
 * agenda events before ICS )
 * 
 * @author Romain Segismont
 * @version 1.0
 * @since 1.0
 * 
 */
public class AgendaActivity extends SkeletonActivity implements View.OnClickListener {

	private String description;
	private String location;
	private Long startTime;
	private Long endTime;
	private String title;

	/**
	 * Insert current values into user agenda
	 * 
	 */
	public void insert() {
		CalendarCompat.registerCalendarEvent(this, title, location, description, startTime, endTime);
		finish();
	}

	public void onClick(View paramView) {
		switch (paramView.getId()) {
		default:
		case R.id.agenda_cancel:
			finish();
			break;
		case R.id.agenda_ok:
			insert();
			break;
		}

	}

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.agenda_activity);

		getSupportActionBar().setSubtitle(R.string.androlife_agenda_subtitle);
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);

		this.title = getIntent().getStringExtra(CalendarCompat.TITLE);
		this.description = getIntent().getStringExtra(CalendarCompat.DESCRIPTION);
		this.location = getIntent().getStringExtra(CalendarCompat.LOCATION);
		this.startTime = Long.valueOf(getIntent().getLongExtra(CalendarCompat.START_TIME, 0));
		this.endTime = Long.valueOf(getIntent().getLongExtra(CalendarCompat.END_TIME, 0));

		int flags = 0;
		flags = (DateUtils.FORMAT_SHOW_TIME | flags);
		final String eventPeriod = String.format(getResources().getString(R.string.androlife_agenda_date),
				DateUtils.formatDateTime(this, startTime.longValue(), flags),
				DateUtils.formatDateTime(this, endTime.longValue(), flags));

		final ListView mListView = (ListView) findViewById(R.id.agenda_list);
		mListView.setCacheColorHint(Color.WHITE);

		final String[] titleList = getResources().getStringArray(R.array.androlife_agenda_title_list);
		final List<String> contentList = new LinkedList<String>();

		addToContentList(contentList,title);
		addToContentList(contentList,eventPeriod);
		addToContentList(contentList,description);
		addToContentList(contentList,location);

		final AgendaAdapter adapter = new AgendaAdapter(this, titleList, contentList);
		mListView.setAdapter(adapter);

		findViewById(R.id.agenda_cancel).setOnClickListener(this);
		findViewById(R.id.agenda_ok).setOnClickListener(this);

	}
	
	/** Add a value to contentList if not empty
	 * 
	 * @param contentList
	 * @param value
	 */
	public void addToContentList(List<String> contentList,String value){
		if(!TextUtils.isEmpty(value)){
			contentList.add(value);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
		switch (paramMenuItem.getItemId()) {
		case android.R.id.home:
			startActivityAfterCleanup(HomeActivity.class);
			return true;
		default:
			return super.onOptionsItemSelected(paramMenuItem);
		}
	}

	/**
	 * Holder of the agenda list
	 * 
	 * @author Romain Segismont
	 * @version 1.0
	 * @since 1.0
	 * 
	 */
	public static class AgendaHolder {
		TextView title;
		TextView content;
	}

	/**
	 * Adapter for the listview
	 * 
	 * @author Romain Segismont
	 * @version 1.0
	 * @since 1.0
	 * 
	 */
	public static class AgendaAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private String[] titleList;
		private List<String> contentList;

		public AgendaAdapter(Context context, String[] title, List<String> content) {
			this.titleList = title;
			this.contentList = content;
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			if (contentList != null) {
				return contentList.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int arg0) {
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View view, ViewGroup container) {
			AgendaHolder holder;
			if (view == null) {
				holder = new AgendaHolder();
				view = mInflater.inflate(R.layout.agenda_item, container, false);
				holder.title = (TextView) view.findViewById(R.id.androlife_agenda_title);
				holder.content = (TextView) view.findViewById(R.id.androlife_agenda_content);
				view.setTag(holder);
			} else {
				holder = (AgendaHolder) view.getTag();
			}

			holder.title.setText(titleList[position]);
			holder.content.setText(contentList.get(position));

			return view;
		}

	}
}