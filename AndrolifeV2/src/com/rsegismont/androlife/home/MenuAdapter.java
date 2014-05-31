package com.rsegismont.androlife.home;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.common.SdkUtils;

public class MenuAdapter extends BaseAdapter {

	public static class DataHolder {
		public int text;
	}

	private static class ViewHolder {
		TextView title;
	}

	private List<DataHolder> items;
	private LayoutInflater mInflater;
	private int mLayout;

	public MenuAdapter(Context context, int layoutId, List<DataHolder> paramList) {
		this.mLayout = layoutId;
		this.mInflater = LayoutInflater.from(context);
		this.items = paramList;
	}

	public int getCount() {
		return this.items.size();
	}

	public Object getItem(int paramInt) {
		return this.items.get(paramInt);
	}

	public long getItemId(int paramInt) {
		return ((DataHolder) this.items.get(paramInt)).text;
	}

	public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
		ViewHolder localViewHolder;
		if (paramView == null) {
			paramView = this.mInflater.inflate(this.mLayout, null);
			localViewHolder = new ViewHolder();
			localViewHolder.title = ((TextView) paramView.findViewById(R.id.menu_itemTitle));
			SdkUtils.setTextViewRoboto(localViewHolder.title, "Roboto-Light");
			paramView.setTag(localViewHolder);
		} else {
			localViewHolder = (ViewHolder) paramView.getTag();
		}
		localViewHolder.title.setText(((DataHolder) this.items.get(paramInt)).text);
		return paramView;

	}
}
