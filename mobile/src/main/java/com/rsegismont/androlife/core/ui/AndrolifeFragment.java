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

package com.rsegismont.androlife.core.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.SherlockFragment;
import com.rsegismont.androlife.core.utils.ActivityEffectsHelper;

/**
 * Generic class for standard {@link Fragment}
 * 
 * @author Romain Segismont
 * @version 1.0
 * @since 1.0
 * 
 */
public class AndrolifeFragment extends SherlockFragment {
	public SkeletonActivity getAndrolifeActivity() {
		return (SkeletonActivity) getActivity();
	}



	public void startActivity(Intent paramIntent) {
		super.startActivity(paramIntent);
		ActivityEffectsHelper.startActivity(getAndrolifeActivity().androlifeApplication, getAndrolifeActivity());
	}
}
