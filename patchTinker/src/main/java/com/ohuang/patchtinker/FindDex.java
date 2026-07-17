package com.ohuang.patchtinker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FindDex {

    public static int sortDex(File file) {
        String absolutePath = file.getAbsolutePath();


        List<File> data=new ArrayList<>();
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File child = files[i];
            if (child.isFile()&&child.getName().endsWith(".dex")){
                data.add(child);
            }
        }
        if (data.isEmpty()) {
            return 0;
        }
        List<File> sort=new ArrayList<>();  //重新排序
        for (int i = 0; i < data.size(); i++) {

            File targeFile=new File(absolutePath + "/a_classes" + (i+1) + ".dex");
            if (i==0){
                targeFile=new File(absolutePath + "/a_classes.dex");
            }
            data.get(i).renameTo(targeFile);
            sort.add(targeFile);
        }
        for (int i = 0; i < sort.size(); i++) {
            File file1 = sort.get(i);
            File targeFile=new File(file1.getParentFile(),file1.getName().replace("a_classes","classes"));
            file1.renameTo(targeFile);
        }

        return data.size();
    }

    /**
     *  根据排序遍历
     * @param file
     * @param callBack
     */
    public static void findSortDex(File file, CallBack callBack) {
        String absolutePath = file.getAbsolutePath();
        int num = 0;
        File file1 = new File(absolutePath + "/classes.dex");
        if (!file1.exists()) {
            return;
        } else {
            callBack.call(file1);
        }
        num = 2;
        while (new File(absolutePath + "/classes" + num + ".dex").exists()) {
            callBack.call(new File(absolutePath + "/classes" + num + ".dex"));
            num++;
        }

    }

    public interface CallBack {
        void call(File file);
    }

}
