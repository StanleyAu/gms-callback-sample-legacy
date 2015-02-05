package com.genesys.gms.mobile.callback.demo.legacy.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import com.genesys.gms.mobile.callback.demo.legacy.BaseActivity;

/**
 * Activities extending this class will be bound to the {@link GenesysService}, and will be
 * able to handle intents by implementing the abstract method {@link #handleIntent(Intent)}.
 */
public abstract class AbstractGenesysActivity extends BaseActivity {

	protected GenesysService genesysService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindGenesysService();		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	@Override
	protected void onDestroy() {
		unbindService(genesysServiceConnection);
		super.onDestroy();
	}
		
	private ServiceConnection genesysServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			genesysService = ((GenesysService.LocalBinder)service).getService();
			onGenesysServiceConnected(genesysService);

			if (getIntent() != null)
				handleIntent(getIntent());
		}

		@Override public void onServiceDisconnected(ComponentName name) {}
	};

	private void bindGenesysService() {
		Intent intent = new Intent(this, GenesysService.class);
		boolean bound = bindService(intent, genesysServiceConnection, BIND_AUTO_CREATE);
		if (!bound)
			throw new RuntimeException("GenesysService not bound");
	}

	/**
	 * Implement this method in order to handle intents received by this activity, either
	 * when the activity is started or when it is running. The <code>protected</code>
	 * {@link #genesysService} field is available for this method to use it.
	 * 
	 * @param intent Intent received when this activity is started or when running.
	 */
	protected abstract void handleIntent(Intent intent);
	
	protected void onGenesysServiceConnected(GenesysService genesysService) {}
	
}
