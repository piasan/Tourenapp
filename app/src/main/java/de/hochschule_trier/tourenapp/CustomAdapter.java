package de.hochschule_trier.tourenapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CustomAdapter extends ArrayAdapter<Tour>{

    private ArrayList<Tour> tour;
    Context mContext;
    private DatabaseReference mDatabase;

    final private String TAG = "CUSTOM_ADAPTER";

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView date;
        RatingBar rating;
    }

    public CustomAdapter(ArrayList<Tour> tour, Context context) {
        super(context, R.layout.row_item, tour);
        this.tour = tour;
        this.mContext=context;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Tour tour = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.date = (TextView) convertView.findViewById(R.id.item_date);
            viewHolder.rating = (RatingBar) convertView.findViewById(R.id.rating);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        // Get an instance of the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();



        viewHolder.name.setText(tour.getTourName());

        Date d = new Date(tour.getTimestamp());
        SimpleDateFormat df = new SimpleDateFormat( "dd.MM.yyyy" );
        viewHolder.date.setText(df.format(d));
        viewHolder.rating.setRating((float)tour.getAverageRating());
        // Return the completed view to render on screen
        return convertView;
    }
}

