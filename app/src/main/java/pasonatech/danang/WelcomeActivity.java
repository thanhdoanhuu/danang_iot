package pasonatech.danang;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this,DeviceScanActivity.class);
        startActivity(intent);
    }
}

