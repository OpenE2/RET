package com.MH.ret;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpConnectionParams;
import com.MH.ret.R;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Activity_grab_screen extends ActionBarActivity {
	protected Bitmap bitmap = null;
	protected String mCurrentPhotoPath;
	protected Intent intent = null;
	protected AlertDialog.Builder alertDialogBuilder = null;
	protected customClass customClass = null;
	protected Resources res = null;
	protected String validUrl;
	protected String validPort;
	static final int VALID_URL = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity_grab_screen);
		customClass = (customClass)getApplicationContext();
		res = getApplicationContext().getResources();
		customClass.res = res;
		alertDialogBuilder = new AlertDialog.Builder(Activity_grab_screen.this);
		intent = getIntent();
		validUrl = intent.getStringExtra("VALID_URL");
		validPort = intent.getStringExtra("VALID_PORT");
		new Screenshot().execute(validUrl+":"+validPort+getString(R.string.req_grab),
				validUrl+":"+validPort+getString(R.string.req_powerstate));
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
	    inflater.inflate(R.menu.activity_grab_screen, menu);
	    return super.onCreateOptionsMenu(menu);
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_refresh:
	        	new Screenshot().execute(intent.getStringExtra("VALID_URL")+getString(R.string.req_grab));
	        	return true;
	        case R.id.action_save:
	        	if(bitmap!=null){
	        		try {
	    				saveImage(createImageFile());
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	        	}else{
	        		Toast toast = Toast.makeText(getApplicationContext(), res.getString(R.string.toast_message_no_image), Toast.LENGTH_SHORT);
					toast.show();
	        	}
	        	return true;
	        case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
	        case R.id.action_settings:
	        	intent = new Intent(this, Activity_settings.class);
	        	startActivityForResult(intent, VALID_URL);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	public class Screenshot extends AsyncTask<String, Void, Bitmap>{
		
		private ProgressDialog progressDialog = null;
	
		protected void onPreExecute (){
			//Start ProgressDialog
			progressDialog = ProgressDialog.show(Activity_grab_screen.this, "", "LOADING IMAGE!");
		}
		@Override
		protected Bitmap doInBackground(String... url) {
			// TODO Auto-generated method stub			
			final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
			HttpConnectionParams.setConnectionTimeout(client.getParams(), 5000);
			HttpConnectionParams.setSoTimeout(client.getParams(), 5000);
		    final HttpGet getRequest = new HttpGet(url[0]);		    
		    try {
		        HttpResponse response = client.execute(getRequest);
		        final int statusCode = response.getStatusLine().getStatusCode();
		        if (statusCode != HttpStatus.SC_OK) { 
		            Log.w("ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url[0]); 
		            return null;
		        }
		        
		        final HttpEntity entity = response.getEntity();
		        if (entity != null) {
		            InputStream inputStream = null;
		            try {
		                inputStream = entity.getContent();
		                bitmap = BitmapFactory.decodeStream(inputStream);
		                return bitmap;
		            } finally {
		                if (inputStream != null) {
		                    inputStream.close();
		                }
		                entity.consumeContent();
		            }
		        }
		    }catch (Exception e) {
		        // Could provide a more explicit error message for IOException or IllegalStateException
		        getRequest.abort();
		        Log.w("ImageDownloader", "Error while retrieving bitmap from " + url[0]);
		    } finally {
		        if (client != null) {
		            client.close();
		        }
		    }
		    return null;
		}
		protected void onPostExecute(Bitmap picture) {
			
			if(picture == null){
				new getRecStatus().execute(customClass.connectWith());
			}else{
				ImageView image = new ImageView(getApplicationContext());
				image.setImageBitmap(picture);
				setContentView(image);
			}
			progressDialog.dismiss();
	    }
	}
	
	public class getRecStatus extends AsyncTask<String, Void, Integer>{	
		String[] profil = customClass.getActive();
		private ProgressDialog progressDialog = null;
		
		protected void onPreExecute (){
			progressDialog = ProgressDialog.show(Activity_grab_screen.this, "", "Checking Settings and Connectivity!");
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
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Activity_grab_screen.this);
			if(failure == 9){
				alertDialogBuilder
				.setTitle(dialogTitleText[0])
			    .setMessage(dialogTitleText[1])
		    	.setPositiveButton(R.string.dialog_yes,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						Intent intent = new Intent(Activity_grab_screen.this, Activity_settings.class);
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
	
	public void saveImage(File image){
		FileOutputStream outStream;
		if(image!=null){
			try { 
				outStream = new FileOutputStream(image);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream); /* 100 to keep full quality of the image */
				outStream.flush();
				outStream.close();
				
				} catch (FileNotFoundException e) { 
					System.out.println("test1");
					e.printStackTrace();
					System.out.println("test");
					Toast toast = Toast.makeText(getApplicationContext(), res.getString(R.string.toast_message_image_error), Toast.LENGTH_SHORT);
					toast.show();
					return;
				} catch (IOException e) { 
					Toast toast = Toast.makeText(getApplicationContext(), res.getString(R.string.toast_message_image_error), Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
			Toast toast = Toast.makeText(getApplicationContext(), res.getString(R.string.toast_message_image_saved,image.getAbsolutePath()), Toast.LENGTH_SHORT);
			toast.show();
		}
		
	}

	private File createImageFile() throws IOException {
	    // Create an image file name
		Date date = new Date();
		File image = null;
	    String imageFileName = "SCREENSHOT_" + date.toString();
	    try{
	    	File storageDir = Environment.getExternalStoragePublicDirectory(
	 	            Environment.DIRECTORY_PICTURES);
	 	    System.out.println(storageDir);
	 	    image = File.createTempFile(
	 	        imageFileName,  /* prefix */
	 	        ".jpg",         /* suffix */
	 	        storageDir      /* directory */
	 	    );
	 	    // Save a file: path for use with ACTION_VIEW intents
	 	    mCurrentPhotoPath = "file:" + image.getAbsolutePath();
	 	    
	    }catch(IOException e){
	    	Toast toast = Toast.makeText(getApplicationContext(), res.getString(R.string.toast_message_image_error), Toast.LENGTH_SHORT);
			toast.show();
	    }
	    return image;
	}
}
