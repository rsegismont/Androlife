package com.rsegismont.androlife.common.api;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

@TargetApi(9)
public class AndrolifeApi9 {

	public static int getSensorPortraitAttribute() {
		return ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
	}

	public static void applyPreferences(Editor editor) {
		editor.apply();
	}

	public static int getXlargeAttribute() {
		return Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	public static Intent getSettingsIntent() {
		final Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		return intent;
	}

}
