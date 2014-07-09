package com.jhdev.mbstest.main.simplepin;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
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
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.jhdev.mbstest.main.R;
import com.jhdev.mbstest.main.core.CloudBackendFragment;
import com.jhdev.mbstest.main.core.CloudCallbackHandler;
import com.jhdev.mbstest.main.core.CloudEntity;
import com.jhdev.mbstest.main.core.CloudQuery;
import com.jhdev.mbstest.main.core.Consts;
import com.jhdev.mbstest.main.core.Filter;

import java.io.IOException;
import java.util.List;

public class LocationDetailActivity extends Activity implements CloudBackendFragment.OnListener {

	private static final String TAG = "detail tag";
	static String value2 = null;
	static String value1 = null;
	static String id = null;
    static int position = 0;
	static String coortext = null;
    static String titletext = null;
    static String lattext = null;
    static String lngtext = null;
    static String addtext = null;

    private FragmentManager mFragmentManager;
    private CloudBackendFragment mProcessingFragment;

    private static final String PROCESSING_FRAGMENT_TAG = "BACKEND_FRAGMENT";
    private static final String BROADCAST_PROP_MESSAGE = "message";

    private CloudEntity cloudEntity = new CloudEntity("simplepin");

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

            titletext = extras.getString("title");
            addtext = extras.getString("address");
            lattext = extras.getString("lat");
            lngtext = extras.getString("lng");
            id = extras.getString("id");
            position = extras.getInt("position");
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
            	String ids = String.valueOf(id);
            	intent.putExtra("ids", ids);
                intent.putExtra("position", position);
            	Log.e("TAG", "Button ShowInMap clicked. ids=" + ids + " position= " + position );

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
            	String uriString = uriBegin + lattext + "," + lngtext + "(" + titletext + ")" + "&z=16";
            	Uri uri = Uri.parse(uriString);
            	Log.d("uri", "is:" + uri);
            	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            	startActivity(intent);

            }
        });

        //delete button to remove this item.
        final Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //TODO add in toast message that it is destroyed.
                //TODO should we add in a confirmation dialog popup?
                //TODO markers are still showing in the mapview.
//                Intent intentMessage = new Intent();
//                intentMessage.putExtra("MESSAGE", message);
//                setResult(2, intentMessage);

                cloudDelete();
            }
        });

        mFragmentManager = getFragmentManager();
        initiateFragments();
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
        setCE();
    }

    //Retrieve CloudEntity based on ID so that the Delete and Update will work later on.
    private void setCE() {
        CloudCallbackHandler<CloudEntity> handler =
                new CloudCallbackHandler<CloudEntity>() {
            @Override
            public void onComplete(CloudEntity result) {
                if (result == null) {
                    Log.d("retrieved list", "data: is empty");
                    Toast.makeText(getBaseContext(), "Cloud data not retrieved.", Toast.LENGTH_SHORT).show();
                }
                else {
//                    Toast.makeText(getBaseContext(), "Cloud Data updated" + result, Toast.LENGTH_SHORT).show();
                    Log.d("retrieved list", "data: " + result.get("title"));
                    //Set cloudEntity to retrieved Entity
                    cloudEntity = result;
                }

            }

            @Override
            public void onError(final IOException exception) {
                handleEndpointException(exception);
            }
        };

        CloudQuery cq = new CloudQuery("simplepin");
        cq.setLimit(1);
        cq.setFilter(Filter.eq("_id",id));

        cloudEntity.setId(id);
        mProcessingFragment.getCloudBackend().get(cloudEntity, handler);
    }

    // Set status from "active" to "user disabled"
    private void cloudDelete () {
        Log.e("cloudDelete","start");
        cloudEntity.put("status", "user disabled");

        CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
            @Override
            public void onComplete(final CloudEntity result) {
                Toast.makeText(getBaseContext(), "Pin deleted.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(final IOException exception) {
                handleEndpointException(exception);
            }
        };

        mProcessingFragment.getCloudBackend().update(cloudEntity, handler);

        finish();
    }

}
