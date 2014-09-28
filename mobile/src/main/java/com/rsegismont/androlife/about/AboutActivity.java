package com.rsegismont.androlife.about;

import android.app.ActionBar;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.common.SdkUtils;
import com.rsegismont.androlife.core.ui.SkeletonActivity;
import com.rsegismont.androlife.home.HomeActivity;

public class AboutActivity extends SkeletonActivity implements ActionBar.TabListener {
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	public Point screenDimensions;

	public static View generateLibraryView(LayoutInflater inflater, ViewGroup container, int drawableId, int titleId,
			int contentId) {
		final View view = inflater.inflate(R.layout.androlife_about_item, container, false);
		((ImageView) view.findViewById(R.id.about_icon)).setImageResource(drawableId);
		((TextView) view.findViewById(R.id.about_title)).setText(titleId);
		((TextView) view.findViewById(R.id.about_content)).setText(contentId);
		return view;
	}

	/**
	 * Method to retrieve screen dimensions from an activity
	 * 
	 * @param activity
	 * @return a {@link Point} where x define width and y define height in pixel
	 * @since 1.0
	 */
	public static Point getScreenDimension(final FragmentActivity activity) {
		final Point mDimensions = new Point();
		final DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mDimensions.x = metrics.widthPixels;
		mDimensions.y = metrics.heightPixels;
		return mDimensions;
	}

	public static View generateAndrolifeView(FragmentActivity context, LayoutInflater inflater, ViewGroup container,
			int drawableId, int titleId, int contentId) {
		final View view = inflater.inflate(R.layout.androlife_about_androlife_item, container, false);
		((ImageView) view.findViewById(R.id.about_icon)).setImageResource(drawableId);
		//((TextView) view.findViewById(R.id.about_title)).setText(titleId);
		((TextView) view.findViewById(R.id.about_content)).setText(contentId);
		return view;
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		screenDimensions = getScreenDimension(this);

		setContentView(R.layout.androlife_about_activity);
		final ActionBar localActionBar = getActionBar();
		localActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		localActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		this.mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		this.mViewPager = ((ViewPager) findViewById(R.id.about_pager));
		this.mViewPager.setAdapter(this.mSectionsPagerAdapter);
		this.mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			public void onPageSelected(int paramAnonymousInt) {
				localActionBar.setSelectedNavigationItem(paramAnonymousInt);
			}
		});
		for (int i = 0;; i++) {
			if (i >= this.mSectionsPagerAdapter.getCount())
				return;
			localActionBar.addTab(localActionBar.newTab().setText(this.mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	public boolean onCreateOptionsMenu(Menu paramMenu) {
		getMenuInflater().inflate(R.menu.about, paramMenu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
		switch (paramMenuItem.getItemId()) {
		default:
			return super.onOptionsItemSelected(paramMenuItem);
		case R.id.about_icon_sitenolife:
			startActivity(SdkUtils.prepare_web(getApplicationContext(), getString(R.string.about_nolife_url)));
			return true;
		case android.R.id.home:
			startActivityAfterCleanup(HomeActivity.class);
			return true;
		}
	}

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
        this.mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(FragmentManager arg2) {
			super(arg2);
		}

		public int getCount() {
			return 3;
		}

		public Fragment getItem(int paramInt) {
			switch (paramInt) {
			case 0:
				return new AboutAndrolifeFragment();
			case 1:
				return new AboutMeFragment();

			case 2:
				return new AboutLibrariesFragment();

			default:
				return new AboutMeFragment();
			}

		}

		public CharSequence getPageTitle(int paramInt) {
			String pageTitle;
			switch (paramInt) {
			default:
				pageTitle = null;
			case 0:
				pageTitle = getString(com.rsegismont.androlife.R.string.about_section1);
				break;
			case 1:
				pageTitle = getString(com.rsegismont.androlife.R.string.about_section2);
				break;
			case 2:
				pageTitle = getString(com.rsegismont.androlife.R.string.about_section3);
				break;
			}

			return pageTitle;

		}
	}
}