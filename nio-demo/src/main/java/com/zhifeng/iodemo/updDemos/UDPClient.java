package com.zhifeng.iodemo.updDemos;

import com.zhifeng.NioDemoConfig;
import com.zhifeng.util.Dateutil;
import com.zhifeng.util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Scanner;

/**
 * udp 客户端
 * @author ganzhifeng
 * @date 2022/4/10
 */
public class UDPClient {

    public void send() throws IOException {
        //操作一：获取DatagramChannel数据报通道
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(false);
        //操作二：输入发送的内容
        ByteBuffer buffer = ByteBuffer.allocate(NioDemoConfig.SEND_BUFFER_SIZE);
        Scanner scanner = new Scanner(System.in);
        Logger.tcfo("UDP 客户端启动成功！");
        Logger.tcfo("请输入发送内容:");
        while (scanner.hasNext()) {
            String next = scanner.next();
            buffer.put((Dateutil.getNow() + " >>" + next).getBytes());
            buffer.flip();
            // 操作三：通过DatagramChannel数据报通道发送数据
            datagramChannel.send(buffer,
                    new InetSocketAddress(NioDemoConfig.SOCKET_SERVER_IP
                            , NioDemoConfig.SOCKET_SERVER_PORT));
            buffer.clear();
        }
        datagramChannel.close();
    }

    public static void main(String[] args) throws IOException {
        new UDPClient().send();
    }

}
