package com.MH.ret;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class Activity_send_mess extends ActionBarActivity {
	protected String typeOfMessage = "1";
	protected String lengthOfMessage = "10";
	protected RadioButton activeTime = null;
	protected RadioButton activeMessageType = null;
	protected String validUrl;
	protected String validPort;
	static final int VALID_URL = 0;
	protected Resources res = null;
	protected customClass customClass = null;
	protected Intent intent = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity_send_mess);
		Intent intent = getIntent();
		validUrl = intent.getStringExtra("VALID_URL");
		validPort = intent.getStringExtra("VALID_PORT");
		customClass = (customClass)getApplicationContext();
		res = getApplicationContext().getResources();
		customClass.res = res;
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

	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();
		
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.message_radio_yesno:
	            if (checked)
	            	typeOfMessage = "0";
	            	activeMessageType = (RadioButton)view;
	            break;
	        case R.id.message_radio_info:
	            if (checked)
	            	typeOfMessage = "1";
	            	activeMessageType = (RadioButton)view;
	            break;
	        case R.id.message_radio_message:
	            if (checked)
	            	typeOfMessage = "2";
	            	activeMessageType = (RadioButton)view;
	            break;
	        case R.id.message_radio_attention:
	            if (checked)
	            	typeOfMessage = "3";
	            	activeMessageType = (RadioButton)view;
	            break;
	    }
	    
	    switch(view.getId()){
		    case R.id.message_length_5:
		    	lengthOfMessage = "5";
		    	activeTime = (RadioButton)view;
		    	break;
		    case R.id.message_length_10:
		    	lengthOfMessage = "10";
		    	activeTime = (RadioButton)view;
		    	break;
		    case R.id.message_length_20:
		    	lengthOfMessage = "20";
		    	activeTime = (RadioButton)view;
		    	break;
		    case R.id.message_length_40:
		    	lengthOfMessage = "40";
		    	activeTime = (RadioButton)view;
		    	break;
		    case R.id.message_length_noLimit:
		    	lengthOfMessage = "";
		    	activeTime = (RadioButton)view;
		    	break;
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_send_mess, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.message_send:
			EditText text = (EditText)findViewById(R.id.message_text);
			if(text.getText().length()>=1){
				new sendCommand().execute(validUrl+":"+validPort+res.getString(R.string.message_com_text,
						text.getText().toString(),typeOfMessage, lengthOfMessage));
			}else{
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, res.getString(R.string.message_noText), duration);
				toast.show();
			}
			return true;
		case R.id.action_settings:
        	intent = new Intent(this, Activity_settings.class);
        	startActivityForResult(intent, VALID_URL);
        	return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	public class getRecStatus extends AsyncTask<String, Void, Integer>{	
		String[] profil = customClass.getActive();
		private ProgressDialog progressDialog = null;		
		protected void onPreExecute (){
			progressDialog = ProgressDialog.show(Activity_send_mess.this, "", "Checking Settings and Connectivity!");
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
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Activity_send_mess.this);
			if(failure == 9){
				alertDialogBuilder
				.setTitle(dialogTitleText[0])
			    .setMessage(dialogTitleText[1])
		    	.setPositiveButton(R.string.dialog_yes,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						Intent intent = new Intent(Activity_send_mess.this, Activity_settings.class);
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

}
