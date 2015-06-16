package com.example.kanban;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class UpdateChecker extends Activity {
    final static private String APP_KEY = "e1xo30w2xylshoo";
    final static private String APP_SECRET = "93l0s65iap4s9lv";
    final static private String APP_TOKEN = "hRyDomXIYzAAAAAAAAAADrF-nHlAvczooOU8lqW8ebOgyJ0e5kpwhnZiyQmaEnZZ";
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private FileOutputStream outputStream;
    private FileInputStream inputStream;
    private File file;
    private Button updateButton;
    private TextView localVersion;
    private TextView serverVersion;
    private TextView updateText;
    private ProgressBar progressBar;
    private int versionNumber;
    private String versionName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_checker);
        PackageInfo pinfo = null;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        versionNumber = pinfo.versionCode;
        versionName = pinfo.versionName;
        localVersion = new TextView(this);
        serverVersion = new TextView(this);
        updateButton = new Button(this);
        localVersion=(TextView)findViewById(R.id.textView1);
        serverVersion=(TextView)findViewById(R.id.textView2);
        updateText=(TextView)findViewById(R.id.upDateText);
        updateButton = (Button)findViewById(R.id.updatebutton);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        updateButton.setText("Update installieren");
        localVersion.setText("Ihre aktuelle Version ist:" + versionNumber);
        serverVersion.setText("Version auf dem Server: ");
        updateButton.setVisibility(View.INVISIBLE);
        updateText.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        mDBApi.getSession().setOAuth2AccessToken(APP_TOKEN);
        file = new File(Environment.getExternalStorageDirectory(),"/test.txt");
        if (!file.exists()) {
            try {
                file.createNewFile(); //otherwise dropbox client will fail silently
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        new GetFile().execute();


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

    public void upDate(View view)
    {
        new GetUpdate().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update_checker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class GetFile  extends AsyncTask<Void, Integer, String>{
        protected void onPreExecute (){
            Log.d("PreExceute","On pre Exceute......");
        }

        protected String doInBackground(Void...arg0) {
            Log.d("DoINBackGround", "On doInBackground...");
            File file = new File(Environment.getExternalStorageDirectory(), "/test.txt");
            FileOutputStream outputStream = null;
            DropboxAPI.DropboxFileInfo info = null;
            try {
                outputStream = new FileOutputStream(file);
                info = mDBApi.getFile("/Version.txt", null, outputStream, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            if (info != null) {
                Log.i("DbExampleLog", "The file's rev is: " + info.getMetadata().rev);
            }
            return "You are at PostExecute";
        }

        protected void onProgressUpdate(Integer...a){
            // Log.d("You are in progress update ... " + a[0]);
        }

        protected void onPostExecute(String result) {
            inputStream = null;
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            StringBuilder datax = new StringBuilder("");
            try {
                FileInputStream fIn = new FileInputStream(file) ;
                InputStreamReader isr = new InputStreamReader ( fIn ) ;
                BufferedReader buffreader = new BufferedReader ( isr ) ;

                String readString = buffreader.readLine();
                while ( readString != null ) {
                    datax.append(readString);
                    readString = buffreader.readLine ( ) ;
                }

                isr.close ( ) ;
            } catch ( IOException ioe ) {
                ioe.printStackTrace ( ) ;
            }
            serverVersion.setText("Version auf dem Server: " + datax.toString());
            if (Integer.parseInt(datax.toString()) <= versionNumber)
            {
                updateButton.setVisibility(View.INVISIBLE);
            }
            else
            {
                updateButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private class GetUpdate  extends AsyncTask<Void, Long, String>{
        public long totalBytes;
        protected void onPreExecute (){

            progressBar.setVisibility(View.VISIBLE);
            updateText.setVisibility(View.VISIBLE);
            updateText.setText("Update wird geladen");
            Log.d("PreExceute","On pre Exceute......");
        }

        protected String doInBackground(Void...arg0) {
            Log.d("DoINBackGround", "On doInBackground...");
            File file = new File(Environment.getExternalStorageDirectory(), "/app-release.apk");
            FileOutputStream outputStream = null;
            DropboxAPI.DropboxFileInfo info = null;
            try {
                outputStream = new FileOutputStream(file);
                info = mDBApi.getFile("/app-release.apk", null, outputStream, new ProgressListener() {
                    @Override
                    public void onProgress(long bytes, long total) {
                        totalBytes = total;
                        publishProgress(bytes);
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            if (info != null) {
                Log.i("DbExampleLog", "The file's rev is: " + info.getMetadata().rev);
            }

            return "You are at PostExecute";
        }

        protected void onProgressUpdate(Long...a){
            // Log.d("You are in progress update ... " + a[0]);
            int percent = (int)(100.0*(double)a[0]/totalBytes  + 0.5);
            progressBar.setProgress(percent);
        }

        protected void onPostExecute(String result) {
           //ToDo: update fahren
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData( Uri.fromFile(new File(Environment.getExternalStorageDirectory(),"/app-release.apk")) );
            startActivity(intent);
        }
    }

    private class DoUpdate  extends AsyncTask<Void, Long, String>{
        public long totalBytes;
        protected void onPreExecute (){

            progressBar.setVisibility(View.VISIBLE);
            updateText.setVisibility(View.VISIBLE);
            updateText.setText("Update wird geladen");
            Log.d("PreExceute","On pre Exceute......");
        }

        protected String doInBackground(Void...arg0) {
            Log.d("DoINBackGround", "On doInBackground...");
            return "You are at PostExecute";
        }

        protected void onProgressUpdate(Long...a){
            // Log.d("You are in progress update ... " + a[0]);
            int percent = (int)(100.0*(double)a[0]/totalBytes  + 0.5);
            progressBar.setProgress(percent);
        }

        protected void onPostExecute(String result) {
            //ToDo: update fahren

        }
    }
}
