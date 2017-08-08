package de.hochschule_trier.tourenapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TourDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewTourName;
    private TextView textViewTourDescription;
    private TextView textViewDate;
    private TextView textViewLastUpdate;
    private RatingBar ratingBar;
    private EditText editComment;

    private ArrayList<Waypoint> waypoints;

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private String tourID;


    private boolean WPComplete;

    private static final String TAG = "TourDetailActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_detail);

        Intent intent = getIntent();
        tourID = intent.getStringExtra("TourID");

        textViewTourName = (TextView) findViewById(R.id.textViewTourName);
        textViewTourDescription = (TextView) findViewById(R.id.textViewTourDescription);
        textViewTourDescription.setMovementMethod(new ScrollingMovementMethod());
        textViewDate = (TextView) findViewById(R.id.textDate);
        textViewLastUpdate = (TextView) findViewById(R.id.textUpdate);
        ratingBar = (RatingBar) findViewById(R.id.rating);
        editComment = (EditText) findViewById(R.id.editComment);

        findViewById(R.id.buttonMaps).setOnClickListener(this);
        findViewById(R.id.rate).setOnClickListener(this);
        findViewById(R.id.ok_button).setOnClickListener(this);


        // Get an instance of the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        // Current Firebase User
        user = FirebaseAuth.getInstance().getCurrentUser();


        //Read tour data from the database
        addTourData();

        //Retrieve Waypoint Data
        addWaypointData();

    }


    public void addTourData() {
        mDatabase.child("Touren").child(tourID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Tour tour = dataSnapshot.getValue(Tour.class);
                textViewTourName.setText(tour.getTourName());
                textViewTourDescription.setText(tour.getDescription());

                Date date = new Date(tour.getTimestamp());
                Date update = new Date(tour.getLastUpdate());
                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                textViewDate.setText(df.format(date));
                textViewLastUpdate.setText(df.format(update));


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                finish();
            }
        });

    }

    public void addWaypointData() {

        mDatabase.child("Waypoints").child("Tour" + tourID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                waypoints = new ArrayList<Waypoint>();
                WPComplete = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Waypoint waypoint = snapshot.getValue(Waypoint.class);
                    waypoints.add(waypoint);

                }

                WPComplete = true;

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                finish();
            }
        });

    }

    public void addListenerOnRatingBar() {

        ratingBar = (RatingBar) findViewById(R.id.rating);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {

                Comment comment = new Comment((long) rating, user.getUid());

                mDatabase.child("Comments").child("Tour" + tourID).push().setValue(comment);

            }
        });
    }


    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.buttonMaps:

                if (WPComplete) {

                    WaypointWrapper wrapper = new WaypointWrapper(waypoints);

                    Intent mapsIntent = new Intent(this, MapsActivity.class);
                    mapsIntent.putExtra("WPList", wrapper);
                    startActivity(mapsIntent);
                }
                break;

            case R.id.rate:

                findViewById(R.id.ratingLayout).setVisibility(View.VISIBLE);
                break;

            case R.id.ok_button:

                String text = editComment.getText().toString();
                Long rating = (long) ratingBar.getRating();
                Comment comment;
                Long time;

                if (rating == 0) {

                    Toast.makeText(this, "Rating!", Toast.LENGTH_SHORT).show();

                } else {

                    if (text.length() > 0) {

                        time = System.currentTimeMillis();
                        comment = new Comment(text, rating, time, user.getUid());

                    } else {

                        comment = new Comment(rating, user.getUid());

                    }

                    mDatabase.child("Comments").child("Tour" + tourID).push().setValue(comment);

                    findViewById(R.id.ratingLayout).setVisibility(View.GONE);


                }


                break;

        }


    }
}
