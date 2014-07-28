package com.rsegismont.androlife.details;

import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.activities.ProgrammeAbstract;
import com.rsegismont.androlife.common.SdkUtils;
import com.rsegismont.androlife.common.SharedInformation;

public class DetailsContentAdapter extends BaseAdapter {
	private int count;
	private SparseArray<String> contentList;
	// private int flags = 0;
	private LayoutInflater inflator;
	private int mLayout;
	private SparseArray<String> titleList;

	public DetailsContentAdapter(ProgrammeAbstract paramActivity, ContentValues paramContentValues, int paramInt,
			boolean paramBoolean, Date paramDate) {
		this.mLayout = paramInt;
		this.inflator = ((LayoutInflater) paramActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE));
		this.titleList = new SparseArray<String>();
		this.contentList = new SparseArray<String>();
		this.count = 0;

		addToAdapter("DŽtails", paramContentValues.getAsString(SharedInformation.DatabaseColumn.DETAIL.stringValue));
		String str;
		if (paramDate != null) {
			str = paramActivity.detailsHeaderDateFormat.format(paramDate);
			if (paramBoolean) {
				addToAdapter("Nolife Online", "Disponible depuis le " + str);
			} else {
				addToAdapter("Nolife Online", "Sera disponible ˆ partir du " + str);
			}
		}

	}

	public void addToAdapter(String title, String content) {
		if (!content.equals("")) {
			this.titleList.append(this.count, title);
			this.contentList.append(this.count, content);
			this.count = (1 + this.count);
		}
	}

	public int getCount() {
		return this.count;
	}

	public Object getItem(int paramInt) {
		return Integer.valueOf(paramInt);
	}

	public long getItemId(int paramInt) {
		return paramInt;
	}

	public View getView(int position, View convertView, ViewGroup container) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = this.inflator.inflate(this.mLayout, container, false);
			holder = new ViewHolder();
			holder.title = ((TextView) convertView.findViewById(R.id.detail_item_title));
			holder.content = ((TextView) convertView.findViewById(R.id.detail_item_content));
			SdkUtils.setTextViewRoboto(holder.title, "Roboto-Bold");
			SdkUtils.setTextViewRoboto(holder.content, "Roboto-Regular");
			convertView.setTag(holder);
		}

		holder = (ViewHolder) convertView.getTag();

		holder.content.setText((CharSequence) this.contentList.get(position, ""));
		holder.title.setText((CharSequence) this.titleList.get(position, ""));
		return convertView;

	}

	private static class ViewHolder {
		TextView content;
		TextView title;
	}
}