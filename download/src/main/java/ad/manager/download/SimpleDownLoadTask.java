package ad.manager.download;

import android.os.Handler;
import android.os.SystemClock;

import java.io.File;

public class SimpleDownLoadTask {
    private SimpleDownLoadListener simpleDownLoadListener;
    private PreDownLoadTask preDownLoadTask;
    private AbstractDownloadTask downloadTask;
    private Handler handler;
    private File file;
    private RemoteFile remoteFile;
    protected OhDownLoad ohDownLoad;
    protected String url;
    private final RemoteFile2File remoteFile2File;
    private boolean isDownloaded=false;

    public SimpleDownLoadTask(OhDownLoad ohDownLoad, String url, SimpleDownLoadListener simpleDownLoadListener, Handler handler,RemoteFile2File remoteFile2File) {
        this.ohDownLoad = ohDownLoad;
        this.url = url;
        this.simpleDownLoadListener = simpleDownLoadListener;
        this.handler = handler;
        this.remoteFile2File=remoteFile2File;
    }

    public void download() {
        if (isDownloaded){
            return;
        }
        isDownloaded=true;
        preDownLoadTask = ohDownLoad.preDownload(url, new PreDownloadListener() {
            @Override
            public void onStart(PreDownLoadTask preDownLoadTask) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        simpleDownLoadListener.onStart(SimpleDownLoadTask.this);
                    }
                });
            }

            @Override
            public void onError(PreDownLoadTask preDownLoadTask, Exception e, RemoteFile remoteFile) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        simpleDownLoadListener.onError(SimpleDownLoadTask.this, e);
                    }
                });
            }

            @Override
            public void onSuccess(PreDownLoadTask preDownLoadTask, RemoteFile remoteFile) {
                SimpleDownLoadTask.this.remoteFile = remoteFile;
                SimpleDownLoadTask.this.file = remoteFile2File.remote2File(remoteFile);
                if (SimpleDownLoadTask.this.file!=null){
                    downloadTask=ohDownLoad.download(url, SimpleDownLoadTask.this.file, new DownloadCallBack() {
                        @Override
                        public void onStart(AbstractDownloadTask downloadTask) {
                            super.onStart(downloadTask);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    simpleDownLoadListener.onDownLoadStart(SimpleDownLoadTask.this);
                                }
                            });
                        }

                        @Override
                        public void onProgress(AbstractDownloadTask downloadTask, long progress, long total) {
                            if (SystemClock.uptimeMillis()-lastUpdateProcessTime>500){
                                handler.post(() -> {
                                    if (downloadTask!=null){
                                        lastUpdateProcessTime= SystemClock.uptimeMillis();
                                        simpleDownLoadListener.onProgress(SimpleDownLoadTask.this,downloadTask.getCurrentSize(),downloadTask.getTotalSize());
                                    }
                                });
                            }else {
                                handler.removeCallbacks(updateProcess);
                                handler.postDelayed(updateProcess,500);
                            }

                        }

                        @Override
                        public void onError(AbstractDownloadTask downloadTask, Exception e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    simpleDownLoadListener.onError(SimpleDownLoadTask.this, e);
                                }
                            });
                        }

                        @Override
                        public void onSuccess(AbstractDownloadTask downloadTask) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    simpleDownLoadListener.onSuccess(SimpleDownLoadTask.this);
                                }
                            });
                        }

                        @Override
                        public void onStop(AbstractDownloadTask downloadTask) {
                            super.onStop(downloadTask);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    simpleDownLoadListener.onStop(SimpleDownLoadTask.this);
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onStop(PreDownLoadTask preDownLoadTask) {
                if (SimpleDownLoadTask.this.downloadTask == null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            simpleDownLoadListener.onStop(SimpleDownLoadTask.this);
                        }
                    });
                }
            }
        });
    }
    private long lastUpdateProcessTime=0;
    private final Runnable updateProcess= () -> {
        if (downloadTask!=null){
            lastUpdateProcessTime= SystemClock.uptimeMillis();
            simpleDownLoadListener.onProgress(this,downloadTask.getCurrentSize(),downloadTask.getTotalSize());
        }
    };

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public boolean isStop(){
        if (downloadTask!=null){
           return downloadTask.isStop();
        }else {
            return preDownLoadTask.isStop();
        }
    }

    public String getUrl() {
        return url;
    }

    public void stop(){
        if (downloadTask!=null){
             downloadTask.stop();
        }else {
             preDownLoadTask.stop();
        }
    }

    public void continueDownload(){
        if (downloadTask!=null){
            downloadTask.continueDownload();
        }else {
            download();
        }
    }

    public File getFile() {
        return file;
    }

    public RemoteFile getRemoteFile() {
        return remoteFile;
    }
}
