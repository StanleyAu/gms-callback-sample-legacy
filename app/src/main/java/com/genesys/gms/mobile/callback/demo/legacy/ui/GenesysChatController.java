package com.genesys.gms.mobile.callback.demo.legacy.ui;

import android.content.Context;
import android.content.SharedPreferences;
import com.genesys.gms.mobile.callback.demo.legacy.ForActivity;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import de.greenrobot.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Created by Stan on 2/8/2015.
 */
public class GenesysChatController {
    private final Logger log = LoggerFactory.getLogger(Globals.GENESYS_LOG_TAG);
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final EventBus bus;

    @Inject
    public GenesysChatController(@ForActivity Context context, SharedPreferences sharedPreferences) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
        this.bus = EventBus.getDefault();
    }
}
