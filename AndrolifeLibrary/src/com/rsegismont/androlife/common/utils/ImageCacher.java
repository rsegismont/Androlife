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

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.rsegismont.androlife.common.BuildConfig;
import com.rsegismont.androlife.common.api.AndrolifeApi19;
import com.rsegismont.androlife.common.utils.ImageCacher.ImageCacheParams;

/**
 * This class handles disk and memory caching of bitmaps in conjunction with the
 * {@link ImageWorker} class and its subclasses. Use
 * {@link ImageCache#getInstance(FragmentManager, ImageCacheParams)} to get an
 * instance of this class, although usually a cache should be added directly to
 * an {@link ImageWorker} by calling
 * {@link ImageWorker#addImageCache(FragmentManager, ImageCacheParams)}.
 */
public class ImageCacher {
	private static final String TAG = "ImageCache";

	// Default memory cache size in kilobytes
	private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB

	// Default disk cache size in bytes
	private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 40; // 40MB

	// Compression settings when writing images to disk cache
	private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
	private static final int DEFAULT_COMPRESS_QUALITY = 90;
	private static final int DISK_CACHE_INDEX = 0;

	// Constants to easily toggle various caches
	private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
	private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
	private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

	private DiskLruCache mDiskLruCache;
	private LruCache<String, BitmapDrawable> mMemoryCacheData;
	private Map<String, Point> mMemoryCacheSize;
	private ImageCacheParams mCacheParams;
	private final Object mDiskCacheLock = new Object();
	private final Object mMemCacheLock = new Object();
	private boolean mDiskCacheStarting = true;

	private ConcurrentSkipListMap<Integer, SoftReference<Bitmap>> mReusableBitmaps;

	/**
	 * Create a new ImageCache object using the specified parameters. This
	 * should not be called directly by other classes, instead use
	 * {@link ImageCache#getInstance(FragmentManager, ImageCacheParams)} to
	 * fetch an ImageCache instance.
	 * 
	 * @param cacheParams
	 *            The cache parameters to use to initialize the cache
	 */
	public ImageCacher(ImageCacheParams cacheParams) {
		init(cacheParams);
	}

	/**
	 * Initialize the cache, providing all parameters.
	 * 
	 * @param cacheParams
	 *            The cache parameters to initialize the cache
	 */
	@SuppressLint("UseSparseArrays")
	private void init(ImageCacheParams cacheParams) {
		mCacheParams = cacheParams;

		// Set up memory cache
		if (mCacheParams.memoryCacheEnabled) {
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "Memory cache created (size = "
						+ mCacheParams.memCacheSize + ")");
			}

			// If we're running on Honeycomb or newer, then
			if (Utils.hasHoneycomb()) {
				mReusableBitmaps = new ConcurrentSkipListMap<Integer, SoftReference<Bitmap>>();
			}

			mMemoryCacheSize = new HashMap<String, Point>();

			mMemoryCacheData = new LruCache<String, BitmapDrawable>(
					mCacheParams.memCacheSize) {

				/**
				 * Notify the removed entry that is no longer being cached
				 */
				@Override
				protected void entryRemoved(boolean evicted, String key,
						BitmapDrawable oldValue, BitmapDrawable newValue) {
					if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {
						// The removed entry is a recycling drawable, so notify
						// it
						// that it has been removed from the memory cache
						((RecyclingBitmapDrawable) oldValue).setIsCached(false);
					} else {
						// The removed entry is a standard BitmapDrawable

						if (Utils.hasHoneycomb()) {
							// We're running on Honeycomb or later, so add the
							// bitmap
							// to a SoftRefrence set for possible use with
							// inBitmap later

							synchronized (mMemCacheLock) {
								if (evicted == true) {
									mReusableBitmaps.put(
											getBitmapSize(oldValue),
											new SoftReference<Bitmap>(oldValue
													.getBitmap()));
								}
							}
						}
					}
				}

				/**
				 * Measure item size in kilobytes rather than units which is
				 * more practical for a bitmap cache
				 */
				@Override
				protected int sizeOf(String key, BitmapDrawable value) {
					final int bitmapSize = getBitmapSize(value) / 1024;
					return bitmapSize == 0 ? 1 : bitmapSize;
				}
			};
		}

		// By default the disk cache is not initialized here as it should be
		// initialized
		// on a separate thread due to disk access.
		if (cacheParams.initDiskCacheOnCreate) {
			// Set up disk cache
			initDiskCache();
		}
	}

	/**
	 * Initializes the disk cache. Note that this includes disk access so this
	 * should not be executed on the main/UI thread. By default an ImageCache
	 * does not initialize the disk cache when it is created, instead you should
	 * call initDiskCache() to initialize it on a background thread.
	 */
	public void initDiskCache() {
		// Set up disk cache
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
				File diskCacheDir = mCacheParams.diskCacheDir;
				if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
					if (!diskCacheDir.exists()) {
						diskCacheDir.mkdirs();
					}

					try {
						mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1,
								Math.min(getUsableSpace(diskCacheDir),
										mCacheParams.diskCacheSize));
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "Disk cache initialized");
						}
					} catch (final IOException e) {
						mCacheParams.diskCacheDir = null;
						Log.e(TAG, "initDiskCache - " + e);
					}

				}
			}
			mDiskCacheStarting = false;
			mDiskCacheLock.notifyAll();
		}
	}

	/**
	 * Adds a bitmap to both memory and disk cache.
	 * 
	 * @param data
	 *            Unique identifier for the bitmap to store
	 * @param value
	 *            The bitmap drawable to store
	 */
	public void addBitmapToCache(String data, BitmapDrawable value,
			int reqWidth, int reqHeight, Point originalSize) {
		if (data == null || value == null) {
			return;
		}

		// Add to memory cache
		if (mMemoryCacheData != null) {
			if (RecyclingBitmapDrawable.class.isInstance(value)) {
				// The removed entry is a recycling drawable, so notify it
				// that it has been added into the memory cache
				((RecyclingBitmapDrawable) value).setIsCached(true);
			}

			final String memoryKey = data + reqWidth + reqHeight;
			final int inSampleSize = ImageResizer.calculateInSampleSize(
					originalSize, reqWidth, reqHeight);
			// Log.e("DEBUG", "PUT mMemoryCacheData=" + (data + inSampleSize));
			// Log.e("DEBUG", "PUT mMemoryCacheSize=" + memoryKey +
			// " : "+originalSize);
			mMemoryCacheData.put(data + inSampleSize, value);
			mMemoryCacheSize.put(memoryKey, originalSize);
		}

		/**
		 * synchronized (mDiskCacheLock) { // Add to disk cache if
		 * (mDiskLruCache != null) { final String key = hashKeyForDisk(data,
		 * reqWidth, reqHeight); OutputStream out = null; try {
		 * DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key); if (snapshot
		 * == null) { final DiskLruCache.Editor editor = mDiskLruCache
		 * .edit(key); if (editor != null) { out =
		 * editor.newOutputStream(DISK_CACHE_INDEX); Log.e("DEBUG",
		 * "value.getBitmap().width="+ value.getBitmap().getWidth());
		 * Log.e("DEBUG", "value.getBitmap().height="+
		 * value.getBitmap().getHeight());
		 * 
		 * value.getBitmap().compress( mCacheParams.compressFormat,
		 * mCacheParams.compressQuality, out); editor.commit(); out.close(); } }
		 * else { snapshot.getInputStream(DISK_CACHE_INDEX).close(); } } catch
		 * (final IOException e) { Log.e(TAG, "addBitmapToCache - " + e); }
		 * catch (Exception e) { Log.e(TAG, "addBitmapToCache - " + e); }
		 * finally { try { if (out != null) { out.close(); } } catch
		 * (IOException e) { } } } }
		 */
	}

	/**
	 * Get from memory cache.
	 * 
	 * @param data
	 *            Unique identifier for which item to get
	 * @return The bitmap drawable if found in cache, null otherwise
	 */
	public BitmapDrawable getBitmapFromMemCache(String data, int reqWidth,
			int reqHeight) {
		BitmapDrawable memValue = null;

		final String memoryKey = data + reqWidth + reqHeight;

		if (mMemoryCacheData != null) {
			
			// Log.e("DEBUG", "GET memoryKey=" + memoryKey);
			final Point value = mMemoryCacheSize.get(memoryKey);
			if (value != null) {
				final int inSampleSize = ImageResizer.calculateInSampleSize(
						value, reqWidth, reqHeight);
				memValue = mMemoryCacheData.get(data + inSampleSize);
			}

		}

		if (BuildConfig.DEBUG && memValue != null) {
			Log.d(TAG, "Memory cache hit");
		}
		return memValue;
	}

	/**
	 * Get from disk cache.
	 * 
	 * @param data
	 *            Unique identifier for which item to get
	 * @param point
	 * @return The bitmap if found in cache, null otherwise
	 */
	public Bitmap getBitmapFromDiskCache(String data, int reqWidth,
			int reqHeight, Point point) {
		final String key = hashKeyForDisk(data, reqWidth, reqHeight);
		Bitmap bitmap = null;

		synchronized (mDiskCacheLock) {
			while (mDiskCacheStarting) {
				try {
					mDiskCacheLock.wait();
				} catch (InterruptedException e) {
				}
			}
			if (mDiskLruCache != null) {
				InputStream inputStream = null;
				try {
					final DiskLruCache.Snapshot snapshot = mDiskLruCache
							.get(key);
					if (snapshot != null) {
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "Disk cache hit");
						}
						inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
						if (inputStream != null) {
							FileDescriptor fd = ((FileInputStream) inputStream)
									.getFD();

							// Decode bitmap, but we don't want to sample so
							// give
							// MAX_VALUE as the target dimensions
							bitmap = ImageResizer.decodeBitmapFromDescriptor(
									fd, reqWidth, reqHeight, this, point);
						}
					}
				} catch (final IOException e) {
					Log.e(TAG, "getBitmapFromDiskCache - " + e);
				} finally {
					try {
						if (inputStream != null) {
							inputStream.close();
						}
					} catch (IOException e) {
					}
				}
			}
			return bitmap;
		}
	}

	/**
	 * @param options
	 *            - BitmapFactory.Options with out* options populated
	 * @return Bitmap that case be used for inBitmap
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public synchronized Bitmap getBitmapFromReusableSet(
			BitmapFactory.Options options) {

		Bitmap bitmap = null;

		if (mReusableBitmaps != null && (mReusableBitmaps.size() != 0)) {

			final Iterator<Integer> iterator = mReusableBitmaps.keySet()
					.iterator();
			int length;
			Bitmap bitmapCandidate;

			while (iterator.hasNext()) {
				length = iterator.next();
				bitmapCandidate = mReusableBitmaps.get(length).get();

				if (null != bitmapCandidate && bitmapCandidate.isMutable()) {
					// Check to see it the item can be used for inBitmap
					if (canUseForInBitmap(bitmapCandidate, options)) {
						bitmap = bitmapCandidate;
						// Remove from reusable set so it can't be used again
						iterator.remove();
						break;
					}

				} else {
					// Remove from the set if the reference has been cleared.
					iterator.remove();
				}
			}

		}

		return bitmap;
	}

	public void clearMemoryCache() {
		if (mMemoryCacheData != null) {
			mMemoryCacheData.evictAll();
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "Memory cache cleared");
			}
		}
	}

	public void clearDiskCache() {
		synchronized (mDiskCacheLock) {
			mDiskCacheStarting = true;
			if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
				try {
					mDiskLruCache.delete();
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "Disk cache cleared");
					}
				} catch (IOException e) {
					Log.e(TAG, "clearCache - " + e);
				}
				mDiskLruCache = null;
				initDiskCache();
			}
		}
	}

	/**
	 * Clears both the memory and disk cache associated with this ImageCache
	 * object. Note that this includes disk access so this should not be
	 * executed on the main/UI thread.
	 */
	public void clearCache() {
		clearMemoryCache();
		clearDiskCache();
	}

	/**
	 * Flushes the disk cache associated with this ImageCache object. Note that
	 * this includes disk access so this should not be executed on the main/UI
	 * thread.
	 */
	public void flush() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					mDiskLruCache.flush();
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "Disk cache flushed");
					}
				} catch (IOException e) {
					Log.e(TAG, "flush - " + e);
				}
			}
		}
	}

	/**
	 * Closes the disk cache associated with this ImageCache object. Note that
	 * this includes disk access so this should not be executed on the main/UI
	 * thread.
	 */
	public void close() {
		synchronized (mDiskCacheLock) {
			if (mDiskLruCache != null) {
				try {
					if (!mDiskLruCache.isClosed()) {
						mDiskLruCache.close();
						mDiskLruCache = null;
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "Disk cache closed");
						}
					}
				} catch (IOException e) {
					Log.e(TAG, "close - " + e);
				}
			}
		}
	}

	/**
	 * A holder class that contains cache parameters.
	 */
	public static class ImageCacheParams {
		public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
		public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
		public File diskCacheDir;
		public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
		public int compressQuality = DEFAULT_COMPRESS_QUALITY;
		public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
		public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
		public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;

		/**
		 * Create a set of image cache parameters that can be provided to
		 * {@link ImageCache#getInstance(FragmentManager, ImageCacheParams)} or
		 * {@link ImageWorker#addImageCache(FragmentManager, ImageCacheParams)}.
		 * 
		 * @param context
		 *            A context to use.
		 * @param diskCacheDirectoryName
		 *            A unique subdirectory name that will be appended to the
		 *            application cache directory. Usually "cache" or "images"
		 *            is sufficient.
		 */
		public ImageCacheParams(Context context, String diskCacheDirectoryName) {
			diskCacheDir = getDiskCacheDir(context, diskCacheDirectoryName);
		}

		/**
		 * Sets the memory cache size based on a percentage of the max available
		 * VM memory. Eg. setting percent to 0.2 would set the memory cache to
		 * one fifth of the available memory. Throws
		 * {@link IllegalArgumentException} if percent is < 0.05 or > .8.
		 * memCacheSize is stored in kilobytes instead of bytes as this will
		 * eventually be passed to construct a LruCache which takes an int in
		 * its constructor.
		 * 
		 * This value should be chosen carefully based on a number of factors
		 * Refer to the corresponding Android Training class for more
		 * discussion: http://developer.android.com/training/displaying-bitmaps/
		 * 
		 * @param percent
		 *            Percent of available app memory to use to size memory
		 *            cache
		 */
		public void setMemCacheSizePercent(float percent) {
			if (percent < 0.05f || percent > 0.8f) {
				throw new IllegalArgumentException(
						"setMemCacheSizePercent - percent must be "
								+ "between 0.05 and 0.8 (inclusive)");
			}
			memCacheSize = Math.round(percent
					* Runtime.getRuntime().maxMemory() / 1024);
		}
	}

	static int getBytesPerPixel(Config config) {
		if (config == Config.ARGB_8888) {
			return 4;
		} else if (config == Config.RGB_565) {
			return 2;
		} else if (config == Config.ARGB_4444) {
			return 2;
		} else if (config == Config.ALPHA_8) {
			return 1;
		}
		return 1;
	}

	/**
	 * @param candidate
	 *            - Bitmap to check
	 * @param targetOptions
	 *            - Options that have the out* value populated
	 * @return true if <code>candidate</code> can be used for inBitmap re-use
	 *         with <code>targetOptions</code>
	 */
	private static boolean canUseForInBitmap(Bitmap candidate,
			BitmapFactory.Options targetOptions) {

		if (Build.VERSION.SDK_INT >= 19) {
			// From Android 4.4 (KitKat) onward we can re-use if the byte size
			// of
			// the new bitmap is smaller than the reusable bitmap candidate
			// allocation byte count.
			int width = targetOptions.outWidth / targetOptions.inSampleSize;
			int height = targetOptions.outHeight / targetOptions.inSampleSize;
			int byteCount = width * height
					* getBytesPerPixel(candidate.getConfig());
			return byteCount <= AndrolifeApi19
					.getAllocationByteCount(candidate);
		}

		// On earlier versions, the dimensions must match exactly and the
		// inSampleSize must be 1
		return candidate.getWidth() == targetOptions.outWidth
				&& candidate.getHeight() == targetOptions.outHeight
				&& targetOptions.inSampleSize == 1;

	}

	/**
	 * Get a usable cache directory (external if available, internal otherwise).
	 * 
	 * @param context
	 *            The context to use
	 * @param uniqueName
	 *            A unique directory name to append to the cache dir
	 * @return The cache dir
	 */
	public static File getDiskCacheDir(Context context, String uniqueName) {
		// Check if media is mounted or storage is built-in, if so, try and use
		// external cache dir
		// otherwise use internal cache dir

		String cachePath = "";
		final String storageState = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(storageState)
				&& (!isExternalStorageRemovable())) {
			cachePath = getExternalCacheDir(context).getPath();
		} else {
			cachePath = context.getCacheDir().getPath();
		}

		// TODO : catch where there is no sd
		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * A hashing method that changes a string (like a URL) into a hash suitable
	 * for using as a disk filename.
	 */
	public static String hashKeyForDisk(String key, int width, int height) {
		String cacheKey;
		key += width + height;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private static String bytesToHexString(byte[] bytes) {
		// http://stackoverflow.com/questions/332079
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	/**
	 * Get the size in bytes of a bitmap in a BitmapDrawable.
	 * 
	 * @param value
	 * @return size in bytes
	 */
	@TargetApi(12)
	public static int getBitmapSize(BitmapDrawable value) {
		Bitmap bitmap = value.getBitmap();

		if (Utils.hasHoneycombMR1()) {
			return bitmap.getByteCount();
		}
		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	/**
	 * Check if external storage is built-in or removable.
	 * 
	 * @return True if external storage is removable (like an SD card), false
	 *         otherwise.
	 */
	@TargetApi(9)
	public static boolean isExternalStorageRemovable() {
		if (Utils.hasGingerbread()) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}

	/**
	 * Get the external app cache directory.
	 * 
	 * @param context
	 *            The context to use
	 * @return The external cache dir
	 */
	@TargetApi(8)
	public static File getExternalCacheDir(Context context) {
		if (Utils.hasFroyo()) {
			return context.getExternalCacheDir();
		}

		// Before Froyo we need to construct the external cache dir ourselves
		final String cacheDir = "/Android/data/" + context.getPackageName()
				+ "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath()
				+ cacheDir);
	}

	/**
	 * Check how much usable space is available at a given path.
	 * 
	 * @param path
	 *            The path to check
	 * @return The space available in bytes
	 */
	@TargetApi(9)
	public static long getUsableSpace(File path) {
		if (Utils.hasGingerbread()) {
			return path.getUsableSpace();
		}
		final StatFs stats = new StatFs(path.getPath());
		return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
	}

}
