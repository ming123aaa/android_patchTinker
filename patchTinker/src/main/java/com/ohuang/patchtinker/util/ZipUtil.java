package com.ohuang.patchtinker.util;



import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.*;

public class ZipUtil {

    /**
     * @param apkPath
     * @param outPath
     * @param dir     dir需要加后缀/ "lib/"
     */
    public static void upZipByDir(String apkPath, String outPath, String dir) {
        boolean b = outPath.endsWith("/") || outPath.endsWith("\\");
        if (!b) {
            outPath = outPath + "/";
        }

        File rootFile = new File(outPath);
        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }
        // 要进行解压缩的zip文件
        File zipFile = new File(apkPath);

        // 1.创建解压缩目录
        // 获取zip文件的名称
        String zipFileName = zipFile.getName();

        // 2.解析读取zip文件
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile))) {
            // 遍历zip文件中的每个子文件
            ZipEntry zipEntry = null;
            while ((zipEntry = in.getNextEntry()) != null) {
                // 获取zip压缩包中的子文件名称
                String zipEntryFileName = zipEntry.getName();

                if (zipEntryFileName.startsWith(dir) && !zipEntry.isDirectory()) {
                    String replace = zipEntryFileName.replace(dir, outPath);
                    // 创建解压缩目录
                    File targetDir = new File(replace);
                    if (targetDir.getParentFile() != null) {
                        targetDir.getParentFile().mkdirs(); // 创建目录
                    }
                    // 创建该文件的输出流

                    // 输出流定义在try()块，结束自动清空缓冲区并关闭
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(replace))) {

                        // 读取该子文件的字节内容
                        byte[] buff = new byte[1024];
                        int len = -1;
                        while ((len = in.read(buff)) != -1) {
                            bos.write(buff, 0, len);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void upZipByName(String apkPath, String outPath, String dir) {
        boolean b = outPath.endsWith("/") || outPath.endsWith("\\");
        if (!b) {
            outPath = outPath + "/";
        }


        // 要进行解压缩的zip文件
        File zipFile = new File(apkPath);

        // 1.创建解压缩目录
        // 获取zip文件的名称
        String zipFileName = zipFile.getName();

        // 2.解析读取zip文件
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile))) {
            // 遍历zip文件中的每个子文件
            ZipEntry zipEntry = null;
            while ((zipEntry = in.getNextEntry()) != null) {
                // 获取zip压缩包中的子文件名称
                String zipEntryFileName = zipEntry.getName();

                if (zipEntryFileName.startsWith(dir) && !zipEntry.isDirectory()) {
                    String replace = zipEntryFileName.replace(dir, outPath);
                    // 创建解压缩目录
                    File targetDir = new File(replace);
                    if (targetDir.getParentFile() != null) {
                        targetDir.getParentFile().mkdirs(); // 创建目录
                    }
                    // 创建该文件的输出流

                    // 输出流定义在try()块，结束自动清空缓冲区并关闭
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(replace))) {

                        // 读取该子文件的字节内容
                        byte[] buff = new byte[1024];
                        int len = -1;
                        while ((len = in.read(buff)) != -1) {
                            bos.write(buff, 0, len);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void upZipByZipIntercept(String apkPath, String outPath, ZipIntercept zipIntercept) {
        boolean b = outPath.endsWith("/") || outPath.endsWith("\\");
        if (!b) {
            outPath = outPath + "/";
        }


        // 要进行解压缩的zip文件
        File zipFile = new File(apkPath);

        // 1.创建解压缩目录
        // 获取zip文件的名称
        String zipFileName = zipFile.getName();

        // 2.解析读取zip文件
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile))) {
            // 遍历zip文件中的每个子文件
            ZipEntry zipEntry = null;
            while ((zipEntry = in.getNextEntry()) != null) {
                // 获取zip压缩包中的子文件名称
                String zipEntryFileName = zipEntry.getName();

                if (zipIntercept.isCopy(zipEntryFileName) && !zipEntry.isDirectory()) {
                    String replace = outPath + zipEntryFileName;
                    // 创建解压缩目录
                    File targetDir = new File(replace);
                    if (targetDir.getParentFile() != null) {
                        targetDir.getParentFile().mkdirs(); // 创建目录
                    }
                    // 创建该文件的输出流

                    // 输出流定义在try()块，结束自动清空缓冲区并关闭
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(replace))) {

                        // 读取该子文件的字节内容
                        byte[] buff = new byte[1024];
                        int len = -1;
                        while ((len = in.read(buff)) != -1) {
                            bos.write(buff, 0, len);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String unzip(String zipFilePath, String outDirPath) {
        boolean b = outDirPath.endsWith("/") || outDirPath.endsWith("\\");
        if (!b) {
            outDirPath = outDirPath + "/";
        }
        File rootFile = new File(outDirPath);
        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }
        int BUFFER = 1024;
        String name = "";
        try {
            BufferedOutputStream dest = null;
            BufferedInputStream is = null;
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(zipFilePath);

            Enumeration dir = zipfile.entries();
            while (dir.hasMoreElements()) {
                entry = (ZipEntry) dir.nextElement();

                if (entry.isDirectory()) {
                    name = entry.getName();
                    name = name.substring(0, name.length() - 1);
                    File fileObject = new File(outDirPath + name);
                    fileObject.mkdir();
                }
            }

            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory()) {
                    continue;
                } else {
                    is = new BufferedInputStream(zipfile.getInputStream(entry));
                    int count;
                    byte[] dataByte = new byte[BUFFER];
                    File file = new File(outDirPath + entry.getName());
                    if (file.getParentFile() != null) {
                        file.getParentFile().mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(outDirPath + entry.getName());
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = is.read(dataByte, 0, BUFFER)) != -1) {
                        dest.write(dataByte, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 压缩成ZIP 方法1
     *
     * @param zipFileName      压缩文件夹路径
     * @param sourceFileName   要压缩的文件路径
     * @param KeepDirStructure 是否保留原来的目录结构,true:保留目录结构;
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static Boolean toZip(String zipFileName, String sourceFileName, boolean KeepDirStructure) {
        Boolean result = true;
        ZipOutputStream zos = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(zipFileName);
            zos = new ZipOutputStream(fileOutputStream);
            File sourceFile = new File(sourceFileName);
            compress(sourceFile, zos, sourceFile.getName(), KeepDirStructure, true, new ZipCompressIntercept() {
                @Override
                public boolean canCompress(String name) {
                    return true;
                }
            });
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.getStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 压缩成ZIP 方法1
     *
     * @param zipFileName      压缩文件夹路径
     * @param sourceFileName   要压缩的文件路径
     * @param KeepDirStructure 是否保留原来的目录结构,true:保留目录结构;
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static Boolean toZip(String zipFileName, String sourceFileName, boolean KeepDirStructure, ZipCompressIntercept zipCompressIntercept) {
        Boolean result = true;
        ZipOutputStream zos = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(zipFileName);
            zos = new ZipOutputStream(fileOutputStream);
            File sourceFile = new File(sourceFileName);
            compress(sourceFile, zos, sourceFile.getName(), KeepDirStructure, true, zipCompressIntercept);
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.getStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 压缩成ZIP 方法2  一次性压缩多个文件
     *
     * @param srcFiles    需要压缩的文件列表
     * @param zipFileName 压缩文件输出
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static void toZip(String zipFileName, List<File> srcFiles) throws Exception {

        ZipOutputStream zos = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(zipFileName);
            zos = new ZipOutputStream(fileOutputStream);
            for (File srcFile : srcFiles) {
                compress(srcFile, zos, srcFile.getName(), true, false, new ZipCompressIntercept() {
                    @Override
                    public boolean canCompress(String name) {
                        return true;
                    }
                });
            }


        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 递归压缩方法
     *
     * @param sourceFile       源文件
     * @param zos              zip输出流
     * @param name             压缩后的名称
     * @param KeepDirStructure 是否保留原来的目录结构,true:保留目录结构;
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @param isRoot           是根路径,不创建文件夹
     * @throws Exception
     */
    public static void compress(File sourceFile, ZipOutputStream zos, String name,
                                boolean KeepDirStructure, boolean isRoot, ZipCompressIntercept zipCompressIntercept) throws Exception {

        if (sourceFile.isFile()) {

            ZipEntry zipEntry = new ZipEntry(name);
            if (!zipCompressIntercept.canCompress(name)){
                zipEntry.setMethod(ZipEntry.STORED);
                zipEntry.setCompressedSize(sourceFile.length());
                zipEntry.setSize(sourceFile.length());
                zipEntry.setCrc(getCRC32(sourceFile));
            }
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字

            zos.putNextEntry(zipEntry);
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if (KeepDirStructure) {
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }
            } else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (KeepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        if (isRoot) {
                            compress(file, zos, file.getName(), true, false, zipCompressIntercept);
                        } else {
                            compress(file, zos, name + "/" + file.getName(), true, false, zipCompressIntercept);
                        }
                    } else {
                        compress(file, zos, file.getName(), false, false, zipCompressIntercept);
                    }

                }
            }
        }
    }

    /**
     * 获取文件CRC32校验值
     *
     * @param
     * @return
     */
    public static long getCRC32(File file) {
        long crc32Value = 0L;
        try {
            CRC32 crc32 = new CRC32();
            int fileLen = (int) file.length();
            InputStream in = new FileInputStream(file);
            //分段进行crc校验
            int let = 10 * 1024 * 1024;
            int sum = fileLen / let + 1;
            for (int i = 0; i < sum; i++) {
                if (i == sum - 1) {
                    let = fileLen - (let * (sum - 1));
                }
                byte[] b = new byte[let];
                in.read(b, 0, let);
                crc32.update(b);
            }
            crc32Value = crc32.getValue();
        } catch (Exception e) {
           e.printStackTrace();
        }
        return crc32Value;
    }

    public interface ZipCompressIntercept {
        /**
         * @param name
         * @return true为默认压缩模式  false为存储模式
         */
        boolean canCompress(String name);
    }

    public interface ZipIntercept {

        boolean isCopy(String fileName);
    }


}
