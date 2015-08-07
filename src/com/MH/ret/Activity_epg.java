package com.MH.ret;

import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;

public class Activity_epg extends ActionBarActivity {
	protected String validUrl;
	protected String validPort;
	protected Resources res = null;
	static final int VALID_URL = 0;
	protected customClass customClass = null;
	protected Intent intent = null;
	protected String chosenServiceRef;
	protected LinearLayout.LayoutParams paramsForText = new LinearLayout.LayoutParams
			(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT,(float) 1.0);
	protected ScrollView.LayoutParams paramsForView = new ScrollView.LayoutParams
			(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
	protected LinearLayout listForScroll;
	protected ScrollView scrollViewEpg;
	protected AlertDialog.Builder alertDialogBuilder;
	protected String e2eventid;
	protected String e2eventstart;
	protected String e2eventduration;
	protected String e2eventtitle;
	protected String e2eventdescription;
	protected String e2eventdescriptionextended;
	protected String e2eventservicereference;
	protected String e2eventservicename = "EPG";
	protected Map<String, String> titles;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getApplicationContext().getResources();
		customClass = (customClass)getApplicationContext();
		customClass.res = res;
		listForScroll = new LinearLayout(this);
		listForScroll.setOrientation(LinearLayout.VERTICAL);
		scrollViewEpg = new ScrollView(this);
		// Show the Up button in the action bar.
		intent = getIntent();
		validUrl = intent.getStringExtra("VALID_URL");	
		validPort = intent.getStringExtra("VALID_PORT");
		new getXmlEpgData().execute(validUrl+":"+validPort+res.getString(R.string.req_get_epg)+intent.getStringExtra("SERVICE_REF"));
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_epg, menu);
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
	        	intent = new Intent(this, Activity_settings.class);
	        	startActivityForResult(intent, VALID_URL);
	        	return true;
	        case R.id.action_refresh:
	        	listForScroll.removeAllViews();
	        	scrollViewEpg.removeViewAt(0);
	        	new getXmlEpgData().execute(validUrl+":"+validPort+res.getString(R.string.req_get_epg)+intent.getStringExtra("SERVICE_REF"));
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
			progressDialog = ProgressDialog.show(Activity_epg.this, "", "Checking Settings and Connectivity!");
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
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Activity_epg.this);
			if(failure == 9){
				alertDialogBuilder
				.setTitle(dialogTitleText[0])
			    .setMessage(dialogTitleText[1])
		    	.setPositiveButton(R.string.dialog_yes,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						Intent intent = new Intent(Activity_epg.this, Activity_settings.class);
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
	
	@SuppressLint("NewApi")
	public class getXmlEpgData extends AsyncTask<String, Void, NodeList>{
		ProgressDialog progressDialog = null;
		String[] request = res.getStringArray(R.array.rec_epg);
		XMLparser xmlParser = new XMLparser();
		protected void onPreExecute (){
			progressDialog = ProgressDialog.show(Activity_epg.this, "", "LOADING!");
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
		protected void onPostExecute(NodeList list) {
			if(isCancelled()||list == null){
				new getRecStatus().execute(customClass.connectWith());
			}else{
				titles = new HashMap<String, String>();
				String regularExpression = ("[0-9]*");
				xmlParser.parseXML(request, list);
				paramsForText.setMargins(20, 0, 20, 0);
				Calendar mCalendar = new GregorianCalendar();
				for(int i = 0;i<xmlParser.iterationCount;i++){
					TextView epg = new TextView(Activity_epg.this);
					e2eventid = xmlParser.result.get("e2eventid"+i);
					e2eventstart = xmlParser.result.get("e2eventstart"+(++i));
					e2eventduration = xmlParser.result.get("e2eventduration"+(++i));
					e2eventtitle = xmlParser.result.get("e2eventtitle"+(++i));		
					e2eventdescription = xmlParser.result.get("e2eventdescription"+(++i));
					e2eventdescriptionextended = xmlParser.result.get("e2eventdescriptionextended"+(++i));
					e2eventservicereference = xmlParser.result.get("e2eventservicereference"+(++i));
					e2eventservicename = xmlParser.result.get("e2eventservicename"+(++i));
					if(e2eventtitle == null || e2eventtitle.equals("None")||e2eventtitle.equals("")){
						epg.append("No EPG available");
					}else{
						mCalendar.setTimeInMillis(Long.parseLong(e2eventstart)*1000);
						epg.setTag(e2eventservicereference);
						epg.setHint(e2eventid);
						epg.setText(Html.fromHtml("<b>"+e2eventtitle+ "</b><br />"));
						epg.setContentDescription(e2eventdescription+ "\n\n"+e2eventdescriptionextended);
						titles.put(e2eventid, e2eventtitle);						
						if(e2eventstart != null && e2eventstart.matches(regularExpression)){
							epg.append(dayOfWeek(mCalendar.get(Calendar.DAY_OF_WEEK))
									+" "+mCalendar.get(Calendar.DAY_OF_MONTH)
									+" "+month(mCalendar.get(Calendar.MONTH))
									+" "+mCalendar.get(Calendar.HOUR_OF_DAY)
									+":");
						}
						if(mCalendar.get(Calendar.MINUTE)<10){
							epg.append("0"+mCalendar.get(Calendar.MINUTE));
						}else epg.append(""+mCalendar.get(Calendar.MINUTE));
						
						mCalendar.setTimeInMillis(Long.parseLong(e2eventstart)*1000+Long.parseLong(e2eventduration)*1000);
						epg.append(" - "+mCalendar.get(Calendar.HOUR_OF_DAY)+":");
						
						if(mCalendar.get(Calendar.MINUTE)<10){
							epg.append("0"+mCalendar.get(Calendar.MINUTE));
						}else epg.append(""+mCalendar.get(Calendar.MINUTE));
					}
					if (Build.VERSION.SDK_INT >= 16){
						epg.setBackground(res.getDrawable(R.drawable.border_bottom));
					}else{
						epg.setBackgroundDrawable(res.getDrawable(R.drawable.border_bottom));
					}
					epg.setGravity(Gravity.CENTER_VERTICAL);
					epg.setPadding(0, 10, 0, 10);
					epg.setClickable(true);
					epg.setOnClickListener(new View.OnClickListener() {
					    public void onClick(View v) {
					    	final TextView choosenView = (TextView)v;
					    	chosenServiceRef = choosenView.getTag().toString();
					    	alertDialogBuilder = new AlertDialog.Builder(Activity_epg.this);
							alertDialogBuilder.setTitle(choosenView.getText().toString()).setCancelable(true)
					    	.setItems(R.array.epg_dialog_array,new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									if(validUrl == null){
										new getRecStatus().execute(customClass.connectWith());
									}else{
										intent(id,choosenView.getHint().toString(), choosenView.getTag().toString(),choosenView.getContentDescription().toString(),choosenView.getText().toString());
									}
									dialog.dismiss();
								}
							  }).setNegativeButton(null, null);
							AlertDialog alertDialog = alertDialogBuilder.create();
							alertDialog.show();
					    }
					});
					listForScroll.addView(epg,paramsForText);					
				}
				scrollViewEpg.addView(listForScroll);
				setTitle(e2eventservicename);
				setContentView(scrollViewEpg);
				scrollViewEpg.scrollTo(0, 0);
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
	
	public void intent(Integer id,String eventId, String serviceRef, String contentDescription, String title){
		
		switch(id){
		case 0:
			alertDialogBuilder = new AlertDialog.Builder(Activity_epg.this);
			alertDialogBuilder
			.setTitle(title)
			.setCancelable(true)
			.setMessage(contentDescription)
			.setNegativeButton(R.string.dialog_back,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams(); 
			Window window = alertDialog.getWindow(); 
			lp.copyFrom(window.getAttributes()); //This makes the dialog take up the full width 
			lp.width = WindowManager.LayoutParams.MATCH_PARENT; 
			lp.height = WindowManager.LayoutParams.WRAP_CONTENT; 
			window.setAttributes(lp);
			
			break;
		case 1:
			new sendCommand().execute(validUrl+":"+validPort+res.getString(R.string.req_add_timer,serviceRef,eventId.toString(),"True"));
			break;
		case 2:
			intent.putExtra("VALID_URL", validUrl);
			intent.putExtra("VALID_PORT", validPort);
			intent.putExtra("TITLE", titles.get(eventId));
			intent.setClass(this, Activity_search_epg.class);	
			startActivityForResult(intent,VALID_URL);
			break;
		default:
		}
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
			}else {
				Toast toast = Toast.makeText(getApplicationContext(), res.getString(R.string.toast_message_timer), Toast.LENGTH_SHORT);
				toast.show();
			}	    	
		}
	}
}
