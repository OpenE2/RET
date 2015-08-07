package com.MH.ret;

import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
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
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class Activity_sub_services extends ActionBarActivity {
	protected String validUrl;
	protected String validPort;
	protected String serviceRef;
	protected String chosenServiceRef;
	static final int VALID_URL = 0;
	protected customClass customClass = null;
	protected Resources res = null;
	protected LinearLayout.LayoutParams paramsForText = new LinearLayout.LayoutParams
			(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT,(float) 1.0);
	protected ScrollView.LayoutParams paramsForView = new ScrollView.LayoutParams
			(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
	protected LinearLayout linearLayout;
	protected ScrollView scrollView;
	protected Intent intent = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		customClass = (customClass)getApplicationContext();
		res = getApplicationContext().getResources();
		customClass.res = res;
		linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		scrollView = new ScrollView(this);
		intent = getIntent();
		validUrl = intent.getStringExtra("VALID_URL");
		validPort = intent.getStringExtra("VALID_PORT");
		serviceRef = intent.getStringExtra("SERVICE_REFERENCE");
		setTitle(intent.getStringExtra("TITLE"));
		new getXmlData().execute(validUrl+":"+validPort+getString
    			(R.string.req_get_subservices)+serviceRef);
	}

	@Override
	public void onResume(){
		intent.putExtra("TITLE", "");
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_sub_services, menu);
		return true;
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
	        case R.id.action_refresh:
	        	linearLayout.removeAllViews();
	        	scrollView.removeViewAt(0);
	        	new getXmlData().execute(validUrl+":"+validPort+getString
	        			(R.string.req_get_subservices)+serviceRef);
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
            	validPort = data.getStringExtra("VALID_Port");
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
			progressDialog = ProgressDialog.show(Activity_sub_services.this, "", "Checking Settings and Connectivity!");
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
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Activity_sub_services.this);
			if(failure == 9){
				alertDialogBuilder
				.setTitle(dialogTitleText[0])
			    .setMessage(dialogTitleText[1])
		    	.setPositiveButton(R.string.dialog_yes,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						Intent intent = new Intent(Activity_sub_services.this, Activity_settings.class);
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
		String[] request = res.getStringArray(R.array.rec_service);
		XMLparser xmlParser = new XMLparser();
		protected void onPreExecute (){
			progressDialog = ProgressDialog.show(Activity_sub_services.this, "", "LOADING!");
		}
		@Override
		protected NodeList doInBackground(String... url) {
			// TODO Auto-generated method stubs
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
		            	InputSource is = new InputSource(in);
		            	//is.setEncoding("UTF-8");
		            	Document doc = db.parse(is);
		            	doc.normalize();
		            	return doc.getElementsByTagName("*").item(0).getChildNodes();
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
				String regularExpression = ("[0-9]*");
				xmlParser.parseXML(request, list);
				paramsForText.setMargins(20, 0, 20, 0);
				Calendar mCalendar = new GregorianCalendar();
				
				String e2eventstart;
				String e2eventduration;
				String e2eventtitle;
				String e2eventservicereference;
				String e2eventservicename;
				String e2eventcurrenttime;
				
				for(int i = 0;i<xmlParser.iterationCount;i++){
					TextView service = new TextView(Activity_sub_services.this);
					e2eventstart = xmlParser.result.get("e2eventstart"+i);
					e2eventduration = xmlParser.result.get("e2eventduration"+(++i));
					e2eventcurrenttime = xmlParser.result.get("e2eventcurrenttime"+(++i));
					e2eventtitle = xmlParser.result.get("e2eventtitle"+(++i));
					e2eventservicereference = xmlParser.result.get("e2eventservicereference"+(++i));
					e2eventservicename = xmlParser.result.get("e2eventservicename"+(++i));
					
					service.setTag(e2eventservicereference);
					service.setText(Html.fromHtml("<b>"+e2eventservicename+ "</b><br />"));
					
					if(e2eventtitle == null || e2eventtitle.equals("None")||e2eventtitle.equals("")){
						service.append("No EPG available");
					}else{
						mCalendar.setTimeInMillis(Long.parseLong(e2eventstart)*1000);
						service.append(Html.fromHtml(e2eventtitle+ "<br />"));
						
						if(e2eventstart != null && e2eventstart.matches(regularExpression)){
							service.append(dayOfWeek(mCalendar.get(Calendar.DAY_OF_WEEK))									
									+" "+mCalendar.get(Calendar.DAY_OF_MONTH)
									+" "+month(mCalendar.get(Calendar.MONTH))
									+" "+mCalendar.get(Calendar.HOUR_OF_DAY)
									+":");							
						}
						if(mCalendar.get(Calendar.MINUTE)<10){
							service.append("0"+mCalendar.get(Calendar.MINUTE));
						}else service.append(""+mCalendar.get(Calendar.MINUTE));
						
						mCalendar.setTimeInMillis(Long.parseLong(e2eventstart)*1000+Long.parseLong(e2eventduration)*1000);
						service.append(" - "+mCalendar.get(Calendar.HOUR_OF_DAY)+":");
						
						if(mCalendar.get(Calendar.MINUTE)<10){
							service.append("0"+mCalendar.get(Calendar.MINUTE));
						}else service.append(""+mCalendar.get(Calendar.MINUTE));
						
						mCalendar.setTimeInMillis(Long.parseLong(e2eventstart)*1000
								+Long.parseLong(e2eventduration)*1000
								-Long.parseLong(e2eventcurrenttime)*1000);
						service.append("      +"+mCalendar.getTimeInMillis()/60000);
					}
				
					if (Build.VERSION.SDK_INT >= 16){
						service.setBackground(res.getDrawable(R.drawable.border_bottom));
					}else{
						service.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
					}
					service.setGravity(Gravity.CENTER_VERTICAL);
					service.setPadding(0, 15, 0, 15);
					service.setClickable(true);
					service.setOnClickListener(new View.OnClickListener() {
					    public void onClick(View v) {
					    	TextView choosenView = (TextView)v;
					    	chosenServiceRef = choosenView.getTag().toString();
					    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Activity_sub_services.this);
							alertDialogBuilder.setTitle(choosenView.getText().toString()).setCancelable(true)
					    	.setItems(R.array.services_dialog_array,new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									if(validUrl == null){
										new getRecStatus().execute(customClass.connectWith());
									}else{
										intent(id,chosenServiceRef);
									}
									dialog.dismiss();
								}
							  }).setNegativeButton(null, null);
							AlertDialog alertDialog = alertDialogBuilder.create();
							alertDialog.show();
					    }
					});
					linearLayout.addView(service,paramsForText);
				}
				scrollView.addView(linearLayout);
				setContentView(scrollView);
			}
			progressDialog.dismiss();
		}
	}
	
	public String dayOfWeek(int day){
		switch(day){
		case 1: return "Sun";
		case 2: return "Mon";
		case 3: return "Tue";
		case 4: return "Wed";
		case 5: return "Thu";
		case 6: return "Fri";
		case 7: return "Sat";
		}
		return null;
	}
	
	public String month(int month){
		switch(month){
		case 0: return "Jan";
		case 1: return "Feb";
		case 2: return "Mar";
		case 3: return "Apr";
		case 4: return "Mai";
		case 5: return "Jun";
		case 6: return "Jul";
		case 7: return "Aug";
		case 8: return "Seb";
		case 9: return "Okt";
		case 10: return "Nov";
		case 11: return "Dec";
		}
		return null;
	}
	
	public void intent(Integer id, String serviceRef){
		switch(id){
		case 0:
			new sendCommand().execute(validUrl+":"+validPort+res.getString(R.string.services_zap)+serviceRef);
			break;
		case 1:
			PackageManager packageManager = getPackageManager();
			Intent testintent = new Intent(Intent.ACTION_VIEW);
			Uri uri = Uri.parse(validUrl+":"+validPort+"01/"+serviceRef); 
			testintent.setDataAndType(uri, "video/mp4");
			List<ResolveInfo> playerList;
			playerList = packageManager.queryIntentActivities(testintent, 0);
			for(ResolveInfo rinfo:playerList){
				System.out.println(rinfo.activityInfo.applicationInfo.loadLabel(packageManager).toString());
			}
			if(playerList.size() > 0){
				startActivity(testintent);
			}
			
			/**Uri uri = Uri.parse(validUrl+":"+validPort+"01/"+serviceRef);
			System.out.println(uri);
			Intent testIntent = new Intent(Intent.ACTION_VIEW,uri);
			testIntent.setDataAndType(uri, "video/*");
			PackageManager packageManager = getPackageManager();
			List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
			if(activities.size() > 0){
				startActivity(testIntent);
			}*/
			break;
		default:
			intent.putExtra("CHOICE", id);
			intent.putExtra("SERVICE_REF", serviceRef);
			intent.putExtra("VALID_URL", validUrl);
			intent.putExtra("VALID_PORT", validPort);
			intent.setClass(this, Activity_epg.class);
			startActivity(intent);
		}
	}
	
	public void streamIntent(){
		
	}
	
	public class XMLparser{
		Boolean jump = false;
		Map<String, String> result = new HashMap<String, String>();
		int iterationCount = 0;
		public void parseXML(String[] request,NodeList List){
			for(int count=0;count < List.getLength();count++){
				for(int i = 0;i<request.length;i++){
					if(List.item(count).getNodeName().equals(request[i])){
						result.put(List.item(count).getNodeName()+iterationCount++, List.item(count).getTextContent());
						jump = true;
					}
				}
				if(List.item(count).hasChildNodes() && jump == false){
					parseXML(request,List.item(count).getChildNodes());
				}else jump = false;
			}
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
