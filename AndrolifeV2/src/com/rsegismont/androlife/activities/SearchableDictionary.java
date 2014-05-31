package com.rsegismont.androlife.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.common.Constantes;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.common.SharedInformation.DatabaseColumn;
import com.rsegismont.androlife.core.ui.SkeletonActivity;
import com.rsegismont.androlife.details.ProgrammesDetailActivity;
import com.rsegismont.androlife.programlist.ProgramListAdapter;

public class SearchableDictionary extends SkeletonActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private Cursor mCursor;
	private ListView mListView;
	private String mQuery;
	private TextView mTextView;

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			final Intent programmeDetails = new Intent(this, ProgrammesDetailActivity.class);
			programmeDetails.setData(intent.getData());
			programmeDetails.putExtra(Constantes.TYPE, Constantes.CURSOR_QUERY_VIEW);
			programmeDetails.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(programmeDetails);
			finish();

		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// handles a search query
			mQuery = intent.getStringExtra(SearchManager.QUERY);
			getSupportLoaderManager().initLoader(0, null, this);

		}
	}

	private void showResults() {
		if (mCursor == null) {
			// There are no results
			mTextView.setText(getString(R.string.no_results, new Object[] { mQuery }));
		} else {
			// Display the number of results
			int count = mCursor.getCount();
			String countString = getResources().getQuantityString(R.plurals.search_results, count,
					new Object[] { count, mQuery });
			mTextView.setText(countString);

			final ProgramListAdapter adapter = new ProgramListAdapter(this, mCursor);
			mListView.setAdapter(adapter);

			// Define the on-click listener for the list items
			mListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					final int columnDateIndex = mCursor.getColumnIndex(DatabaseColumn.DATE_UTC.stringValue);
					final int initialPosition = mCursor.getPosition();

					mCursor.moveToPosition(position);

					Intent intent = new Intent(SearchableDictionary.this, ProgrammesDetailActivity.class);

					intent.putExtra(Constantes.DETAIL_DATE_TIME, mCursor.getLong(columnDateIndex));
					intent.putExtra(Constantes.TYPE, Constantes.CURSOR_QUERY);
					intent.putExtra(Constantes.QUERY, mQuery);

					if (initialPosition >= 0)
						mCursor.moveToPosition(initialPosition);

					startActivity(intent);

				}
			});
		}
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.androlife_search_activity);

		mTextView = (TextView) findViewById(R.id.text);
		mListView = (ListView) findViewById(R.id.list);

		handleIntent(getIntent());
	}

	public Loader<Cursor> onCreateLoader(int paramInt, Bundle paramBundle) {
		return new CursorLoader(this, SharedInformation.CONTENT_URI_PROGRAMMES, null,
				SharedInformation.DatabaseColumn.DESCRIPTION.stringValue + " LIKE ?", new String[]{"%"+mQuery+"%"},
				SharedInformation.DatabaseColumn.DATE_UTC.stringValue + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		mCursor = cursor;
		showResults();
	}

	public void onLoaderReset(Loader<Cursor> paramLoader) {
	}

	protected void onNewIntent(Intent paramIntent) {
		handleIntent(paramIntent);
	}
}