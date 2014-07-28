package com.rsegismont.androlife.about;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.core.ui.AndrolifeFragment;

public class AboutMeFragment extends AndrolifeFragment {

	@SuppressLint("SetJavaScriptEnabled")
	public View onCreateView(LayoutInflater inflater, ViewGroup paramViewGroup, Bundle paramBundle) {
		final WebView view = (WebView) inflater.inflate(R.layout.androlife_about_me_fragment, paramViewGroup, false);

		final WebSettings settings = view.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setAppCacheMaxSize(1024 * 1024 * 8);
		settings.setAppCachePath(getActivity().getApplicationContext().getCacheDir().getAbsolutePath());
		settings.setAllowFileAccess(true);
		settings.setAppCacheEnabled(true);
		settings.setDomStorageEnabled(true);
		final ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(
				Activity.CONNECTIVITY_SERVICE);
		if (cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
			settings.setCacheMode(WebSettings.LOAD_DEFAULT);
		} else {
			settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		}
		settings.setBuiltInZoomControls(true);
		final String url = "http://fr.linkedin.com/pub/romain-segismont/56/916/b25";
		view.loadUrl(url);
		// needed in order to support javascript alerts and other javascript operations
		view.setWebChromeClient(new WebChromeClient());
		view.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
				super.shouldOverrideUrlLoading(view, url);
				view.loadUrl(url);
				return true;
			}

		});
		return view;
	}
}
