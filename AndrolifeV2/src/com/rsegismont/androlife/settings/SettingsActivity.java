package com.rsegismont.androlife.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.rsegismont.androlife.R;
import com.rsegismont.androlife.application.AndrolifeApplication;
import com.rsegismont.androlife.common.SdkUtils;
import com.rsegismont.androlife.common.api.AndrolifeApi9;
import com.rsegismont.androlife.home.HomeActivity;
import com.rsegismont.androlife.utils.ActivityEffectsHelper;
import com.rsegismont.androlife.utils.ActivityEffectsHelper.EffectListener;

public class SettingsActivity extends SherlockPreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener, EffectListener {

	public static final String FILTERS_CLIP = "settings_filters_clip";
	public static final String FILTERS_MAGAZINES = "settings_filters_magazines";
	public static final String FILTERS_SERIES = "settings_filters_series";
	public static final String GENERAL_ORIENTATION = "settings_general_orientation";
	public static final String HOME_NUMBERS = "settings_home_programs_number";
	public static final String PERFORMANCES_TRANSITIONS = "settings_performances_transitions";
	public static final String PERFORMANCES_IMAGES = "settings_performances_images";

	//
	public static final String PERFORMANCES_TRANSITIONS_NONE = "none";
	public static final String PERFORMANCES_TRANSITIONS_FADE = "fade";
	public static final String PERFORMANCES_TRANSITIONS_SLIDE = "slide";

	public static String classToString(Class<?> classToConvert) {
		return classToConvert.getName();
	}

	public static List<String> fragmentWhiteList = Arrays.asList(GeneralPreferenceFragment.class.getName(),
			FiltersPreferenceFragment.class.getName(), HomePreferenceFragment.class.getName(),
			PerformancesPreferenceFragment.class.getName());

	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String str = newValue.toString();

			if ((preference instanceof ListPreference)) {
				final ListPreference listPreference = (ListPreference) preference;
				int i = listPreference.findIndexOfValue(str);
				if (i >= 0) {
					final CharSequence localCharSequence = listPreference.getEntries()[i];
					preference.setSummary(localCharSequence);
					return true;
				}
			}

			preference.setSummary(str);

			return true;
		}
	};
	private ArrayList<String> preferencesList = new ArrayList<String>();

	private static void bindPreferenceSummaryToValue(Preference paramPreference) {
		paramPreference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
		sBindPreferenceSummaryToValueListener.onPreferenceChange(paramPreference, PreferenceManager
				.getDefaultSharedPreferences(paramPreference.getContext()).getString(paramPreference.getKey(), ""));
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return fragmentWhiteList.contains(fragmentName);
	}

	private static boolean isSimplePreferences(Context paramContext) {
		if (isXlargeTablet(paramContext)) {
			return false;
		}
		return true;
	}

	public static boolean isXlargeTablet(Context context) {
		if (VERSION.SDK_INT < 11) {
			return false;
		} else {
			return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= AndrolifeApi9
					.getXlargeAttribute();
		}
	}

	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}
		addPreferencesFromResource(R.xml.settings_dummy);
		if (SdkUtils.isATablet(getApplicationContext())) {
			// General
			PreferenceCategory localPreferenceCategory1 = new PreferenceCategory(this);
			localPreferenceCategory1.setTitle(R.string.settings_general);
			getPreferenceScreen().addPreference(localPreferenceCategory1);
			addPreferencesFromResource(R.xml.settings_general);
			bindPreferenceSummaryToValue(findPreference(GENERAL_ORIENTATION));
		}

		// Home
		PreferenceCategory localPreferenceCategory2 = new PreferenceCategory(this);
		localPreferenceCategory2.setTitle(R.string.settings_home);
		getPreferenceScreen().addPreference(localPreferenceCategory2);
		addPreferencesFromResource(R.xml.settings_home);

		// Filters
		PreferenceCategory localPreferenceCategory3 = new PreferenceCategory(this);
		localPreferenceCategory3.setTitle(R.string.settings_filters);
		getPreferenceScreen().addPreference(localPreferenceCategory3);
		addPreferencesFromResource(R.xml.settings_filters);

		// Performances
		PreferenceCategory localPreferenceCategory4 = new PreferenceCategory(this);
		localPreferenceCategory4.setTitle(R.string.settings_performances);
		getPreferenceScreen().addPreference(localPreferenceCategory4);
		addPreferencesFromResource(R.xml.settings_performances);
		bindPreferenceSummaryToValue(findPreference(HOME_NUMBERS));
		SettingsActivity.bindPreferenceSummaryToValue(findPreference(PERFORMANCES_TRANSITIONS));
	}

	public void onBackPressed() {
		if (this.preferencesList.size() > 0) {
			new BackupManager(this).dataChanged();
			startActivityAfterCleanup(HomeActivity.class);
		} else {
			super.onBackPressed();
		}

		ActivityEffectsHelper.onBackPressed((AndrolifeApplication) getApplication(), this);

	}

	@TargetApi(11)
	public void onBuildHeaders(List<PreferenceActivity.Header> paramList) {
		if (!isSimplePreferences(this))
			loadHeadersFromResource(R.xml.settings_headers, paramList);
	}

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		this.preferencesList = new ArrayList<String>();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);

	}

	public boolean onIsMultiPane() {
		if (isXlargeTablet(this) && (!isSimplePreferences(this))) {
			return true;
		}
		return false;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	public void onPause() {
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	protected void onPostCreate(Bundle paramBundle) {
		super.onPostCreate(paramBundle);
		setupSimplePreferencesScreen();
	}

	public void onResume() {
		super.onResume();
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.registerOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences paramSharedPreferences, String paramString) {
		if (this.preferencesList.contains(paramString))
			this.preferencesList.remove(paramString);
		else {
			preferencesList.add(paramString);
		}
	}

	protected void startActivityAfterCleanup(Class<?> paramClass) {
		Intent localIntent = new Intent(getApplicationContext(), paramClass);
		localIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(localIntent);
	}

	@TargetApi(11)
	public static class FiltersPreferenceFragment extends PreferenceFragment {
		public void onCreate(Bundle paramBundle) {
			super.onCreate(paramBundle);
			addPreferencesFromResource(R.xml.settings_filters);
		}
	}

	@TargetApi(11)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		public void onCreate(Bundle paramBundle) {
			super.onCreate(paramBundle);
			addPreferencesFromResource(R.xml.settings_general);
			SettingsActivity.bindPreferenceSummaryToValue(findPreference(GENERAL_ORIENTATION));
		}
	}

	@TargetApi(11)
	public static class HomePreferenceFragment extends PreferenceFragment {
		public void onCreate(Bundle paramBundle) {
			super.onCreate(paramBundle);
			addPreferencesFromResource(R.xml.settings_home);
			SettingsActivity.bindPreferenceSummaryToValue(findPreference(HOME_NUMBERS));
		}
	}

	@TargetApi(11)
	public static class PerformancesPreferenceFragment extends PreferenceFragment {
		public void onCreate(Bundle paramBundle) {
			super.onCreate(paramBundle);
			addPreferencesFromResource(R.xml.settings_performances);
			SettingsActivity.bindPreferenceSummaryToValue(findPreference(PERFORMANCES_TRANSITIONS));
		}
	}

	@Override
	public void onTransitionEffect(int enterAnim, int exitAnim) {
		overridePendingTransition(enterAnim, exitAnim);
	}

}