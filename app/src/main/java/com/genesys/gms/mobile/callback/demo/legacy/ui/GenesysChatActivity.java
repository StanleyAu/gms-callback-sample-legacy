package com.genesys.gms.mobile.callback.demo.legacy.ui;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.genesys.gms.mobile.callback.demo.legacy.R;
import com.genesys.gms.mobile.callback.demo.legacy.client.ChatEvent;
import com.genesys.gms.mobile.callback.demo.legacy.client.ChatListener;
import com.genesys.gms.mobile.callback.demo.legacy.client.ChatSession;
import com.genesys.gms.mobile.callback.demo.legacy.client.ChatCreationParameters;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.TranscriptEntry;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.ChatResponseEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.ChatStartEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.ChatTranscriptEvent;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;

import javax.inject.Inject;

public class GenesysChatActivity extends AbstractGenesysActivity {
	
	private static final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
	
	private final Executor uiExecutor = new Executor() {
		@Override public void execute(Runnable command) {
			runOnUiThread(command);
		}
	};
	
	@Inject SharedPreferences sharedPreferences;
    @Inject GenesysChatController controller;
    private final EventBus bus;

	private TextView transcriptTextView;
	private View sendButton;
	private EditText sendEditText;
	private TextView infoTextView;
	
	private ChatSession chatSession;
	private boolean chatFinished;

    private String cometUrl;
    private String chatId;
    private String subject;

    @DebugLog
    public GenesysChatActivity() {
        this.bus = EventBus.getDefault();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        if (Globals.ACTION_GENESYS_START_CHAT.equals(intent.getAction())) {
            cometUrl = intent.getStringExtra(Globals.EXTRA_COMET_URL);

            chatId = intent.getStringExtra(Globals.EXTRA_CHAT_ID);
            subject = intent.getStringExtra(Globals.EXTRA_SUBJECT);
            if(chatId != null && subject != null) {
                controller.startChat(chatId, subject);
            }
        } else {
            // TODO: What is it doing?
        }
		setupUi();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_actions, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
        if(cometUrl != null && !cometUrl.isEmpty()) {
            controller.startComet(cometUrl);
        }
    }

    @Override
    protected void onPause() {
        controller.stopComet();
        bus.unregister(this);
        super.onPause();
    }
	
	private ChatListener chatListener = new ChatListener() {
		
		@Override public void chatEventReceived(ChatEvent chatEvent) {
			switch (chatEvent.eventType) {
			case PARTY_JOINED:
			case PARTY_LEFT:
				appendTranscriptInfo(chatEvent.nickname + " " + chatEvent.text);
				break;
			case TYPING_STARTED:
			case TYPING_STOPPED:
				showInfo(chatEvent.nickname + " " + chatEvent.text);
				break;
			case MESSAGE:
			case PUSH_URL:
				showPermanentInfo("");
				appendTranscriptMessage(chatEvent.nickname, chatEvent.text);
				break;
			}
		}

		@Override public void chatDisconnected() {
			chatFinished = true;
			updateSendButtonState();
			sendEditText.setText("");
			sendEditText.setEnabled(false);
			showPermanentInfo("Chat finished");
		}
	};
	
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
		
		sendEditText.addTextChangedListener(new TextWatcher() {
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				updateSendButtonState();
			}
			
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void afterTextChanged(Editable s) {}
		});
		
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
		genesysService.execute(new Runnable() {
			@Override public void run() {
				chatSession.send(text);
			}
		});
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
	
	ScheduledFuture<?> scheduledWipeInformationalMessage;
	
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
		
		if (!permanent)
			scheduledWipeInformationalMessage = timer.schedule(wipeInformationalMessage, 10, TimeUnit.SECONDS);
	}
	
	private void setInformationalMessageImpl(final String text) {
		infoTextView.setText(text);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.close) {
            controller.disconnectChat();
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
                // TODO: We'll need to process the Comet equivalent of this too
                chatFinished = true;
                updateSendButtonState();
                sendEditText.setText("");
                sendEditText.setEnabled(false);
                showPermanentInfo("Chat finished");
                break;
            default:
                break;
        }
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
}
