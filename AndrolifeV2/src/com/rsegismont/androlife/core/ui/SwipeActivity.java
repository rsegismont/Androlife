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

package com.rsegismont.androlife.core.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import com.actionbarsherlock.view.MenuItem;
import com.rsegismont.androlife.common.SdkUtils;

/**
 * Activity that will have a {@link DrawerLayout}
 * 
 * @author Romain Segismont
 * @version 1.0
 * @since 1.0
 * 
 */
public abstract class SwipeActivity extends SkeletonActivity {
	protected DrawerLayout mDrawerLayout;
	protected ActionBarDrawerToggle mDrawerToggle;

	@Override
	public void onConfigurationChanged(Configuration paramConfiguration) {
		super.onConfigurationChanged(paramConfiguration);
		if (this.mDrawerToggle != null)
			this.mDrawerToggle.onConfigurationChanged(paramConfiguration);
	}

	@Override
	protected void onPostCreate(Bundle paramBundle) {
		super.onPostCreate(paramBundle);
		if (this.mDrawerToggle != null)
			this.mDrawerToggle.syncState();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem paramMenuItem) {

		switch (paramMenuItem.getItemId()) {
		case android.R.id.home:
			if (!SdkUtils.isTabletLandscape(getApplicationContext())) {
				if (!this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
					this.mDrawerLayout.openDrawer(GravityCompat.START);
				} else {
					this.mDrawerLayout.closeDrawer(GravityCompat.START);
				}
			}
			return true;
		default:
			return super.onOptionsItemSelected(paramMenuItem);
		}

	}

}