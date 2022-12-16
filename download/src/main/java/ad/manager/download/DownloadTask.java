package ad.manager.download;

import java.io.File;

public abstract class DownloadTask {
    public static final int SATE_NEW = 0;
    public static final int SATE_RUN = 1;
    public static final int SATE_STOP = 2;
    public static final int SATE_WAIT_STOP = 3;
    public static final int SATE_START = 4;


    protected volatile int taskSate = DownloadTask.SATE_NEW;

    protected OhDownLoad ohDownLoad;
    protected String url;


    public DownloadTask(OhDownLoad ohDownLoad, String url) {
        this.ohDownLoad = ohDownLoad;
        this.url = url;


    }

    public String getUrl() {
        return url;
    }

    public int getRunSate() {
        return taskSate;
    }

    public boolean isStop(){
        return (taskSate == DownloadTask.SATE_WAIT_STOP)||(taskSate == DownloadTask.SATE_STOP);
    }


    public void stop() {
        if (taskSate==DownloadTask.SATE_NEW||taskSate==DownloadTask.SATE_STOP){
            taskSate=DownloadTask.SATE_STOP;
        }else {
            taskSate = DownloadTask.SATE_WAIT_STOP;
        }
    }
}
