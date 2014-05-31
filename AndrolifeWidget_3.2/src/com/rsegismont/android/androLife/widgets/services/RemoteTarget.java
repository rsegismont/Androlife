package com.rsegismont.android.androLife.widgets.services;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;

import com.rsegismont.android.androLife.widgets.R;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

public class RemoteTarget implements Target {

	private RemoteViews rv;

	public RemoteTarget(RemoteViews rv) {
		this.rv = rv;
	}

	@Override
	public void onBitmapFailed(Drawable arg0) {

	}

	@Override
	public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
		rv.setImageViewBitmap(R.id.widget_image, arg0);

	}

	@Override
	public void onPrepareLoad(Drawable arg0) {

	}

}
