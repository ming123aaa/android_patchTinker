package com.ohuang.hotupdate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends AppCompatActivity {
    public static final String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        TextView viewById = findViewById(R.id.tv_test);
        Log.d(TAG, "onCreate: tv_test=" + viewById + "  id=" + R.id.tv_test);
        viewById.setText("月半猫");
        findViewById(R.id.iv_icon2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TestActivity.this, "月半猫", Toast.LENGTH_LONG).show();
            }
        });

//        ImageView imageView = findViewById(R.id.iv_icon2);
//        Log.d(TAG, "onCreate: iv_icon2="+imageView+"  id="+R.id.iv_icon2);
    }
}