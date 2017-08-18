package de.hochschule_trier.tourenapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {


    private int radius;
    private String posOrder;
    private String posAscDesc;

    private EditText radiusText;
    private Spinner orderSpinner;
    private Spinner ascDescSpinner;

    private ArrayList<CheckBox> checkboxes;
    private ArrayList<String> tags;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        findViewById(R.id.search_button).setOnClickListener(this);

        radiusText = (EditText) findViewById(R.id.radiusText);

        addSpinners();

        addCheckboxes();

        Intent intent = getIntent();

        String r = intent.getStringExtra("Radius");
        radiusText.setText(r);
    }


    public void addSpinners() {

        orderSpinner = (Spinner) findViewById(R.id.spOrder);
        ascDescSpinner = (Spinner) findViewById(R.id.spAscDesc);

        final ArrayList<String> orderList = new ArrayList<>();
        final ArrayList<String> ascDescList = new ArrayList<>();

        orderList.add(getResources().getString(R.string.distance));
        orderList.add(getResources().getString(R.string.date));
        orderList.add(getResources().getString(R.string.last_update));
        orderList.add(getResources().getString(R.string.rating));

        ascDescList.add(getResources().getString(R.string.asc));
        ascDescList.add(getResources().getString(R.string.desc));

        ArrayAdapter<String> orderAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, orderList);
        ArrayAdapter<String> AscDescAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, ascDescList);

        orderSpinner.setAdapter(orderAdapter);
        ascDescSpinner.setAdapter(AscDescAdapter);

        posOrder = orderList.get(0);
        posAscDesc = ascDescList.get(0);


        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                posOrder = orderList.get(position);

                if (orderList.get(position).equals(getResources().getString(R.string.rating)))
                    posOrder = "averageRating";
                else if (orderList.get(position).equals(getResources().getString(R.string.date)))
                    posOrder = "date";
                else if (orderList.get(position).equals(getResources().getString(R.string.last_update)))
                    posOrder = "lastUpdate";
                else if (orderList.get(position).equals(getResources().getString(R.string.distance)))
                    posOrder = "tourID";

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ascDescSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0)
                    posAscDesc = "ascending";
                else
                    posAscDesc = "descending";

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void addCheckboxes() {

        String[] tagList = getResources().getStringArray(R.array.tag_list);


        getApplicationContext().setTheme(R.style.AppTheme);
        ViewGroup checkboxContainer = (ViewGroup) findViewById(R.id.checkBoxLayout);

        checkboxes = new ArrayList<>();
        for (int i = 0; i < tagList.length; i++) {
            CheckBox checkbox = new CheckBox(this);
            checkbox.setText(tagList[i]);
            checkboxContainer.addView(checkbox);

            checkboxes.add(checkbox);

        }
    }

    public String getTagName(String checkboxName) {

        if (checkboxName.equals(getResources().getString(R.string.foot)))
            return "foot";
        else if (checkboxName.equals(getResources().getString(R.string.bike)))
            return "bike";
        else if (checkboxName.equals(getResources().getString(R.string.dogs)))
            return "dogs";
        else if (checkboxName.equals(getResources().getString(R.string.wheelchair)))
            return "wheelchair";
        else if (checkboxName.equals(getResources().getString(R.string.flat)))
            return "flat";
        else if (checkboxName.equals(getResources().getString(R.string.multi)))
            return "multi";
        else if (checkboxName.equals(getResources().getString(R.string.games)))
            return "games";
        else if (checkboxName.equals(getResources().getString(R.string.restricted)))
            return "restrictetd";

        else return "";

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.search_button:

                radius = Integer.parseInt(radiusText.getText().toString());

                tags = new ArrayList<>();

                for (int i = 0; i < checkboxes.size(); i++) {
                    if (checkboxes.get(i).isChecked()) {

                        tags.add(getTagName((String) checkboxes.get(i).getText()));

                    }
                }

                Intent intent = new Intent();
                intent.putExtra("Radius", radius);
                intent.putExtra("OrderBy", posOrder);
                intent.putExtra("Direction", posAscDesc);
                intent.putExtra("TAGS", tags);

                setResult(RESULT_OK, intent);
                finish();
                break;

        }

    }

}
