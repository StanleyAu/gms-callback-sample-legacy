package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import com.genesys.gms.mobile.callback.demo.legacy.ForApplication;
import com.genesys.gms.mobile.callback.demo.legacy.data.async.GcmRegisterAsync;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.GcmReceiveEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.GcmRegisterEvent;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.NoSubscriberEvent;
import hugo.weaving.DebugLog;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stau on 30/11/2014.
 */

public class GcmManager {
    private final GoogleCloudMessaging googleCloudMessaging;
    private final EventBus bus;
    private final Context context;
    private AtomicInteger idGen = new AtomicInteger();

    NotificationManager mNotificationManager;
    // private static final long[] VIBRATE_PATTERN = {350L, 200L, 350L};
    public static final String GCM_NOTIFICATION_ID = "gcm_notification_id";
    private Object savedEvent;

    @Inject @DebugLog
    public GcmManager(GoogleCloudMessaging googleCloudMessaging, @ForApplication Context context) {
        this.googleCloudMessaging = googleCloudMessaging;
        this.bus = EventBus.getDefault();
        this.context = context;
    }

    public void onEvent(GcmRegisterEvent event) {
        // Cache should be consulted prior to delivering GcmRegisterEvent
        GcmRegisterAsync async = new GcmRegisterAsync(googleCloudMessaging, event.senderId);
        async.execute();
    }

    /**
     * DeadEvent subscription allows us to observe events for which there are
     * no subscribers. This is particularly handy for seeing if there is an
     * Activity around to handle our GCM event (in the event that the application
     * has been put into the background when our notification arrives).
     *
     * @param event Returned event due to no subscribers
     */
    public void onEvent(NoSubscriberEvent event) {
        CharSequence message = "Message received!";
        if(event.originalEvent instanceof GcmReceiveEvent) {
            savedEvent = event.originalEvent;
            CharSequence extraMessage = ((GcmReceiveEvent) savedEvent).extras.getCharSequence("message");
            if(extraMessage != null) {
                message = extraMessage;
            }
        }

        /*
        NotificationCompat.Builder builder =
            getNotificationBuilder(R.drawable.ic_launcher, context.getResources().getString(R.string.launcher_name), message);

        // int notificationId = mNotifyId.getAndIncrement();
        Intent resultIntent = new Intent(context, MainActivity.class);
        //resultIntent.putExtra(GCM_NOTIFICATION_ID, notificationId);
        resultIntent.putExtra(GCM_NOTIFICATION_ID, 0);

        // TaskStack allows us to go back to Home from the Notification action
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
            stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
            );
        builder.setContentIntent(resultPendingIntent);
        //getNotificationManager().notify(notificationId, builder.build());
        /*
         * Force notification ID to 0 to prevent creating new entries in the
         * notification drawer.
         */
        //getNotificationManager().notify(0, builder.build());
    }

    protected NotificationManager getNotificationManager() {
        if(mNotificationManager==null) {
            mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    protected NotificationCompat.Builder getNotificationBuilder(int icon, CharSequence title, CharSequence text) {
        // TODO: Is it worthwhile to cache this at all?
        // setDefaults will default the notification vibration/sound/lights settings
        return new NotificationCompat.Builder(context)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true);
    }
}