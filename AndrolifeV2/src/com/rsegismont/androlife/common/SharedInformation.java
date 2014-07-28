package com.rsegismont.androlife.common;

import java.util.LinkedList;
import java.util.List;

import android.app.SearchManager;
import android.net.Uri;
import android.provider.BaseColumns;

public class SharedInformation {

	/** Database version where HD attribute was added */

	public static String AUTHORITY = "com.rsegismont.androlife.core.database.AndrolifeProvider";

	public static final String INTENT_DETAILS = "com.rsegismont.androlife.intent.action.DETAILS";

	public static final String APP_LAUNCH = "com_rsegismont_androlife_applaunch";

	public static final String urlProgramme = "http://www.nolife-tv.com/noair/noair.xml";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public static final String CONTENT_PROVIDER_PROGRAMME_TABLE_NAME = "programmes";

	public static final Uri CONTENT_URI_PROGRAMMES = Uri.parse("content://" + AUTHORITY + "/"
			+ CONTENT_PROVIDER_PROGRAMME_TABLE_NAME);

	public static final String FULL_UPDATE = "FULL_UPDATE";

	public static enum DatabaseColumn implements BaseColumns {

		ID("_id"),
		/** */
		DATE("date"),
		/** */
		DATE_UTC("dateUTC"),
		/** */
		COLOR("color"),
		/** */
		TITLE("title"),
		/** */
		SUBTITLE("sub_title"),
		/** */
		DESCRIPTION("description"),
		/** */
		DETAIL(SearchManager.SUGGEST_COLUMN_TEXT_2),
		/** */
		LEVELTYPE("leveltype"),
		/** */
		CSA("csa"),
		/** */
		URL("url"),
		/** */
		SCREENSHOT("screenshot"),
		/** */
		SCREENSHOT_EMISSION("AdditionalScreenshot"),
		/** */
		TYPE("type"), PREMIERE_DIFFUSION("premierediff"),
		/** */
		ID_MASTERSHOW("id_mastershow"),
		/** */
		NolifeOnlineURL("NolifeOnlineURL"),
		/** */
		NolifeOnlineStart("NolifeOnlineStart"),
		/** */
		NolifeOnlineEnd("NolifeOnlineEnd"),
		/** */
		NolifeOnlineShowDate("NolifeOnlineShowDate"),
		/** */
		SUGGEST_TOP(SearchManager.SUGGEST_COLUMN_TEXT_1),
		/** */
		HD("HD");

		public String stringValue;

		DatabaseColumn(String noairValue) {
			this.stringValue = noairValue;
		}

		public static List<String> getList() {
			List<String> tmp = new LinkedList<String>();
			for (DatabaseColumn column : values()) {
				tmp.add(column.stringValue);
			}
			return tmp;
		}

	}
}
