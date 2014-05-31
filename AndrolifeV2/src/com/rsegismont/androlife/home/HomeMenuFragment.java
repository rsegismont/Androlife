package com.rsegismont.androlife.home;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.rsegismont.androlife.R;
import com.rsegismont.androlife.about.AboutActivity;
import com.rsegismont.androlife.common.Constantes;
import com.rsegismont.androlife.core.ui.AndrolifeFragment;
import com.rsegismont.androlife.home.MenuAdapter.DataHolder;
import com.rsegismont.androlife.noqr.NoqrCaptureActivity;
import com.rsegismont.androlife.programlist.ProgramListActivity;

public class HomeMenuFragment extends AndrolifeFragment implements
		OnItemClickListener {

	private static final int TYPE_DEFAULT = -1;
	public static final int TYPE_AUTRES = 1;
	public static final int TYPE_PROGRAMMES = 0;
	
	public static final String TYPE = "type";
	private List<MenuAdapter.DataHolder> texts;
	private MenuAdapter mAdapter;

	public void addGuide() {
		MenuAdapter.DataHolder localDataHolder1 = new MenuAdapter.DataHolder();
		localDataHolder1.text = R.string.home_menu_programmes;
		this.texts.add(localDataHolder1);
		MenuAdapter.DataHolder localDataHolder2 = new MenuAdapter.DataHolder();
		localDataHolder2.text = R.string.home_menu_news;
		this.texts.add(localDataHolder2);
		MenuAdapter.DataHolder localDataHolder3 = new MenuAdapter.DataHolder();
		localDataHolder3.text = R.string.home_menu_smartguide;
		this.texts.add(localDataHolder3);
	}

	public void addOthers() {
		/**
		 * MenuAdapter.DataHolder localDataHolder1 = new
		 * MenuAdapter.DataHolder(); if
		 * (getActivity().getPackageManager().hasSystemFeature
		 * ("android.hardware.camera")) { localDataHolder1.text =
		 * R.string.home_menu_qr; this.texts.add(localDataHolder1); }
		 */
		MenuAdapter.DataHolder localDataHolder2 = new MenuAdapter.DataHolder();
		localDataHolder2.text = R.string.home_menu_apropos;
		this.texts.add(localDataHolder2);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		texts = new LinkedList<DataHolder>();

		int mType = TYPE_DEFAULT;
		try {
			mType = getArguments().getInt(TYPE, TYPE_DEFAULT);
		} catch (Throwable e) {

		}

		switch (mType) {
		case TYPE_PROGRAMMES:

			addGuide();
			break;

		case TYPE_AUTRES:

			addOthers();

			break;

		default:

			addGuide();
			addOthers();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final ListView listview = new ListView(getActivity());
		mAdapter = new MenuAdapter(getActivity(),
				R.layout.home_activities_list_item, this.texts);
		listview.setAdapter(mAdapter);
		listview.setOnItemClickListener(this);
		listview.setBackgroundColor(Color.TRANSPARENT);
		listview.setCacheColorHint(Color.TRANSPARENT);
		return listview;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		Intent intent;

		if (id == R.string.home_menu_programmes) {

			intent = new Intent(getActivity(), ProgramListActivity.class);
			intent.putExtra(Constantes.TYPE, Constantes.CURSOR_FULL);
			startActivity(intent);
		} else if (id == R.string.home_menu_smartguide) {

			intent = new Intent(getActivity(), ProgramListActivity.class);
			intent.putExtra(Constantes.TYPE, Constantes.CURSOR_SELECTION);
			startActivity(intent);
		} else if (id == R.string.home_menu_news) {

			intent = new Intent(getActivity(), ProgramListActivity.class);
			intent.putExtra(Constantes.TYPE, Constantes.CURSOR_NEWS);
			startActivity(intent);
		} else if (id == R.string.home_menu_apropos) {

			intent = new Intent(getActivity(), AboutActivity.class);
			startActivity(intent);
		}

		else if (id == R.string.home_menu_qr) {
			intent = new Intent(getActivity(), NoqrCaptureActivity.class);
			startActivity(intent);
		}
	}

}