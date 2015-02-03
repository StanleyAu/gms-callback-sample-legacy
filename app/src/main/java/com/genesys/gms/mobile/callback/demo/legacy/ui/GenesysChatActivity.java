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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.genesys.gms.mobile.callback.demo.legacy.client.ChatEvent;
import com.genesys.gms.mobile.callback.demo.legacy.client.ChatListener;
import com.genesys.gms.mobile.callback.demo.legacy.client.ChatSession;
import com.genesys.gms.mobile.callback.demo.legacy.client.ChatCreationParameters;

public class GenesysChatActivity extends AbstractGenesysActivity {
	
	private static final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
	
	private final Executor uiExecutor = new Executor() {
		@Override public void execute(Runnable command) {
			runOnUiThread(command);
		}
	};
	
	private SharedPreferences sharedPreferences;

	private TextView transcriptTextView;
	private View sendButton;
	private EditText sendEditText;
	private TextView infoTextView;
	
	private ChatSession chatSession;
	private boolean chatFinished;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		setupUi();
	}
	
	@Override
	protected void onDestroy() {
		if (chatSession != null) {
			genesysService.execute(new Runnable() {
				@Override public void run() {
					chatSession.disconnect();
				}
			});
		}

		super.onDestroy();
	}
	
	@Override
	protected void handleIntent(final Intent intent) {
		genesysService.execute(new Runnable() {
			@Override public void run() {
				if (Globals.ACTION_GENESYS_START_CHAT.equals(intent.getAction())) {
					chatSession = genesysService.startChat(
							intent.getStringExtra(Globals.EXTRA_CHAT_URL),
							intent.getStringExtra(Globals.EXTRA_COMET_URL),
							chatListener, uiExecutor);

					ChatCreationParameters chatParams = new ChatCreationParameters();
					chatParams.firstName = sharedPreferences.getString("first_name", null);
					chatParams.lastName = sharedPreferences.getString("last_name", null);
					chatParams.userDisplayName = sharedPreferences.getString("chat_display_name", null);
					chatParams.email = sharedPreferences.getString("chat_email", null);
					chatParams.subject = intent.getStringExtra(Globals.EXTRA_SUBJECT);
					chatSession.createInteraction(chatParams);
				}
			}
		});
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

}
