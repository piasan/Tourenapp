package de.hochschule_trier.tourenapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import javax.xml.datatype.Duration;


public class CreateNewTourActivity extends AppCompatActivity implements View.OnClickListener {


    //Text fields
    private EditText editTourName;
    private EditText editTourDescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_tour);

        findViewById(R.id.ok_button).setOnClickListener(this);
        findViewById(R.id.cancel_button).setOnClickListener(this);

        editTourName = (EditText) findViewById(R.id.editTourName);
        editTourDescription = (EditText) findViewById(R.id.editDescription);

    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.ok_button:

                String tourName = editTourName.getText().toString();
                String tourDescription = editTourDescription.getText().toString();

                if (tourName.length() < 1) {
                    Toast.makeText(CreateNewTourActivity.this, R.string.no_name, Toast.LENGTH_LONG).show();
                } else if (tourDescription.length() < 1) {
                    Toast.makeText(CreateNewTourActivity.this, R.string.no_description, Toast.LENGTH_LONG).show();
                } else {


                    Intent intent = new Intent();
                    intent.putExtra("TOUR_NAME", tourName);
                    intent.putExtra("TOUR_DESCRIPTION", tourDescription);

                    setResult(RESULT_OK, intent);
                    finish();
                }

                break;


            case R.id.cancel_button:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.cancel_rec_message)
                        .setTitle(R.string.cancel_rec_title);

                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });

                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                break;

        }

    }

}
