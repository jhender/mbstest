package com.jhdev.mbstest.main.simplepin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jhdev.mbstest.main.R;
import com.jhdev.mbstest.main.core.CloudEntity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * This ArrayAdapter uses CloudEntities as items and displays them as a post in
 * the pin list. Layout uses row.xml.
 *
 */
public class PinAdapter extends ArrayAdapter<CloudEntity> {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss ", Locale.US);

    private LayoutInflater mInflater;

    /**
     * Creates a new instance of this adapter.
     *
     * @param context
     * @param textViewResourceId
     * @param objects
     */
    public PinAdapter(Context context, int textViewResourceId, List<CloudEntity> objects) {
        super(context, textViewResourceId, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView != null ?
                convertView : mInflater.inflate(R.layout.list_item, parent, false);

        CloudEntity ce = getItem(position);
        if (ce != null) {
            TextView title = (TextView) view.findViewById(R.id.firstLine);
            TextView address = (TextView) view.findViewById(R.id.secondLine);
            if (title != null) {
                title.setText(ce.get("title").toString());
            }
            if (address != null) {
                address.setText(ce.get("address").toString());
            }
//            if (signature != null) {
//                signature.setText(getAuthor(ce) + " " + SDF.format(ce.getCreatedAt()));
//            }
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return this.getView(position, convertView, parent);
    }

    /**
     * Gets the author field of the CloudEntity.
     *
     * @param post the CloudEntity
     * @return author string
     */
    private String getAuthor(CloudEntity post) {
        if (post.getCreatedBy() != null) {
            return " " + post.getCreatedBy().replaceFirst("@.*", "");
        } else {
            return "<anonymous>";
        }
    }
}
