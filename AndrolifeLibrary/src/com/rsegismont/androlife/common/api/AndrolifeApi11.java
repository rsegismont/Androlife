package com.rsegismont.androlife.common.api;

import android.annotation.TargetApi;
import android.support.v4.app.FragmentActivity;

@TargetApi(11)
public class AndrolifeApi11 {



	public static void invalidateOptionsMenu(FragmentActivity context) {
		context.invalidateOptionsMenu();
	}

}
