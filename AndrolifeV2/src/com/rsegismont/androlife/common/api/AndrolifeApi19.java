package com.rsegismont.androlife.common.api;

import android.annotation.TargetApi;
import android.graphics.Bitmap;

@TargetApi(19)
public class AndrolifeApi19 {

	public static int getAllocationByteCount(Bitmap bitmap) {
		return bitmap.getAllocationByteCount();
	}

}
