package com.rsegismont.androlife.common.api;

import android.annotation.TargetApi;
import android.view.View;

@TargetApi(16)
public class AndrolifeApi16 {

	public static void postInvalidateOnAnimation(View view) {
		view.postInvalidateOnAnimation();
	}

}
