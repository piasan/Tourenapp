package de.hochschule_trier.tourenapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StationActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE_CAPTURE = 5;

    private EditText editStationName;
    private EditText editStationDescription;
    private CheckBox missionCheckbox;

    private EditText editQuestion;
    private EditText editAnswer;
    private EditText editAttempts;
    private CheckBox multiCheckbox;

    private TextView addPhoto;
    private ImageView imageView;

    private byte[] imageData;
    private String imageFileName;

    private LinearLayout missionLayout;

    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_station);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        editStationName = (EditText) findViewById(R.id.editStationName);
        editStationDescription = (EditText) findViewById(R.id.editDescription);
        editQuestion = (EditText) findViewById(R.id.missionQuestion);
        editAnswer = (EditText) findViewById(R.id.missionAnswer);
        editAttempts = (EditText) findViewById(R.id.editAttempts);

        missionCheckbox = (CheckBox) findViewById(R.id.missionCheckbox);
        multiCheckbox = (CheckBox) findViewById(R.id.multiCheckbox);

        addPhoto = (TextView) findViewById(R.id.photoText);
        imageView = (ImageView) findViewById(R.id.imageView);

        missionLayout = (LinearLayout) findViewById(R.id.missionLayout);

        missionCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    missionLayout.setVisibility(View.VISIBLE);
                } else {
                    missionLayout.setVisibility(View.GONE);
                }

            }
        });

        //Everytime checkbox or Text for numAttempts are changed, check for conflict
        multiCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && editAttempts.getText().toString().length() > 0)

                    findViewById(R.id.warning).setVisibility(View.VISIBLE);

                else

                    findViewById(R.id.warning).setVisibility(View.GONE);
            }
        });

        editAttempts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && multiCheckbox.isChecked())
                    findViewById(R.id.warning).setVisibility(View.VISIBLE);
                else
                    findViewById(R.id.warning).setVisibility(View.GONE);

            }
        });

        findViewById(R.id.ok_button).setOnClickListener(this);
        findViewById(R.id.cancel_button).setOnClickListener(this);
        addPhoto.setOnClickListener(this);


    }


    // Get message from other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            imageData = baos.toByteArray();

            imageView.setImageBitmap(imageBitmap);

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = timeStamp + "_" + user.getUid() + ".jpg";


        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.photoText:

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                }

                break;

            case R.id.ok_button:

                String stationName = editStationName.getText().toString();
                String stationDescription = editStationDescription.getText().toString();

                if (stationName.length() == 0) {
                    Toast.makeText(this, getResources().getString(R.string.no_name), Toast.LENGTH_SHORT).show();
                } else {

                    boolean mission = false;

                    Intent intent = new Intent();
                    intent.putExtra("STATION_NAME", stationName);
                    intent.putExtra("STATION_DESCRIPTION", stationDescription);

                    if (missionCheckbox.isChecked()) {
                        mission = true;

                        if (editQuestion.getText().toString().length() > 0 && editAnswer.getText().toString().length() > 0) {

                            intent.putExtra("QUESTION", editQuestion.getText().toString());
                            intent.putExtra("ANSWER", editAnswer.getText().toString());

                            intent.putExtra("MULTI", multiCheckbox.isChecked());

                            intent.putExtra("ATTEMPTS", editAttempts.getText().toString());

                        } else {

                            Toast.makeText(this, getResources().getString(R.string.no_mission), Toast.LENGTH_SHORT).show();
                            break;

                        }
                    }

                    if (imageData != null) {
                        UploadTask uploadTask = storageRef.child(imageFileName).putBytes(imageData);
                        uploadTask.addOnFailureListener(new OnFailureListener() {

                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                            }
                        });

                        intent.putExtra("IMAGE", imageFileName);
                    }

                    intent.putExtra("MISSION", mission);


                    setResult(RESULT_OK, intent);
                    finish();
                }

                break;


            case R.id.cancel_button:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.cancel_rec_title);

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
