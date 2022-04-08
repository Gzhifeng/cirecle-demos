package com.zhifeng.iodemo.socketDemos;

import com.zhifeng.util.IOUtil;
import com.zhifeng.util.Logger;
import com.zhifeng.util.ThreadUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * 使用 socketChannel 传递文件
 * 文件传输客户端
 * @author ganzhifeng
 * @date 2022/4/7
 */
public class NioSendClient {

    /**
     * 构造函数，与服务器建立联系
     */
    public NioSendClient() {
    }

    private Charset charset = Charset.forName("UTF-8");

    /**
     * 向服务器端发送文件
     */
    public void sendFile() {

        try {
            //发送小文件
            String srcPath = "/Users/ganzhifeng/Documents/《Java开发手册》v1.5.0 华山版.pdf";
//        //发送大文件
//        String srcPath = "/Users/ganzhifeng/Downloads/归档.zip";

            File file = new File(srcPath);
            if (!file.exists()) {
                Logger.debug("文件不存在");
            }
            FileChannel fileChannel = new FileInputStream(file).getChannel();
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            socketChannel.socket().connect(new InetSocketAddress("127.0.0.1", 18889));
            socketChannel.configureBlocking(false);

            Logger.debug("Client 成功连接服务端");

            //在非阻塞情况下，与服务器的连接可能还没有真正建立，connect() 方法可能已经返回了
            //因此需要不断的自旋检查是否已经连接到了主机
            while (!socketChannel.isConnected()) {
                //不断自旋等待，或者做一些其他的事情
            }

            //发送文件名
            ByteBuffer filenameByteBuffer = charset.encode(file.getName());
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int fileNameLen = filenameByteBuffer.remaining();
            buffer.clear();
            buffer.putInt(fileNameLen);
            //切换读模式
            buffer.flip();
            socketChannel.write(buffer);
            Logger.info("Client 文件名长度发送完毕：", fileNameLen);

            //发送文件名称
            filenameByteBuffer.flip();
            socketChannel.write(filenameByteBuffer);
            Logger.info("Client 文件名发送成功：", file.getName());

            //发送文件长度
            buffer.clear();
            buffer.putInt((int) file.length());
            //切换读模式
            buffer.flip();
            //写入文件长度
            socketChannel.write(buffer);
            Logger.info("Client 文件长度发送完毕：", file.length());

            //发送文件内容
            Logger.debug("开始传输文件");
            int length = 0;
            long offset = 0;
            buffer.clear();

            while ((length = fileChannel.read(buffer)) > 0) {
                buffer.flip();
                socketChannel.write(buffer);
                offset += length;
                Logger.debug("| " + (100 * offset / file.length()) + "% |");
                buffer.clear();
            }

            //等待一分钟
            ThreadUtil.sleepSeconds(60);

            if (length == -1) {
                IOUtil.closeQuietly(fileChannel);
                //向对方发送结束标志 -1
                socketChannel.shutdownOutput();
                IOUtil.closeQuietly(socketChannel);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 入口
     * @param args
     */
    public static void main(String[] args) {

        // 启动客户端连接
        NioSendClient client = new NioSendClient();
        // 传输文件
        client.sendFile();
    }
}
