package ad.manager.download;

public abstract class  DownloadCallBack implements DownLoadListener{

    public abstract void onProgress(AbstractDownloadTask downloadTask,long progress,long total);

    public abstract void onError(AbstractDownloadTask downloadTask,Exception e);

    public abstract void onSuccess(AbstractDownloadTask downloadTask);


    public void onStart(AbstractDownloadTask downloadTask){}



    public void onStop(AbstractDownloadTask downloadTask){

    }
}
