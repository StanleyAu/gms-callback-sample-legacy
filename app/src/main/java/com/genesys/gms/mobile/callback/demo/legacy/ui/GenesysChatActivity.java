package com.genesys.gms.mobile.callback.demo.legacy.ui;

import java.util.concurrent.*;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.genesys.gms.mobile.callback.demo.legacy.BaseActivity;
import com.genesys.gms.mobile.callback.demo.legacy.R;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.TranscriptEntry;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.*;
import com.genesys.gms.mobile.callback.demo.legacy.data.push.GcmIntentService;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;

import javax.inject.Inject;

public class GenesysChatActivity extends BaseActivity {
	
	private static final ScheduledExecutorService timer = Executors.newScheduledThreadPool(2);
	
	private final Executor uiExecutor = new Executor() {
		@Override public void execute(Runnable command) {
			runOnUiThread(command);
		}
	};

    @Inject GenesysChatController controller;
    private final EventBus bus;

	private TextView transcriptTextView;
	private View sendButton;
	private EditText sendEditText;
	private TextView infoTextView;
    private TextWatcher textWatcher;

	private boolean chatFinished;
    private boolean userTyping;

    private String cometUrl;
    private String chatId;
    private String subject;

    @DebugLog
    public GenesysChatActivity() {
        this.bus = EventBus.getDefault();
    }

	@Override
	protected void onCreate(Bundle inState) {
        super.onCreate(inState);

        final Intent intent = getIntent();

        if (inState == null) {
            if (Globals.ACTION_GENESYS_START_CHAT.equals(intent.getAction())) {
                cometUrl = intent.getStringExtra(Globals.EXTRA_COMET_URL);

                chatId = intent.getStringExtra(Globals.EXTRA_CHAT_ID);
                subject = intent.getStringExtra(Globals.EXTRA_SUBJECT);
                if (chatId != null && subject != null) {
                    controller.startChat(chatId, subject);
                }
            } else {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    // Determine if activity was started as a result of GCM notification
                    int notificationId = extras.getInt(GcmIntentService.GCM_NOTIFICATION_ID, -1);
                    if (notificationId != -1) {
                        Log.i("GenesysChatActivity", "Clearing notification with ID " + notificationId + " from drawer.");
                        // Remove the notification from the Notification Drawer
                        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.cancel(notificationId);
                    }
                }
            }
        }

        setupUi();

        if(inState != null) {
            chatFinished = inState.getBoolean("chatFinished");
            cometUrl = inState.getString("cometUrl");
            chatId = inState.getString("chatId");
            subject = inState.getString("subject");
            transcriptTextView.setText(inState.getCharSequence("transcript"));
            sendEditText.setText(inState.getString("sendEditText"));

            controller.restoreState(inState);
        }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_actions, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("chatFinished", chatFinished);
        outState.putString("cometUrl", cometUrl);
        outState.putString("chatId", chatId);
        outState.putString("subject", subject);
        outState.putCharSequence("transcript", transcriptTextView.getText());
        outState.putString("sendEditText", sendEditText.getText().toString());
        controller.persistState(outState);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
        if(chatFinished) {
            finishChat();
        }
        if(cometUrl != null && !cometUrl.isEmpty()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    controller.startComet(cometUrl);
                    return null;
                }
            }.execute();
        }
        Log.d("GenesysChatActivity", "controller: " + controller);
    }

    @Override
    protected void onPause() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                controller.stopComet();
                return null;
            }
        }.execute();
        bus.unregister(this);
        super.onPause();
    }
	
	private void setupUi() {
		setContentView(R.layout.chat_layout);
		
		sendEditText = (EditText)findViewById(R.id.sendText);
		sendEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					sendMessage();
					return true;
				}
				return false;
			}
		});

        textWatcher = new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonState();
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(!userTyping) {
                    userTyping = true;
                    controller.startTyping();
                }
            }
            @Override public void afterTextChanged(Editable s) {
                if(scheduledStopTypingMessage == null || scheduledStopTypingMessage.isDone()) {
                    scheduledStopTypingMessage = timer.schedule(updateUserTyping, 5, TimeUnit.SECONDS);
                }
            }
        };
		sendEditText.addTextChangedListener(textWatcher);
		
		transcriptTextView = (TextView)findViewById(R.id.transcriptText);
		transcriptTextView.setMovementMethod(new ScrollingMovementMethod());
		
		sendButton = findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				sendMessage();
			}
		});

		infoTextView = (TextView)findViewById(R.id.informationalMessageTextView);
		infoTextView.setTextColor(Color.GRAY);
	}
	
	private void updateSendButtonState() {
		sendButton.setEnabled(!chatFinished && sendEditText.length() > 0);
	}
	
	private void sendMessage() {
		final String text = sendEditText.getText().toString();
		sendEditText.setText("");
        controller.sendText(text);
	}
	
	private void appendTranscriptMessage(final String tag, final String message) {
		SpannableString text = new SpannableString(tag + ": " + message + "\n");
		text.setSpan(
				new ForegroundColorSpan(Color.GRAY),
				0, tag.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		transcriptTextView.append(text);
	}
	
	private void appendTranscriptInfo(final String message) {
		SpannableString text = new SpannableString(message + "\n");
		text.setSpan(new ForegroundColorSpan(Color.GRAY), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		text.setSpan(new StyleSpan(Typeface.ITALIC), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		transcriptTextView.append(text);
	}
	
	private final Runnable wipeInformationalMessage = new Runnable() {
		@Override public void run() {
			uiExecutor.equals(new Runnable() {
				@Override public void run() {
					setInformationalMessageImpl("");
				}
			});
		}
	};

    private final Runnable updateUserTyping = new Runnable() {
        @Override
        public void run() {
            userTyping = false;
            controller.stopTyping();
        }
    };
	
	ScheduledFuture<?> scheduledWipeInformationalMessage;
    ScheduledFuture<?> scheduledStopTypingMessage;
	
	private void showInfo(String text) {
		showInfoImpl(text, false);
	}
	
	private void showPermanentInfo(String text) {
		showInfoImpl(text, true);
	}
	
	private void showInfoImpl(String text, boolean permanent) {
		if (scheduledWipeInformationalMessage != null) {
			boolean mayInterruptIfRunning = false;
			scheduledWipeInformationalMessage.cancel(mayInterruptIfRunning);
		}
		
		setInformationalMessageImpl(text);
		
		if (!permanent) {
            scheduledWipeInformationalMessage = timer.schedule(wipeInformationalMessage, 10, TimeUnit.SECONDS);
        }
	}
	
	private void setInformationalMessageImpl(final String text) {
		infoTextView.setText(text);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.close) {
            if(!chatFinished) {
                controller.disconnectChat();
            } else {
                Log.d("GenesysChatActivity", "Ending chat activity.");
                finish();
            }
        }
        return true;
    }

    public void onEventMainThread(ChatResponseEvent event) {
        switch(event.chatRequestType) {
            case START:
                // handle start result
                break;
            case SEND:
                // handle send result
                break;
            case REFRESH:
                // handle refresh result
                break;
            case START_TYPING:
                break;
            case STOP_TYPING:
                break;
            case DISCONNECT:
                finishChat();
                break;
            default:
                break;
        }
    }

    private void finishChat() {
        chatFinished = true;
        updateSendButtonState();
        if(textWatcher != null) {
            sendEditText.removeTextChangedListener(textWatcher);
            textWatcher = null;
        }
        sendEditText.setText("");
        sendEditText.setEnabled(false);
        showPermanentInfo("Chat finished");
    }

    public void onEventMainThread(ChatTranscriptEvent event) {
        TranscriptEntry transcriptEntry = event.transcriptEntry;
        switch(transcriptEntry.getChatEvent()) {
            case PARTY_JOINED:
            case PARTY_LEFT:
                appendTranscriptInfo(transcriptEntry.getNickname() + " " + transcriptEntry.getText());
                break;
            case TYPING_STARTED:
            case TYPING_STOPPED:
                showInfo(transcriptEntry.getNickname() + " " + transcriptEntry.getText());
                break;
            case MESSAGE:
            case PUSH_URL:
                showPermanentInfo("");
                appendTranscriptMessage(transcriptEntry.getNickname(), transcriptEntry.getText());
                break;
        }
    }

    public void onEventMainThread(ChatErrorEvent event) {
        Log.e("GenesysChatActivity", "Chat Error encountered: " + event.chatException);
    }
}
