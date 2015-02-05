package com.genesys.gms.mobile.callback.demo.legacy.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.genesys.gms.mobile.callback.demo.legacy.BuildConfig;
import com.genesys.gms.mobile.callback.demo.legacy.R;

/**
 * Created by stau on 12/4/2014.
 */
public class AboutDialogFragment extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        View view = inflater.inflate(R.layout.about_dialog, container, false);
        TextView versionNumber = (TextView)view.findViewById(R.id.txt_value_version);
        versionNumber.setText(BuildConfig.VERSION_NAME);
        TextView buildDate = (TextView)view.findViewById(R.id.txt_value_date);
        buildDate.setText(BuildConfig.BUILD_TIME);

        getDialog().setTitle("About " + getResources().getString(R.string.title_activity_genesys_sample));
        getDialog().setCanceledOnTouchOutside(true);
        return view;
    }
}
