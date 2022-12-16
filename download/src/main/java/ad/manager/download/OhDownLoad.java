package ad.manager.download;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class OhDownLoad {
    DownLoadClient downLoadClient;
    private int threadPoolSize = 5;
    ExecutorService executorService = null;
    volatile LinkedList<AbstractDownloadTask> runningTask = new LinkedList<>();
    volatile LinkedList<AbstractDownloadTask> waitingTask = new LinkedList<>();
    private int preThreadPoolSize = 10;
    ExecutorService preExecutorService = null;
    volatile LinkedList<PreDownLoadTask> runningPreTask = new LinkedList<>();
    volatile LinkedList<PreDownLoadTask> waitingPreTask = new LinkedList<>();

    Handler handler = new Handler(Looper.getMainLooper());

    public OhDownLoad(DownLoadClient downLoadClient) {
        this.downLoadClient = downLoadClient;
        executorService = Executors.newFixedThreadPool(threadPoolSize);
        preExecutorService = Executors.newFixedThreadPool(preThreadPoolSize);
    }

    public OhDownLoad(DownLoadClient downLoadClient, int threadPoolSize) {
        this.downLoadClient = downLoadClient;
        this.threadPoolSize = threadPoolSize;
        executorService = Executors.newFixedThreadPool(threadPoolSize);
        preExecutorService = Executors.newFixedThreadPool(preThreadPoolSize);
    }

    public OhDownLoad(DownLoadClient downLoadClient, int threadPoolSize, int preThreadPoolSize) {
        this.downLoadClient = downLoadClient;
        this.threadPoolSize = threadPoolSize;
        this.preThreadPoolSize = preThreadPoolSize;
        executorService = Executors.newFixedThreadPool(threadPoolSize);
        preExecutorService = Executors.newFixedThreadPool(preThreadPoolSize);
    }

    synchronized void downloadTaskStop(AbstractDownloadTask task) {
        removeTask(task);
        handler.post(this::runWaitingTask);
    }

    synchronized void downloadPreTaskStop(PreDownLoadTask task) {
        removePreTask(task);
        handler.post(this::runWaitingPreTask);
    }

    synchronized void addWaitTask(AbstractDownloadTask task){
        waitingTask.add(task);
        task.taskSate=DownloadTask.SATE_NEW;
        handler.post(this::runWaitingTask);
    }
    synchronized void removeWaitTask(AbstractDownloadTask task){
        waitingTask.remove(task);
        task.taskSate=DownloadTask.SATE_STOP;
    }

    /**
     * @param url
     * @param file
     * @param downloadCallBack 除了DownloadCallBack.onProgress不在主线程其他方法均在主线程中运行
     * @return
     */
    public AbstractDownloadTask download(String url, File file, DownloadCallBack downloadCallBack) {
        AbstractDownloadTask task = downLoadClient.createTask(this, url, file, new MainThreadCallBack(downloadCallBack));
        waitingTask.add(task);
        handler.post(this::runWaitingTask);
        return task;
    }

    /**
     * 预下载
     *
     * @param url
     * @param preDownloadListener
     * @return
     */
    public PreDownLoadTask preDownload(String url, PreDownloadListener preDownloadListener) {
        PreDownLoadTask preTask = downLoadClient.createPreTask(this, url, preDownloadListener);
        waitingPreTask.add(preTask);
        handler.post(this::runWaitingPreTask);
        return preTask;
    }


    public List<AbstractDownloadTask> getRunningTask() {
        return new ArrayList<>(runningTask);
    }

    public List<AbstractDownloadTask> getWaitingTask() {
        return new ArrayList<>(waitingTask);
    }

    public List<PreDownLoadTask> getRunningPreTask() {
        return new ArrayList<>(runningPreTask);
    }

    public List<PreDownLoadTask> getWaitingPreTask() {
        return new ArrayList<>(waitingPreTask);
    }

    private synchronized void runWaitingTask() {
        if (runningTask.size() < threadPoolSize) {
            if (waitingTask.size() > 0) {
                AbstractDownloadTask remove = waitingTask.remove(0);
                runningTask.add(remove);
                executorService.execute(remove);
            }

        }
    }

    private synchronized void runWaitingPreTask() {
        if (runningTask.size() < preThreadPoolSize) {
            if (waitingPreTask.size() > 0) {
                PreDownLoadTask remove = waitingPreTask.remove(0);
                runningPreTask.add(remove);
                preExecutorService.execute(remove);
            }

        }
    }


    private void removeTask(AbstractDownloadTask task) {
        runningTask.remove(task);
    }


    private void removePreTask(PreDownLoadTask task) {
        runningPreTask.remove(task);
    }

    private class MainThreadCallBack implements DownLoadListener {
        DownloadCallBack downloadCallBack;

        public MainThreadCallBack(DownloadCallBack downloadCallBack) {
            this.downloadCallBack = downloadCallBack;
        }

        @Override
        public void onStart(AbstractDownloadTask downloadTask) {
            handler.post(() -> {
                downloadCallBack.onStart(downloadTask);
            });
        }

        @Override
        public void onProgress(AbstractDownloadTask downloadTask, long progress, long total) {
            downloadCallBack.onProgress(downloadTask, progress, total);
        }

        @Override
        public void onError(AbstractDownloadTask downloadTask, Exception e) {
            handler.post(() -> {
                downloadCallBack.onError(downloadTask, e);
            });
        }

        @Override
        public void onSuccess(AbstractDownloadTask downloadTask) {
            handler.post(() -> {
                downloadCallBack.onSuccess(downloadTask);
            });
        }

        @Override
        public void onStop(AbstractDownloadTask downloadTask) {
            handler.post(() -> {
                downloadCallBack.onStop(downloadTask);
            });
        }
    }
}
