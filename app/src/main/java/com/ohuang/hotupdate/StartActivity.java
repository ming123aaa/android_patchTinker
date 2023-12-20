package com.ohuang.hotupdate;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ohuang.download.DownLoadManager;
import com.ohuang.download.SimpleDownLoadListener;
import com.ohuang.download.SimpleDownLoadTask;
import com.ohuang.hotupdate.processPhoenix.ProcessPhoenix;
import com.ohuang.patchtinker.PatchTinker;
import com.ohuang.patchtinker.PatchUtil;

import java.io.File;
import java.io.IOException;


public class StartActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(getResources().getIdentifier("activity_start", "layout", this.getPackageName()));
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, MainActivity.class));
            }
        });
        findViewById(R.id.btn_start2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText viewById = findViewById(R.id.edit_url);

                DownloadUtil.download(StartActivity.this, viewById.getText().toString(), new SimpleDownLoadListener() {
                    @Override
                    public void onError(SimpleDownLoadTask downloadTask, Exception e) {
                        Toast.makeText(StartActivity.this, "下载失败", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(SimpleDownLoadTask downloadTask) {
                        String str_patch_apk = downloadTask.getFile().getAbsolutePath();
                        Toast.makeText(StartActivity.this, "开始加载补丁", Toast.LENGTH_LONG).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                PatchTinker.getInstance().installPatch(StartActivity.this, str_patch_apk);

                                v.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(StartActivity.this, "加载完成", Toast.LENGTH_LONG).show();

                                    }
                                });
                                new File(str_patch_apk).delete();
                                v.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ProcessPhoenix.triggerRebirth(StartActivity.this);
                                    }
                                }, 1000);
                            }
                        }).start();

                    }
                });


            }
        });

        findViewById(R.id.btn_restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartApp(StartActivity.this);
            }
        });

    }

    private static void restartApp(Context mContext) {

        Intent intent = mContext.getPackageManager()
                .getLaunchIntentForPackage(mContext.getPackageName());
        PendingIntent restartIntent = null;
        if (Build.VERSION.SDK_INT >= 31) {
            restartIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            restartIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        }
        AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
        exitApp();
    }

    public static void exitApp() {

        int id = android.os.Process.myPid();
        if (id != 0) {
            android.os.Process.killProcess(id);
        }

    }


}