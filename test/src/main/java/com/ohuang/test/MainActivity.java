package com.ohuang.test;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
            if (viewById!=null) {
                viewById.setText(Arrays.toString(list));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



//        getSupportFragmentManager().beginTransaction().add(R.id.fl_main,new BlankFragment()).commit();
    }





}