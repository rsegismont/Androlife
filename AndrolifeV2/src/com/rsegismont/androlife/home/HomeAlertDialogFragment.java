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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.core.utils.AndrolifeUtils;

public class HomeAlertDialogFragment extends DialogFragment {
	public static final String CANCEL = "cancel";
	public static final String OK = "ok";
	public static final String TITLE = "title";
	private HomeActivity andro;
	private int title;

	private void doNegativeClick() {
		switch (this.title) {
		case R.string.alert_dialog_core_title:
			this.andro.finish();
		default:
			return;
		}

	}

	private void doPositiveClick() {
		switch (this.title) {

		case R.string.alert_dialog_core_title:
			AndrolifeUtils.openAndrolifeCoreSettings(getActivity());
			andro.finish();
			return;

		case R.string.alert_dialog_exit_title:
			andro.finish();
			return;
		case R.string.alert_dialog_update_title:
			andro.download();
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
