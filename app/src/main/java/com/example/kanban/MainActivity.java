package com.example.kanban;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	ListView listView ;
	
	ArrayList<String> arrayList = new ArrayList<String>();
	ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		View decorView = getWindow().getDecorView();
		// Hide the status bar.
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
        listView = (ListView) findViewById(R.id.listView1);
        adapter = new ArrayAdapter<String>(this, R.layout.buttonrow, R.id.textView1, arrayList);
        listView.setAdapter(adapter); 
        createDirIfNotExists("/kanban_log");
        String PACKAGE_NAME = getApplicationContext().getPackageName();
        PackageInfo packageInfo = null;
		try {
			packageInfo = getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        int curVersionCode = packageInfo.versionCode;
        Log.d(PACKAGE_NAME, Integer.toString(curVersionCode));
    }
    
    protected void onResume(Bundle savedInstanceState){
    	super.onResume();
    	View decorView = getWindow().getDecorView();
		// Hide the status bar.
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN );	

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public static boolean createDirIfNotExists(String path) {
        boolean ret = true;


        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
               
                ret = false;
            }
        }
        return ret;
    } 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
            	startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void deleteRow(View view){
    	RelativeLayout vwParentRow = (RelativeLayout)view.getParent();
        TextView child = (TextView)vwParentRow.getChildAt(0);
        adapter.remove((String) child.getText());
        adapter.notifyDataSetChanged();
    }
    
    public void scan(View view) {
    	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
    	intent.putExtra("SCAN_MODE", "ONE_D_MODE");
    	startActivityForResult(intent, 0);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	   if (requestCode == 0) {
    	      if (resultCode == RESULT_OK) {
    	    	Boolean found = false;
				//@SuppressWarnings("unused")
				String contents = intent.getStringExtra("SCAN_RESULT");
    	         @SuppressWarnings("unused")
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
    	         for (int i = 0; i < arrayList.size(); i++)
    	         {
    	        	 //toastText(arrayList.get(i) + "   " + contents);
    	        	 if (contents.equals(arrayList.get(i)))
    	        	 { //doppelte Scannung
    	        		found=true;
    	        		toastText("Diese KIN wurde bereits gescannt!");
    	        	 }
    	         }
    	         if (found == false)
    	         {
    	        	
    	        	 arrayList.add(contents);
    	        	 adapter.notifyDataSetChanged();
    	         }
    	         
    	      }
    	}
    
    }

    public void sendEmail(View view) {
    	if (!arrayList.isEmpty())
    	{
	    	createSendFile();
	    	File f= new File(Environment.getExternalStorageDirectory().getPath()+"/bestellung.csv");
	        Uri u1=Uri.fromFile(f);
	    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
	    	String sEmail = sharedPref.getString("KEY_EMAIL","Fehler");
	        String adressarray[] = { sEmail };
	        Intent sendIntent = new Intent(Intent.ACTION_SEND);
	        sendIntent.putExtra(android.content.Intent.EXTRA_EMAIL, adressarray);
	        sendIntent.putExtra(Intent.EXTRA_SUBJECT, sharedPref.getString("KEY_SUBJECT",null));
	        sendIntent.putExtra(Intent.EXTRA_TEXT, sharedPref.getString("KEY_EMAILBODY",null));
	        sendIntent.putExtra(Intent.EXTRA_STREAM, u1);
	        sendIntent.setType("text/html");
	        startActivity(sendIntent);
			adapter.clear();
			adapter.notifyDataSetChanged();
    	}
    	else
    	{
    		toastText("Keine gescannten Artikel vorhanden.");
    	}	  
    }
        
    public void createSendFile()
    {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    	File f = new File(Environment.getExternalStorageDirectory().getPath()+"/bestellung.csv");
    	FileOutputStream fos, foslog;
    	Time today = new Time(Time.getCurrentTimezone());
    	today.setToNow();
    	int day = today.monthDay;
    	int month = today.month;
    	int year = today.year;
    	int hour = today.hour;
    	int minute = today.minute;
    	File log = new File(Environment.getExternalStorageDirectory().getPath()+"/kanban_log/" + year+(month+1)+day+hour+minute + ".csv");
    	String stringDay, stringMonth;
    	if (day < 10)
    	{
    		stringDay = "0" + day;
    	}
    	else
    	{
    		stringDay = day + "";
    	}
    	if (month+1 < 10)
    	{
    		stringMonth = "0" + (month+1);
    	}
    	else
    	{
    		stringMonth = month + "";
    	}
		try {
			fos = new FileOutputStream(f, false);
			foslog = new FileOutputStream(log, false);
			FileWriter fWriter, logWriter;
	        String bestellText = "";
	        try {
	        	fWriter = new FileWriter(fos.getFD());
	        	logWriter = new FileWriter(foslog.getFD());
	        	if (!arrayList.isEmpty())
	        	{
	        	for (int i=0; i < arrayList.size(); i++)
	        	{
	        		bestellText = bestellText + arrayList.get(i) + ";;1;;;_AdrArtNr:"+arrayList.get(i)+"#;";
	        	}
	        	//toastText(bestellText);
	        	}
	        	else
	        	{
	        		toastText("Keine gescannten Artikel vorhanden.");
				}
				fWriter.write(";;" + arrayList.size() + ";6;14;B;;" + stringDay + "." + stringMonth + "." + year + ";;;;;" + sharedPref.getString("KEY_KdnNR", "Fehler") + ";" + bestellText);
				logWriter.write(";;" + arrayList.size() + ";6;14;B;;" + stringDay + "." + stringMonth + "." + year + ";;;;;" + sharedPref.getString("KEY_KdnNR","Fehler")+";"+bestellText);
	        	fWriter.close();
	        	logWriter.close();

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }
    }
    
public void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }
    
    public void toastText(String text){
    	Context context = getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;

    	Toast toast = Toast.makeText(context, text, duration);
    	toast.show();
    }
}
