package com.rsegismont.androlife.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.core.ui.AndrolifeFragment;
import com.rsegismont.androlife.core.utils.AndrolifeViewFactory;

public class AboutAndrolifeFragment extends AndrolifeFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.androlife_about_androlife_activity, container, false);
		final LinearLayout linearContainer = (LinearLayout) view.findViewById(R.id.androlife_about_androlife_container);

		linearContainer.addView(AboutActivity.generateAndrolifeView(getActivity(),inflater, linearContainer,
				R.drawable.androlife_about_nolife, R.string.androlife_about_nolife_title,
				R.string.androlife_about_nolife_content));

		final TextView aboutTextView = (TextView) view.findViewById(R.id.androlife_about_version);
		AndrolifeViewFactory.addVersionView(aboutTextView);
		return view;
	}
}