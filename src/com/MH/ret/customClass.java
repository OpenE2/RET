package com.MH.ret;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpConnectionParams;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class customClass extends Application {
	protected MySQLiteHelper dbHelper = new MySQLiteHelper(this);
	protected static Cursor cursor = null;
	protected Resources res = null;
	protected String validUrl;
	protected String validPort;
	
	public int setLayoutForOrientation(int layoutLandscape, int layoutPortrait){
		int defaultOrientation = getDeviceDefaultOrientation();
		int currentRotation = getRotation(getApplicationContext());
		if((defaultOrientation == 2 && (currentRotation == 0 || currentRotation == 2)) 
			|| (defaultOrientation == 1 && (currentRotation == 1 || currentRotation == 3))){
			return layoutLandscape;//Layout for Landscape
		}else{
			return layoutPortrait;//Layout for Portrait
		}
	}
	
	public int setLayoutForOrientation(int layout){
		return layout;
	}
	
	public int getDeviceDefaultOrientation() {
		WindowManager windowManager =  (WindowManager) getSystemService(WINDOW_SERVICE);
	    Configuration config = getResources().getConfiguration();
	    int rotation = windowManager.getDefaultDisplay().getRotation();
	    if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
	    	config.orientation == Configuration.ORIENTATION_LANDSCAPE)
	        || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&    
	        config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
	    	return Configuration.ORIENTATION_LANDSCAPE;
	    }
	    else return Configuration.ORIENTATION_PORTRAIT;
	}
	
	public int getRotation(Context context){
		final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
			return Surface.ROTATION_0;
		case Surface.ROTATION_90:
			return Surface.ROTATION_90;
		case Surface.ROTATION_180:
			return Surface.ROTATION_180;
		default:
			return Surface.ROTATION_270;
		}
	}
	
	public Boolean executeCommand(String... url){
		NodeList nodeList = null;
		Boolean commandSend = false;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();	
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 1000);
		HttpConnectionParams.setSoTimeout(client.getParams(), 1000);
	    final HttpGet getRequest = new HttpGet(url[0]);
		try {
	        HttpResponse response = client.execute(getRequest);
	        final int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode != HttpStatus.SC_OK) {
	            Log.w("RecInformation", "Error " + statusCode + " while retrieving Information from " + url[0]);
	            client.close();
	            return null;
	        }
	        final HttpEntity entity = response.getEntity();
	        if (entity != null) {
	            InputStream in = null;
	            try{
            		in = entity.getContent();
	            	DocumentBuilder db = dbf.newDocumentBuilder();
	            	Document doc = db.parse(new InputSource(in));
	            	nodeList = doc.getElementsByTagName("*");
	            	for(int count=0;count != nodeList.getLength();count++){
	    				if(nodeList.item(count).getNodeName().equals((getString(R.string.con_result)))){
	    					if(nodeList.item(count).getTextContent().equals(getString(R.string.con_received))){
	    						commandSend = true;
	    					}
	    					break;
	    				}else if(nodeList.item(count).getNodeName().equals("e2state")){
	    					if(nodeList.item(count).getTextContent().equals("True")){
	    						commandSend = true;
	    					}
	    					break;
	    				}
	            	}	            	
	            	client.close();
	    			return commandSend;
	            }finally{
	            	if (client != null) {
			            client.close();
			        }
	            }
	        }else client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		} // end try and catch	
	return false;
	}
	
	public String[] connectWith(){
		if(checkDatabase()){
			String[] profil = getActive();
			String user = "";
			String password = "";
			String port = "";
			if(!profil[6].equals("")){
				if(!profil[5].equals("")){user = profil[5]+":";}
				if(!profil[6].equals("")){password = profil[6]+"@";}
				if(!profil[4].equals("")){port = ":"+profil[4];}
			}else{
				port = ":"+profil[4];
			}
						
			if(connectionType() == 1){
				if(!profil[2].equals("") && !profil[3].equals("")){						
					return new String []{"http://"+user+password+profil[2]+port, 
							"http://"+user+password+profil[3]+port};
				}else if(!profil[2].equals("") && profil[3].equals("")){
					return new String []{"http://"+user+password+profil[2]+port, ""};
				}else if(profil[2].equals("") && !profil[3].equals("")){
					return new String []{"", "http://"+user+password+profil[3]+port};
				}else{
					return new String []{"", ""};
				}
			}else if(connectionType()==0){
				if(!profil[3].equals("")){
					return new String []{"", "http://"+user+password+profil[3]+port};					
				}else if(profil[3].equals("") && profil[2].equals("")){
					return new String []{"", ""};
				}
				else{
					return new String []{"", "noDNS"};
				}
			}else{
				return new String []{"", "noCON"};
			}
		}
		return new String []{"", ""};
	}
	
	public int connectionType(){
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if(netInfo !=null){
			if(netInfo.isConnectedOrConnecting()){
				return netInfo.getType();
			}
		}		
		return 20;
	}

	public int getReceiverStatus(String... url){
		if(url != null){
			if(url[1].equals("noCON")){
				return 0;//Keine Verbindung, Phone-Settings überprüfen
			}else if(url[1].equals("noDNS")){
				return 1;//Kein DYNDS-Eintrag
			}else if(!url[0].equals("") && !url[1].equals("")){
				if(tryCon(url[0]+res.getString(R.string.req_about))){
					String[] separated = url[0].split(":");
					validUrl = separated[0]+":"+separated[1];
					validPort = separated[2];
					return 2;//Verbindung über das eigene Wlan
				}else if(tryCon(url[1]+res.getString(R.string.req_about))){
					String[] separated = url[0].split(":");
					validUrl = separated[0]+":"+separated[1];
					validPort = separated[2];
					return 3;//Verbindung über Fremd-Wlan/DYNDNS
				}return 4;//Settings überprüfen
			}else if(!url[0].equals("") && url[1].equals("")){
				if(tryCon(url[0]+res.getString(R.string.req_about))){
					String[] separated = url[0].split(":");
					validUrl = separated[0]+":"+separated[1];
					validPort = separated[2];
					return 5;//Verbindung über das eigene Wlan
				}return 6;//IP-Adresse überprüben
			}else if(url[0].equals("") && !url[1].equals("")){
				if(tryCon(url[1]+res.getString(R.string.req_about))){
					String[] separated = url[1].split(":");
					validUrl = separated[0]+":"+separated[1];
					validPort = separated[2];
					return 7;//verbindung über MOBILE/DYNDNS
				}return 8;//DYNDNS überprüfen
			}else if(url[0].equals("") && url[1].equals("")){
				return 9;//Keine Einstellungen vorgenommen, Settings aufrufen
			}
		}return 9;
	}
	
	public String[] assembleDialog(Integer choice){
		String dialogTitle = "";
		String dialogText = "";
		switch(choice){
		case 0:
			dialogTitle = res.getString(R.string.dialog_not_connected_title);
			dialogText = res.getString(R.string.dialog_not_connected_text);
			break;
		case 1:
			dialogTitle = res.getString(R.string.dialog_no_dynDns_title);
			dialogText = res.getString(R.string.dialog_no_dynDns_text);
			break;
		case 6:
			dialogTitle = res.getString(R.string.dialog_connection_failed_title);
			dialogText = res.getString(R.string.dialog_connection_failed_ip_text);
			break;
		case 8:
			dialogTitle = res.getString(R.string.dialog_connection_failed_title);
			dialogText = res.getString(R.string.dialog_connection_failed_dynDns_text);
			break;
		case 4:
			dialogTitle = res.getString(R.string.dialog_connection_failed_title);
			dialogText = res.getString(R.string.dialog_connection_failed_text);
			break;
		case 9:
			dialogTitle = res.getString(R.string.dialog_start_settings_title);
			dialogText = res.getString(R.string.dialog_start_settings_text);
			break;
		}
		if(!dialogTitle.equals("")){
			return new String[]{dialogTitle,dialogText};
		}return null;
		
	}
	
	public boolean tryCon(String url){
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 3000);
		HttpConnectionParams.setSoTimeout(client.getParams(), 3000);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		NodeList nodeList = null;
	    final HttpGet getRequest = new HttpGet(url);
	    
	    try {
	        HttpResponse response = client.execute(getRequest);
	        final int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode != HttpStatus.SC_OK) {
	        	client.close();
	            return false;
	        }else{
	        	final HttpEntity entity = response.getEntity();
		        if (entity != null) {
		            InputStream in = null;
		            try{
		            	in = entity.getContent();
		            	DocumentBuilder db = dbf.newDocumentBuilder();
		            	Document doc = db.parse(new InputSource(in));
		            	nodeList = doc.getElementsByTagName("*");
		            	if(nodeList.item(0).getNodeName().equals(res.getString(R.string.req_test))){
		            		return true;
		            	}else return false;
		            }finally{
		            	if (client != null) {
				            client.close();
				        }
		            }
		        }
	        }
	    }catch (Exception e) {
	    	client.close();
	    	getRequest.abort();
	    } finally {
	        if (client != null) {
	            client.close();
	        }
	    }
	    return false;	
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
			cursor.close();
			return result;
		}
		cursor.close();
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
	
	public final void prettyPrint(Document xml) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    tf.setOutputProperty(OutputKeys.INDENT, "yes");
	    Writer out = new StringWriter();
	    tf.transform(new DOMSource(xml), new StreamResult(out));
	}
}
