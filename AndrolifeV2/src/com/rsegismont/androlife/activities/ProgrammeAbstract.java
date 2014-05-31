package com.rsegismont.androlife.activities;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.core.ui.SkeletonActivity;
import com.rsegismont.androlife.details.DetailsAdapter;

public abstract class ProgrammeAbstract extends SkeletonActivity {
	protected DetailsAdapter adapterDetails;
	public Cursor mCursor;
	public SimpleDateFormat detailsHeaderDateFormat;

	@Override
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		detailsHeaderDateFormat = new SimpleDateFormat(getString(R.string.date_details_header), Locale.FRANCE);
	}

	@Override
	public void supportInvalidateOptionsMenu() {
		new Handler().post(new Runnable() {

			@Override
			public void run() {
				ProgrammeAbstract.super.supportInvalidateOptionsMenu();
			}
		});

	}

	public void onDetailsSwiped(int paramInt) {
	}

}