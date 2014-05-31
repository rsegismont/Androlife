package com.rsegismont.android.androLife.widgets.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;

import com.rsegismont.android.androLife.widgets.R;
import com.rsegismont.androlife.common.SdkUtils;

public class WidgetsUtils {

	public static String getDisplayDate(Context context, SimpleDateFormat format, long date) {
		switch (SdkUtils.getDiffDate(date)) {
		case -1:
			return context.getString(R.string.androlife_dates_yesterday);
		case 0:
			return context.getString(R.string.androlife_dates_today);
		case 1:
			return context.getString(R.string.androlife_dates_tommorow);

		default:
			return format.format(new Date(date));
		}
	}

}
