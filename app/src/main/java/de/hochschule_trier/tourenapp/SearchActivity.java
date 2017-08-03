package de.hochschule_trier.tourenapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener{


    private int radius;
    private EditText radiusText;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        findViewById(R.id.ok_button).setOnClickListener(this);

        radiusText = (EditText) findViewById(R.id.radiusText);
    }


    @Override
    public void onClick(View v){

        switch (v.getId()){

            case R.id.ok_button:

                radius = Integer.parseInt(radiusText.getText().toString());

                Intent intent = new Intent();
                intent.putExtra("Radius", radius);

                setResult(RESULT_OK, intent);
                finish();
                break;

        }

    }
}
