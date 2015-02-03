package com.genesys.gms.mobile.callback.demo.legacy.ui;

import android.app.Activity;
import android.os.Bundle;

public class LogActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
        	.replace(android.R.id.content, new LogFragment())
        	.commit();
	}

}
