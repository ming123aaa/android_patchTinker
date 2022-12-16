package ad.manager.download;

import java.io.File;
import java.util.Map;

public abstract class AbstractDownloadTask extends DownloadTask implements Runnable {
    public File file;
    protected DownLoadListener loadListener;

    protected long totalSize;//总文件大小
    protected long currentSize;//当前下载已文件大小

    public AbstractDownloadTask(OhDownLoad ohDownLoad, String url, File file, DownLoadListener loadListener) {
        super(ohDownLoad, url);
        this.loadListener = loadListener;
        this.file = file;
    }

    /**
     * 继续下载
     */
    public void continueDownload() {
        if (taskSate == DownloadTask.SATE_STOP) {
            ohDownLoad.addWaitTask(this);
        }else if (taskSate ==DownloadTask.SATE_WAIT_STOP){
            taskSate=DownloadTask.SATE_NEW;
        }
    }

    @Override
    public void run() {
        if (taskSate == DownloadTask.SATE_NEW) {
            try {
                taskSate = DownloadTask.SATE_START;
                loadListener.onStart(this);
                taskSate = DownloadTask.SATE_RUN;
                download(url, file, loadListener);
            } catch (Exception ignored) {
            }
        }
        ohDownLoad.downloadTaskStop(this);
        if (taskSate==DownloadTask.SATE_NEW) {
            ohDownLoad.addWaitTask(this);
        }else {
            taskSate = DownloadTask.SATE_STOP;
        }
        loadListener.onStop(this);
    }

    @Override
    public void stop() {
        super.stop();
        if (taskSate==DownloadTask.SATE_NEW){
            ohDownLoad.removeWaitTask(this);
        }
    }

    protected abstract void download(String url, File file, DownLoadListener downloadListener);

    public long getTotalSize() {
        return totalSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }
}
