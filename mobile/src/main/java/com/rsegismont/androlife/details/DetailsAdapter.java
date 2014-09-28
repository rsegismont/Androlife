package com.rsegismont.androlife.details;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.rsegismont.androlife.activities.ProgrammeAbstract;
import com.rsegismont.androlife.common.SharedInformation;

public class DetailsAdapter extends FragmentStatePagerAdapter implements OnPageChangeListener {

	/**
	 * Listener for creation/destruction of fragment in {@link com.rsegismont.androlife.home.HomeProgrammesAdapter}
	 * 
	 * @author Romain Segismont
	 * @version 1.0
	 * @since 1.0
	 */
	public static interface FragmentInstanceListener {
		/**
		 * Called when a fragment is created for "position"
		 * 
		 * @param position
		 *            the position of the fragment in the adapter
		 */
		public void onFragmentCreated(int position);

		/**
		 * Called when a fragment is destroyed for "position"
		 * 
		 * @param position
		 *            the position of the fragment in the adapter
		 */
		public void onFragmentDestroyed(int position);
	}

	public boolean ignoreSelected = false;
	private SparseArray<String> titleHelp = new SparseArray<String>();
	private SparseArray<ContentValues> valuesHelp = new SparseArray<ContentValues>();
	private ProgrammeAbstract pgAbstract;
	public SparseArrayCompat<DetailsFragment> fragmentMaps;
	private FragmentInstanceListener fragmentListener;
	public int mCurrentPosition = 0;
	private boolean firstInit = true;

	/**
	 * Set a new {@link FragmentInstanceListener}
	 * 
	 * @param fragmentListener
	 */
	public void setFragmentListener(FragmentInstanceListener fragmentListener) {
		this.fragmentListener = fragmentListener;
	}

	public DetailsAdapter(ProgrammeAbstract pgAbstract) {
		super(pgAbstract.getSupportFragmentManager());
		fragmentMaps = new SparseArrayCompat<DetailsFragment>();
		this.pgAbstract = pgAbstract;
	}

	public void clear() {
		titleHelp = new SparseArray<String>();
		valuesHelp = new SparseArray<ContentValues>();
	}

	public int getCount() {
		try {

			if (pgAbstract.mCursor == null || pgAbstract.mCursor.isClosed()) {
				return 0;
			}
			return pgAbstract.mCursor.getCount();
		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
			return 0;
		}
	}

	public Fragment getItem(int position) {

		DetailsFragment fragment = null;

		if (pgAbstract.mCursor == null || pgAbstract.mCursor.isClosed()) {
			return new Fragment();
		}

		long date = -1;

		try {
			if (pgAbstract.mCursor.moveToPosition(position + 1)) {
				date = pgAbstract.mCursor.getLong(pgAbstract.mCursor
						.getColumnIndex(SharedInformation.DatabaseColumn.DATE_UTC.stringValue));
			}

			fragment = new DetailsFragment();
			Bundle mBundle = new Bundle();
			ContentValues localContentValues = (ContentValues) this.valuesHelp.get(position);
			if (localContentValues == null) {
				localContentValues = new ContentValues();
				pgAbstract.mCursor.moveToPosition(position);
				DatabaseUtils.cursorRowToContentValues(pgAbstract.mCursor, localContentValues);
				valuesHelp.append(position, localContentValues);
			}
			mBundle.putParcelable(DetailsFragment.CURSOR, localContentValues);
			mBundle.putLong(DetailsFragment.TIME, date);
			fragment.setArguments(mBundle);

		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
		}

		fragmentMaps.put(position, fragment);
		if (fragmentListener != null) {
			fragmentListener.onFragmentCreated(position);
		}

		return fragment;

	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
		if (fragmentListener != null) {
			fragmentListener.onFragmentDestroyed(position);
		}
		fragmentMaps.remove(position);
	}

	public CharSequence getPageTitle(int paramInt) {
		String str = (String) this.titleHelp.get(paramInt);
		if ((str == null) && (pgAbstract.mCursor.moveToPosition(paramInt))) {
			str = pgAbstract.mCursor.getString(
					pgAbstract.mCursor.getColumnIndex(SharedInformation.DatabaseColumn.DATE.stringValue)).substring(11);
			this.titleHelp.append(paramInt, str);
		}
		return str;
	}

	public void onPageScrollStateChanged(int paramInt) {
	}

	public void onPageScrolled(int paramInt1, float paramFloat, int paramInt2) {
	}

	public void onPageSelected(int position) {

		if (firstInit == true) {
			firstInit = false;
			pgAbstract.supportInvalidateOptionsMenu();
		}

		mCurrentPosition = position;

		if (ignoreSelected) {
			ignoreSelected = false;
		} else {
			pgAbstract.onDetailsSwiped(position);
		}

		/**final DetailsFragment fragment = fragmentMaps.get(position);
		if (fragment != null) {
			fragment.setupActionBarMenu(pgAbstract.getMenu());
		} */

	}
}