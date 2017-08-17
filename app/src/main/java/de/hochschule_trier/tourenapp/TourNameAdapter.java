package de.hochschule_trier.tourenapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TourNameAdapter extends ArrayAdapter<Tour>{

    Context mContext;


    final private String TAG = "CUSTOM_ADAPTER";

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView date;
        RatingBar rating;
        TextView distance;
    }

    public TourNameAdapter(ArrayList<Tour> tour, Context context) {
        super(context, R.layout.row_item, tour);
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
            viewHolder.distance = (TextView) convertView.findViewById(R.id.item_distance);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }




        viewHolder.name.setText(tour.getTourName());

        Date d = new Date(tour.getTimestamp());
        SimpleDateFormat df = new SimpleDateFormat( "dd.MM.yyyy" );
        viewHolder.date.setText(df.format(d));
        viewHolder.rating.setRating((float)tour.getAverageRating());
        viewHolder.distance.setText(formatDistance(tour.getDistance()));
        // Return the completed view to render on screen
        return convertView;
    }


    public String formatDistance(double distance){

        if (distance < 1000)
            return ((int) distance +  "m");
        else {
            DecimalFormat d = new DecimalFormat("#0.0");
            return (d.format(distance / 1000) + "km");
        }
    }
}

