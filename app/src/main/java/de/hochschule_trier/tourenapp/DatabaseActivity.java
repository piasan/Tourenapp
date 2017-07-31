package de.hochschule_trier.tourenapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DatabaseActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "DatabaseActivity";

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    private ValueEventListener mTourListener;

    //Database Snapshot Array List
    private static ArrayList<Tour> touren;

    private CustomAdapter tourNameAdapter;
    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        // Current Firebase User
        user = FirebaseAuth.getInstance().getCurrentUser();


        // Buttons
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.new_tour_button).setOnClickListener(this);

        touren = new ArrayList<>();

        // Set up ListView and Adapter
        tourNameAdapter = new CustomAdapter(touren, this);

        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(tourNameAdapter);
        listView.setOnItemClickListener(this);


        // Get an instance of the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();


        //Read User Data
               mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Check if current User already exists.
                // If not, create new database entry for this user
                User currentUser = dataSnapshot.child(user.getUid()).getValue(User.class);

                if (currentUser == null) {
                    createNewUser();
                } else {
                    mDatabase.child("Users").child(user.getUid()).child("lastLogin").setValue(System.currentTimeMillis());
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                finish();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();


        // Read from the database
        ValueEventListener tourListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.


                tourNameAdapter.clear();

                /*
                for (DataSnapshot snapshot : dataSnapshot.child("Touren").getChildren()) {
                    Tour tour = snapshot.getValue(Tour.class);
                    touren.add(tour.getTourName());

                }*/

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Tour tour = snapshot.getValue(Tour.class);
                    touren.add(tour);

                }



                tourNameAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                finish();
            }


        };


        mDatabase.child("Touren").addValueEventListener(tourListener);
        mTourListener = tourListener;

    }


    //Add new user data to the database
    private void createNewUser() {

        String name = user.getDisplayName();
        User newUser = new User(name, System.currentTimeMillis());


        mDatabase.child("Users").child(user.getUid()).setValue(newUser);

    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mTourListener != null) {
            mDatabase.removeEventListener(mTourListener);
        }


    }

    // On Click Listener
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.new_tour_button:

                Intent newTourIntent = new Intent(this, RecordTourActivity.class);
                startActivity(newTourIntent);

                break;

            case R.id.sign_out_button:

                Intent signOutIntent = new Intent(this, SignInActivity.class);
                String message = "signout";

                signOutIntent.putExtra("EXTRA_MESSAGE", message);
                startActivity(signOutIntent);
                break;


        }
    }


    //OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Tour tour = tourNameAdapter.getItem(position);
        String tourID = tour.getTourID();
        Log.d(TAG, tourID);

        Intent TourDetailIntent = new Intent(this, TourDetailActivity.class);
        TourDetailIntent.putExtra("TourID", tourID);
        startActivity(TourDetailIntent);
    }

}
