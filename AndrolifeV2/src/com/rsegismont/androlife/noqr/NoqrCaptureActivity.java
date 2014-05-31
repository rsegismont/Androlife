package com.rsegismont.androlife.noqr;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.rsegismont.androlife.R;
import com.rsegismont.androlife.application.AndrolifeApplication;
import com.rsegismont.androlife.common.Constantes;
import com.rsegismont.androlife.common.SdkUtils;
import com.rsegismont.androlife.common.SharedInformation;
import com.rsegismont.androlife.core.ui.SkeletonActivity;
import com.rsegismont.androlife.details.ProgrammesDetailActivity;
import com.rsegismont.androlife.noqr.camera.CameraManager;
import com.rsegismont.androlife.noqr.helper.BeepManager;
import com.rsegismont.androlife.noqr.helper.CaptureActivityHandler;
import com.rsegismont.androlife.noqr.helper.FinishListener;
import com.rsegismont.androlife.noqr.helper.InactivityTimer;
import com.rsegismont.androlife.noqr.helper.ViewfinderView;
import com.rsegismont.androlife.utils.AndrolifeUtils;

public final class NoqrCaptureActivity extends SkeletonActivity implements SurfaceHolder.Callback, View.OnClickListener {

	private static final String TAG = NoqrCaptureActivity.class.getSimpleName();
	public static final String NOLIFE_ONLINE = "online.nolife-tv.com";

	private TextView barcodeTextview;
	private BeepManager beepManager;
	private Button cameraDetailButton;
	private CameraManager cameraManager;
	private Button cameraSeeButton;
	private String characterSet;
	public long dateUtc;
	private Collection<BarcodeFormat> decodeFormats;
	private TextView descriptionView;
	private CaptureActivityHandler handler;
	private boolean hasSurface;
	private ImageView imageView;
	private InactivityTimer inactivityTimer;
	private Result lastResult;
	private AndrolifeUriParser mAndrolifeUriParser;
	private LinearLayout previewLayout;
	private ProgressBar progressBar;
	private View resultView;
	private Result savedResultToShow;
	private TextView statusView;
	private ViewfinderView viewfinderView;

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		// Bitmap isn't used yet -- will be used soon
		if (handler == null) {
			savedResultToShow = result;
		} else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	private static void drawLine(Canvas paramCanvas, Paint paramPaint, ResultPoint paramResultPoint1,
			ResultPoint paramResultPoint2) {
		paramCanvas.drawLine(paramResultPoint1.getX(), paramResultPoint1.getY(), paramResultPoint2.getX(),
				paramResultPoint2.getY(), paramPaint);
	}

	private void drawResultPoints(Bitmap barcode, Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.result_image_border));
			paint.setStrokeWidth(3.0f);
			paint.setStyle(Paint.Style.STROKE);
			Rect border = new Rect(2, 2, barcode.getWidth() - 2, barcode.getHeight() - 2);
			canvas.drawRect(border, paint);

			paint.setColor(getResources().getColor(R.color.result_points));
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1]);
			} else if (points.length == 4
					&& (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
				// Hacky special case -- draw two lines, for the barcode and
				// metadata
				drawLine(canvas, paint, points[0], points[1]);
				drawLine(canvas, paint, points[2], points[3]);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					canvas.drawPoint(point.getX(), point.getY(), paint);
				}
			}
		}
	}

	private void handleDecodeInternally(Result paramResult, Bitmap paramBitmap) {
		statusView.setVisibility(View.GONE);
		viewfinderView.setVisibility(View.GONE);
		resultView.setVisibility(View.VISIBLE);

		launchAndrolifeUriParser(paramBitmap, paramResult);
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	private void resetStatusView() {

		this.progressBar.setVisibility(View.VISIBLE);
		cameraDetailButton.setVisibility(View.INVISIBLE);
		this.cameraDetailButton.setEnabled(false);
		this.resultView.setVisibility(View.GONE);
		this.imageView.setImageBitmap(null);
		this.descriptionView.setText("");
		this.statusView.setText(R.string.msg_default_status);
		this.statusView.setVisibility(View.VISIBLE);
		this.viewfinderView.setVisibility(View.VISIBLE);
		this.lastResult = null;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	protected int getActivityOrientation() {
		return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	public Handler getHandler() {
		return handler;
	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public void handleDecode(Result paramResult, Bitmap paramBitmap) {
		this.inactivityTimer.onActivity();
		this.lastResult = paramResult;
		this.beepManager.playBeepSoundAndVibrate();
		drawResultPoints(paramBitmap, paramResult);
		handleDecodeInternally(paramResult, paramBitmap);
	}

	public void launchAndrolifeUriParser(Bitmap bitmapResult, Result zxingResult) {
		if (mAndrolifeUriParser != null) {
			mAndrolifeUriParser.cancel(true);
			mAndrolifeUriParser = null;
		}
		mAndrolifeUriParser = new AndrolifeUriParser(bitmapResult);
		mAndrolifeUriParser.execute(zxingResult);
	}

	public void onClick(View paramView) {
		switch (paramView.getId()) {
		default:
			break;

		case R.id.androlife_camera_detail:
			final Intent localIntent = new Intent(this, ProgrammesDetailActivity.class);
			localIntent.putExtra(Constantes.DETAIL_DATE_TIME, this.dateUtc);
			startActivity(localIntent);
			finish();
			break;
		case R.id.androlife_camera_see:
			try {
				final String barcode = this.barcodeTextview.getText().toString();
				Intent intent;
				if (barcode.contains(NOLIFE_ONLINE)) {
					intent = SdkUtils.prepare_web(getApplicationContext(), AndrolifeUtils.getMobileUrl(barcode));
					cameraSeeButton.setText(R.string.androlife_camera_see);
				} else {
					intent = SdkUtils.prepare_web(getApplicationContext(), barcode);
					cameraSeeButton.setText(R.string.androlife_camera_see_normal);
				}
				startActivity(intent);
				finish();
			} catch (Throwable localThrowable) {
			}
			break;
		case R.id.androlife_camera_scananother:
			cameraSeeButton.setText(R.string.androlife_camera_see_normal);
			if (this.lastResult != null) {
				restartPreviewAfterDelay(0);
			}
			break;
		}

	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		try {

			Window window = getWindow();
			window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			setContentView(R.layout.androlife_noqr_capture);

			viewfinderView = (ViewfinderView) findViewById(R.id.androlife_viewfinder_view);
			this.resultView = findViewById(R.id.result_view);
			this.barcodeTextview = ((TextView) findViewById(R.id.androlife_camera_url));
			this.imageView = ((ImageView) this.resultView.findViewById(R.id.androlife_camera_imageview));
			this.descriptionView = ((TextView) this.resultView.findViewById(R.id.androlife_camera_description));
			this.previewLayout = ((LinearLayout) findViewById(R.id.androlife_camera_preview));
			this.cameraDetailButton = ((Button) findViewById(R.id.androlife_camera_detail));
			cameraDetailButton.setVisibility(View.INVISIBLE);
			this.cameraSeeButton = ((Button) findViewById(R.id.androlife_camera_see));
			this.progressBar = ((ProgressBar) findViewById(R.id.androlife_camera_loading));
			this.cameraDetailButton.setOnClickListener(this);
			this.cameraSeeButton.setOnClickListener(this);
			findViewById(R.id.androlife_camera_scananother).setOnClickListener(this);
			this.hasSurface = false;
			this.inactivityTimer = new InactivityTimer(this);
			this.beepManager = new BeepManager(this);
			PreferenceManager.setDefaultValues(this, R.xml.pref_qr, false);

		} catch (Throwable localThrowable) {
			localThrowable.printStackTrace();
		}
	}

	protected void onDestroy() {
		this.inactivityTimer.shutdown();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (lastResult != null) {
				restartPreviewAfterDelay(0L);
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
			// Handle these events so they don't launch the Camera app
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		cameraManager.closeDriver();
		if (!hasSurface) {
			((SurfaceView) findViewById(R.id.preview_view)).getHolder().removeCallback(this);
		}
		super.onPause();
	}

	protected void onResume() {
		super.onResume();

		cameraManager = new CameraManager(getApplicationContext());

		viewfinderView.setCameraManager(cameraManager);
		statusView = (TextView) findViewById(R.id.status_view);
		this.handler = null;
		this.lastResult = null;
		resetStatusView();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		beepManager.updatePrefs();
		inactivityTimer.onResume();
		decodeFormats = null;
		characterSet = null;
	}

	public void restartPreviewAfterDelay(long paramLong) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(R.id.restart_preview, paramLong);
		}
		resetStatusView();
	}

	public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int paramInt1, int paramInt2, int paramInt3) {
	}

	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null)
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
		if (!hasSurface) {
			hasSurface = true;
			initCamera(surfaceHolder);
		}
	}

	public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder) {
		this.hasSurface = false;
	}

	private class AndrolifeUriParser extends AsyncTask<Result, String, UriParserResult> {
		private Bitmap barcode;

		public static final String HTTP_PREFIX = "http://";

		public AndrolifeUriParser(Bitmap paramBitmap) {
			this.barcode = paramBitmap;
		}

		protected UriParserResult doInBackground(final Result... paramVarArgs) {
			String urlToParse = paramVarArgs[0].getText();
			if (!urlToParse.startsWith(HTTP_PREFIX)) {
				urlToParse = HTTP_PREFIX + urlToParse;
			}

			String tmpUrl = urlToParse;
			while (tmpUrl != null) {
				urlToParse = tmpUrl;
				tmpUrl = resolve(Uri.parse(tmpUrl));
			}

			final Cursor cursor = getContentResolver().query(SharedInformation.CONTENT_URI_PROGRAMMES, null,
					SharedInformation.DatabaseColumn.NolifeOnlineURL.stringValue + "=?", new String[] { urlToParse },
					null);
			UriParserResult uriResult = new UriParserResult();
			if (cursor != null) {
				if (cursor.moveToNext()) {
					dateUtc = cursor.getLong(cursor
							.getColumnIndex(SharedInformation.DatabaseColumn.DATE_UTC.stringValue));
					String str3 = cursor.getString(cursor
							.getColumnIndex(SharedInformation.DatabaseColumn.SCREENSHOT.stringValue));
					String str4 = cursor.getString(cursor
							.getColumnIndex(SharedInformation.DatabaseColumn.SCREENSHOT_EMISSION.stringValue));
					uriResult.description = cursor.getString(cursor
							.getColumnIndex(SharedInformation.DatabaseColumn.DESCRIPTION.stringValue));
					if (TextUtils.isEmpty(str3)) {
						uriResult.imageUrl = str4;
					} else {
						uriResult.imageUrl = str3;
					}
				}
				cursor.close();
			}
			return uriResult;
		}

		protected void onPostExecute(UriParserResult paramUriParserResult) {
			super.onPostExecute(paramUriParserResult);
			progressBar.setVisibility(View.GONE);
			if (paramUriParserResult.description != null) {
				previewLayout.setVisibility(View.VISIBLE);
				cameraDetailButton.setEnabled(true);
				descriptionView.setText(paramUriParserResult.description);
				// Picasso.with(imageView.getContext())
				// .load(paramUriParserResult.imageUrl).fit()
				// .into(imageView);
				AndrolifeApplication.instance.mImageDownloader.loadImage(paramUriParserResult.imageUrl, imageView);
			} else {

				if (this.barcode == null) {
					imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.toutsuite));
				} else {
					imageView.setImageBitmap(this.barcode);
				}
			}

			if (barcodeTextview.getText().toString().contains(NOLIFE_ONLINE)) {
				cameraSeeButton.setText(R.string.androlife_camera_see);
				cameraDetailButton.setVisibility(View.VISIBLE);
			} else {
				cameraSeeButton.setText(R.string.androlife_camera_see_normal);
			}

		}

		protected void onProgressUpdate(String... progressResult) {
			super.onProgressUpdate(progressResult);
			barcodeTextview.setText(progressResult[0]);
		}

		public String resolve(Uri uri) {
			String str = "";

			try {
				str = uri.toString();
				publishProgress(str);
				HttpURLConnection urlConnection = (HttpURLConnection) new URL(str).openConnection();
				urlConnection.setInstanceFollowRedirects(false);
				str = urlConnection.getHeaderField("Location");
				Log.e("DEBUG", "Location=" + str);
				urlConnection.disconnect();
			} catch (Exception localException) {

				localException.printStackTrace();

			}

			return str;

		}
	}

	private class UriParserResult {
		String description;
		String imageUrl;
	}
}