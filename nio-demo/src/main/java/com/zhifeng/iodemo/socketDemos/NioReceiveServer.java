package com.zhifeng.iodemo.socketDemos;

import com.zhifeng.util.IOUtil;
import com.zhifeng.util.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 文件传输 server 端
 * @author ganzhifeng
 * @date 2022/4/8
 */
public class NioReceiveServer {

    /**
     * 接受文件的路径
     */
    private static  final String RECEIVE_PATH = "/Users/ganzhifeng/Downloads/test";

    private Charset charset = Charset.forName("UTF-8");

    /**
     * 服务端保存客户端对象，对应一个客户端文件
     */
    static class Session {

        //1. 读取文件长度
        //2. 读取文件名称
        //3. 读取文件内容的长度
        //4. 读取文件的内容
        int step = 1;

        //文件名称
        String fileName = null;

        //长度
        long fileLength;
        int filenameLength;

        //开始传输时间
        long startTime;

        //客户端地址
        InetSocketAddress remoteAddress;

        //输出文件的通道
        FileChannel fileChannel;

        //接收长度
        long receiveLength;

        public boolean isFinished() {
            return receiveLength >= fileLength;
        }
    }

    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    /**
     * 使用Map保存每个客户端传输，当OP_READ通道可读时，根据channel找到对应的对象
     */
    Map<SelectableChannel, Session> clientMap = new HashMap<>();

    public void startServer() throws IOException {

        //1.获取Selector选择器
        Selector selector = Selector.open();

        //2.获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverSocketChannel.socket();

        //3.设置为非阻塞
        serverSocketChannel.configureBlocking(false);

        //4.绑定连接
        InetSocketAddress inetSocketAddress = new InetSocketAddress(18889);
        serverSocket.bind(inetSocketAddress);

        //5.将通道注册到选择器上，并注册的IO事件为："接收新连接"
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        Logger.tcfo("serverChannel is linstening...");

        //6.轮询感兴趣的I/O就绪事件（选择键集合）
        while (selector.select() > 0) {
            if (null == selector.selectedKeys()) {
                continue;
            }

            //7.获取选择键集合
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                //8. 获取单个选择键并处理
                SelectionKey key = it.next();
                if (null == key) { continue; }

                //9. 判断key具体是什么事件，是否为新连接事件
                if (key.isAcceptable()) {
                    //10. 若接受的事件是"新连接"事件，就获取客户端新连接
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = server.accept();
                    if (null == socketChannel) {continue;}
                    //11. 客户端新连接，切换为非阻塞模式
                    socketChannel.configureBlocking(false);
                    socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);

                    //12. 将新客户端通道注册到selector选择器上
                    SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);

                    //余下处理业务
                    Session session = new Session();
                    session.remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                    clientMap.put(socketChannel, session);
                    Logger.debug(socketChannel.getRemoteAddress() + "连接成功...");

                } else if (key.isReadable()) {
                    handleData(key);
                }

                //NIO的特点只会累加，已选择的键的集合不会删除
                //如果不删除，下一次又会被select函数选中
                it.remove();
            }
        }

    }

    /**
     * 处理客户端传输过来的数据
     * @param key
     */
    private void handleData(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int num = 0;
        Session session = clientMap.get(key.channel());
        buffer.clear();
        while ((num = socketChannel.read(buffer)) > 0) {
            Logger.cfo("收到的字节数 = " + num);
            //切换到读模式
            buffer.flip();
            process(session, buffer);
            buffer.clear();
        }
    }

    private void process(Session session, ByteBuffer buffer) throws IOException {
        while (len(buffer) > 0) {
            if (1 == session.step) {
                int filenameLengthByteLen = len(buffer);
                System.out.println("读取文件名称长度之前，可读取的字节数 = " + filenameLengthByteLen);
                System.out.println("读取文件名称长度之前，buffer.remaining() = " + buffer.remaining());
                System.out.println("读取文件名称长度之前，buffer.capacity() = " + buffer.capacity());
                System.out.println("读取文件名称长度之前，buffer.limit() = " + buffer.limit());
                System.out.println("读取文件名称长度之前，buffer.position() = " + buffer.position());

                if (len(buffer) < 4) {
                    Logger.cfo("出现半包问题，需要更加复杂的拆包方案");
                    throw new RuntimeException("出现半包问题，需要更加复杂的拆包方案");
                }

                //获取文件名称长度
                session.filenameLength = buffer.getInt();

                System.out.println("读取文件名称长度之后，buffer.remaining() = " + buffer.remaining());
                System.out.println("读取文件名称长度 = " + session.filenameLength);

                session.step = 2;
            } else if (2 == session.step) {
                Logger.cfo("step 2");

                if (len(buffer) < session.filenameLength) {
                    Logger.cfo("出现半包问题，需要更加复杂的拆包方案");
                    throw new RuntimeException("出现半包问题，需要更加复杂的拆包方案");
                }

                byte[] fileNameBytes = new byte[session.filenameLength];

                //读取文件名称
                buffer.get(fileNameBytes);
                //文件名
                String filename = new String(fileNameBytes, charset);
                System.out.println("读取文件名称 = " + filename);

                File directory = new File(RECEIVE_PATH);
                if (!directory.exists()) {
                    directory.mkdir();
                }
                Logger.info("NIO  传输目标dir：", directory);

                session.fileName = filename;
                String fullName = directory.getAbsolutePath() + File.separatorChar + filename;
                Logger.info("NIO  传输目标文件：", fullName);
                File file = new File(fullName.trim());

                if (!file.exists()) {
                    file.createNewFile();
                }

                FileChannel fileChannel = new FileInputStream(file).getChannel();
                session.fileChannel = fileChannel;
                session.step = 3;
            } else if (3 == session.step) {
                Logger.cfo("step 3");

                //客户端发送过来的，首先处理文件内容长度
                if (len(buffer) < 4) {
                    Logger.cfo("出现半包问题，需要更加复杂的拆包方案");
                    throw new RuntimeException("出现半包问题，需要更加复杂的拆包方案");
                }

                //获取文件内容长度
                session.fileLength = buffer.getInt();

                System.out.println("读取文件内容长度之后，buffer.remaining() = " + buffer.remaining());
                System.out.println("读取文件内容长度 = " + session.fileLength);

                session.step = 4;
                session.startTime = System.currentTimeMillis();
            } else if (4 == session.step) {
                Logger.cfo("step 4");

                //客户端发送过来的，最后是文件内容
                session.receiveLength += len(buffer);
                session.fileChannel.write(buffer);
                if (session.isFinished()) {
                    finished(session);
                }
            }
        }
    }

    private void finished(Session session) {
        IOUtil.closeQuietly(session.fileChannel);
        Logger.info("上传完毕");
        Logger.debug("文件接收成功,File Name：" + session.fileName);
        Logger.debug(" Size：" + IOUtil.getFormatFileSize(session.fileLength));
        long endTime = System.currentTimeMillis();
        Logger.debug("NIO IO 传输毫秒数：" + (endTime - session.startTime));
    }

    private int len(ByteBuffer buffer) {
        Logger.cfo(" >>>  buffer left：" + buffer.remaining());
        return buffer.remaining();
    }

    /**
     * 入口
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        NioReceiveServer server = new NioReceiveServer();
        server.startServer();
    }
}
