package com.ohuang.hotupdate;

import android.content.Context;

import com.ohuang.download.DownLoadManager;
import com.ohuang.download.OhDownLoad;
import com.ohuang.download.SimpleDownLoadListener;
import com.ohuang.download_httpurlconnect.HttpUrlConnectClient;

public class DownloadUtil {
    public static DownLoadManager downLoadManager;

    public static void download(Context context, String url, SimpleDownLoadListener simpleDownLoadListener) {
        if (downLoadManager == null) {
            downLoadManager = new DownLoadManager(context.getCacheDir() + "/download", new OhDownLoad(new HttpUrlConnectClient()));
        }
        downLoadManager.download(url, simpleDownLoadListener);
    }
}
