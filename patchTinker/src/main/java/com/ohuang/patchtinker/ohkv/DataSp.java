package com.ohuang.patchtinker.ohkv;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class DataSp {

    static final String Path = "/mydataSp";

    static void saveData(Context context, String dataName, JSONObject data) {
        String absolutePath = context.getFilesDir().getAbsolutePath();
        String rootPath = absolutePath + Path;
        String myPath = rootPath + "/" + dataName + ".txt";
        String s = data.toString();

        try {
            writeText(myPath,s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static JSONObject readData(Context context, String dataName) {
        String absolutePath = context.getFilesDir().getAbsolutePath();
        String rootPath = absolutePath + Path;
        String myPath = rootPath + "/" + dataName + ".txt";
        try {
            String s = readText(myPath);
            if (s!=null){
                return new JSONObject(s);
            }
        } catch (Exception ignored) {

        }
        return new JSONObject();
    }


    private static void writeText(String path, String content) throws IOException {
        File file = new File(path);
        if (file.getParentFile()!=null&&!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
        fileOutputStream.close();
    }

    private static String readText(String path) throws FileNotFoundException {
        File file = new File(path);
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            reader.close();
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

}
