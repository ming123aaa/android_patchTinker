package com.ohuang.test;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ohuang.patchuptate.Patch;
import com.ohuang.patchuptate.PatchUtil;
import com.ohuang.patchuptate.ResPatch;

import java.io.IOException;

public class StartActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ResPatch.replaceActivityResources(this,ResPatch.sm_resources);
        Toast.makeText(this,"新补丁",Toast.LENGTH_LONG).show();
        setContentView(R.layout.activity_start);
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this,MainActivity.class));
            }
        });
        findViewById(R.id.btn_start2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_patch_apk = StartActivity.this.getFilesDir().getAbsolutePath() + "/app.apk";
                Toast.makeText( StartActivity.this,"开始加载补丁",Toast.LENGTH_LONG).show();
                try {
                    PatchUtil.getInstance().loadPatchApk( StartActivity.this,str_patch_apk);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(StartActivity.this,"加载完成",Toast.LENGTH_LONG).show();
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(StartActivity.this, StartActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        System.exit(0);
                    }
                },1000);

            }
        });
    }


}