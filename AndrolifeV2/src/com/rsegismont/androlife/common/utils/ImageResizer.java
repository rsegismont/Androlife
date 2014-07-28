/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rsegismont.androlife.common.utils;

import java.io.FileDescriptor;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;

import com.rsegismont.androlife.BuildConfig;

/**
 * A simple subclass of {@link ImageWorker} that resizes images from resources
 * given a target width and height. Useful for when the input images might be
 * too large to simply load directly into memory.
 */
public class ImageResizer extends ImageWorker {

	/**
	 * Initialize providing a single target image size (used for both width and
	 * height);
	 * 
	 * @param context
	 * @param imageSize
	 */
	public ImageResizer(Context context) {
		super(context);
	}

	@Override
	protected Bitmap processBitmap(Object data, int reqWidth, int reqHeight,
			Point point) {
		return processBitmap(Integer.parseInt(String.valueOf(data)), reqWidth,
				reqHeight, point);
	}

	/**
	 * Decode and sample down a bitmap from a file input stream to the requested
	 * width and height.
	 * 
	 * @param fileDescriptor
	 *            The file descriptor to read from
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @param cache
	 *            The ImageCache used to find candidate bitmaps for use with
	 *            inBitmap
	 * @param point
	 * @return A bitmap sampled down from the original with the same aspect
	 *         ratio and dimensions that are equal to or greater than the
	 *         requested width and height
	 */
	public static Bitmap decodeBitmapFromDescriptor(
			FileDescriptor fileDescriptor, int reqWidth, int reqHeight,
			ImageCacher cache, Point point) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

		if (point != null) {
			point.x = options.outWidth;
			point.y = options.outHeight;
		}

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		// If we're running on Honeycomb or newer, try to use inBitmap
		if (Utils.hasHoneycomb()) {
			addInBitmapOptions(options, cache);
		}

		return BitmapFactory
				.decodeFileDescriptor(fileDescriptor, null, options);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void addInBitmapOptions(BitmapFactory.Options options,
			ImageCacher cache) {
		// inBitmap only works with mutable bitmaps so force the decoder to
		// return mutable bitmaps.
		options.inMutable = true;

		if (cache != null && (options.inSampleSize == 1)) {
			// Try and find a bitmap to use for inBitmap
			Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

			if (inBitmap != null) {
				if (BuildConfig.DEBUG) {
					Log.e("PP", "Found bitmap to use for inBitmap");
				}
				options.inBitmap = inBitmap;
			}
		}
	}

	/**
	 * Calculate an inSampleSize for use in a {@link BitmapFactory.Options}
	 * object when decoding bitmaps using the decode* methods from
	 * {@link BitmapFactory}. This implementation calculates the closest
	 * inSampleSize that will result in the final decoded bitmap having a width
	 * and height equal to or larger than the requested width and height. This
	 * implementation does not ensure a power of 2 is returned for inSampleSize
	 * which can be faster when decoding but results in a larger bitmap which
	 * isn't as useful for caching purposes.
	 * 
	 * @param options
	 *            An options object with out* params already populated (run
	 *            through a decode* method with inJustDecodeBounds==true
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @return The value to be used for inSampleSize
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		return calculateInSampleSize(options.outWidth, options.outHeight,
				reqWidth, reqHeight);

	}

	/**
	 * Calculate an inSampleSize width and height when decoding bitmaps using
	 * the decode* methods from {@link BitmapFactory}. This implementation
	 * calculates the closest inSampleSize that will result in the final decoded
	 * bitmap having a width and height equal to or larger than the requested
	 * width and height. This implementation does not ensure a power of 2 is
	 * returned for inSampleSize which can be faster when decoding but results
	 * in a larger bitmap which isn't as useful for caching purposes.
	 * 
	 * @param options
	 *            An options object with out* params already populated (run
	 *            through a decode* method with inJustDecodeBounds==true
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @return The value to be used for inSampleSize
	 */
	public static int calculateInSampleSize(Point originalValues, int reqWidth,
			int reqHeight) {
		return calculateInSampleSize(originalValues.x, originalValues.y,
				reqWidth, reqHeight);
	}

	public static int calculateInSampleSize(int width, int height,
			int reqWidth, int reqHeight) {

		final int strech_width = Math.round((float) width / (float) reqWidth);
		final int strech_height = Math
				.round((float) height / (float) reqHeight);

		if (strech_width <= strech_height) {
			return strech_height;
		} else {
			return strech_width;
		}
	}
}
