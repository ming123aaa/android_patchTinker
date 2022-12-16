package ad.manager.download.client;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import ad.manager.download.AbstractDownloadTask;
import ad.manager.download.DownLoadClient;
import ad.manager.download.DownLoadListener;
import ad.manager.download.DownloadTask;
import ad.manager.download.ExceptionCode416;
import ad.manager.download.OhDownLoad;
import ad.manager.download.PreDownLoadTask;
import ad.manager.download.PreDownloadListener;
import ad.manager.download.RemoteFile;
import ad.manager.download.util.CloseableUtil;

public class HttpUrlConnectClient extends DownLoadClient {
    @Override
    public AbstractDownloadTask createTask(OhDownLoad ohDownLoad, String url, File file, DownLoadListener downloadListener) {
        return new HttpUrlConnectDownloadTask(ohDownLoad, url, file, downloadListener);
    }

    @Override
    public PreDownLoadTask createPreTask(OhDownLoad ohDownLoad, String url, PreDownloadListener preDownloadListener) {
        return new HttpUrlConnectPreDownLoadTask(ohDownLoad, url, preDownloadListener);
    }

    private static class HttpUrlConnectPreDownLoadTask extends PreDownLoadTask {

        public HttpUrlConnectPreDownLoadTask(OhDownLoad ohDownLoad, String url, PreDownloadListener preDownloadListener) {
            super(ohDownLoad, url, preDownloadListener);
        }

        @Override
        public void predownload(String url) {
            RemoteFile remoteFile = new RemoteFile(url);
            URL mUrl = null;
            try {
                mUrl = new URL(url);

                HttpURLConnection urlConnection = (HttpURLConnection) mUrl.openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Range", "bytes=" + 0 + "-");
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == 206 || responseCode == 200) {
                    Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
                    remoteFile.setAndDecodeHeaders(headerFields);
                    preDownloadListener.onSuccess(this, remoteFile);
                } else if (responseCode == 416) {
                    preDownloadListener.onError(this, new ExceptionCode416("url="+url), remoteFile);
                } else {
                    preDownloadListener.onError(this, new RuntimeException("responseCode=" + responseCode), remoteFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
                preDownloadListener.onError(this, e, remoteFile);
            }
        }
    }

    private static class HttpUrlConnectDownloadTask extends AbstractDownloadTask {
        public HttpUrlConnectDownloadTask(OhDownLoad ohDownLoad, String url, File file, DownLoadListener loadListener) {
            super(ohDownLoad, url, file, loadListener);
        }


        @Override
        public void download(String url, File file, DownLoadListener downloadListener) {

            InputStream inputStream = null;
            RandomAccessFile randomAccessFile = null;
            try {

                if (!file.exists()) {
                    if (file.getParentFile() != null) {
                        file.getParentFile().mkdirs();
                    }
                    file.createNewFile();
                }
                randomAccessFile = new RandomAccessFile(file, "rw");
                long lastIndex = randomAccessFile.length();
                URL mUrl = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) mUrl.openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Range", "bytes=" + lastIndex + "-");
                int responseCode = urlConnection.getResponseCode();
                long index = lastIndex;
                if (responseCode == 206 || responseCode == 200) {
                    if (responseCode == 200) {
                        index = 0;
                    }
                    inputStream = urlConnection.getInputStream();
                    String contentRange = urlConnection.getHeaderField("Content-Range");
                    if (contentRange != null && contentRange.length() > 0) {
                        String[] strings = contentRange.split("/");
                        if (strings.length > 1) {
                            String string = strings[1];
                            try {
                                totalSize = Long.parseLong(string);
                            } catch (Exception e) {

                            }
                        }
                    }

                    randomAccessFile.seek(index);
                    byte[] bytes = new byte[1024];
                    int i = 0;
                    while ((i = inputStream.read(bytes, 0, 1024)) != -1 && getRunSate() == DownloadTask.SATE_RUN) {
                        randomAccessFile.write(bytes, 0, i);
                        lastIndex += i;
                        currentSize = lastIndex;
                        loadListener.onProgress(this, lastIndex, totalSize);
                    }
                    if (getRunSate() == DownloadTask.SATE_RUN) {
                        loadListener.onSuccess(this);
                    }
                }  else if (responseCode == 416) {
                    loadListener.onError(this, new ExceptionCode416("url="+url));
                }else {
                    loadListener.onError(this, new RuntimeException("responseCode=" + responseCode));
                }

            } catch (IOException e) {
                e.printStackTrace();
                loadListener.onError(this, e);
            } finally {
                CloseableUtil.closeQuietly(randomAccessFile);
                CloseableUtil.closeQuietly(inputStream);
            }
        }
    }
}
