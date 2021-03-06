package com.genesys.gms.mobile.callback.demo.legacy.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;
import com.genesys.gms.mobile.callback.demo.legacy.R;
import com.genesys.gms.mobile.callback.demo.legacy.common.BaseActivity;
import com.genesys.gms.mobile.callback.demo.legacy.data.api.pojo.TranscriptEntry;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.ChatErrorEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.ChatResponseEvent;
import com.genesys.gms.mobile.callback.demo.legacy.data.events.chat.ChatTranscriptEvent;
import com.genesys.gms.mobile.callback.demo.legacy.util.Globals;
import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.concurrent.*;

public class GenesysChatActivity extends BaseActivity {

  private static final ScheduledExecutorService timer = Executors.newScheduledThreadPool(2);

  private final Executor uiExecutor = new Executor() {
    @Override
    public void execute(@NonNull Runnable command) {
      runOnUiThread(command);
    }
  };

  @Inject
  SharedPreferences sharedPreferences;
  @Inject
  GenesysChatController controller;
  private final EventBus bus;

  private TextView transcriptTextView;
  private View sendButton;
  private EditText sendEditText;
  private TextView infoTextView;
  private TextWatcher textWatcher;

  private boolean chatFinished;
  private boolean userTyping;

  private String cometUrl;
  private String sessionId;
  private String subject;

  @DebugLog
  public GenesysChatActivity() {
    this.bus = EventBus.getDefault();
  }

  @Override
  @DebugLog
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  @DebugLog
  protected void onCreate(Bundle inState) {
    super.onCreate(inState);

    final Intent intent = getIntent();

    if (inState == null) {
      if (Globals.ACTION_GENESYS_START_CHAT.equals(intent.getAction())) {
        cometUrl = intent.getStringExtra(Globals.EXTRA_COMET_URL);
        sessionId = intent.getStringExtra(Globals.EXTRA_SESSION_ID);
        subject = intent.getStringExtra(Globals.EXTRA_SUBJECT);
        if (sessionId != null && subject != null) {
          controller.startChat(sessionId, subject);
        }

        // Persist state in case of unexpected app termination
        sharedPreferences.edit()
            .putBoolean("CHAT_chatFinished", chatFinished)
            .putString("CHAT_cometUrl", cometUrl)
            .putString("CHAT_sessionId", sessionId)
            .putString("CHAT_subject", subject)
            .apply();
      }
    }

    setupUi(inState);

    if (inState == null) {
      Timber.d("Attempt to restore Chat state from persistence.");
      inState = new Bundle();
      inState.putBoolean("chatFinished", sharedPreferences.getBoolean("CHAT_chatFinished", false));
      inState.putString("cometUrl", sharedPreferences.getString("CHAT_cometUrl", null));
      inState.putString("sessionId", sharedPreferences.getString("CHAT_sessionId", null));
      inState.putString("subject", sharedPreferences.getString("CHAT_subject", null));
    }
    chatFinished = inState.getBoolean("chatFinished");
    cometUrl = inState.getString("cometUrl");
    sessionId = inState.getString("sessionId");
    subject = inState.getString("subject");

    controller.restoreState(inState);
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
    outState.putString("sessionId", sessionId);
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
    if (chatFinished) {
      finishChat();
    }
    if (cometUrl != null && !cometUrl.isEmpty()) {
      new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
          controller.startComet(cometUrl);
          return null;
        }
      }.execute();
    }
  }

  @Override
  protected void onPause() {
    if (userTyping) {
      controller.stopTyping();
      if (scheduledStopTypingMessage != null && !scheduledStopTypingMessage.isDone()) {
        scheduledStopTypingMessage.cancel(false);
        scheduledStopTypingMessage = null;
      }
    }
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

  private void setupUi(Bundle inState) {
    setContentView(R.layout.chat_layout);

    sendEditText = (EditText) findViewById(R.id.sendText);
    transcriptTextView = (TextView) findViewById(R.id.transcriptText);
    sendButton = findViewById(R.id.sendButton);
    infoTextView = (TextView) findViewById(R.id.informationalMessageTextView);

    if (inState != null) {
      transcriptTextView.setText(inState.getCharSequence("transcript"));
      sendEditText.setText(inState.getString("sendEditText"));
    }

    sendEditText.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
          sendMessage();
          return true;
        }
        return false;
      }
    });

    textWatcher = new TextWatcher() {
      @Override
      @DebugLog
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateSendButtonState();
      }

      @Override
      @DebugLog
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!userTyping && after > count) {
          userTyping = true;
          controller.startTyping();
        } else if (scheduledStopTypingMessage != null && !scheduledStopTypingMessage.isDone()) {
          scheduledStopTypingMessage.cancel(false);
          scheduledStopTypingMessage = null;
        }
      }

      @Override
      @DebugLog
      public void afterTextChanged(Editable s) {
        if (scheduledStopTypingMessage == null || scheduledStopTypingMessage.isDone()) {
          scheduledStopTypingMessage = timer.schedule(updateUserTyping, 5, TimeUnit.SECONDS);
        }
      }
    };
    sendEditText.addTextChangedListener(textWatcher);

    transcriptTextView.setMovementMethod(new ScrollingMovementMethod());
    sendButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        sendMessage();
      }
    });
    infoTextView.setTextColor(Color.GRAY);
  }

  private void updateSendButtonState() {
    sendButton.setEnabled(!chatFinished && sendEditText.length() > 0);
  }

  private void sendMessage() {
    stopTyping();
    final String text = sendEditText.getText().toString();
    sendEditText.setText("");
    controller.sendText(text);
  }

  private void stopTyping() {
    if (userTyping) {
      if (scheduledStopTypingMessage != null) {
        if (!scheduledStopTypingMessage.isDone()) {
          scheduledStopTypingMessage.cancel(false);
        }
        scheduledStopTypingMessage = null;
      }
      userTyping = false;
      controller.stopTyping();
    }
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
    @Override
    public void run() {
      uiExecutor.execute(new Runnable() {
        @Override
        public void run() {
          setInformationalMessageImpl("");
        }
      });
    }
  };

  private final Runnable updateUserTyping = new Runnable() {
    @Override
    public void run() {
      stopTyping();
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
      scheduledWipeInformationalMessage.cancel(false);
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
    if (item.getItemId() == R.id.close) {
      if (!chatFinished) {
        controller.disconnectChat();
      } else {
        finish();
      }
    }
    return true;
  }

  public void onEventMainThread(ChatResponseEvent event) {
    switch (event.chatRequestType) {
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
    if (textWatcher != null) {
      sendEditText.removeTextChangedListener(textWatcher);
      textWatcher = null;
    }
    sendEditText.setText("");
    sendEditText.setEnabled(false);
    showPermanentInfo("Chat finished");
  }

  public void onEventMainThread(ChatTranscriptEvent event) {
    TranscriptEntry transcriptEntry = event.transcriptEntry;
    if (transcriptEntry.getChatEvent() == null) {
      Timber.e("Unknown ChatEvent encountered, see raw response for details.");
      return;
    }
    switch (transcriptEntry.getChatEvent()) {
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
    Timber.e("Chat error encountered: %s", event.chatException);
    Toast.makeText(this, event.chatException.getMessage(), Toast.LENGTH_SHORT).show();
  }
}
