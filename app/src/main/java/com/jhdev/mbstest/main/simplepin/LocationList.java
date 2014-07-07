package com.jhdev.mbstest.main.simplepin;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.analytics.tracking.android.EasyTracker;
import com.jhdev.mbstest.main.R;
import com.jhdev.mbstest.main.core.CloudBackend;
import com.jhdev.mbstest.main.core.CloudBackendFragment;
import com.jhdev.mbstest.main.core.CloudCallbackHandler;
import com.jhdev.mbstest.main.core.CloudEntity;
import com.jhdev.mbstest.main.core.CloudQuery;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class LocationList extends ListActivity
        implements LoaderCallbacks<Cursor>, CloudBackendFragment.OnListener {

    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter mAdapter;

    private FragmentManager mFragmentManager;
    private CloudBackendFragment mProcessingFragment;
    private static final String PROCESSING_FRAGMENT_TAG = "BACKEND_FRAGMENT";

    private ListView mListView;

    private List<CloudEntity> pins = new LinkedList<CloudEntity>();

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);

        mListView = getListView();

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);

//        // For the cursor adapter, specify which columns go into which views
//        String[] fromColumns = {ContactsContract.Data.DISPLAY_NAME};
//        int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1

        // For the cursor adapter, specify which columns go into which views
//        String[] fromColumns = {LocationsContentProvider.FIELD_TITLE, LocationsContentProvider.FIELD_ADDRESS};
//        int[] toViews = {R.id.firstLine, R.id.secondLine}; // The TextView in simple_list_item_1
//
        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
//        mAdapter = new SimpleCursorAdapter(this,
//                R.layout.list_item,
//                null,
//                fromColumns,
//                toViews,
//                0);
//        setListAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
//        getLoaderManager().initLoader(0, null, this);

        mFragmentManager = getFragmentManager();
        initiateFragments();
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
                        pins = results;
                        if (results != null) {
                            Toast.makeText(getBaseContext(), "cq list size= " + results.size(), Toast.LENGTH_SHORT).show();
                        }
                        updateList();
                    }

                    @Override
                    public void onError(IOException exception) {
//                        mAnnounceTxt.setText(R.string.announce_fail);
//                        animateArrival();
//                        handleEndpointException(exception);
                        Toast.makeText(getBaseContext(), "cq list failed", Toast.LENGTH_SHORT).show();

                    }
                };

        // execute the query with the handler
        // TODO watch out for the 50 Limit in place
        mProcessingFragment.getCloudBackend().listByKind(
                "simplepin", CloudEntity.PROP_CREATED_AT, CloudQuery.Order.DESC, 50,
                CloudQuery.Scope.FUTURE_AND_PAST, handler);
    }

    // Loading the data
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 
        // Uri to the content provider LocationsContentProvider
        Uri uri = LocationsContentProvider.CONTENT_URI;
 
        // Fetches all the rows from locations table
        return new CursorLoader(this, uri, null, null, null, null);
    }        
     
    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(arg1);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    @Override 
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Do something when a list item is clicked
     	
    	Intent intent = new Intent(LocationList.this, LocationDetailActivity.class);
    	
    	intent.putExtra("position", position);
    	String ids = String.valueOf(id);
    	intent.putExtra("ids", ids);
    	Log.d("detail tag1", "onListItemClick position=" + position +"\n ID=" + ids + "\n View=" + v + "\n ListView=" + l ); 
    	
    	startActivity(intent);    	    	    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_list_actions, menu);
        return true;
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
    public boolean onOptionsItemSelected (MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.about:
	          // Display 'About' page (activity)
	    Intent opAbout = new Intent(this, About.class);
	    startActivity(opAbout);
	    break;
    	case R.id.delete_all_markers:
    		// Removing all markers from the Google Map
            //googleMap.clear();
//    		TODO problem: when it goes back to the map screen, all the old markers are still on the map but not in the database. 
            // Creating an instance of LocationDeleteTask
            LocationDeleteTask deleteTask = new LocationDeleteTask();
            // Deleting all the rows from SQLite database table
            deleteTask.execute();
            Toast.makeText(getBaseContext(), "All markers are removed", Toast.LENGTH_LONG).show();
            this.finish();
            return true;
    	case android.R.id.home:
    		this.finish();
    	    return true;
    	default:
    	    return super.onOptionsItemSelected(item);
    	}
		return true;
    }
    
    //For Google Analytics
    @Override
    public void onStart() {
      super.onStart();
      // The rest of your onStart() code.
      EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    @Override
    public void onStop() {
      super.onStop();
      // The rest of your onStop() code.
      EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

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
//            String message = (String) e.get(BROADCAST_PROP_MESSAGE);
//            int duration = Integer.parseInt((String) e.get(BROADCAST_PROP_DURATION));
//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//            Log.i(Consts.TAG, "A message was received with content: " + message);
        }
    }

    /**
     * Method called via OnListener in {@link com.google.cloud.backend.core.CloudBackendFragment}.
     */
    @Override
    public void onCreateFinished() {
        listPins();
    }

    private void updateList() {
        Log.d("log", "start updateList" );
        mListView.setAdapter(
                new PinAdapter(this, R.layout.list_item, pins)
        );
    }
    	
}



