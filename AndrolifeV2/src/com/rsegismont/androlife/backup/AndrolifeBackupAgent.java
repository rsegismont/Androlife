package com.rsegismont.androlife.backup;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class AndrolifeBackupAgent extends BackupAgentHelper {

	// A key to uniquely identify the set of backup data
	static final String PREFS_BACKUP_KEY = "com.rsegismont.androlife.backup_keys";

	@Override
	public void onCreate() {
		Log.e("DEBUG", "onCreate =" + (getPackageName() + "_preferences"));
		final SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(getApplicationContext(),
				getPackageName() + "_preferences");
		addHelper(PREFS_BACKUP_KEY, helper);
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		Log.e("DEBUG", "restore =" + appVersionCode + ":");
		super.onRestore(data, appVersionCode, newState);
	}
}
