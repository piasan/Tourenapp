package de.hochschule_trier.tourenapp;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private TextView loadMore;
    private TextView authorName;
    private TextView textViewTags;

    private LinearLayout layout;

    private ArrayList<Waypoint> waypoints;
    private ArrayList<Comment> comments;
    private ArrayList<String> tags;
    private ArrayList<Station> stations;

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private String tourID;

    private Comment comment;
    private Station station;

    private long start = System.currentTimeMillis();
    private int page = 0;


    private boolean WPComplete;

    private static final String TAG = "TourDetailActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_detail);

        Intent intent = getIntent();
        tourID = intent.getStringExtra("TourID");

        textViewTourName = (TextView) findViewById(R.id.textViewTourName);
        authorName = (TextView) findViewById(R.id.author);
        textViewTourDescription = (TextView) findViewById(R.id.textViewTourDescription);
        textViewTourDescription.setMovementMethod(new ScrollingMovementMethod());
        textViewDate = (TextView) findViewById(R.id.textDate);
        textViewLastUpdate = (TextView) findViewById(R.id.textUpdate);
        textViewTags = (TextView) findViewById(R.id.textViewTags);
        ratingBar = (RatingBar) findViewById(R.id.rating);
        editComment = (EditText) findViewById(R.id.editComment);
        loadMore = (TextView) findViewById(R.id.loadMore);

        comments = new ArrayList<>();
        stations = new ArrayList<>();
        layout = (LinearLayout) findViewById(R.id.ratingLayout);

        findViewById(R.id.buttonMaps).setOnClickListener(this);
        findViewById(R.id.rate).setOnClickListener(this);
        findViewById(R.id.ok_button).setOnClickListener(this);
        findViewById(R.id.cancel_button).setOnClickListener(this);
        loadMore.setOnClickListener(this);

        // Get an instance of the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        // Current Firebase User
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Read tour data from the database
        addTourData();

        // Retrieve Waypoint Data
        addWaypointData();

        if (savedInstanceState != null) {

            editComment.setText(savedInstanceState.getString("commentary"));
            if (savedInstanceState.getBoolean("visible"))
                layout.setVisibility(View.VISIBLE);
            start = savedInstanceState.getLong("start");
            page = savedInstanceState.getInt("page");
            comments = savedInstanceState.getParcelableArrayList("comments");

            addComments();

        } else

            // Load Comments from Database
            loadComments();

        loadStations();

    }




    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString("commentary", editComment.getText().toString());
        savedInstanceState.putBoolean("visible", layout.getVisibility() == View.VISIBLE);
        savedInstanceState.putInt("page", page);
        savedInstanceState.putLong("start", start);
        savedInstanceState.putParcelableArrayList("comments", comments);

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


                String tagString = "";
                tags = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.child("Tags").getChildren()) {
                    if (snapshot.getValue(Boolean.class) == true) {

                        String key = snapshot.getKey();

                        switch (key) {
                            case "foot":
                                tags.add(getResources().getString(R.string.foot));
                                break;
                            case "bike":
                                tags.add(getResources().getString(R.string.bike));
                                break;
                            case "dogs":
                                tags.add(getResources().getString(R.string.dogs));
                                break;
                            case "wheelchair":
                                tags.add(getResources().getString(R.string.wheelchair));
                                break;
                            case "flat":
                                tags.add(getResources().getString(R.string.flat));
                                break;
                            case "food":
                                tags.add(getResources().getString(R.string.food));
                                break;
                            case "multi":
                                tags.add(getResources().getString(R.string.multi));
                                break;
                            case "games":
                                tags.add(getResources().getString(R.string.games));
                                break;
                            case "restricted":
                                tags.add(getResources().getString(R.string.restricted));
                                break;

                        }


                        tagString += "#" + tags.get(tags.size() - 1) + " ";

                    }
                }

                textViewTags.setText(tagString);


                mDatabase.child("Users").child(tour.getAuthorName()).child("name").
                        addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String name = dataSnapshot.getValue(String.class);
                                authorName.setText(name);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Failed to read value
                                Log.w(TAG, "Failed to read value.", databaseError.toException());
                                finish();
                            }
                        });


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

                waypoints = new ArrayList<>();
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


    public void loadComments() {

        mDatabase.child("Comments").child("Tour" + tourID).orderByChild("timestamp").startAt(1).endAt(start).
                limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    comment = snapshot.getValue(Comment.class);
                    comments.add(page * 5, comment);

                }


                addComments();

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                finish();
            }
        });


    }

    public void addComments() {

        TextView noComment = (TextView) findViewById(R.id.nocomments);

        LinearLayout listView = (LinearLayout) findViewById(R.id.listLayout);
        listView.removeAllViews();
        LinearLayout hiding = (LinearLayout) findViewById(R.id.hidingLayout);
        getApplicationContext().setTheme(R.style.AppTheme);

        if (comments.size() > 0) {
            hiding.setVisibility(View.VISIBLE);
            noComment.setVisibility(View.GONE);

        } else {

            hiding.setVisibility(View.GONE);
            noComment.setVisibility(View.VISIBLE);

        }

        if (comments.size() < (page + 1) * 5) {
            loadMore.setVisibility(View.GONE);
        }

        for (int i = 0; i < comments.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

            View view = inflater.inflate(R.layout.comment_item, null);

            TextView date = (TextView) view.findViewById(R.id.item_date);
            Date d = new Date(comments.get(i).getTimestamp());
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            date.setText("(" + df.format(d) + ")");
            RatingBar rating = (RatingBar) view.findViewById(R.id.rating);
            rating.setRating(comments.get(i).getRating());
            TextView author = (TextView) view.findViewById(R.id.author);
            author.setText(comments.get(i).getAuthor());
            TextView commentary = (TextView) view.findViewById(R.id.commentary);
            commentary.setText(comments.get(i).getCommentary());

            listView.addView(view);

        }


    }

    public void loadStations(){

        mDatabase.child("Stations").child("Tour" + tourID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    station = snapshot.getValue(Station.class);
                    stations.add(station);

                }

                addStations();

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
                finish();
            }
        });

    }

    public void addStations() {

        TextView noStations = (TextView) findViewById(R.id.noStations);

        LinearLayout stationList = (LinearLayout) findViewById(R.id.stationList);
        stationList.removeAllViews();
        getApplicationContext().setTheme(R.style.AppTheme);

        if (stations.size() > 0) {
            noStations.setVisibility(View.GONE);
        } else
            noStations.setVisibility(View.VISIBLE);


        for (int i = 0; i < stations.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

            View stationView = inflater.inflate(R.layout.station_item, null);

            TextView stationName = (TextView) stationView.findViewById(R.id.stationName);
            stationName.setText(stations.get(i).getName());
            ImageView image = (ImageView) stationView.findViewById(R.id.imageView);


            stationList.addView(stationView);


        }


    }

    public void getAverageRating() {

        mDatabase.child("Comments").child("Tour" + tourID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int counter = 0;
                long sum = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Comment comment = snapshot.getValue(Comment.class);
                    long r = comment.getRating();
                    counter++;

                    sum += r;

                }

                if (counter == 0) {

                    //no ratings yet. Set average rating to 0 in order to avoid dividing by 0
                    mDatabase.child("Touren").child(tourID).child("averageRating").setValue(0);

                } else {

                    double average = (double) sum / (double) counter;
                    mDatabase.child("Touren").child(tourID).child("averageRating").setValue(average);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
                finish();
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

                layout.setVisibility(View.VISIBLE);
                break;

            case R.id.ok_button:

                String text = editComment.getText().toString();
                long rating = (long) ratingBar.getRating();
                Comment comment;
                long time;

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
                    if (text.length() > 0) {
                        comments.add(0, comment);
                        addComments();
                    }

                    getAverageRating();
                    layout.setVisibility(View.GONE);

                }
                break;


            case R.id.cancel_button:

                editComment.setText("");
                ratingBar.setRating(0);
                layout.setVisibility(View.GONE);
                break;


            case R.id.loadMore:

                page++;
                start = comments.get(comments.size() - 1).getTimestamp() - 1;
                loadComments();
                break;

        }


    }
}
