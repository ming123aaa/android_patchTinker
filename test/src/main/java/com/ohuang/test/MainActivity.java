package com.ohuang.test;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



import com.ohuang.patchuptate.Patch;
import com.ohuang.patchuptate.ResPatch;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
public static final String TAG="MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate: version="+1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int[] a=new int[]{9,5,7,1,0,56,23,11};
//        OhuangUtil.sort(a);
        Toast.makeText(this, Arrays.toString(a),Toast.LENGTH_LONG).show();




        Log.d(TAG, "onCreate: "+R.id.tv_main);
        TextView viewById = findViewById(R.id.tv_main);
        try {

            String[] list = getAssets().list("");
            viewById.setText(Arrays.toString(list));
        } catch (IOException e) {
            e.printStackTrace();
        }

        findViewById(R.id.tv_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"你點擊了",Toast.LENGTH_LONG).show();
            }
        });

//        getSupportFragmentManager().beginTransaction().add(R.id.fl_main,new BlankFragment()).commit();
    }

    @Override
    public Resources getResources() {
        if (ResPatch.sm_resources!=null){
            return ResPatch.sm_resources;
        }else {
            return super.getResources();
        }
//        return getApplicationContext().getResources();
    }

    @Override
    public AssetManager getAssets() {
        if (ResPatch.sm_resources!=null){
            return ResPatch.sm_resources.getAssets();
        }else {
            return super.getAssets();
        }
//        return getApplicationContext().getAssets();
    }
}