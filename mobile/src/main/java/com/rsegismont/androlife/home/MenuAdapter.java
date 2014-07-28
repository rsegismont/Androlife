/* The MIT License (MIT)
 *
 * Copyright (c) 2014 Romain Segismont
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
			localViewHolder.title = ((TextView) paramView
					.findViewById(R.id.menu_itemTitle));
			SdkUtils.setTextViewRoboto(localViewHolder.title, "Roboto-Light");
			paramView.setTag(localViewHolder);
		} else {
			localViewHolder = (ViewHolder) paramView.getTag();
		}
		localViewHolder.title
				.setText(((DataHolder) this.items.get(paramInt)).text);
		return paramView;

	}
}
