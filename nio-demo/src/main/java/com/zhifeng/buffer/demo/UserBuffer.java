package com.zhifeng.buffer.demo;

import com.zhifeng.util.Logger;

import java.nio.IntBuffer;

/**
 * 测试Buffer对象
 * @author ganzhifeng
 * @date 2022/3/27
 */
public class UserBuffer {

    static IntBuffer intBuffer = null;

    /**
     * 创建容量为20的buffer对象
     */
    public static void allocateTest() {
        int capacity = 20;
        intBuffer = IntBuffer.allocate(capacity);
        Logger.debug("------------after allocate------------------");
        Logger.debug("position= " + intBuffer.position());
        Logger.debug("limit= " + intBuffer.limit());
        Logger.debug("capacity= " + intBuffer.capacity());
    }

    /**
     * 写入
     */
    public static void putTest() {

        for (int i = 0; i < 5; i++) {
            intBuffer.put(i);
        }

        Logger.debug("------------after putTest------------------");
        Logger.debug("position= " + intBuffer.position());
        Logger.debug("limit= " + intBuffer.limit());
        Logger.debug("capacity= " + intBuffer.capacity());
    }

    /**
     * buffer 模式转换
     */
    public static void flipTest() {
        intBuffer.flip();

        Logger.debug("------------after flipTest------------------");
        Logger.debug("position= " + intBuffer.position());
        Logger.debug("limit= " + intBuffer.limit());
        Logger.debug("capacity= " + intBuffer.capacity());
    }

    /**
     * 读取数据
     */
    public static void getTest() {
//        for (int i = 0; i < intBuffer.limit(); i++) {
//            Logger.debug("i= " + intBuffer.get(i));
//
//            Logger.debug("------------after getTest------------------");
//            Logger.debug("position= " + intBuffer.position());
//            Logger.debug("limit= " + intBuffer.limit());
//            Logger.debug("capacity= " + intBuffer.capacity());
//        }

        Logger.debug("------------after getTest------------------");
        for (int i = 0; i < intBuffer.limit(); i++) {
            Logger.debug("i= " + intBuffer.get());
        }
        Logger.debug("position= " + intBuffer.position());
        Logger.debug("limit= " + intBuffer.limit());
        Logger.debug("capacity= " + intBuffer.capacity());
    }

    /**
     * 倒带
     */
    public static void rewindTest() {
        intBuffer.rewind();
        Logger.debug("------------after rewindTest------------------");
        Logger.debug("position= " + intBuffer.position());
        Logger.debug("limit= " + intBuffer.limit());
        Logger.debug("capacity= " + intBuffer.capacity());
    }

    /**
     * 重复读
     * mark 需要搭配 reset 一块使用
     */
    public static void reRead() {
        Logger.debug("------------after reRead------------------");
        for (int i = 0; i < intBuffer.limit(); i++) {
            if (i == 2) {
                intBuffer.mark();
            }
            Logger.debug("i= " + intBuffer.get());
        }
        Logger.debug("position=" + intBuffer.position());
        Logger.debug("limit=" + intBuffer.limit());
        Logger.debug("capacity=" + intBuffer.capacity());
    }

    /**
     * 从 mark 标记的位置重复读取
     */
    public static void resetTest() {
        intBuffer.reset();
        Logger.debug("------------after resetTest------------------");
        for (int i = intBuffer.position(); i < intBuffer.limit(); i++) {
            Logger.debug("i= " + intBuffer.get());
        }
        Logger.debug("position=" + intBuffer.position());
        Logger.debug("limit=" + intBuffer.limit());
        Logger.debug("capacity=" + intBuffer.capacity());
    }

    /**
     * 在读模式下调用 clear 切换为写模式
     */
    public static void clearTest() {
        intBuffer.clear();

        Logger.debug("------------after clearTest------------------");
        Logger.debug("position=" + intBuffer.position());
        Logger.debug("limit=" + intBuffer.limit());
        Logger.debug("capacity=" + intBuffer.capacity());
    }

    public static void main(String[] args) {
        Logger.debug("分配内存");
        allocateTest();

        Logger.debug("写入");
        putTest();

        Logger.debug("写模式翻转为读模式");
        flipTest();

        Logger.debug("读取");
        getTest();

        Logger.debug("倒带");
        rewindTest();

        Logger.debug("重复读");
        reRead();

        Logger.debug("mark & reset 读写");
        resetTest();

        Logger.debug("清空切换为写模式");
        clearTest();
    }
}
