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
