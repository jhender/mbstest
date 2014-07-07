package com.jhdev.mbstest.main.simplepin;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;
import com.jhdev.mbstest.main.R;

public class LocationDetailActivity extends Activity {

	private static final String TAG = "detail tag";
	static String value2 = null;
	static String value1 = null;
	static double lat = 0;
	static double lng = 0;
	static long id = 0;
	static String coortext = null;
    static String titletext = null;
    static String lattext = null;
    static String lngtext = null;
    static String addtext = null;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        //ViewGroup root = (ViewGroup) findViewById(R.id.location_detail);

		setContentView(R.layout.location_detail);
        TextView title = (TextView) findViewById(R.id.textView1);
        TextView add = (TextView) findViewById(R.id.textView3);
        	//allow links to be clickable
        	add.setMovementMethod(LinkMovementMethod.getInstance());
        TextView coor = (TextView) findViewById(R.id.textView4);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
            }
        // Get data via the key
        value1 = extras.getString("title");
        value2 = extras.getString("id");

        // value1 is from mapview after marker is clicked.
        if (value1 != null) {
	    	Log.d(TAG, "getExtra value1: " + value1 );

	    	//TODO add in code to search content resolver using title?, or find a better ID from marker
        }
        // value2 is from listview.
        if (value2 != null) {
	    	Log.d(TAG, "getExtra value2: " + value2 );

//			ContentResolver content = getContentResolver();
//			Uri uri = Uri.withAppendedPath(LocationsContentProvider.CONTENT_URI, value2);
//			Cursor cursor = content.query(uri, null, null, null, null);
//			cursor.moveToFirst();
//			id = cursor.getLong(0);
//			titletext = cursor.getString(6);
//			addtext = cursor.getString(4);
//			lattext = cursor.getString(1);
//			lngtext = cursor.getString(2);
//			coortext = lattext + ", " + lngtext;
            titletext = extras.getString("title");
            addtext = extras.getString("address");
            lattext = extras.getString("lat");
            lngtext = extras.getString("lng");
            id = extras.getLong("id");
            coortext = lattext + ", " + lngtext;

		    title.setText(titletext);
		    add.setText(addtext);
		    coor.setText(coortext);
        }

        //show in map button
        final Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            	Intent intent = new Intent(LocationDetailActivity.this, MainActivity.class);
            	//String title = String.valueOf(marker.getTitle());
            	String ids = String.valueOf(id);
            	intent.putExtra("ids", ids);
            	Log.d("TAG", "Button showinmap clicked. ids=" + ids );

            	startActivity(intent);
            }
        });

        //share button
        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
    			//send coordinates
            	//geo: url or maps.google.com url both should work
            	//String uriBegin = "geo:" + lattext + "," + lngtext;
            	String uriBegin = "https://maps.google.com/maps?q=loc:";
            	String query = lattext + "," + lngtext + "(" + titletext + ")";
            	String encodedQuery = Uri.encode(query);
            	//String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
            	String uriString = uriBegin + lattext + "," + lngtext + "(" + titletext + ")" + "&z=16";
            	Uri uri = Uri.parse(uriString);
            	Log.d("uri", "is:" + uri);
            	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            	startActivity(intent);

            }
        });

        //delete button
        final Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform delete on click
    			Uri uri = Uri.withAppendedPath(LocationsContentProvider.CONTENT_URI, value2);
                /** Deleting all the locations stored in SQLite database */
                getContentResolver().delete(uri, null, null);

                //TODO add in toast message that it is destroyed.
                //TODO should we add in a confirmation dialog popup?
                //TODO markers are still showing in the mapview.
//                Intent intentMessage = new Intent();
//                intentMessage.putExtra("MESSAGE", message);
//                setResult(2, intentMessage);
                finish();

            }
        });


    // end onCreate
	}

	// For Sharing Intent via Action Bar.
	@Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.activity_location_detail_actions, menu);
	    Log.d("shareintent", "1");

	    return true;
	  }

	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_item_share:

		    // populate the share intent with data
		    Intent intent = new Intent(Intent.ACTION_SEND);
		    intent.setType("text/plain");
		    StringBuilder sb = new StringBuilder(titletext);
		    sb.append(addtext);
		    sb.append("\n");
		    sb.append("Saved with SimplePin app");
		    intent.putExtra(Intent.EXTRA_TEXT, sb.toString() );
		    Log.d("shareintent", "4" + intent);
	        startActivity(Intent.createChooser(intent, "Send to"));
	        break;
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;

	    default:
	      break;
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
