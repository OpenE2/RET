package com.MH.ret;

import com.MH.ret.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity
{
//	public final static String EXTRA_MESSAGE = "com.MH.ucon";{
	protected TextView controls = null;
	protected TextView services = null;
//	protected TextView timers = null;
	protected TextView extra = null;
//	protected TextView movies = null;
	protected TextView settings = null;
	protected Toast toast = null;
	protected Resources res = null;
	protected int count = 0;
	protected customClass customClass = null;
	protected String validUrl;
	protected String validPort;
	protected Intent intent = null;
	static final int VALID_URL = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		customClass = (customClass)getApplicationContext();
		res = getApplicationContext().getResources();
		customClass.res = res;
		setContentView(customClass.setLayoutForOrientation(R.layout.main));	
		controls = (TextView)findViewById(R.id.controls);
		services = (TextView)findViewById(R.id.services);
//		timers = (TextView)findViewById(R.id.timers);
		extra = (TextView)findViewById(R.id.extra);
//		movies = (TextView)findViewById(R.id.movies);
		settings = (TextView)findViewById(R.id.settings);
		new getRecStatus().execute(customClass.connectWith());
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VALID_URL) {
            if (resultCode == RESULT_OK) {
            	validUrl = data.getStringExtra("VALID_URL");
            	validPort = data.getStringExtra("VALID_PORT");
            	if(data.hasExtra("CHANGED")){
            		if(data.getStringExtra("CHANGED").equals("true")){
            			new getRecStatus().execute(customClass.connectWith());
            		}
            	}
            }
        }
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    	count++;
	    	if(count>=2){
	    		onBackPressed();
	    		return true;
	    	}
	    	toast = Toast.makeText(getApplicationContext(), res.getString(R.string.toast_message_exit), Toast.LENGTH_SHORT);
	    	toast.show();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_about:
	        	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
				alertDialogBuilder.setTitle(R.string.action_about).setMessage(R.string.dialog_about).setCancelable(true)
		    	.setPositiveButton(R.string.dialog_ok,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				  }).setNegativeButton(null, null);
				AlertDialog alertDialog = alertDialogBuilder.create();
				/*WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			    lp.copyFrom(alertDialog.getWindow().getAttributes());
			    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			    lp.height = WindowManager.LayoutParams.MATCH_PARENT;*/
				alertDialog.show();
				//alertDialog.getWindow().setAttributes(lp);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public class getRecStatus extends AsyncTask<String, Void, Integer>{	
		String[] profil = customClass.getActive();
		private ProgressDialog progressDialog = null;
		
		protected void onPreExecute (){
			progressDialog = ProgressDialog.show(MainActivity.this, "", "Checking Settings and Connectivity!");
		}
		@Override
		protected Integer doInBackground(String... url) {
			// TODO Auto-generated method stub
			int result = customClass.getReceiverStatus(url);
			if(result == 2 || result == 3 || result == 5 || result == 7){
				validUrl = customClass.validUrl;
				validPort = customClass.validPort;
			}			
			return result;
		}
		protected void onPostExecute(Integer status) {
			progressDialog.dismiss();
			dialogNotConnected(status);
	    }
	}
	
	
	
	public void dialogNotConnected(int failure){
		String[] dialogTitleText = customClass.assembleDialog(failure);
		if(dialogTitleText != null){
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
			if(failure == 9){
				alertDialogBuilder
				.setTitle(dialogTitleText[0])
			    .setMessage(dialogTitleText[1])
		    	.setPositiveButton(R.string.dialog_yes,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						intent(settings);
					}
				}).setNegativeButton(R.string.dialog_no,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				});
			}else{
				alertDialogBuilder
				.setTitle(dialogTitleText[0])
			    .setMessage(dialogTitleText[1]).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				}).setNegativeButton(null, null);
			}
			AlertDialog notConnected = alertDialogBuilder.create();
			notConnected.show();
		}
	}

	public void intent(View v) {
		int id = v.getId();		
		switch (id) {
		case R.id.extra:
			intent = new Intent(this, Activity_extra.class);
			break;
//		case R.id.timers:
//			intent = new Intent(this, Activity_timers.class);
//			break;
		case R.id.controls:
			intent = new Intent(this, Activity_first_controls.class);
			break;
		case R.id.services:
			intent = new Intent(this, Activity_services.class);
			break;
		case R.id.settings:
			intent = new Intent(this, Activity_settings.class);
			break;
		/*case R.id.movies:
			intent = new Intent(this, Activity_movies.class);
			break;*/
		}
		if(intent != null){
			intent.putExtra("VALID_URL", validUrl);
			intent.putExtra("VALID_PORT", validPort);
			startActivityForResult(intent, VALID_URL);
		}
	}
}
