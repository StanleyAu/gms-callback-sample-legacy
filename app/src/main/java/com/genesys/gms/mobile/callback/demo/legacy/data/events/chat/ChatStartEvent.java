package com.genesys.gms.mobile.callback.demo.legacy.data.events.chat;

/**
 * Created by stau on 2/6/2015.
 */
public class ChatStartEvent {
    public final String serviceId;
    public final boolean verbose;
    public final String notifyBy;
    public final String firstName;
    public final String lastName;
    public final String email;
    public final String subject;
    public final String subscriptionId;
    public final String userDisplayName;
    public final String pushNotificationDeviceId;
    public final String pushNotificationType;
    public final String pushNotificationLanguage;
    public final boolean pushNotificationDebug;

    public ChatStartEvent(String serviceId,
                          boolean verbose,
                          String notifyBy,
                          String firstName,
                          String lastName,
                          String email,
                          String subject,
                          String subscriptionId,
                          String userDisplayName,
                          String pushNotificationDeviceId,
                          String pushNotificationType,
                          String pushNotificationLanguage,
                          boolean pushNotificationDebug) {
        this.serviceId = serviceId;
        this.verbose = verbose;
        this.notifyBy = notifyBy;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.subject = subject;
        this.subscriptionId = subscriptionId;
        this.userDisplayName = userDisplayName;

        this.pushNotificationDeviceId = pushNotificationDeviceId;
        this.pushNotificationType = pushNotificationType;
        this.pushNotificationLanguage = pushNotificationLanguage;
        this.pushNotificationDebug = pushNotificationDebug;
    }

    @Override public String toString() {
        return getClass().getName() + "@" + hashCode() +
            "[" +
            "serviceId=" + serviceId +
            ",verbose=" + verbose +
            ",notifyBy=" + notifyBy +
            ",firstName=" + firstName +
            ",lastName=" + lastName +
            ",email=" + email +
            ",subject=" + subject +
            ",subscriptionId=" + subscriptionId +
            ",userDisplayName=" + userDisplayName +
            "]";
    }
}