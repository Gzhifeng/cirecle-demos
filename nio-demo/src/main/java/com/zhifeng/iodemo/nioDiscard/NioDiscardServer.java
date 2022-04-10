package com.zhifeng.iodemo.nioDiscard;

import com.zhifeng.NioDemoConfig;
import com.zhifeng.util.IOUtil;
import com.zhifeng.util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * 仅读取用户通道的输入数据，读取完毕关闭通道，并抛弃数据
 * @author ganzhifeng
 * @date 2022/4/10
 */
public class NioDiscardServer {

    public static void startServer() throws IOException {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(NioDemoConfig.SOCKET_SERVER_IP, NioDemoConfig.SOCKET_SERVER_PORT));

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        Logger.info("服务器启动成功");
        while (selector.select() > 0) {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(NioDemoConfig.SERVER_BUFFER_SIZE);
                    int length = 0;
                    while ((length = socketChannel.read(buffer)) > 0) {
                        buffer.flip();
                        Logger.info(new String(buffer.array(), 0, length));
                        buffer.clear();
                    }
                    IOUtil.closeQuietly(socketChannel);
                }
            }
            iterator.remove();
        }
        IOUtil.closeQuietly(serverSocketChannel);
    }

    public static void main(String[] args) throws IOException {
        startServer();
    }
}
