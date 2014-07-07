package com.jhdev.mbstest.main.simplepin;

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
import android.widget.ShareActionProvider;
import android.widget.Toast;
import com.google.analytics.tracking.android.EasyTracker;
import com.jhdev.mbstest.main.R;

public class LocationList extends ListActivity
        implements LoaderCallbacks<Cursor> {

    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter mAdapter;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);

//        // For the cursor adapter, specify which columns go into which views
//        String[] fromColumns = {ContactsContract.Data.DISPLAY_NAME};
//        int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {LocationsContentProvider.FIELD_TITLE, LocationsContentProvider.FIELD_ADDRESS};
        int[] toViews = {R.id.firstLine, R.id.secondLine}; // The TextView in simple_list_item_1
        
        
        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(this, 
                R.layout.list_item,
                null,
                fromColumns, 
                toViews, 
                0);
        setListAdapter(mAdapter);
        
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//          super.onCreate(savedInstanceState);
//          Cursor mCursor = getContacts();
//          startManagingCursor(mCursor);
//          // Now create a new list adapter bound to the cursor.
//          // SimpleListAdapter is designed for binding to a Cursor.
//          ListAdapter adapter = new SimpleCursorAdapter(
//        		this, // Context.
//              android.R.layout.two_line_list_item, // Specify the row template to use (here, two columns bound to the two retrieved cursor rows).
//              mCursor, 					// Pass in the cursor to bind to. Array of cursor columns to bind to.
//              new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME },
//              					// Parallel array of which template objects to bind to those columns.
//              new int[] { android.R.id.text1, android.R.id.text2 });
//
//          // Bind to our new adapter.
//          setListAdapter(adapter);
//        }
               

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }
  
    // Loading the data
    @Override
    public Loader<Cursor> onCreateLoader(int arg0,
        Bundle arg1) {
 
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
    
   
    
    	
}



