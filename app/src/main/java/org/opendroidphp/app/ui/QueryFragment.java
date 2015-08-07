package org.opendroidphp.app.ui;


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;

import org.apache.commons.io.IOUtils;
import org.opendroidphp.R;
import org.opendroidphp.app.AppController;
import org.opendroidphp.app.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class QueryFragment extends SherlockFragment implements View.OnClickListener {

    private static InputStream stdout;
    private static OutputStream stdin;
    private Button mBtnQuery;
    private EditText mQuery;
    private Process process;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_mysql, container, false);
        prepareView(view);

        SharedPreferences preferences = PreferenceManager.
                getDefaultSharedPreferences(getSherlockActivity());

        final String username = preferences.getString("mysql_username", "root");
        final String password = preferences.getString("mysql_password", "");

        new Thread(new Runnable() {
            @Override
            public void run() {
                initializeShell(username, password);
            }
        }).start();
        new AsyncCommandTask().execute();
        return view;
    }

    protected void prepareView(View view) {
        mQuery = (EditText) view.findViewById(R.id.create_mysql_query);
        mBtnQuery = (Button) view.findViewById(R.id.btn_execute_query);
        mBtnQuery.setOnClickListener(this);
    }

    protected Process initializeShell(final String username, final String password) {

        String[] baseShell = new String[]{
                Constants.MYSQL_MONITOR_SBIN_LOCATION, "-h",
                "127.0.0.1", "-T", "-f", "-r", "-t", "-E", "--disable-pager",
                "-n", "--user=" + username, "--password=" + password,
                "--default-character-set=utf8", "-L"};
        try {
            process = new ProcessBuilder(baseShell).
                    redirectErrorStream(true).
                    start();
        } catch (IOException e) {
            e.printStackTrace();
            AppController.toast(getSherlockActivity(), "Unable to run sql server");
        }
        return process;
    }

    @Override
    public void onClick(View view) {
        if (stdin == null || process == null) return;
        String command = mQuery.getText().toString();
        try {
            stdin.write((command + "\r\n").getBytes());
            stdin.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class AsyncCommandTask extends AsyncTask<Void, String, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (stdout == null || process == null) return null;
            BufferedReader buff = new BufferedReader(
                    new InputStreamReader(stdout));
            try {
                while (true) {
                    String stream = buff.readLine();
                    if (stream == null) break;
                    publishProgress(stream);
                }
                IOUtils.closeQuietly(buff);
            } catch (Exception e) {
                e.printStackTrace();
                IOUtils.closeQuietly(buff);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mQuery.append(values[0]);
        }
    }
}