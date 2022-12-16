package ad.manager.download;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

import ad.manager.download.client.HttpUrlConnectClient;

public class DownLoadUtil {
    public static final String TAG = "DownLoadUtil";
    private static OhDownLoad ohDownLoad;

    public static OhDownLoad get() {
        if (ohDownLoad == null) {
            ohDownLoad = new OhDownLoad(new HttpUrlConnectClient(), 10);
        }
        return ohDownLoad;
    }

    /**
     * 下载文件   若当前url正在下载,不会自动调用SimpleDownLoadTask.download方法
     * @param context
     * @param url
     * @param simpleDownLoadListener
     * @return
     */
    public static SimpleDownLoadTask download(Context context, String url, SimpleDownLoadListener simpleDownLoadListener) {

        SimpleDownLoadTask simpleDownLoadTask = new SimpleDownLoadTask(get(), url, simpleDownLoadListener, new Handler(Looper.getMainLooper()), new RemoteFile2File() {
            /**
             * 获取文件后缀类型
             *
             * @param urlFilePath
             * @return
             */
            public String getUrlFileSuffixType(String urlFilePath) {
                return urlFilePath.substring((urlFilePath.lastIndexOf('.')));
            }

            /**
             * @param urlFilePath
             * @return
             */
            public String getUrlFileName(String urlFilePath) {
                String[] split = urlFilePath.split("\\/");
                return split[split.length - 1];
            }


            @Override
            public File remote2File(RemoteFile remoteFile) {
                if (!TextUtils.isEmpty(remoteFile.fileName)) {
                    return new File(context.getCacheDir() + "/" + remoteFile.fileName);
                } else {

                    String urlFileSuffixType = getUrlFileSuffixType(remoteFile.url);
                    if (urlFileSuffixType.length() > 8) {
                        String[] split = remoteFile.contentType.split("/");
                        String s = split[split.length - 1];
                        String fileName = null;

                        if (!TextUtils.isEmpty(s)) {
                            String md5Str = getMD5Str(url);
                            fileName = md5Str + "." + s;
                        }
                        if (fileName != null) {
                            return new File(context.getCacheDir() + "/" + fileName);
                        }
                    } else {
                        String urlFileName = getUrlFileName(remoteFile.url);
                        return new File(context.getCacheDir() + "/" + urlFileName);
                    }

                }
                return null;
            }
        });
        if (isDownload(url)){//若当前url正在下载  直接return  不自动调用download方法
            return simpleDownLoadTask;
        }
        simpleDownLoadTask.download();

        return simpleDownLoadTask;

    }

    /**
     * 当前url是否在下载
     * @param url
     * @return
     */
    public static boolean isDownload(String url) {
        List<AbstractDownloadTask> runningTask = get().getRunningTask();
        List<AbstractDownloadTask> waitingTask = get().getWaitingTask();
        List<PreDownLoadTask> runningPreTask = get().getRunningPreTask();
        List<PreDownLoadTask> waitingPreTask = get().getWaitingPreTask();
        for (AbstractDownloadTask abstractDownloadTask : runningTask) {
            if (abstractDownloadTask.getUrl().equals(url)) {
                return true;
            }
        }
        for (AbstractDownloadTask abstractDownloadTask : waitingTask) {
            if (abstractDownloadTask.getUrl().equals(url)) {
                return true;
            }
        }
        for (PreDownLoadTask abstractDownloadTask : runningPreTask) {
            if (abstractDownloadTask.getUrl().equals(url)) {
                return true;
            }
        }
        for (PreDownLoadTask abstractDownloadTask : waitingPreTask) {
            if (abstractDownloadTask.getUrl().equals(url)) {
                return true;
            }
        }

        return false;
    }

    public static String getMD5Str(String str) {
        byte[] digest = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            digest = md5.digest(str.getBytes("utf-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //16是表示转换为16进制数
        String md5Str = new BigInteger(1, digest).toString(16);
        return md5Str;
    }

}
