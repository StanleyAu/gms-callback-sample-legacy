package com.genesys.gms.mobile.callback.demo.legacy.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import com.genesys.gms.mobile.callback.demo.legacy.common.BaseFragment;
import com.genesys.gms.mobile.callback.demo.legacy.R;

// TODO: make log scroll with inertia
public class LogFragment extends BaseFragment {

	private String logFile;
	private ScrollView logScrollView;
	private TextView logTextView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.log_layout, container, false);
		logScrollView = (ScrollView)view.findViewById(R.id.logScrollView);
		logTextView = (TextView)view.findViewById(R.id.logTextView);
        Typeface typeFace= Typeface.createFromAsset(getActivity().getAssets(), "fonts/DroidSansMono.ttf");
        logTextView.setTypeface(typeFace);
		logTextView.setMovementMethod(new ScrollingMovementMethod());
		Button clearButton = (Button) view.findViewById(R.id.clearButton);
		clearButton.setOnClickListener(clearButtonClickListener);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		logFile = getActivity().getCacheDir().getAbsolutePath() + File.separator + "log";
	}
	
	@Override
	public void onResume() {
		super.onResume();
		loadLog();
	}

	private void loadLog() {
		logTextView.setText("");
	    BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(logFile));
		    String line = null;
            while ((line = reader.readLine()) != null) {
                logTextView.append(line + "\n");
            }
            logScrollView.post(new Runnable() {
                @Override
                public void run() {
                    logScrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
		} catch (FileNotFoundException e) {
			logTextView.append("Log file not found");
		} catch (IOException e) {
			logTextView.append("Unable to read log file: " + e);
		}
	}
	
	private final OnClickListener clearButtonClickListener = new OnClickListener() {
		@Override public void onClick(View view) {
			try {
				OutputStream out = new FileOutputStream(logFile);
				out.write(new byte[0]);
				out.close();
				loadLog();
			} catch (IOException e) {
				logTextView.append("Log file not cleared");
			}
		}
	};

}
