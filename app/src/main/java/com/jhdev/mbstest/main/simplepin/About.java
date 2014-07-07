package com.jhdev.mbstest.main.simplepin;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.jhdev.mbstest.main.R;

public class About extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		// Button click handler
		Button obClose = (Button)findViewById(R.id.buttonClose);
		obClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Close the 'About' page
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

}
