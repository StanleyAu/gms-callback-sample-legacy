package com.genesys.gms.mobile.callback.demo.legacy.data.api;

import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatBasicResponse;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatException;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.ChatResponse;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.UnknownErrorEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.*;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by stau on 2/6/2015.
 */
@Singleton
public class ChatServiceManager {
    private final ChatService chatService;
    private final EventBus bus;

    @Inject
    public ChatServiceManager(ChatService chatService) {
        this.chatService = chatService;
        this.bus = EventBus.getDefault();
    }

    public void onEvent(ChatStartEvent event) {
        chatService.startChat(
            event.serviceId,
            event.verbose,
            event.notifyBy,
            event.firstName,
            event.lastName,
            event.email,
            event.subject,
            event.subscriptionId,
            event.userDisplayName,
            event.pushNotificationDeviceId, // Added for Jeff's changes
            event.pushNotificationType,
            event.pushNotificationLanguage,
            event.pushNotificationDebug,
            new Callback<ChatResponse>() {
                @Override
                public void success(ChatResponse chatResponse, Response response) {
                    bus.post(new ChatResponseEvent(
                        chatResponse,
                        ChatResponseEvent.ChatRequestType.START
                    ));
                }

                @Override
                public void failure(RetrofitError error) {
                    try {
                        if (error.getResponse() != null) {
                            ChatException body = (ChatException) error.getBodyAs(ChatException.class);
                            bus.post(new ChatErrorEvent(body));
                            return;
                        }
                    } catch (Exception e) {;}
                    bus.post(new UnknownErrorEvent(error));
                }
            }
        );
    }

    public void onEvent(ChatSendEvent event) {
        chatService.send(
            event.serviceId,
            event.message,
            event.verbose,
            new Callback<ChatResponse>() {
                @Override
                public void success(ChatResponse chatResponse, Response response) {
                    bus.post(new ChatResponseEvent(
                        chatResponse,
                        ChatResponseEvent.ChatRequestType.SEND
                    ));
                }

                @Override
                public void failure(RetrofitError error) {
                    try {
                        if (error.getResponse() != null) {
                            ChatException body = (ChatException) error.getBodyAs(ChatException.class);
                            bus.post(new ChatErrorEvent(body));
                            return;
                        }
                    } catch (Exception e) {
                        ;
                    }
                    bus.post(new UnknownErrorEvent(error));
                }
            }
        );
    }

    public void onEvent(ChatRefreshEvent event) {
        chatService.refresh(
            event.serviceId,
            event.transcriptPosition,
            event.message,
            event.verbose,
            new Callback<ChatResponse>() {
                @Override
                public void success(ChatResponse chatResponse, Response response) {
                    bus.post(new ChatResponseEvent(
                        chatResponse,
                        ChatResponseEvent.ChatRequestType.REFRESH
                    ));
                }

                @Override
                public void failure(RetrofitError error) {
                    try {
                        if (error.getResponse() != null) {
                            ChatException body = (ChatException) error.getBodyAs(ChatException.class);
                            bus.post(new ChatErrorEvent(body));
                            return;
                        }
                    } catch (Exception e) {
                        ;
                    }
                    bus.post(new UnknownErrorEvent(error));
                }
            }
        );
    }

    public void onEvent(ChatStartTypingEvent event) {
        chatService.startTyping(event.serviceId, event.verbose, new Callback<ChatResponse>() {
            @Override
            public void success(ChatResponse chatResponse, Response response) {
                bus.post(new ChatResponseEvent(
                    chatResponse,
                    ChatResponseEvent.ChatRequestType.START_TYPING
                ));
            }

            @Override
            public void failure(RetrofitError error) {
                try {
                    if (error.getResponse() != null) {
                        ChatException body = (ChatException) error.getBodyAs(ChatException.class);
                        bus.post(new ChatErrorEvent(body));
                        return;
                    }
                } catch (Exception e) {;}
                bus.post(new UnknownErrorEvent(error));
            }
        });
    }

    public void onEvent(ChatStopTypingEvent event) {
        chatService.stopTyping(event.serviceId, event.verbose, new Callback<ChatResponse>() {
            @Override
            public void success(ChatResponse chatResponse, Response response) {
                bus.post(new ChatResponseEvent(
                    chatResponse,
                    ChatResponseEvent.ChatRequestType.STOP_TYPING
                ));
            }

            @Override
            public void failure(RetrofitError error) {
                try {
                    if (error.getResponse() != null) {
                        ChatException body = (ChatException) error.getBodyAs(ChatException.class);
                        bus.post(new ChatErrorEvent(body));
                        return;
                    }
                } catch (Exception e) {;}
                bus.post(new UnknownErrorEvent(error));
            }
        });
    }

    public void onEvent(ChatDisconnectEvent event) {
        chatService.disconnect(event.serviceId, event.verbose, new Callback<ChatResponse>() {
            @Override
            public void success(ChatResponse chatResponse, Response response) {
                bus.post(new ChatResponseEvent(
                    chatResponse,
                    ChatResponseEvent.ChatRequestType.DISCONNECT
                ));
            }

            @Override
            public void failure(RetrofitError error) {
                try {
                    if (error.getResponse() != null) {
                        ChatException body = (ChatException) error.getBodyAs(ChatException.class);
                        bus.post(new ChatErrorEvent(body));
                        return;
                    }
                } catch (Exception e) {;}
                bus.post(new UnknownErrorEvent(error));
            }
        });
    }

    public void onEvent(ChatCreateBasicEvent event) {
        chatService.basicChat(event.verbose, event.params, new Callback<ChatBasicResponse>() {
            @Override
            public void success(ChatBasicResponse chatBasicResponse, Response response) {
                bus.post(new ChatBasicResponseEvent(chatBasicResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                try {
                    if (error.getResponse() != null) {
                        ChatException body = (ChatException) error.getBodyAs(ChatException.class);
                        bus.post(new ChatErrorEvent(body));
                        return;
                    }
                } catch (Exception e) {;}
                bus.post(new UnknownErrorEvent(error));
            }
        });
    }
}
