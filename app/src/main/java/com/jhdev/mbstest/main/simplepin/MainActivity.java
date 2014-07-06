package com.jhdev.mbstest.main.simplepin;
 
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
 
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.analytics.tracking.android.EasyTracker;
import com.jhdev.mbstest.main.R;
import com.jhdev.mbstest.main.core.CloudBackend;
import com.jhdev.mbstest.main.core.CloudBackendAsync;
import com.jhdev.mbstest.main.core.CloudBackendFragment;
import com.jhdev.mbstest.main.core.CloudBackendFragment.OnListener;
import com.jhdev.mbstest.main.core.CloudCallbackHandler;
import com.jhdev.mbstest.main.core.CloudEntity;
import com.jhdev.mbstest.main.core.Consts;
//import com.jhdev.mbstest.main.guestbook.SplashFragment;



public class MainActivity extends FragmentActivity 
	implements LoaderCallbacks<Cursor>, MarkerCreateDialogFragment.MarkerCreateDialogListener,
    GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, OnListener {
	
//	private ShareActionProvider mShareActionProvider;
 	 
    protected static final String Alert = null;
	private static final String TAG = null;
    public static String newMarkerCompleteAddress = null;
	GoogleMap googleMap;
	public static LatLng newMarkerLatLng = null;
	public static LatLng lastMarkerLatLng = null;
	LocationClient mLocationClient;
	Location mCurrentLocation;

    private FragmentManager mFragmentManager;
    private CloudBackendFragment mProcessingFragment;
//    private SplashFragment mSplashFragment;

    private static final String PROCESSING_FRAGMENT_TAG = "BACKEND_FRAGMENT";
    private static final String SPLASH_FRAGMENT_TAG = "SPLASH_FRAGMENT";

    private static final String BROADCAST_PROP_DURATION = "duration";
    private static final String BROADCAST_PROP_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_simplepin);

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
 
        // Showing status
        if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available
 
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
 
        }else { // Google Play Services are available
        	Log.e("Load", "Google Play Services available");
            // Getting reference to the SupportMapFragment of activity_main.xml
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        	//MapFragment fm = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
 
            // Getting GoogleMap object from the fragment
            googleMap = fm.getMap();
   
            // Enabling MyLocation Layer of Google Map
            // disabled this feature because it keeps GPS on
            googleMap.setMyLocationEnabled(true);
            
			// Invoke LoaderCallbacks to retrieve and draw already saved locations in map
            getSupportLoaderManager().initLoader(0, null, this);
            
            //centers the map somewhere
            if (lastMarkerLatLng == null) {
	            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(37.809,-122.476));
	            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
	            googleMap.moveCamera(center);
	            googleMap.animateCamera(zoom);
            };
        }
        


        // Listen for Marker clicks
        googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker){
                //Toast.makeText(getBaseContext(), "marker clicked" + marker, Toast.LENGTH_SHORT).show();
            	return false;
            }
        });
        
        // Listen for Map Long clicks to create and save new markers
        googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
             @Override
            public void onMapLongClick(LatLng point) {
            	
            	newMarkerLatLng = point;
            	//start geo coder
    			Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
    			try {    	
    				List<Address> addresses = geoCoder.getFromLocation(
    						point.latitude,
    						point.longitude, 1);    				
    				
    				newMarkerCompleteAddress = "";
    				Address address = addresses.get(0);
    				if (address.getLocality() == null) {
        				if (addresses.size() > 0) {    				
    						newMarkerCompleteAddress = String.format("%s, %s",
    							address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
    							address.getCountryName());
        				} 
    				} else {
        				if (addresses.size() > 0) {    				
    						newMarkerCompleteAddress = String.format("%s, %s, %s",
    							address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
    							address.getLocality(),
    							address.getCountryName());
        				}    	
    				}		
    				    			    				
    				//Log.i("MyLocTAG => ", newMarkerCompleteAddress);
    				    				
    			} catch (IOException e) {		
    				e.printStackTrace();
    			}
    			//end Geocoder
                    			
    			// Start Dialog    			
    			showMarkerCreateDialog();  
    			

            }            
        });
      
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
        
 /*       // Listen for clicks on infowindow
        googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
        	@Override
        	public void onInfoWindowClick(Marker marker) {
        		
            	Intent intent = new Intent(MainActivity.this, LocationDetailActivity.class);
            	
            	//intent.putExtra("position", position);
            	String title = String.valueOf(marker.getTitle());
            	intent.putExtra("title", title);
            	Log.e("detail tag1", "onInfoWindowClick title=" + title );
                     	
            	startActivity(intent); 
        		
        	}
        });*/

        
        // Listen for Intents from other apps
        // Get the intent that started this activity
        Intent intent = getIntent();
        Uri data = intent.getData();        
        // Figure out what to do based on the intent type
        //if (intent.getType().indexOf("image/") != -1) {
            // Handle intents with image data ...
        	String action = intent.getAction();
        	String type = intent.getType();
        	Log.d("intent", "uri is:" + data);
        	Log.d("intent2", "action is:" + action);
        	Log.d("intent3", "type is:" + type);
        	
        //} else if (intent.getType().equals("text/plain")) {
            // Handle intents with text ...
        //}
        
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	String id = extras.getString("ids");
        	
        	if (id != null){
        		Log.e("Intent", "Intent received, id=" + id);
    			ContentResolver content = getContentResolver();
    			Uri uri = Uri.withAppendedPath(LocationsContentProvider.CONTENT_URI, id);
    			Cursor cursor = content.query(uri, null, null, null, null);
    			cursor.moveToFirst();
    			double mlat = cursor.getDouble(1);
    			double mlng = cursor.getDouble(2);
    			String mtitle = cursor.getString(6);
    			lastMarkerLatLng = new LatLng(mlat, mlng);
        		Log.e("Intent", "mlatlng=" + lastMarkerLatLng + "title: " + mtitle);

                // Moving CameraPosition to this marker's position
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastMarkerLatLng,10));                       
        	}        	
        }

        mFragmentManager = getFragmentManager();
        initiateFragments();
        //end of section
    }

    @Override
    protected void onNewIntent (Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.e("Intent", "onNewIntent is called. Intent=" + intent);
        //now getIntent() should always return the last received intent
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	Log.e("Intent", "onResume is called.");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	String id = extras.getString("ids");
        	
        	if (id != null){
        		Log.e("Intent", "Intent received, id=" + id);
    			ContentResolver content = getContentResolver();
    			Uri uri = Uri.withAppendedPath(LocationsContentProvider.CONTENT_URI, id);
    			Cursor cursor = content.query(uri, null, null, null, null);
    			cursor.moveToFirst();
    			double mlat = cursor.getDouble(1);
    			double mlng = cursor.getDouble(2);
    			String mtitle = cursor.getString(6);
    			float mzoom = cursor.getFloat(3);
    			lastMarkerLatLng = new LatLng(mlat, mlng);
        		Log.e("Intent", "mlatlng=" + lastMarkerLatLng + "title: " + mtitle);

                // Moving CameraPosition to this marker's position
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastMarkerLatLng,mzoom));                       
        	}        	
        }   	

    }
    
/*    private void drawMarker(LatLng point){
        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();
         // Setting latitude and longitude for the marker
        markerOptions.position(point);
         // Adding marker on the Google Map
        googleMap.addMarker(markerOptions);              
    }*/
 
    private class LocationInsertTask extends AsyncTask<ContentValues, Void, Void>{
        @Override
        protected Void doInBackground(ContentValues... contentValues) {
 
            /** Setting up values to insert the clicked location into SQLite database */
            getContentResolver().insert(LocationsContentProvider.CONTENT_URI, contentValues[0]);
            return null;
        }
    }
 
    private class LocationDeleteTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
 
            /** Deleting all the locations stored in SQLite database */
            getContentResolver().delete(LocationsContentProvider.CONTENT_URI, null, null);
            return null;
        }
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        
//	    // Locate MenuItem with ShareActionProvider
//	    MenuItem item = menu.findItem(R.id.menu_item_share);
//
//	    // Fetch and store ShareActionProvider
//	    mShareActionProvider = (ShareActionProvider) item.getActionProvider();	 
//    	Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("text/plain");
//        shareIntent.putExtra(Intent.EXTRA_TEXT, "This is a shareintent from mainactivity");            
//        mShareActionProvider.setShareIntent(shareIntent);
//        
//    	Log.d("shareintent", "1" + shareIntent);
    	
    	return true;
    }
    

    

 
    @Override
    public Loader<Cursor> onCreateLoader(int arg0,
        Bundle arg1) {
 
        // Uri to the content provider LocationsContentProvider
        Uri uri = LocationsContentProvider.CONTENT_URI;
 
        // Fetches all the rows from locations table
        return new CursorLoader(this, uri, null, null, null, null);
    }
 
    @Override
    public void onLoadFinished(Loader<Cursor> arg0,
        Cursor arg1) {
        int locationCount = 0;
        double lat=0;
        double lng=0;
        float zoom=0;
        
        // Number of locations available in the SQLite database table
        locationCount = arg1.getCount();
 
        // Move the current record pointer to the first row of the table
        arg1.moveToFirst();
  
        for(int i=0;i<locationCount;i++){
 
        	// Get the details for putting into marker. I'm sure there's some way of condensing this.
            // Get the latitude
            lat = arg1.getDouble(arg1.getColumnIndex(LocationsContentProvider.FIELD_LAT));
            Log.d(TAG, "marker: LAT :"  + lat );
            lng = arg1.getDouble(arg1.getColumnIndex(LocationsContentProvider.FIELD_LNG));
            zoom = arg1.getFloat(arg1.getColumnIndex(LocationsContentProvider.FIELD_ZOOM));
             // Creating an instance of LatLng to plot the location in Google Maps
            LatLng location = new LatLng(lat, lng);
            String addr = arg1.getString(arg1.getColumnIndex(LocationsContentProvider.FIELD_ADDRESS));
            // TODO replace sub_map_name once second table is joined
            //String sub_map_name = arg1.getString(arg1.getColumnIndex(LocationsContentProvider.FIELD_SUB_MAP));            
            String title = arg1.getString(arg1.getColumnIndex(LocationsContentProvider.FIELD_TITLE));
            
            googleMap.addMarker(new MarkerOptions()
            		.position(location)
            		//.title("submap: " + sub_map_name + "\n Title: " + title )
            		.title(title)
            		.snippet(addr)
    				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
             
//            // Drawing the marker in the Google Maps
//            drawMarker(location);
            
             // Traverse the pointer to the next row
            arg1.moveToNext();
        }
 
        if(locationCount>0 && lastMarkerLatLng == null){
            // Moving CameraPosition to last clicked position
        	googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,lng)));
            // Setting the zoom level in the map on last position  is clicked
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
            }
    }
 
    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub
    }

    public void showMarkerCreateDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new MarkerCreateDialogFragment();
        dialog.show(getFragmentManager(), "MarkerCreateDialogFragment");
    }
    
    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the MarkerCreateDialogFragment.MarkerCreateDialogListener interface
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
        Log.i("MarkerCrateAlertDialog", "Positive click!");
        // retrieve dialog
        Dialog dialogView = dialog.getDialog();
        // look for text
        EditText newMarkerTitleEdit = (EditText) dialogView.findViewById(R.id.marker_title);
		String newMarkerTitle = newMarkerTitleEdit.getText().toString();
        Log.i("MarkerCreateAlertDialog", "Return EditText_to_s:" + newMarkerTitle);
        Log.i("MarkerCreateAlertDialog", "Return newMarkerCompleteAddress:" + newMarkerCompleteAddress);
        Log.i("MarkerCreateAlertDialog", "Return newMarkerLatLng:" + newMarkerLatLng);
        // assign text
        
        // Save a new marker       

        // Creating an instance of ContentValues
        ContentValues contentValues = new ContentValues();

        // Setting latitude in ContentValues
        contentValues.put(LocationsContentProvider.FIELD_LAT, newMarkerLatLng.latitude );
        contentValues.put(LocationsContentProvider.FIELD_LNG, newMarkerLatLng.longitude);
        contentValues.put(LocationsContentProvider.FIELD_ZOOM, googleMap.getCameraPosition().zoom);
        contentValues.put(LocationsContentProvider.FIELD_ADDRESS, newMarkerCompleteAddress);
        contentValues.put(LocationsContentProvider.FIELD_SUB_MAP_ID, "1");
        contentValues.put(LocationsContentProvider.FIELD_TITLE, newMarkerTitle);

        // Creating an instance of LocationInsertTask
        LocationInsertTask insertTask = new LocationInsertTask();

        // Storing the latitude, longitude and zoom level to SQLite database
        insertTask.execute(contentValues);
        
        // Get address and create full new marker            		
        Marker marker = googleMap.addMarker(new MarkerOptions()
        		.position(newMarkerLatLng)
        		.title(newMarkerTitle)
        		.snippet(newMarkerCompleteAddress)
        		.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        // Drawing marker on the map
        marker.showInfoWindow();
        //drawMarker(point);
    	googleMap.moveCamera(CameraUpdateFactory.newLatLng(newMarkerLatLng));

        
        Toast.makeText(getBaseContext(), "Marker is added to the Map.\nAddress: " + newMarkerCompleteAddress, Toast.LENGTH_SHORT).show();                

        // New Cloud Backend Appengine Entity
        CloudEntity ce = new CloudEntity("simplepin");
        ce.put("title", newMarkerTitle);
        ce.put("address", newMarkerCompleteAddress);
        ce.put("latitude", newMarkerLatLng.latitude);
        ce.put("longitude", newMarkerLatLng.longitude);

        CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
            @Override
            public void onComplete(final CloudEntity result) {
//                mPosts.add(0, result);
//                updateGuestbookView();
//                mMessageTxt.setText("");
//                mMessageTxt.setEnabled(true);
//                mSendBtn.setEnabled(true);
            }

            @Override
            public void onError(final IOException exception) {
                handleEndpointException(exception);
            }
        };

        // execute the insertion with the handler
        mProcessingFragment.getCloudBackend().insert(ce, handler);

//
//            CloudBackend cb = new CloudBackend();
//        CloudBackendAsync cba = new CloudBackendAsync(getApplicationContext());
//        try {
//            cba.insert(ce);
//            Toast.makeText(getBaseContext(), "datastore save", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            Toast.makeText(getBaseContext(), "datastore failed", Toast.LENGTH_SHORT).show();
//        }


//        cloudInsert(ce);
//        //TODO do in background to see if it solves the error
//        CloudBackend cb = new CloudBackend();
//        try {
//            cb.insert(ce);
//            Toast.makeText(getBaseContext(), "datastore save", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            Toast.makeText(getBaseContext(), "datastore failed", Toast.LENGTH_SHORT).show();
//        }
	}

//    private class cloudInsert extends AsyncTask<CloudEntity, Void, Void> {
//        CloudBackend cb = new CloudBackend();
//
//            protected String doInBackground(CloudEntity ce) {
//                // Do work
//                try {
//                    cb.insert(ce);
//                    Toast.makeText(getBaseContext(), "datastore save", Toast.LENGTH_SHORT).show();
//                } catch (IOException e) {
//                    Toast.makeText(getBaseContext(), "datastore failed", Toast.LENGTH_SHORT).show();
//                }
////            @Override protected void onPostExecute(String result) {
////                Log.d("MyAsyncTask", "Received result: " + result);
////            }
//        }
//
//    }


	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
        Log.i("MarkerCrateAlertDialog", "Negative click!");

	}

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
    	//Toast.makeText(getBaseContext(), "Selected", Toast.LENGTH_SHORT).show();    	
    	switch (item.getItemId()) {
    	
    	case R.id.delete_all_markers:
    		// Removing all markers from the Google Map
            googleMap.clear();

            // Creating an instance of LocationDeleteTask
            LocationDeleteTask deleteTask = new LocationDeleteTask();

            // Deleting all the rows from SQLite database table
            deleteTask.execute();

            Toast.makeText(getBaseContext(), "All markers are removed", Toast.LENGTH_LONG).show();
            return true;
    	case R.id.location_list:
    		startActivity(new Intent(this, LocationList.class));
    		break;
    	case R.id.about:
    	    // Display 'About' page (activity)    
    	    Intent opAbout = new Intent(this, About.class);
    	    startActivity(opAbout);
    	    break;    	    
    	case R.id.save_my_location:
    		saveMyLocation();
    		break;
    	}
        
    	return true;
    }
    
    
    private void saveMyLocation() {
		mCurrentLocation = mLocationClient.getLastLocation();
        //Toast.makeText(getBaseContext(), "save my location pressed = " + mCurrentLocation, Toast.LENGTH_LONG).show();

        
    	newMarkerLatLng = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
    	//start geo coder
		Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
		try {    	
			List<Address> addresses = geoCoder.getFromLocation(
					newMarkerLatLng.latitude,
					newMarkerLatLng.longitude, 1);    				
			
			newMarkerCompleteAddress = "";
			Address address = addresses.get(0);
			if (address.getLocality() == null) {
				if (addresses.size() > 0) {    				
					newMarkerCompleteAddress = String.format("%s, %s",
						address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
						address.getCountryName());
				} 
			} else {
				if (addresses.size() > 0) {    				
					newMarkerCompleteAddress = String.format("%s, %s, %s",
						address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
						address.getLocality(),
						address.getCountryName());
				}    	
			}		
			    			    				
			//Log.i("MyLocTAG => ", newMarkerCompleteAddress);
			    				
		} catch (IOException e) {		
			e.printStackTrace();
		}
		//end Geocoder
            			
		// Start Dialog    			
		showMarkerCreateDialog();  
		

    }
    
    
    
    
    
    
    //For Google Analytics
    @Override
    public void onStart() {
      super.onStart();
      // The rest of your onStart() code.
      EasyTracker.getInstance(this).activityStart(this);  // Add this method.
      mLocationClient.connect(); // for location
    }

    @Override
    public void onStop() {
      super.onStop();
      // The rest of your onStop() code.
      EasyTracker.getInstance(this).activityStop(this);  // Add this method.
      mLocationClient.disconnect(); // for location
    }

    
    
    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
    	// disable for public mode
        // Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }
    
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    //For CLoudBackend Processing
    private void initiateFragments() {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        // Check to see if we have retained the fragment which handles
        // asynchronous backend calls
        mProcessingFragment = (CloudBackendFragment) mFragmentManager.
                findFragmentByTag(PROCESSING_FRAGMENT_TAG);
        // If not retained (or first time running), create a new one
        if (mProcessingFragment == null) {
            mProcessingFragment = new CloudBackendFragment();
            mProcessingFragment.setRetainInstance(true);
            fragmentTransaction.add(mProcessingFragment, PROCESSING_FRAGMENT_TAG);
        }

        // Add the splash screen fragment
//        mSplashFragment = new SplashFragment();
//        fragmentTransaction.add(R.id.activity_main, mSplashFragment, SPLASH_FRAGMENT_TAG);
        fragmentTransaction.commit();
    }

    private void handleEndpointException(IOException e) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        //mSendBtn.setEnabled(true);
    }


    /**
     * Method called via OnListener in {@link com.google.cloud.backend.core.CloudBackendFragment}.
     */
    @Override
    public void onBroadcastMessageReceived(List<CloudEntity> l) {
        for (CloudEntity e : l) {
            String message = (String) e.get(BROADCAST_PROP_MESSAGE);
            int duration = Integer.parseInt((String) e.get(BROADCAST_PROP_DURATION));
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            Log.i(Consts.TAG, "A message was recieved with content: " + message);
        }
    }

    /**
     * Method called via OnListener in {@link com.google.cloud.backend.core.CloudBackendFragment}.
     */
    @Override
    public void onCreateFinished() {
//        listPosts();
    }
}