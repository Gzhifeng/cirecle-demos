package com.zhifeng.iodemo.fileDemos;

import com.zhifeng.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 文件copy
 * @author ganzhifeng
 * @date 2022/3/28
 */
public class FileNioCopy {

    /**
     * copy 文件
     * @param srcPath 原文件
     * @param destPath 目标文件
     */
    public static void nioCopyFile(String srcPath, String destPath) {
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);

        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }

            FileInputStream fis = null;
            FileOutputStream fos = null;
            FileChannel inChannel = null;
            FileChannel outChannel = null;
            long startTime = System.currentTimeMillis();
            try {
                fis = new FileInputStream(srcFile);
                fos = new FileOutputStream(destFile);
                inChannel = fis.getChannel();
                outChannel = fos.getChannel();

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while (inChannel.read(buffer) != -1) {

                    //翻转buf,变成成读模式
                    buffer.flip();
                    int outlength = 0;
                    //将buf写入到输出的通道
                    while ((outlength = outChannel.write(buffer)) != 0) {
                        Logger.debug("写入字节数：" + outlength);
                    }
                    //清除buf,变成写入模式
                    buffer.clear();
                }
                //强制刷新磁盘
                outChannel.force(true);
            } finally {
                outChannel.close();
                fos.close();
                inChannel.close();
                fis.close();
            }
            long endTime = System.currentTimeMillis();
            Logger.debug("base 复制毫秒数：" + (endTime - startTime));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String srcPath = "/Users/ganzhifeng/Documents/《Java开发手册》v1.5.0 华山版.pdf";
        String destPath = "/Users/ganzhifeng/Documents/《Java开发手册》v1.5.0 华山版 copy.pdf";
        nioCopyFile(srcPath, destPath);
    }
}
