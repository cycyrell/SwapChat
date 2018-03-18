package xyz.teamcatalyst.breedr.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import xyz.teamcatalyst.breedr.R;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView signin = (TextView) findViewById(R.id.signin);
        signin.setOnClickListener(v -> {
            Intent it = new Intent(MainActivity.this, ActivitySignin.class);
            startActivity(it);
        });
    }
}
