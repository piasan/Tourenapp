package de.hochschule_trier.tourenapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CommentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment);

        //Text fields
        final EditText editComment = (EditText) findViewById(R.id.editComment);


        findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String comment = editComment.getText().toString();

                if (comment.length() < 1) {
                    Toast.makeText(CommentActivity.this, R.string.no_comment, Toast.LENGTH_LONG).show();
                } else {

                    Intent intent = new Intent();
                    intent.putExtra("COMMENT", comment);

                    setResult(RESULT_OK, intent);
                    finish();
                }

            }
        });

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

}
