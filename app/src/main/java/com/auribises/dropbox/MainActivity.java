package com.auribises.dropbox;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import google.dropboxfilesample.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final static private String APP_KEY = "ecb6qni0ns8b9kn";
    final static private String APP_SECRET = "ozy7y0au8rjt78y";

    private DropboxAPI<AndroidAuthSession> mDBApi;

    private Button uploadFileButton;
    private Button downloadFileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        mDBApi.getSession().startOAuth2Authentication(MainActivity.this);

        uploadFileButton = (Button) findViewById(R.id.uploadFileButton);
        downloadFileButton = (Button) findViewById(R.id.downloadFileButton);

        uploadFileButton.setOnClickListener(this);
        downloadFileButton.setOnClickListener(this);
    }

    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uploadFileButton:
                try {
                    uploadFile();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DropboxException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.downloadFileButton:
                try {
                    downloadFile();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DropboxException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void uploadFile() throws FileNotFoundException, DropboxException {
        final File file = new File(Environment.getExternalStorageDirectory(), "sample.mp3");
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading File");
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread() {
            @Override
            public void run() {
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    mDBApi.putFile("/sample.mp3", inputStream, file.length(), null, null);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "File Uploaded Successfully!", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (DropboxException dropboxException) {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    dropboxException.printStackTrace();
                } catch (FileNotFoundException e) {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void downloadFile() throws FileNotFoundException, DropboxException {
        final File file = new File(Environment.getExternalStorageDirectory(), "sample.mp3");
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading File");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread() {
            @Override
            public void run() {
                try {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    mDBApi.getFile("/sample.mp3", null, outputStream, null);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "File Downloaded Successfully!", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (DropboxException dropboxException) {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    dropboxException.printStackTrace();
                } catch (FileNotFoundException e) {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
