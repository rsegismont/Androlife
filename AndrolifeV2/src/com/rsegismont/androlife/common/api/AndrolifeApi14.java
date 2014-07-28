package com.rsegismont.androlife.common.api;

import android.annotation.TargetApi;
import android.view.View;

@TargetApi(14)
public class AndrolifeApi14 {



	public static void setFitsSystemWindows(View view,boolean value) {
		view.setFitsSystemWindows(value);
	}

}
