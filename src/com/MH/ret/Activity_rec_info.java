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
import com.MH.ret.R;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;

public class Activity_rec_info extends ActionBarActivity {
	private static Map<String, String> result = new HashMap<String, String>();
	private int multicount = 0;
	protected String validUrl;
	protected String validPort;
	static final int VALID_URL = 0;
	protected customClass customClass = null;
	protected Intent intent = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		customClass = (customClass)getApplicationContext();
		intent = getIntent();
		validUrl = intent.getStringExtra("VALID_URL");
		validPort = intent.getStringExtra("VALID_PORT");
		new getXmlData().execute(validUrl+getString(R.string.req_about));
		setContentView(R.layout.activity_activity_rec_info);
	}

	
	public void onBackPressed(){
		Intent intent = new Intent();
		intent.putExtra("VALID_URL", validUrl);
		intent.putExtra("VALID_PORT", validPort);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		onBackPressed();
	    return true;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VALID_URL) {
            if (resultCode == RESULT_OK) {
            	validUrl = data.getStringExtra("VALID_URL");
            	validPort = data.getStringExtra("VALID_PORT");
            	if(data.hasExtra("CHANGED")){
            		if(data.getStringExtra("CHANGED").equals("true")){
            			new getXmlData().execute(validUrl+":"+validPort+getString(R.string.req_about));
            		}          		
            	}
            }
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_rec_info, menu);
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
	        	intent = new Intent(this, Activity_settings.class);
	        	startActivityForResult(intent, VALID_URL);
	        	return true;
	        case R.id.action_refresh_info:
	        	new getXmlData().execute(validUrl+":"+validPort+getString(R.string.req_about));
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public class getXmlData extends AsyncTask<String, Void, NodeList>{
		ProgressDialog progressDialog = null;
		protected void onPreExecute (){
			progressDialog = ProgressDialog.show(Activity_rec_info.this, "", "LOADING!");
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
				String dhcp = null;
				Resources res = getResources();
				String[] request = res.getStringArray(R.array.rec_info);
				XmlParser(request,list.getLength(),list);
				
				TextView textview = (TextView)findViewById(R.id.enigmaversion);
				textview.setText(Html.fromHtml("<b>" + getString(R.string.rec_info_gui) + "</b>"));
				textview.append("\n"+result.get("e2enigmaversion"));
				//textview.setTextColor(Color.parseColor(getString(R.color.text_color_inaktiv)));
				
				if (Build.VERSION.SDK_INT >= 16)
				    textview.setBackground(res.getDrawable(R.drawable.border_bottom));
				else
				    textview.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
				
				textview = (TextView)findViewById(R.id.imageversion);
				textview.setText(Html.fromHtml("<b>" + getString(R.string.rec_info_image) + "</b>"));
				textview.append("\n"+result.get("e2imageversion"));
				//textview.setTextColor(Color.parseColor(getString(R.color.text_color_inaktiv)));
				
				if (Build.VERSION.SDK_INT >= 16)
				    textview.setBackground(res.getDrawable(R.drawable.border_bottom));
				else
				    textview.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
				
				textview = (TextView)findViewById(R.id.webifversion);
				textview.setText(Html.fromHtml("<b>" + getString(R.string.rec_info_webif) + "</b>"));
				textview.append("\n"+result.get("e2webifversion"));
				//textview.setTextColor(Color.parseColor(getString(R.color.text_color_inaktiv)));
				
				if (Build.VERSION.SDK_INT >= 16)
				    textview.setBackground(res.getDrawable(R.drawable.border_bottom));
				else
				    textview.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
				
				textview = (TextView)findViewById(R.id.fpversion);
				textview.setText(Html.fromHtml("<b>" + getString(R.string.rec_info_front) + "</b>"));
				textview.append("\n"+result.get("e2fpversion"));
				//textview.setTextColor(Color.parseColor(getString(R.color.text_color_inaktiv)));
				
				if (Build.VERSION.SDK_INT >= 16)
				    textview.setBackground(res.getDrawable(R.drawable.border_bottom));
				else
				    textview.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
				
				textview = (TextView)findViewById(R.id.recmodel);
				textview.setText(Html.fromHtml("<b>" + getString(R.string.rec_info_receiver) + "</b>"));
				textview.append("\n"+result.get("e2model"));
				//textview.setTextColor(Color.parseColor(getString(R.color.text_color_inaktiv)));
				
				if (Build.VERSION.SDK_INT >= 16)
				    textview.setBackground(res.getDrawable(R.drawable.border_bottom));
				else
				    textview.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
				
				textview = (TextView)findViewById(R.id.landhcp);
				if(result.get("e2landhcp").equals("True")){dhcp = "enabled";}else{dhcp = "disabled";}
				textview.setText(Html.fromHtml("<b>" + getString(R.string.rec_info_dhcp) + "</b>"));
				textview.append("\n"+dhcp);
				//textview.setTextColor(Color.parseColor(getString(R.color.text_color_inaktiv)));
				
				if (Build.VERSION.SDK_INT >= 16)
				    textview.setBackground(res.getDrawable(R.drawable.border_bottom));
				else
				    textview.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
				
				textview = (TextView)findViewById(R.id.lanmac);
				textview.setText(Html.fromHtml("<b>" + getString(R.string.rec_info_mac) + "</b>"));
				textview.append("\n"+result.get("e2lanmac"));
				//textview.setTextColor(Color.parseColor(getString(R.color.text_color_inaktiv)));
				
				if (Build.VERSION.SDK_INT >= 16)
				    textview.setBackground(res.getDrawable(R.drawable.border_bottom));
				else
				    textview.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
				
				textview = (TextView)findViewById(R.id.lanip);
				textview.setText(Html.fromHtml("<b>" + getString(R.string.rec_info_ip) + "</b>"));
				textview.append("\n"+result.get("e2lanip"));
				//textview.setTextColor(Color.parseColor(getString(R.color.text_color_inaktiv)));
				
				if (Build.VERSION.SDK_INT >= 16)
				    textview.setBackground(res.getDrawable(R.drawable.border_bottom));
				else
				    textview.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
				
				textview = (TextView)findViewById(R.id.lanmask);
				textview.setText(Html.fromHtml("<b>" + getString(R.string.rec_info_subnet) + "</b>"));
				textview.append("\n"+result.get("e2lanmask"));
				//textview.setTextColor(Color.parseColor(getString(R.color.text_color_inaktiv)));
				
				if (Build.VERSION.SDK_INT >= 16)
				    textview.setBackground(res.getDrawable(R.drawable.border_bottom));
				else
				    textview.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
				
				textview = (TextView)findViewById(R.id.langateway);
				textview.setText(Html.fromHtml("<b>" + getString(R.string.rec_info_gateway) + "</b>"));
				textview.append("\n"+result.get("e2langw"));
				//textview.setTextColor(Color.parseColor(getString(R.color.text_color_inaktiv)));
				
				if (Build.VERSION.SDK_INT >= 16)
				    textview.setBackground(res.getDrawable(R.drawable.border_bottom));
				else
				    textview.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
				
				textview = (TextView)findViewById(R.id.harddisk);
				textview.setText(Html.fromHtml("<b>" + getString(R.string.rec_info_harddisk) + "</b>"));
				textview.append("\n"+result.get("model")+"\n Capacity: "+result.get("capacity")+"\n free Capacity: "+result.get("free"));
				//textview.setTextColor(Color.parseColor(getString(R.color.text_color_inaktiv)));
				
				if (Build.VERSION.SDK_INT >= 16)
				    textview.setBackground(res.getDrawable(R.drawable.border_bottom));
				else
				    textview.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
				
				textview = (TextView)findViewById(R.id.tuners);
				textview.setText(Html.fromHtml("<b>" + getString(R.string.rec_info_tuners) + "</b>"));
				textview.append("\n"+result.get("name")+": "+result.get("type"));
				for(int count = 0; count <= multicount;count++){
					if(result.get("name"+count)==null){
						break;
					}else{
						textview.append("\n"+result.get("name"+count)+": "+result.get("type"+(++count)));
					}
				}
				//textview.setTextColor(Color.parseColor(getString(R.color.text_color_inaktiv)));
				
				if (Build.VERSION.SDK_INT >= 16)
				    textview.setBackground(res.getDrawable(R.drawable.border_bottom));
				else
				    textview.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
			}
			progressDialog.dismiss();
	    }
		
	}
	
	public void XmlParser(String[] request,int ListLength,NodeList List){
		for(int count=0;count < List.getLength();count++){
			if(List.item(count).hasChildNodes()){
				XmlParser(request,List.getLength(),List.item(count).getChildNodes());
				count++;
			}else{
				for(int i = 0;i < request.length;i++){
					if(List.item(count).getParentNode().getNodeName().equals(request[i])){
						if(result.containsKey(List.item(count).getParentNode().getNodeName())){
							result.put(List.item(count).getParentNode().getNodeName()+multicount++, List.item(count).getTextContent());
						}else{
							result.put(List.item(count).getParentNode().getNodeName(), List.item(count).getTextContent());
						}						
					}
				}
			}
		}
	}
	
	public class getRecStatus extends AsyncTask<String, Void, Integer>{	
		String[] profil = customClass.getActive();
		private ProgressDialog progressDialog = null;
		
		protected void onPreExecute (){
			progressDialog = ProgressDialog.show(Activity_rec_info.this, "", "Checking Settings and Connectivity!");
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
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Activity_rec_info.this);
			if(failure == 9){
				alertDialogBuilder
				.setTitle(dialogTitleText[0])
			    .setMessage(dialogTitleText[1])
		    	.setPositiveButton(R.string.dialog_yes,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						Intent intent = new Intent(Activity_rec_info.this, Activity_settings.class);
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
}
