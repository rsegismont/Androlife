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

package com.rsegismont.android.androLife.core;

import java.util.ArrayList;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.common.SharedInformation.DatabaseColumn;

/**
 * Content provider that will store all datas
 * 
 * @author Romain Segismont
 * @version 1.0
 * @since 1.0
 * 
 */
public class AndrolifeProvider extends ContentProvider {

	public static final String CONTENT_PROVIDER_DB_NAME = "androlife_provider.db";
	public static final int CONTENT_PROVIDER_DB_VERSION = 9;

	/**
	 * To identify search Suggestions in the URI
	 * 
	 */
	private static final int SEARCH_SUGGEST = 2;

	/**
	 * Create the programmes table
	 * 
	 * @since 1.0
	 */
	private static final String CREATE_PROGRAMMES_TABLE = "CREATE TABLE "
			+ SharedInformation.CONTENT_PROVIDER_PROGRAMME_TABLE_NAME + " (" + DatabaseColumn.ID.stringValue
			+ " INTEGER PRIMARY KEY  AUTOINCREMENT," + DatabaseColumn.DATE.stringValue + " VARCHAR(35) NOT NULL,"
			+ DatabaseColumn.DATE_UTC.stringValue + " VARCHAR(35) NOT NULL," + DatabaseColumn.COLOR.stringValue
			+ " VARCHAR(20) NOT NULL," + DatabaseColumn.TITLE.stringValue + " VARCHAR(150) NOT NULL,"
			+ DatabaseColumn.SUBTITLE.stringValue + " VARCHAR(150) NOT NULL," + DatabaseColumn.DESCRIPTION.stringValue
			+ " VARCHAR(300) NOT NULL," + DatabaseColumn.DETAIL.stringValue + " VARCHAR(250) NOT NULL,"
			+ DatabaseColumn.LEVELTYPE.stringValue + " VARCHAR(6) NOT NULL," + DatabaseColumn.CSA.stringValue
			+ " VARCHAR(3) NOT NULL," + DatabaseColumn.URL.stringValue + " VARCHAR(150) NOT NULL,"
			+ DatabaseColumn.SCREENSHOT.stringValue + " VARCHAR(250),"
			+ DatabaseColumn.SCREENSHOT_EMISSION.stringValue + " VARCHAR(250),"
			+ DatabaseColumn.TYPE.stringValue + " VARCHAR(40) NOT NULL,"
			+ DatabaseColumn.PREMIERE_DIFFUSION.stringValue + " VARCHAR(1) NOT NULL,"
			+ DatabaseColumn.ID_MASTERSHOW.stringValue + " VARCHAR(50) NOT NULL,"
			+ DatabaseColumn.NolifeOnlineURL.stringValue + " VARCHAR(250),"
			+ DatabaseColumn.NolifeOnlineStart.stringValue + " VARCHAR(35),"
			+ DatabaseColumn.NolifeOnlineEnd.stringValue + " VARCHAR(35),"
			+ DatabaseColumn.NolifeOnlineShowDate.stringValue + " VARCHAR(35),"
			+ DatabaseColumn.SUGGEST_TOP.stringValue + " VARCHAR(200),"
			+ SearchManager.SUGGEST_COLUMN_SHORTCUT_ID + " VARCHAR(5)," + SearchManager.SUGGEST_COLUMN_INTENT_DATA
			+ " VARCHAR(50), " + DatabaseColumn.HD.stringValue + " VARCHAR(35)" + ")";

	private static final UriMatcher sURIMatcher = buildUriMatcher();

	/**
	 * Create a {@link UriMatcher} to identify which URI is a search suggest or a programmes URI
	 * 
	 * @since 1.0
	 * @return
	 */
	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

		matcher.addURI(SharedInformation.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
		matcher.addURI(SharedInformation.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
		return matcher;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, CONTENT_PROVIDER_DB_NAME, null, CONTENT_PROVIDER_DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_PROGRAMMES_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (oldVersion < SharedInformation.DATABASE_HD_VERSION
					&& newVersion >= SharedInformation.DATABASE_HD_VERSION) {
				db.execSQL("ALTER TABLE " + SharedInformation.CONTENT_PROVIDER_PROGRAMME_TABLE_NAME + " ADD "
						+ DatabaseColumn.HD.stringValue + " VARCHAR(35) default '0'");
			}
			if (oldVersion == SharedInformation.DATABASE_HD_VERSION) {
				db.execSQL("DROP TABLE IF EXISTS " + SharedInformation.CONTENT_PROVIDER_PROGRAMME_TABLE_NAME);
				onCreate(db);
			}
			if (oldVersion <= SharedInformation.DATABASE_NOCO_VERSION) {
				db.execSQL("DROP TABLE IF EXISTS " + SharedInformation.CONTENT_PROVIDER_PROGRAMME_TABLE_NAME);
				onCreate(db);
			}

		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + SharedInformation.CONTENT_PROVIDER_PROGRAMME_TABLE_NAME);
			onCreate(db);
		}
	}

	private DatabaseHelper dbHelper;

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}

	private static final String CONTENT_PROVIDER_PROGRAMMES_TYPE = "vnd.android.cursor.item/com.rsegismont.android.androLife.core.parameters.DatabaseColumn";

	@Override
	public String getType(Uri uri) {
		return CONTENT_PROVIDER_PROGRAMMES_TYPE;
	}

	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {

		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			final int numOperations = operations.size();
			final ContentProviderResult[] results = new ContentProviderResult[numOperations];
			for (int i = 0; i < numOperations; i++) {
				results[i] = operations.get(i).apply(this, results, i);
			}
			db.setTransactionSuccessful();
			return results;
		} finally {
			db.endTransaction();

		}
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
		sqlDB.beginTransaction();

		try {
			for (ContentValues cv : values) {
				long newID = sqlDB.insertOrThrow(SharedInformation.CONTENT_PROVIDER_PROGRAMME_TABLE_NAME, null, cv);
				if (newID <= 0) {
					throw new SQLException("Failed to insert row into " + uri);
				}
			}
			sqlDB.setTransactionSuccessful();
			return values.length;
		} finally {
			sqlDB.endTransaction();
		}
	}

	/**
	 * Insert a value
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long id = 0;

		if (values == null)
			return ContentUris.withAppendedId(uri, id);

		if (uri.equals(SharedInformation.CONTENT_URI_PROGRAMMES))
			id = db.insert(SharedInformation.CONTENT_PROVIDER_PROGRAMME_TABLE_NAME, null, values);

		return ContentUris.withAppendedId(uri, id);

	}

	/**
	 * update a value
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return dbHelper.getWritableDatabase().update(SharedInformation.CONTENT_PROVIDER_PROGRAMME_TABLE_NAME, values,
				selection, selectionArgs);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return dbHelper.getWritableDatabase().delete(SharedInformation.CONTENT_PROVIDER_PROGRAMME_TABLE_NAME,
				selection, selectionArgs);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		final SQLiteDatabase db = dbHelper.getReadableDatabase();

		switch (sURIMatcher.match(uri)) {

		// Intercept query "search" for handle search request ( system search or in-app search )
		case SEARCH_SUGGEST:

			final Cursor cursor = db.query(SharedInformation.CONTENT_PROVIDER_PROGRAMME_TABLE_NAME, projection,
					DatabaseColumn.DESCRIPTION.stringValue + " LIKE ?", new String[] { "%" + selectionArgs[0] + "%" },
					null, null, sortOrder);

			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		default:

			final Cursor cursor2 = db.query(SharedInformation.CONTENT_PROVIDER_PROGRAMME_TABLE_NAME, projection,
					selection, selectionArgs, null, null, sortOrder);
			cursor2.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor2;

		}

	}	db.execSQL("ALTER TABLE " + SharedInformation.CONTENT_PROVIDER_PROGRAMME_TABLE_NAME + " ADD "
						+ DatabaseColumn.HD.stringValue + " VARCHAR(35) default '0'");
			}
			if (oldVersion == SharedInformation.DATABASE_HD_VERSION) {
				db.execSQL("DROP TABLE IF EXISTS " + SharedInformation.CONTENT_PROVIDER_PROGRAMME_TABLE_NAME);
				onCreate(db);
			}

		}

}
