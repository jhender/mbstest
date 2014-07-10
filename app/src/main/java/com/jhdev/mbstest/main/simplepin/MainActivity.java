package com.jhdev.mbstest.main.simplepin;
 
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import com.jhdev.mbstest.main.core.CloudBackendFragment;
import com.jhdev.mbstest.main.core.CloudBackendFragment.OnListener;
import com.jhdev.mbstest.main.core.CloudCallbackHandler;
import com.jhdev.mbstest.main.core.CloudEntity;
import com.jhdev.mbstest.main.core.CloudQuery;
import com.jhdev.mbstest.main.core.Consts;
import com.jhdev.mbstest.main.core.Filter;

public class MainActivity extends FragmentActivity 
	implements
//        LoaderCallbacks<Cursor>,
        MarkerCreateDialogFragment.MarkerCreateDialogListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        OnListener {

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

    private static final String PROCESSING_FRAGMENT_TAG = "BACKEND_FRAGMENT";
    private static final String BROADCAST_PROP_MESSAGE = "message";

    private List<CloudEntity> masterPinList = new LinkedList<CloudEntity>();

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

            // Getting GoogleMap object from the fragment
            googleMap = fm.getMap();
   
            // Enabling MyLocation Layer of Google Map
            // disabled this feature because it keeps GPS on
            googleMap.setMyLocationEnabled(true);
            
			// Invoke LoaderCallbacks to retrieve and draw already saved locations in map
//            getSupportLoaderManager().initLoader(0, null, this);
            
            //centers the map somewhere
            if (lastMarkerLatLng == null) {
	            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(37.809,-122.476));
	            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
	            googleMap.moveCamera(center);
	            googleMap.animateCamera(zoom);
            }
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

//        updatePinsOnMap();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	String id = extras.getString("ids");

        	if (id != null){
        		Log.e("Intent", "Intent received, id=" + id);

                CloudEntity ce = masterPinList.get(extras.getInt("position"));
                Double lat = Double.parseDouble(ce.get("latitude").toString());
                Double lng = Double.parseDouble(ce.get("longitude").toString());
                LatLng markerLatLng = new LatLng(lat, lng);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,15));
        	}        	
        }
    }


//    private class LocationInsertTask extends AsyncTask<ContentValues, Void, Void>{
//        @Override
//        protected Void doInBackground(ContentValues... contentValues) {
//
//            /** Setting up values to insert the clicked location into SQLite database */
//            getContentResolver().insert(LocationsContentProvider.CONTENT_URI, contentValues[0]);
//            return null;
//        }
//    }
//
//    private class LocationDeleteTask extends AsyncTask<Void, Void, Void>{
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            /** Deleting all the locations stored in SQLite database */
//            getContentResolver().delete(LocationsContentProvider.CONTENT_URI, null, null);
//            return null;
//        }
//    }
//
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
 
//    @Override
//    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
//
////        // Uri to the content provider LocationsContentProvider
////        Uri uri = LocationsContentProvider.CONTENT_URI;
////
////        // Fetches all the rows from locations table
////        return new CursorLoader(this, uri, null, null, null, null);
//        return null;
//    }

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

        // New Cloud Backend AppEngine Entity
        CloudEntity ce = new CloudEntity("simplepin");
        ce.put("title", newMarkerTitle);
        ce.put("address", newMarkerCompleteAddress);
        ce.put("latitude", newMarkerLatLng.latitude);
        ce.put("longitude", newMarkerLatLng.longitude);
        ce.put("status", "active");

        CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
            @Override
            public void onComplete(final CloudEntity result) {
//                mPosts.add(0, result);
//                updateGuestbookView();
//                mMessageTxt.setText("");
//                mMessageTxt.setEnabled(true);
//                mSendBtn.setEnabled(true);
                Toast.makeText(getBaseContext(), "Marker saved to Cloud data", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(final IOException exception) {
                handleEndpointException(exception);
            }
        };

        // execute the cloud insertion with the handler
        mProcessingFragment.getCloudBackend().insert(ce, handler);
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
        Log.i("MarkerCrateAlertDialog", "Negative click!");
	}

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
    	//Toast.makeText(getBaseContext(), "Selected", Toast.LENGTH_SHORT).show();    	
    	switch (item.getItemId()) {
    	
    	case R.id.delete_all_markers:
//    		// Removing all markers from the Google Map
//            googleMap.clear();
//
//            // Creating an instance of LocationDeleteTask
//            LocationDeleteTask deleteTask = new LocationDeleteTask();
//
//            // Deleting all the rows from SQLite database table
//            deleteTask.execute();
//
//            Toast.makeText(getBaseContext(), "All markers are removed", Toast.LENGTH_LONG).show();
//            return true;
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
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
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
     * Method called via OnListener in {@link com.jhdev.mbstest.main.core.CloudBackendFragment}.
     */
    @Override
    public void onBroadcastMessageReceived(List<CloudEntity> l) {
        for (CloudEntity e : l) {
            String message = (String) e.get(BROADCAST_PROP_MESSAGE);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            Log.i(Consts.TAG, "A message was received with content: " + message);
        }
    }

    /**
     * Method called via OnListener in {@link com.jhdev.mbstest.main.core.CloudBackendFragment}.
     */
    @Override
    public void onCreateFinished() {
        listPins();
    }

    private void listPins() {
        // create a response handler that will receive the result or an error
        CloudCallbackHandler<List<CloudEntity>> handler =
                new CloudCallbackHandler<List<CloudEntity>>() {
                    @Override
                    public void onComplete(List<CloudEntity> results) {
//                        mAnnounceTxt.setText(R.string.announce_success);
//                        mPosts = results;
//                        animateArrival();
//                        updateGuestbookView();
                        masterPinList = results;
                        if (results != null) {
//                            Toast.makeText(getBaseContext(), "cq list size= " + results.size(), Toast.LENGTH_SHORT).show();
                            Log.d("simplepin","cq list size=" + results.size());
                        }
                        updatePinsOnMap();
                    }

                    @Override
                    public void onError(IOException exception) {
//                        mAnnounceTxt.setText(R.string.announce_fail);
//                        animateArrival();
                        handleEndpointException(exception);
                    }
                };

//        mProcessingFragment.getCloudBackend().listByKind(
//                "simplepin", CloudEntity.PROP_CREATED_AT, CloudQuery.Order.DESC, 50,
//                CloudQuery.Scope.FUTURE_AND_PAST, handler);

        CloudQuery cq = new CloudQuery("simplepin");
        cq.setScope(CloudQuery.Scope.PAST);
        cq.setFilter(Filter.eq("status", "active"));
//        cq.setSort("status", CloudQuery.Order.DESC);
//        cq.setSort(CloudEntity.PROP_CREATED_AT, CloudQuery.Order.DESC);

        mProcessingFragment.getCloudBackend().list(cq, handler);



    }

    private void updatePinsOnMap (){

        double lat = 0;
        double lng = 0;

        for (int i=0; i< masterPinList.size(); i++) {
            CloudEntity ce = masterPinList.get(i);

            lat = Double.parseDouble(ce.get("latitude").toString());
            lng = Double.parseDouble(ce.get("longitude").toString());
            LatLng location = new LatLng(lat, lng);

            String title;
            if (ce.get("title") == "") {
                title = "pin";
            } else { title = ce.get("title").toString(); }

            String addr = ce.get("address").toString();

            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(addr)
                    .snippet(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            marker.showInfoWindow();
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng),14));
    }


}
