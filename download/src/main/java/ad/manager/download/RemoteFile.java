package ad.manager.download;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteFile {

    public RemoteFile(String url) {
        this.url = url;
    }

    public String url = "";


    public long fileSize = 0;


    public String fileName = "";

    public String contentType = "";

    public Map<String, List<String>> headers;

    public void setAndDecodeHeaders(Map<String, List<String>> map) {
        headers = new HashMap<String, List<String>>();
        for (String s : map.keySet()) {
            if (s != null) {
                String key = s.toLowerCase();
                headers.put(key, map.get(s));
            } else {
                headers.put(s, map.get(s));
            }
        }
        decodeHeaders();
    }

    private void decodeHeaders() {
        if (headers != null) {
            decodeFileSize();
            decodeFileName();
            decedeContentType();
        }
    }

    private void decedeContentType() {
        List<String> conent = headers.get("content-type");
        if (conent!=null&&conent.size()>0){
            contentType=conent.get(0);
        }
    }


    private void decodeFileName(){
        List<String> content_disposition = headers.get("content-disposition");
        if (content_disposition!=null) {
            for (String s : content_disposition) {
                String[] split = s.split(";");
                for (String s1 : split) {
                    if (s1.startsWith("filename=")) {
                        String replace = s1.replace("filename=", "");
                        fileName = replace;
                    }
                }
            }
        }
    }

    private void decodeFileSize() {
        List<String> content_length = headers.get("content-length");
        String size = "0";
        if (content_length != null && content_length.size() == 1) {
            size = content_length.get(0);
        } else {
            List<String> contentRange = headers.get("content-range");
            if (contentRange != null && contentRange.size() == 1) {
                String s = contentRange.get(0);
                if (!TextUtils.isEmpty(s)) {
                    String[] strings = s.split("/");
                    if (strings.length > 1) {
                        size = strings[1];
                    }
                }
            }
        }
        try {
            fileSize = Long.parseLong(size);
        } catch (Exception e) {

        }
    }

    @Override
    public String toString() {
        return "RemoteFile{" +
                "url='" + url + '\'' +
                ", fileSize=" + fileSize +
                ", fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}
