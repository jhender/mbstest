package com.jhdev.mbstest.main.simplepin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jhdev.mbstest.main.R;


public class MarkerCreateDialogFragment extends DialogFragment {
    
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface MarkerCreateDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    
    // Use this instance of the interface to deliver action events
    MarkerCreateDialogListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the MarkerCreateDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the MarkerCreateDialogListener so we can send events to the host
            mListener = (MarkerCreateDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement MarkerCreateDialogListener");
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        //// Get layout inflater
        //LayoutInflater inflater = getActivity().getLayoutInflater();
        
        LayoutInflater inflator = LayoutInflater.from(this.getActivity());
        View viewer = inflator.inflate(R.layout.dialog_create_marker, null);
        // Set Texts
        // TODO load actual string from MainActivity
        // newMarkerCompleteAddress and newMarkerLatLng
        TextView tv2 = (TextView)viewer.findViewById(R.id.textView2);
        TextView tv4 = (TextView)viewer.findViewById(R.id.textView4);
        //tv.setText("Some address that I can't load");
        tv2.setText(MainActivity.newMarkerCompleteAddress);
        //String string = newMarkerCompleteAddress;
        tv4.setText(MainActivity.newMarkerLatLng.toString());
        
        
        
        //builder.setView(inflater.inflate(R.layout.dialog_create_marker, null));
        builder.setView(viewer);
        
        
        builder//.setMessage(R.string.marker_create)
        	   .setTitle(R.string.marker_create)
               .setPositiveButton(R.string.marker_create_yes, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Send the positive button event back to the host activity
                       mListener.onDialogPositiveClick(MarkerCreateDialogFragment.this);
                   }
               })
               .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Send the negative button event back to the host activity
                       mListener.onDialogNegativeClick(MarkerCreateDialogFragment.this);
                   }
               });
        return builder.create();
    }
    
    
}