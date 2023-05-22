package com.ohuang.patchtinker.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyFileUtil {

    /**
     * @param sourceRootPath 源文件根路径
     * @param targetRootPath 输出目标文件根路径
     * @param path           路径
     * @param isCover        是否覆盖
     */
    public static void copyPathAllFile(String sourceRootPath, String targetRootPath, String path, boolean isCover) {
        if (path == null) {
            path = "";
        }
        File source = new File(sourceRootPath + path);
        File target = new File(targetRootPath + path);
        if (source.isFile()) {
            copyFile(sourceRootPath, targetRootPath, isCover);
            return;
        }
        if (!target.exists()) {
            target.mkdirs();
        }
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String newFile = path + "/" + file.getName();
                    copyPathAllFile(sourceRootPath, targetRootPath, newFile, isCover);
                } else {
                    copyFile(file.getAbsolutePath(), target.getAbsolutePath() + "/" + file.getName(), isCover);
                }
            }
        }

    }

    public static void copyFile(String sourcePath, String targetPath, boolean isCover) {
        File file = new File(targetPath);
        if (file.getParentFile()!=null){
            file.getParentFile().mkdirs();
        }
        if (!file.exists()){
            copyFile(sourcePath, targetPath);
        }else if (isCover){
            copyFile(sourcePath, targetPath);
        }

    }

    public static void copyFile(String sourcePath, String targetPath){

        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(sourcePath);
            os = new FileOutputStream(targetPath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException ignored) {

        } finally {
            try {
                if (is!=null) {
                    is.close();
                }
                if (os!=null){
                    os.close();
                }
            }catch (Exception ignored){

            }

        }

    }

    /**
     * @param sourceRootPath 源文件根路径
     * @param targetRootPath 输出目标文件根路径
     * @param path           路径
     * @param isCover        是否覆盖
     */
    public static void renamePathAllFile(String sourceRootPath, String targetRootPath, String path, boolean isCover) {
        if (path == null) {
            path = "";
        }
        File source = new File(sourceRootPath + path);
        File target = new File(targetRootPath + path);
        if (source.isFile()) {
            renameFile(sourceRootPath, targetRootPath, isCover);
            return;
        }
        if (!target.exists()) {
            target.mkdirs();
        }
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String newFile = path + "/" + file.getName();
                    renamePathAllFile(sourceRootPath, targetRootPath, newFile, isCover);
                } else {
                    renameFile(file.getAbsolutePath(), target.getAbsolutePath() + "/" + file.getName(), isCover);
                }
            }
        }

    }


    public static void renameFile(String sourcePath, String targetPath, boolean isCover) {
        File file = new File(targetPath);
        if (file.getParentFile()!=null){
            file.getParentFile().mkdirs();
        }
        if (!file.exists()){
            renameFile(sourcePath, targetPath);
        }else if (isCover){
            renameFile(sourcePath, targetPath);
        }
    }

    public static void renameFile(String sourcePath, String targetPath){
        File sourcePathFile = new File(sourcePath);
        File targetPathFile =new File(targetPath);
        if (targetPathFile.exists()){
            targetPathFile.delete();
        }
        if (sourcePathFile.exists()) {
            sourcePathFile.renameTo(targetPathFile);
        }
    }




}
