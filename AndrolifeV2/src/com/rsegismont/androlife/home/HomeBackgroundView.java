package com.rsegismont.androlife.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class HomeBackgroundView extends RelativeLayout {

	private Paint mPaint;
	private int mWidth;
	private Rect mRect;
	private int horizontalMargin = -1, verticalMargin = -1;

	public HomeBackgroundView(Context context) {
		super(context);
		init();
	}

	public HomeBackgroundView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HomeBackgroundView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setColor(Color.WHITE);

		setWillNotDraw(false);

		if (getContext() instanceof HomeActivity) {
			final HomeActivity homeInstance = (HomeActivity) getContext();
			horizontalMargin = homeInstance.mHorizontalMargin;
			verticalMargin = homeInstance.mVerticalMargin;
		}

		if (mRect == null) {
			mRect = new Rect();
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mWidth > 0) {
			canvas.drawRect(mRect, mPaint);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w > 0) {
			this.mWidth = w;

			if (horizontalMargin >= 0) {

				mRect.left = horizontalMargin;
				mRect.right = w - horizontalMargin;
				mRect.top = verticalMargin;
				mRect.bottom = h - verticalMargin;

			}
		}
	}
}
