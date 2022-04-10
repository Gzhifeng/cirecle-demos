package com.zhifeng.iodemo.nioDiscard;

import com.zhifeng.NioDemoConfig;
import com.zhifeng.util.IOUtil;
import com.zhifeng.util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 *
 * @author ganzhifeng
 * @date 2022/4/10
 */
public class NioDiscardClient {

    public static void startClient() throws IOException {

        InetSocketAddress address = new InetSocketAddress(NioDemoConfig.SOCKET_SERVER_IP, NioDemoConfig.SOCKET_SERVER_PORT);
        SocketChannel socketChannel = SocketChannel.open(address);
        socketChannel.configureBlocking(false);

        while (!socketChannel.finishConnect()) {

        }

        Logger.info("客户端连接成功");
        ByteBuffer byteBuffer = ByteBuffer.allocate(NioDemoConfig.SEND_BUFFER_SIZE);
        byteBuffer.put("hello world".getBytes());
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        socketChannel.shutdownOutput();
        IOUtil.closeQuietly(socketChannel);

    }

    public static void main(String[] args) throws IOException {
        startClient();
    }
}
