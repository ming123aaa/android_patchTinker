package ad.manager.download;

public interface PreDownloadListener {

    void onStart(PreDownLoadTask preDownLoadTask);

    void onError(PreDownLoadTask preDownLoadTask,Exception e,RemoteFile remoteFile);

    void onSuccess(PreDownLoadTask preDownLoadTask,RemoteFile remoteFile);

    void onStop(PreDownLoadTask preDownLoadTask);
}
