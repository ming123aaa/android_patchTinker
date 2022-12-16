package ad.manager.download;

public interface DownLoadListener {

     void onStart(AbstractDownloadTask downloadTask);

     void onProgress(AbstractDownloadTask downloadTask,long progress,long total);

     void onError(AbstractDownloadTask downloadTask,Exception e);

     void onSuccess(AbstractDownloadTask downloadTask);

     void onStop(AbstractDownloadTask downloadTask);


}
