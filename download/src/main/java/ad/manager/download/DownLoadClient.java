package ad.manager.download;

import java.io.File;

public abstract class  DownLoadClient {

    public abstract AbstractDownloadTask createTask(OhDownLoad ohDownLoad,String url, File file, DownLoadListener downloadListener);


    public abstract PreDownLoadTask createPreTask(OhDownLoad ohDownLoad,String url, PreDownloadListener preDownloadListener);
}
