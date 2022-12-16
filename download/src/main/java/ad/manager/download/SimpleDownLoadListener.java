package ad.manager.download;

public abstract class SimpleDownLoadListener {

    public void onStart(SimpleDownLoadTask downloadTask) {

    }

    public void onDownLoadStart(SimpleDownLoadTask downloadTask) {
    }

    public void onProgress(SimpleDownLoadTask downloadTask, long progress, long total) {

    }

    public abstract void onError(SimpleDownLoadTask downloadTask, Exception e) ;

    public abstract void onSuccess(SimpleDownLoadTask downloadTask);

    public  void onStop(SimpleDownLoadTask downloadTask){}

}
