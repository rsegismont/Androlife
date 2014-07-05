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

public class AboutLibrariesFragment extends AndrolifeFragment {

	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.androlife_about_libraries_activity, container, false);
		final LinearLayout linearContainer = (LinearLayout) view.findViewById(R.id.androlife_about_libraries_container);

		linearContainer.addView(AboutActivity.generateLibraryView(inflater, linearContainer, R.drawable.actionbarsherlock,
				R.string.androlife_about_actionbarsherlock_title, R.string.androlife_about_actionbarsherlock_content));
		linearContainer.addView(AboutActivity.generateLibraryView(inflater, linearContainer, R.drawable.zxing,
				R.string.androlife_about_zxing_title, R.string.androlife_about_zxing_content));
		linearContainer.addView(AboutActivity.generateLibraryView(inflater, linearContainer, R.drawable.disklrucache,
				R.string.androlife_about_disklrucache_title, R.string.androlife_about_disklrucache_content));

		final TextView aboutTextView = (TextView) view.findViewById(R.id.androlife_about_version);
		AndrolifeViewFactory.addVersionView(aboutTextView);
		return view;
	}
}
