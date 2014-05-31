package com.rsegismont.androlife.common;

import java.util.LinkedList;
import java.util.List;

import android.app.SearchManager;
import android.net.Uri;
import android.provider.BaseColumns;

public class SharedInformation {
	
	/** Database version where HD attribute was added */
	public static final int DATABASE_HD_VERSION = 7;

	public static String AUTHORITY = "com.rsegismont.android.androLife.core.AndrolifeProvider";

	public static final String INTENT_DETAILS = "com.rsegismont.androlife.intent.action.DETAILS";

	public static final String APP_LAUNCH = "com_rsegismont_androlife_applaunch";

	public static final String urlProgramme = "http://www.nolife-tv.com/noair/noair.xml";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public static final String CONTENT_PROVIDER_PROGRAMME_TABLE_NAME = "programmes";

	public static final Uri CONTENT_URI_PROGRAMMES = Uri.parse("content://" + AUTHORITY + "/"
			+ CONTENT_PROVIDER_PROGRAMME_TABLE_NAME);

	public static final String FULL_UPDATE = "FULL_UPDATE";

	public static enum DatabaseColumn implements BaseColumns {
		


		ID("_id", 0), DATE("date", 1), DATE_UTC("dateUTC", 2), COLOR("color", 3), TITLE("title", 4), SUBTITLE(
				"sub_title", 5), DESCRIPTION("description", 6), DETAIL(SearchManager.SUGGEST_COLUMN_TEXT_2, 7), LEVELTYPE(
				"leveltype", 8), CSA("csa", 9), URL("url", 10), SCREENSHOT("screenshot", 11), SCREENSHOT_EMISSION(
				"AdditionalScreenshot", 12), TYPE("type", 13), PREMIERE_DIFFUSION("premierediff", 14), ID_MASTERSHOW(
				"id_mastershow", 15), NolifeOnlineURL("NolifeOnlineURL", 16), NolifeOnlineStart("NolifeOnlineStart", 17), NolifeOnlineEnd(
				"NolifeOnlineEnd", 18), NolifeOnlineShowDate("NolifeOnlineShowDate", 19), NolifeOnlineExternalURL(
				"Online_ExternalURL", 20), SUGGEST_TOP(SearchManager.SUGGEST_COLUMN_TEXT_1, 21),
				HD("HD", 22);

		public String stringValue;
		public int columnPosition;
		

		DatabaseColumn(String noairValue, int columnPosition) {
			this.stringValue = noairValue;
			this.columnPosition = columnPosition;
		}
		
		public static List<String> getList(){
			List<String> tmp = new LinkedList<String>();
			for(DatabaseColumn column : values()){
				tmp.add(column.stringValue);
			}
			return tmp;
		}

	}
}
