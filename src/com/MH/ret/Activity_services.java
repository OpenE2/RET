package com.MH.ret;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpConnectionParams;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class Activity_services extends ActionBarActivity {
	protected String validUrl;
	protected String validPort;
	static final int VALID_URL = 0;
	protected customClass customClass = null;
	protected Resources res = null;
	protected Intent intent = null;
	protected LinearLayout.LayoutParams paramsForText = new LinearLayout.LayoutParams
			(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT,(float) 1.0);
	protected ScrollView.LayoutParams paramsForView = new ScrollView.LayoutParams
			(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
	protected LinearLayout linearLayout;
	protected ScrollView scrollView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		customClass = (customClass)getApplicationContext();
		res = getApplicationContext().getResources();
		customClass.res = res;
		linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		scrollView = new ScrollView(this);
		//setContentView(R.layout.activity_activity_services);
		intent = getIntent();
		validUrl = intent.getStringExtra("VALID_URL");
		validPort = intent.getStringExtra("VALID_PORT");
		new getXmlData().execute(validUrl+":"+validPort+getString(R.string.req_get_services));
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_services, menu);
	    return super.onCreateOptionsMenu(menu);
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
		    case android.R.id.home:
	        	onBackPressed();
				return true;
	        case R.id.action_settings:
	        	Intent intent = new Intent(this, Activity_settings.class);
	        	startActivityForResult(intent, VALID_URL);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
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
	
	public class getRecStatus extends AsyncTask<String, Void, Integer>{	
		String[] profil = customClass.getActive();
		private ProgressDialog progressDialog = null;
		
		protected void onPreExecute (){
			progressDialog = ProgressDialog.show(Activity_services.this, "", "Checking Settings and Connectivity!");
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
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Activity_services.this);
			if(failure == 9){
				alertDialogBuilder
				.setTitle(dialogTitleText[0])
			    .setMessage(dialogTitleText[1])
		    	.setPositiveButton(R.string.dialog_yes,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						Intent intent = new Intent(Activity_services.this, Activity_settings.class);
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
	
	public class getXmlData extends AsyncTask<String, Void, NodeList>{
		ProgressDialog progressDialog = null;
		String[] request = res.getStringArray(R.array.rec_bouquet);
		XMLparser xmlParser = new XMLparser();
		protected void onPreExecute (){
			progressDialog = ProgressDialog.show(Activity_services.this, "", "LOADING!");
		}
		@Override
		protected NodeList doInBackground(String... url) {
			// TODO Auto-generated method stub
			NodeList nodeList = null;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			HttpConnectionParams.setConnectionTimeout(client.getParams(), 3000);
			HttpConnectionParams.setSoTimeout(client.getParams(), 3000);
		    final HttpGet getRequest = new HttpGet(url[0]);
			try {
		        HttpResponse response = client.execute(getRequest);
		        final int statusCode = response.getStatusLine().getStatusCode();
		        if (statusCode != HttpStatus.SC_OK) {
		            Log.w("RecInformation", "Error " + statusCode + " while retrieving Information from " + url[0]); 
		            return null;
		        }
		        final HttpEntity entity = response.getEntity();
		        if (entity != null) {
		            InputStream in = null;
		            try{
		            	in = entity.getContent();
		            	DocumentBuilder db = dbf.newDocumentBuilder();
		            	Document doc = db.parse(new InputSource(in));
		            	doc.normalize();
		            	nodeList = doc.getElementsByTagName("*");
		            	return nodeList.item(0).getChildNodes();
		            }finally{
		            	if (client != null) {
				            client.close();
				        }
		            }
		        }
			}catch (Exception e) {
		        // Could provide a more explicit error message for IOException or IllegalStateException
		        getRequest.abort();
			}finally {
		        if (client != null) {
		            client.close();
		        }
			}
			return null;
		}
		@SuppressWarnings("deprecation")
		@SuppressLint("NewApi")
		protected void onPostExecute(NodeList list) {
			if(isCancelled()||list == null){
				new getRecStatus().execute(customClass.connectWith());
			}else{		
				xmlParser.parseXML(request, 0, list);
				paramsForText.setMargins(20, 0, 20, 0);
				for(int i = 0;i<xmlParser.iterationCount;i++){
					TextView textView = new TextView(Activity_services.this);				
					textView.setTag(xmlParser.result.get("e2servicereference"+i));
					textView.setText(xmlParser.result.get("e2servicename"+(++i)));
					if (Build.VERSION.SDK_INT >= 16)
						textView.setBackground(res.getDrawable(R.drawable.border_bottom));
					else
						textView.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
					textView.setGravity(Gravity.CENTER_VERTICAL);
					textView.setPadding(0, 25, 0, 25);
					textView.setClickable(true);
					textView.setOnClickListener(new View.OnClickListener() {
					    public void onClick(View v) {
					    	classIntent(v);
					    }
					});
					linearLayout.addView(textView,paramsForText);
				}
				scrollView.addView(linearLayout);
				setContentView(scrollView);
			}
			progressDialog.dismiss();
		}
	}
	

	public class XMLparser{
		Map<String, String> result = new HashMap<String, String>();
		int iterationCount = 0;
		public void parseXML(String[] request,int ListLength,NodeList List){
			for(int count=0;count < List.getLength();count++){
				if(List.item(count).hasChildNodes()){
					parseXML(request,List.getLength(),List.item(count).getChildNodes());
					count++;
				}else{
					for(int i = 0;i < request.length;i++){
						if(List.item(count).getParentNode().getNodeName().equals(request[i])){
							result.put(List.item(count).getParentNode().getNodeName()+iterationCount++, List.item(count).getTextContent());						
						}
					}
				}
			}
		}
	}
	
	public void classIntent(View v){
		TextView textView = (TextView)v;
		String titleServices = textView.getText().toString();
		intent.setClass(this, Activity_sub_services.class);
		intent.putExtra("VALID_URL", validUrl);
		intent.putExtra("VALID_PORT", validPort);
		intent.putExtra("SERVICE_REFERENCE", v.getTag().toString().replace(" ", "%20").replace("\"", "%22"));
		intent.putExtra("TITLE", titleServices);
		startActivity(intent);
	}
	
	
}
