package com.rsegismont.androlife.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.common.SdkUtils;

public class HomeAlertDialogFragment extends DialogFragment {
	public static final String CANCEL = "cancel";
	public static final String OK = "ok";
	public static final String TITLE = "title";
	private HomeActivity andro;
	private int title;

	private void doNegativeClick() {
		switch (this.title) {
		case R.string.alert_dialog_hd_title:
		case R.string.alert_dialog_market_title:
			this.andro.finish();
		default:
			return;
		}

	}

	private void doPositiveClick() {
		switch (this.title) {
		case R.string.alert_dialog_exit_title:
			andro.finish();
			return;
		case R.string.alert_dialog_hd_title:
		case R.string.alert_dialog_update_title:
			andro.download();
			return;
		case R.string.alert_dialog_market_title:
			startActivity(SdkUtils.prepare_web(andro,
					"https://play.google.com/store/apps/details?id=com.rsegismont.android.androLife.core"));
			this.andro.finish();
			return;
		}
	}

	public static HomeAlertDialogFragment newInstance(HomeActivity paramAndrolifeActivity, int title, int ok, int cancel) {
		HomeAlertDialogFragment homeAlertDialogFragment = new HomeAlertDialogFragment();
		Bundle localBundle = new Bundle();
		localBundle.putInt(TITLE, title);
		localBundle.putInt(OK, ok);
		localBundle.putInt(CANCEL, cancel);
		homeAlertDialogFragment.setArguments(localBundle);
		return homeAlertDialogFragment;
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setCancelable(false);
	}

	public Dialog onCreateDialog(Bundle paramBundle) {
		this.title = getArguments().getInt(TITLE);
		return new AlertDialog.Builder(getActivity()).setIcon(R.drawable.alert).setCancelable(false)
				.setTitle("Attention").setMessage(this.title)
				.setPositiveButton(getArguments().getInt(OK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
						HomeAlertDialogFragment.this.doPositiveClick();
					}
				}).setNegativeButton(getArguments().getInt(CANCEL), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
						HomeAlertDialogFragment.this.doNegativeClick();
					}
				}).create();
	}

	public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
		this.andro = ((HomeActivity) getActivity());
		return super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
	}
}
