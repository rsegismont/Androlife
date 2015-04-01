/* The MIT License (MIT)
 *
 * Copyright (c) 2013 Romain Segismont
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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.GridView;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.common.Constantes;
import com.rsegismont.androlife.common.SdkUtils;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.core.ui.AndrolifeFragment;
import com.rsegismont.androlife.details.ProgrammesDetailActivity;
import com.rsegismont.androlife.programlist.ProgramListActivity;

/**
 * Class that display a {@link GridView} of 1 column on phones and tablets <= 7 inch. 2 columns on 10 inch and more
 * 
 * @author Romain Segismont
 * @version 1.0
 * @since 1.0
 * 
 */
public class HomeProgrammesGridFragment extends AndrolifeFragment implements AdapterView.OnItemClickListener,
		OnScrollListener {

	public HomeProgrammesAdapter adapter;
	private GridView gridView;


	private int currrentScrollState = -1;

	private boolean wasPaused = false;

	public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
		final View view = paramLayoutInflater.inflate(R.layout.home_programs_fragment, paramViewGroup, false);

		wasPaused = false;

		gridView = (GridView) view.findViewById(R.id.home_programs_grid);

		// Listener
		this.gridView.setOnItemClickListener(this);
		gridView.setOnScrollListener(this);

		// Optimization
		gridView.setCacheColorHint(getResources().getColor(R.color.background));

		// date display
		//dateFormatter = new SimpleDateFormat(getString(R.string.androlife_time_home_hint), Locale.FRANCE);
		return view;
	}

	public void onItemClick(AdapterView<?> paramAdapterView, View view, int position, long positionId) {
		try {
			final Cursor cursor = this.adapter.getCursor();
			final int previousPosition = cursor.getPosition();
			if (cursor.moveToPosition(position + this.adapter.getCurrentPosition())) {
				final int dateUtcIndex = cursor.getColumnIndex(SharedInformation.DatabaseColumn.DATE_UTC.stringValue);
				final Intent intent = new Intent(getActivity(),
						SdkUtils.isTabletLandscape(getActivity()) ? ProgramListActivity.class
								: ProgrammesDetailActivity.class);

				intent.putExtra(Constantes.DETAIL_DATE_TIME, cursor.getLong(dateUtcIndex));
				if (previousPosition >= 0)
					cursor.moveToPosition(previousPosition);

                View photo = (View) adapter.getView(position, null, gridView).findViewById(R.id.home_programme_large_image);
                ActivityOptionsCompat options =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                photo,   // The view which starts the transition
                                getString(R.string.androlife_details_transition_image)    // The transitionName of the view we’re transitioning to
                        );
                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
			}
		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
		}
	}

	public void setAdapter(HomeProgrammesAdapter paramHomeProgrammesAdapter) {
		this.gridView.setAdapter(paramHomeProgrammesAdapter);
		this.adapter = paramHomeProgrammesAdapter;
	}

	@Override
	public void onPause() {
		super.onPause();
		wasPaused = true;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

		/**
		 * if (currentFirstVisibleItem != firstVisibleItem) { currentFirstVisibleItem = firstVisibleItem; if (adapter !=
		 * null) { final String text = AndrolifeUtils.getDisplayDate( getActivity(), dateFormatter,
		 * adapter.getDateForPosition(currentFirstVisibleItem));
		 * 
		 * } }
		 */
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

		if (currrentScrollState == SCROLL_STATE_IDLE) {
			if (scrollState != currrentScrollState && (wasPaused)) {

			}
		}

		if (scrollState == SCROLL_STATE_IDLE) {
			// mHandler.removeCallbacks(hideRunnable);
			// mHandler.postDelayed(hideRunnable, 2 * SdkUtils.ONE_SECOND);
		} else {
			// mHandler.removeCallbacks(hideRunnable);
		}

		currrentScrollState = scrollState;

	}
}
