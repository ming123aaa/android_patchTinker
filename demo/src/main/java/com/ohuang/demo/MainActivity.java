package com.ohuang.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_demo);
        findViewById(R.id.btn_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "同志们好", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name="safdnaldnasd";
                Class<?> aClass = null;
                try {
                    aClass = Class.forName(name);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(MainActivity.this,aClass));
            }
        });
    }
}