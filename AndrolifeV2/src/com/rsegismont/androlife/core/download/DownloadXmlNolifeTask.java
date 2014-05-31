package com.rsegismont.androlife.core.download;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.common.SharedInformation.DatabaseColumn;
import com.rsegismont.androlife.utils.AndrolifeUtils;

public class DownloadXmlNolifeTask extends AsyncTask<Void, Integer, Void> {

	private static final String NOAIR_URL = "http://www.nolife-tv.com/noair/noair.xml";
	private static final int BATCH_SIZE = 200;

	private Context mContext;
	private ProgressDialog mDialog;
	private Resources mResources;

	public DownloadXmlNolifeTask(Context paramContext) {
		mContext = paramContext;
		mResources = mContext.getResources();
	}

	private void setProgressPercent(Integer paramInteger) {
		final ProgressDialog localProgressDialog = mDialog;
		int i = paramInteger.intValue();
		localProgressDialog.setMessage(mResources.getQuantityString(R.plurals.programme_loaded, i, paramInteger));
	}

	@Override
	protected Void doInBackground(Void... params) {
		downloadXML();
		return null;
	}

	public void downloadXML() {
		try {
			// final InputStream localInputStream = AndrolifeUtils.getInputStreamUrl(mContext, NOAIR_URL);
			final InputStream localInputStream = AndrolifeUtils.getInputStreamUrl(mContext, NOAIR_URL);
			if (localInputStream == null) {
				return;
			}

			final XmlParser localXmlParser = new XmlParser(this);

			final XMLReader localXMLReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			localXMLReader.setContentHandler(localXmlParser);
			localXMLReader.parse(new InputSource(new BufferedInputStream(localInputStream)));
		} catch (Throwable localException2) {
			localException2.printStackTrace();
		}
	}

	protected void onCancelled() {
		super.onCancelled();
		try {
			mDialog.dismiss();
			mDialog = null;
			return;
		} catch (Throwable localThrowable) {

		}
	}

	public void onPostExecute(Void paramVoid) {
		super.onPostExecute(null);
		try {
			mDialog.dismiss();
			mDialog = null;
			return;
		} catch (Throwable localThrowable) {

		}
	}

	public void onPreExecute() {
		mDialog = new ProgressDialog(mContext);
		mDialog.setTitle("Chargement");
		mDialog.setMessage("Chargement de Noair");
		mDialog.setIndeterminate(true);
		mDialog.setCancelable(false);
		mDialog.show();
		super.onPreExecute();
	}

	protected void onProgressUpdate(Integer... params) {
		if (params != null) {
			if (params.length > 0) {
				setProgressPercent(params[0]);
			}
		}
	}

	public static class XmlParser extends DefaultHandler2 {
		private static final String NOAIR_TAG = "NoAir";
		private int cpt;
		private int flags;
		private List<ContentValues> listValues = new LinkedList<ContentValues>();
		private List<String> mList;
		private SimpleDateFormat localSimpleDateFormat;
		private WeakReference<DownloadXmlNolifeTask> mDownloadTask;

		public XmlParser(DownloadXmlNolifeTask downloadTask) {
			this.mDownloadTask = new WeakReference<DownloadXmlNolifeTask>(downloadTask);
			flags = 0;
			flags = (DateUtils.FORMAT_SHOW_DATE | flags);
			flags = (DateUtils.FORMAT_ABBREV_MONTH | flags);
			flags = (DateUtils.FORMAT_SHOW_TIME | flags);
		}

		public void endDocument() throws SAXException {

			final DownloadXmlNolifeTask xmlTask = mDownloadTask.get();
			if (xmlTask != null) {

				if (listValues.size() > 0) {
					xmlTask.mContext.getContentResolver().bulkInsert(SharedInformation.CONTENT_URI_PROGRAMMES,
							(ContentValues[]) listValues.toArray(new ContentValues[listValues.size()]));
					listValues.clear();
				}

				ContentValuesHelper.shutdown();

				xmlTask.mContext.getContentResolver().notifyChange(SharedInformation.CONTENT_URI_PROGRAMMES, null);
				PreferenceManager.getDefaultSharedPreferences(xmlTask.mContext.getApplicationContext()).edit()
						.putLong(NOAIR_URL, AndrolifeUtils.timeToConfirm).commit();
			}
		}

		public void endElement(String paramString1, String paramString2, String paramString3) throws SAXException {
		}

		public void startDocument() throws SAXException {

			final DownloadXmlNolifeTask xmlTask = mDownloadTask.get();
			if (xmlTask != null) {

				ContentValuesHelper.init(BATCH_SIZE, DatabaseColumn.values().length + 3);

				final String databaseFormatString = xmlTask.mContext.getResources().getString(R.string.format_database);
				localSimpleDateFormat = new SimpleDateFormat(databaseFormatString, Locale.getDefault());
				localSimpleDateFormat.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));

				cpt = 0;
				mList = DatabaseColumn.getList();
				xmlTask.mContext.getContentResolver().delete(SharedInformation.CONTENT_URI_PROGRAMMES, null, null);
			}
		}

		public void startElement(String paramString1, String paramString2, String paramString3,
				Attributes paramAttributes) throws SAXException {

			if ((!paramString2.equals(NOAIR_TAG)) && (paramString2.equals("slot"))) {
				final DownloadXmlNolifeTask xmlTask = mDownloadTask.get();
				if (xmlTask != null) {
					List<String> columns = new ArrayList<String>(mList);
					final ContentValues contentValues = ContentValuesHelper.obtain();
					String str1 = "";
					String str2 = "";
					Object suggest_top = "";

					for (int i = 0; i < paramAttributes.getLength(); i++) {
						final String value = paramAttributes.getValue(i);
						String key = paramAttributes.getLocalName(i).replaceAll("-", "_");

						if (key.equals(SharedInformation.DatabaseColumn.DATE.stringValue)) {
							contentValues.put(key, value.substring(0, value.lastIndexOf(":")));
							final long longDate = AndrolifeUtils.stringToDate(localSimpleDateFormat, value, false)
									.getTime();
							contentValues.put(SharedInformation.DatabaseColumn.DATE_UTC.stringValue, longDate);
							str1 = String.valueOf(longDate);
							str2 = DateUtils.formatDateTime(xmlTask.mContext, longDate, flags);
						} else {

							if (key.equals("detail")) {
								key = SharedInformation.DatabaseColumn.DETAIL.stringValue;
							} else if (key.equals("description")) {
								suggest_top = value;
							}

							if (!key.equals(SharedInformation.DatabaseColumn.DATE_UTC.stringValue)) {
								if (columns.contains(key)) {
									columns.remove(key);
									contentValues.put(key, value);
								}
							}
						}

					}

					contentValues.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA, str1);
					contentValues.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "_-1");
					contentValues.put(SharedInformation.DatabaseColumn.SUGGEST_TOP.stringValue, str2 + " : "
							+ (String) suggest_top);
					listValues.add(contentValues);
					if (listValues.size() >= BATCH_SIZE) {
						xmlTask.mContext.getContentResolver().bulkInsert(SharedInformation.CONTENT_URI_PROGRAMMES,
								(ContentValues[]) listValues.toArray(new ContentValues[listValues.size()]));
						listValues.clear();
					}
					cpt++;
					xmlTask.publishProgress(cpt);

				}
			}
		}
	}
}