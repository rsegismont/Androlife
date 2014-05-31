/* The MIT License (MIT)
 *
 * Copyright (c) 2014 Romain Segismont
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

package com.rsegismont.androlife.utils;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.application.AndrolifeApplication;
import com.rsegismont.androlife.settings.SettingsActivity;

public class ActivityEffectsHelper {

	public static interface EffectListener {
		public void onTransitionEffect(int enterAnim, int exitAnim);
	}

	public static String getEffect(AndrolifeApplication application) {
		return application.preferencesManager.getString(SettingsActivity.PERFORMANCES_TRANSITIONS,
				SettingsActivity.PERFORMANCES_TRANSITIONS_FADE);
	}

	public static void onBackPressed(AndrolifeApplication application, EffectListener listener) {

		final String effect = getEffect(application);

		if (effect.equalsIgnoreCase(SettingsActivity.PERFORMANCES_TRANSITIONS_FADE)) {
			listener.onTransitionEffect(R.anim.fadein, R.anim.fadeout);
		} else if (effect.equalsIgnoreCase(SettingsActivity.PERFORMANCES_TRANSITIONS_SLIDE)) {
			listener.onTransitionEffect(R.anim.translate_right_in, R.anim.translate_right_out);
		}
	}

	public static void startActivity(AndrolifeApplication application, EffectListener listener) {
		final String effect = getEffect(application);

		if (effect.equalsIgnoreCase(SettingsActivity.PERFORMANCES_TRANSITIONS_FADE)) {
			listener.onTransitionEffect(R.anim.fadein, R.anim.fadeout);
		} else if (effect.equalsIgnoreCase(SettingsActivity.PERFORMANCES_TRANSITIONS_SLIDE)) {
			listener.onTransitionEffect(R.anim.translate_left_in, R.anim.translate_left_out);
		}
	}

}
