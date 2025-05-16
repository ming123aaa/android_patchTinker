package com.ohuang.hotupdate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.ohuang.patchtinker.PatchTinker;
import com.ohuang.patchtinker.PatchUtil;
import com.ohuang.patchtinker.util.AndroidXmlUtil;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
public static final String TAG="MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate: version="+ PatchTinker.getInstance().getPatchInfo());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int[] a=new int[]{9,5,7,1,0,56,23,11};
//        OhuangUtil.sort(a);


        PatchTinker.getInstance().getPatchTinkerVersion(this);
        findViewById(R.id.a121).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "狗别叫",Toast.LENGTH_LONG).show();
            }
        });

        Log.d(TAG, "onCreate: "+ R.id.tv_main);
        TextView viewById = findViewById(R.id.tv_main);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(MainActivity.this,Class.forName("com.ohuang.hotupdate.TestActivity")));
                    Toast.makeText(MainActivity.this, "gogogo",Toast.LENGTH_LONG).show();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        try {

            String[] list = getAssets().list("");
            if (viewById!=null) {
                viewById.setText(Arrays.toString(list));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



//        getSupportFragmentManager().beginTransaction().add(R.id.fl_main,new BlankFragment()).commit();
    }





}