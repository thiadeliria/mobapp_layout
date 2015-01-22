package com.stackbase.mobapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class ManageActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.borrower_list);
	}
}
