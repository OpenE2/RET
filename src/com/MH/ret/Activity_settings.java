package com.MH.ret;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class Activity_settings extends ActionBarActivity implements OnFocusChangeListener,TextWatcher,OnItemSelectedListener{
	protected View currentView = null;
	protected String currentSelection = null;
	protected String previousSelection = null;
	protected AlertDialog.Builder alertDialogBuilder = null;
	protected boolean allowOnPause = false;
	protected DataCommunication dataCom = null;
	protected String validUrl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity_settings);
		alertDialogBuilder = new AlertDialog.Builder(Activity_settings.this);
		
		dataCom = new DataCommunication();
		refreshSpinner();

		EditText ipAddress = (EditText)findViewById(R.id.ip_address);
		ipAddress.setOnFocusChangeListener(this);
		ipAddress.addTextChangedListener(this);
		
		EditText dynDns = (EditText)findViewById(R.id.dyn_address);
		dynDns.setOnFocusChangeListener(this);
		
		EditText newName = (EditText)findViewById(R.id.new_profilname);
		newName.setOnFocusChangeListener(this);
		
		EditText port = (EditText)findViewById(R.id.port);
		port.setOnFocusChangeListener(this);
		port.addTextChangedListener(this);
	}
	
	
	@Override
	public void onBackPressed() {
		alertDialogBuilder.setTitle(R.string.input_error);
		EditText ipAddress = (EditText)findViewById(R.id.ip_address);
		EditText dynDns = (EditText)findViewById(R.id.dyn_address);
		
		if(verifyInput(ipAddress) == false){
	    	alertDialogBuilder.setMessage(R.string.dialog_wrong_ip).setCancelable(false)
	    	.setPositiveButton(R.string.dialog_exit,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					Activity_settings.super.onBackPressed();
				}
			  })
			.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});
	    	AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
	    }else if(verifyInput(dynDns) == false){
	    	alertDialogBuilder.setMessage(R.string.dialog_wrong_dyndns).setCancelable(false)
	    	.setPositiveButton(R.string.dialog_exit,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					Activity_settings.super.onBackPressed();
				}
			  })
			.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});
	    	AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
	    }else{
			dataCom.saveData(currentSelection);
			setResult(RESULT_OK, new Intent().putExtra("CHANGED", "true"));
	        super.onBackPressed();
	    }
	}


	
	public Boolean verifyInput(View view){
		EditText cView = (EditText)findViewById(view.getId());
		Editable s = cView.getText();
		Pattern p = null;
		switch(view.getId()){
		case R.id.ip_address:
			p = Pattern.compile("((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])"); 
			break;
		case R.id.dyn_address:
			p = Pattern.compile("^[a-zA-Z0-9\\-\\.]+\\.(me|com|org|net|mil|edu|COM|ORG|NET|MIL|EDU)$");
			break;
		}
		if(p!=null && s.length()!=0){
			Matcher m = p.matcher(s);
			return m.matches();
		}else return true;
	}
	
	public void refreshSpinner(){
		ArrayAdapter<String> adapter = null;
		Spinner spinner = (Spinner) findViewById(R.id.profil_spinner);
		spinner.setOnItemSelectedListener(this);
		
		if(dataCom.checkDatabase()){
			adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, dataCom.getSpinnerArray());
		}else{
			dataCom.initializeDB();
			adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, dataCom.getSpinnerArray());
		}
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
		EditText ipAddress = (EditText)findViewById(R.id.ip_address);
		EditText dynDns = (EditText)findViewById(R.id.dyn_address);
	    switch (item.getItemId()) {
	        case R.id.action_delete:
	        	alertDialogBuilder.setTitle(R.string.caution);
				alertDialogBuilder.setMessage(R.string.dialog_delete).setCancelable(false)
		    	.setPositiveButton(R.string.dialog_yes,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dataCom.deleteRow();
						refreshSpinner();
					}
				  })
				.setNegativeButton(R.string.dialog_no,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				});
				AlertDialog alertDialog_delete = alertDialogBuilder.create();
				alertDialog_delete.show();
	            return true;
	        case R.id.action_save:
	        	alertDialogBuilder.setTitle(R.string.input_error);				
				if(verifyInput(ipAddress) == false){
			    	alertDialogBuilder.setMessage(R.string.dialog_wrong_ip).setCancelable(false)
					.setNegativeButton(R.string.dialog_ok,new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							dialog.cancel();
						}
					});
			    	AlertDialog alertDialog_save = alertDialogBuilder.create();
			    	alertDialog_save.show();
			    }else if(verifyInput(dynDns) == false){
			    	alertDialogBuilder.setMessage(R.string.dialog_wrong_dyndns).setCancelable(false)
					.setNegativeButton(R.string.dialog_ok,new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							dialog.cancel();
						}
					});
			    	AlertDialog alertDialog_save = alertDialogBuilder.create();
			    	alertDialog_save.show();
			    }else{
					dataCom.saveData(currentSelection);
					refreshSpinner();
			    }								
	            return true;
	        case android.R.id.home:
	        	onBackPressed();
				return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	    
	}
	
	
	public class DataCommunication{
		private MySQLiteHelper dbHelper = new MySQLiteHelper(Activity_settings.this);
		private Cursor cursor = null;
		private EditText ipAddressView = (EditText)findViewById(R.id.ip_address);
		private EditText dynDnsView = (EditText)findViewById(R.id.dyn_address);
		private EditText port = (EditText)findViewById(R.id.port);
		private EditText user = (EditText)findViewById(R.id.username);
		private EditText password = (EditText)findViewById(R.id.password);
		private EditText newNameView = (EditText)findViewById(R.id.new_profilname);
		
		public void deleteRow(){
			if(currentSelection!="default"){
				cursor = dbHelper.getWritableDatabase().rawQuery("DELETE FROM settings WHERE profil = ?",new String[] {currentSelection});
				cursor.getCount();
				cursor.close();
				changeActive("default");
			}else{
				cursor = dbHelper.getWritableDatabase().rawQuery("UPDATE settings SET "
						+ "profil = \"default\""
						+ "ip = \"\", "
						+ "dyndns = \"\", "
						+ "port = \"\", "
						+ "user = \"\", "
						+ "password = \"\" "
						+ "WHERE profil = \"default\"", null);
				cursor.getCount();
				cursor.close();
			}			
		}
		
		public void setData(String currentSelection){
			cursor = dbHelper.getReadableDatabase().rawQuery("SELECT profil, ip, dyndns, port, user, password FROM settings WHERE profil = ?",new String[] {currentSelection});
			if(cursor.moveToFirst()){
				ipAddressView.setText(cursor.getString(1));
				dynDnsView.setText(cursor.getString(2));
				port.setText(cursor.getString(3));
				user.setText(cursor.getString(4));
				password.setText(cursor.getString(5));
				newNameView.setText("");
			}
			cursor.close();
		}
		public void saveData(String currentSelection){
			
			String newName = newNameView.getText().toString().trim();
			newNameView.requestFocus();
			String searchedProfil = null;
			int id = 0;
			cursor = dbHelper.getReadableDatabase().rawQuery("SELECT _id, profil FROM settings WHERE profil = ?",new String[] {newName});
			if(cursor.moveToFirst()){
				searchedProfil = cursor.getString(1);
			}else if(newName.equals("")){
				searchedProfil = currentSelection;
			}
			cursor.close();
			if(searchedProfil != null){
				cursor = dbHelper.getReadableDatabase().rawQuery("SELECT _id, profil FROM settings WHERE profil = ?",new String[] {searchedProfil});
				cursor.moveToFirst();
				id = cursor.getInt(0);
				cursor.close();
				cursor = dbHelper.getWritableDatabase().rawQuery("UPDATE OR REPLACE settings SET "
						+ "_id = \"" + id + "\", "
						+ "ip = \"" + ipAddressView.getText().toString() + "\", "
						+ "dyndns = \"" + dynDnsView.getText().toString() + "\", "
						+ "port = \"" + port.getText().toString() + "\", "
						+ "user = \"" + user.getText().toString() + "\", "
						+ "password = \"" + password.getText().toString() + "\" "
						+ "WHERE _id = \"" + id + "\"", null);
				cursor.getCount();
				cursor.close();
			}else{
				cursor = dbHelper.getWritableDatabase().rawQuery("INSERT OR REPLACE INTO settings"
						+ "(profil, ip, dyndns, port, user, password, status)VALUES"
						+ "(\"" + newName + "\", \"" 
						+ ipAddressView.getText().toString() + "\", \"" 
						+ dynDnsView.getText().toString() + "\", \"" 
						+ port.getText().toString() + "\", \""
						+ user.getText().toString() + "\", \""
						+ password.getText().toString() + "\","
						+ "  0)",null);
				cursor.getCount();
				cursor.close();
				changeActive(newName);
			}
		}
		
		public void initializeDB(){
			cursor = dbHelper.getWritableDatabase().rawQuery("INSERT OR REPLACE "
					+ "INTO settings(profil, ip, dyndns, port, user, password, status)"
					+ "VALUES(?, ?, ?, ?, ?, ?, 1)",new String[] {"default", "", "", "8080", "root", ""});
			cursor.getCount();
			cursor.close();
		}

		public String[] getSpinnerArray(){
			cursor = dbHelper.getReadableDatabase().rawQuery("select profil from settings",null);
			String[] result = new String[cursor.getCount()];
			cursor.close();
			cursor = dbHelper.getReadableDatabase().rawQuery("select profil from settings where status = ?", new String[] { "1" });
			if(cursor.moveToFirst()){
				result[0] = cursor.getString(0);
			}
			cursor.close();
			cursor = dbHelper.getReadableDatabase().rawQuery("select profil from settings where status != ?", new String[] { "1" });
			if(cursor.moveToFirst()){
				int i = 1;
				do{
					result[i++] = cursor.getString(0);
					cursor.moveToNext();
				}while(!cursor.isAfterLast());
			}
			cursor.close();				
			return result;
		}	
		
		public boolean checkDatabase(){
			cursor = dbHelper.getReadableDatabase().rawQuery("select * from settings where status = ?", new String[] { "1" });
			boolean status = cursor.moveToFirst();
			cursor.close();
			return status;
		}
		
		public String[] getActive(){
			cursor = dbHelper.getReadableDatabase().rawQuery("select * from settings where status = ?", new String[] { "1" });
			if(cursor.moveToFirst()){	
				String[] result = new String[cursor.getColumnCount()];
				for(int i=0;i<cursor.getColumnCount();i++){
					result[i] = cursor.getString(i);
				}
				return result;
			}
			return null;
		}
		
		public void changeActive(String active){
			cursor = dbHelper.getWritableDatabase().rawQuery("UPDATE OR REPLACE settings SET status = 0 WHERE status = ?", new String[]{"1"});
			cursor.getCount();
			cursor.close();
			cursor = dbHelper.getWritableDatabase().rawQuery("UPDATE OR REPLACE settings SET status = 1 WHERE profil = ?", new String[]{active});
			cursor.getCount();
			cursor.close();
		}
	}
	
	public class MySQLiteHelper extends SQLiteOpenHelper {		
		private static final String TABLE_SETTINGS = "settings";
		private static final String COLUMN_ID = "_id";
		private static final String COLUMN_PROFILNAME = "profil";
		private static final String COLUMN_IP = "ip";
		private static final String COLUMN_DYNDNS = "dyndns";  
		private static final String COLUMN_PORT = "port";
		private static final String COLUMN_USER = "user";
		private static final String COLUMN_PASS = "password";
		private static final String COLUMN_STATUS = "status";  
		private static final String DATABASE_NAME = "settings.db";
		private static final int DATABASE_VERSION = 1;
		
	
		  // Database creation sql statement
		  private static final String DATABASE_CREATE = "create table "
		      + TABLE_SETTINGS + "(" 
		      + COLUMN_ID + " integer primary key, " 
		      + COLUMN_PROFILNAME + " text, "
		      + COLUMN_IP + " text, " 
		      + COLUMN_DYNDNS + " text, "
		      + COLUMN_PORT + " text, "
		      + COLUMN_USER + " text, "
		      + COLUMN_PASS + " text, "
		      + COLUMN_STATUS + " integer)";
	
		  public MySQLiteHelper(Context context) {
		    super(context, DATABASE_NAME, null, DATABASE_VERSION);
		  }
	
		  @Override
		  public void onCreate(SQLiteDatabase database) {
		    database.execSQL(DATABASE_CREATE);
		  }
	
		  @Override
		  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		    Log.w(MySQLiteHelper.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		    db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
		    onCreate(db);
		  }
	} 

	@Override
	public void onFocusChange(View v, boolean arg1) {
		// TODO Auto-generated method stub
		if(currentView != null){
			Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;
			Toast toast = null;
			EditText cView = (EditText)findViewById(currentView.getId());
			String currentViewText = cView.getText().toString();
			if(!currentViewText.equals("")){
				if(!verifyInput(currentView)){
					switch(currentView.getId()){
					case R.id.ip_address:
						CharSequence ipFalse = "IP is not valid!";
						toast = Toast.makeText(context, ipFalse, duration);
						toast.show();
						break;
					case R.id.dyn_address:
						CharSequence dnsFalse = "Dyndns is not valid";
						toast = Toast.makeText(context, dnsFalse, duration);
						toast.show();
						break;
					}
				}
			}
		}else{
			currentView = v;
		}
		currentView = v;
	}
	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		Pattern p = null;
		switch(currentView.getId()){
		case R.id.ip_address:
			p = null;
			p = Pattern.compile("^(?!\\.)([0-1]?[0-9]?[0-9]?|2?[0-4]?[0-9]?|25?[0-5]?)(?!\\d)(\\.?)(?!\\.)"
					+ "([0-1]?[0-9]?[0-9]?|2?[0-4]?[0-9]?|25?[0-5]?)(?!\\d)(\\.?)(?!\\.)"
					+ "([0-1]?[0-9]?[0-9]?|2?[0-4]?[0-9]?|25?[0-5]?)(?!\\d)(\\.?)(?!\\.)"
					+ "([0-1]?[0-9]?[0-9]?|2?[0-4]?[0-9]?|25?[0-5]?)(?!\\d)$"); 				 			
			break;
		case R.id.port:
			p = null;
			p = Pattern.compile("^[1-5]?[0-9]?[0-9]?[0-9]?[0-9]?|6?[0-4]?[0-9]?[0-9]?[0-9]?|(65000)?$");
			break;
		}
		if(p!=null && s!=null){
			Matcher m = p.matcher(s);
			if(m.matches()==false) {
				if(s.length()!=0){
					s.delete(s.length()-1, s.length());
				}else{
					s.delete(s.length(), s.length());
				}
			}
		}	
	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub		
	}	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		// TODO Auto-generated method stub
		currentSelection = parent.getItemAtPosition(pos).toString();
		dataCom.changeActive(parent.getItemAtPosition(pos).toString());
		dataCom.setData(currentSelection);
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub	
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_settings, menu);
	    return super.onCreateOptionsMenu(menu);
	}


}
