package ad.manager.download;

import java.io.File;

public abstract class  PreDownLoadTask extends DownloadTask implements Runnable {
    public int taskSate = DownloadTask.SATE_NEW;


    public PreDownloadListener preDownloadListener;

    public PreDownLoadTask(OhDownLoad ohDownLoad, String url,PreDownloadListener preDownloadListener) {
        super(ohDownLoad, url);
        this.preDownloadListener=preDownloadListener;
    }




    @Override
    public void run() {
        if (taskSate == DownloadTask.SATE_NEW) {
            preDownloadListener.onStart(this);
            taskSate = DownloadTask.SATE_RUN;
            predownload(url);

        }
        taskSate = DownloadTask.SATE_STOP;
        ohDownLoad.downloadPreTaskStop( this);
        preDownloadListener.onStop(this);
    }

    protected abstract void predownload(String url);


}
