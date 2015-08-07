package com.MH.ret;

import com.MH.ret.R;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;

public class Activity_first_controls extends ActionBarActivity {
	protected customClass customClass = null;
	protected Resources res = null;
	protected Intent intent;
	protected String validUrl;
	protected String validPort;
	static final int VALID_URL = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		customClass = (customClass)getApplicationContext();
		res = getApplicationContext().getResources();
		customClass.res = res;
		intent = getIntent();
		validUrl = intent.getStringExtra("VALID_URL");
		validPort = intent.getStringExtra("VALID_PORT");
		super.onCreate(savedInstanceState);
		setContentView(customClass.setLayoutForOrientation
				(R.layout.activity_activity_first_controls_landscape,
				R.layout.activity_activity_first_controls));
	}
	
	public void onBackPressed(){
		Intent intent = new Intent();
		intent.putExtra("VALID_URL", validUrl);
		intent.putExtra("VALID_PORT", validPort);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
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
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_first_controls, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	public class getRecStatus extends AsyncTask<String, Void, Integer>{	
		String[] profil = customClass.getActive();
		private ProgressDialog progressDialog = null;
		
		protected void onPreExecute (){
			progressDialog = ProgressDialog.show(Activity_first_controls.this, "", "Checking Settings and Connectivity!");
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
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Activity_first_controls.this);
			if(failure == 9){
				alertDialogBuilder
				.setTitle(dialogTitleText[0])
			    .setMessage(dialogTitleText[1])
		    	.setPositiveButton(R.string.dialog_yes,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						Intent intent = new Intent(Activity_first_controls.this, Activity_settings.class);
						startActivity(intent);
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
	
	public class sendCommand extends AsyncTask<String, Void, Boolean>{
		@Override
		protected Boolean doInBackground(String... url) {
			// TODO Auto-generated method stub
			return customClass.executeCommand(url);			
		}
		protected void onPostExecute(Boolean status) {
			if(!status){
				new getRecStatus().execute(customClass.connectWith());
			}
		}
	}	
	
	public void intent(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.con_more:
			intent.setClass(this, Activity_sec_controls.class);
			startActivity(intent);
			break;
		case R.id.con_pow:
			power(v);
			break;
		default:
			if(validUrl == null){
				new getRecStatus().execute(customClass.connectWith());
			}else{
				new sendCommand().execute(validUrl+":"+validPort+v.getTag());
			}
		}
	}
	
	public void power(final View v){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Activity_first_controls.this);
		alertDialogBuilder.setTitle(R.string.con_select_pow).setCancelable(true)
    	.setItems(R.array.con_array_pow,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				if(validUrl == null){
					new getRecStatus().execute(customClass.connectWith());
				}else{					
					new sendCommand().execute(validUrl+":"+validPort+res.getString(R.string.con_com_pow)+id);
				}
				dialog.dismiss();
			}
		  }).setNegativeButton(null, null);
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        	onBackPressed();
				return true;
	        case R.id.action_settings:
	        	intent = new Intent(this, Activity_settings.class);
	        	startActivityForResult(intent, VALID_URL);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
