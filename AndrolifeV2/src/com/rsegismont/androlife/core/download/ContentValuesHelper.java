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

package com.rsegismont.androlife.core.download;

import android.content.ContentValues;

/**
 * Helper class to manage a pool of {@link ContentValues} objects
 * 
 * @author Romain Segismont
 * 
 */
public class ContentValuesHelper {

	private static int index = 0, mContainerSize = 0, mValuesength = 0;
	private static ContentValues[] valuesList;

	public static void init(int containerSize, int valuesLength) {
		valuesList = new ContentValues[containerSize];
		mContainerSize = containerSize;
		mValuesength = valuesLength;
		index = 0;
	}

	public static ContentValues obtain() {
		ContentValues values;
		values = valuesList[index];
		if (values != null) {
			values.clear();
		} else {
			values = new ContentValues(mValuesength);
			valuesList[index] = values;
		}
		index++;
		if (index >= mContainerSize) {
			index = 0;
		}
		return values;
	}

	public static void shutdown() {
		valuesList = null;
		mContainerSize = 0;
		mValuesength = 0;
		index = 0;
	}

}
