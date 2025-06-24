package com.pxp.learn.basics.other;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author pxp
 * @description
 */
public class TestJava {
    @Test
    public static void main(String[] args) {
        //1. 分配容量为10 ，默认是 HeapByteBuffer
//        ByteBuffer buffer = ByteBuffer.allocate(10);
        ByteBuffer buffer = ByteBuffer.allocateDirect(10);//创建直接内存缓存
        //2. 写入4个字节
        buffer.put(new byte[]{10, 2, 3, 4});
        System.out.println(String.format("写模式下:pos=%d, limit=%d, capacity=%d", buffer.position(), buffer.limit(), buffer.capacity()));
        System.out.println("切换为读模式");
        //3. 切换为读模式
        buffer.flip();
        System.out.println(String.format("读模式下:pos=%d, limit=%d, capacity=%d", buffer.position(), buffer.limit(), buffer.capacity()));
        System.out.println("读取数据");
        //4. 读取数据
        while (buffer.hasRemaining()) {
            System.out.println(buffer.get());
        }
        System.out.println(String.format("读取数据后:pos=%d, limit=%d, capacity=%d", buffer.position(), buffer.limit(), buffer.capacity()));
        System.out.println("设置pos=1");
        //5. 重新设置pos=1
        buffer.position(1);
        //6. 调用compact 进行数据压缩
        buffer.compact();
        System.out.println(String.format("compact后:pos=%d, limit=%d, capacity=%d", buffer.position(), buffer.limit(), buffer.capacity()));
        while (buffer.hasRemaining()) {
            System.out.println(buffer.get());
        }
        buffer.flip();
        //7. 测试mark & reset
        System.out.println("测试mark&rest 读取一个数据");
        System.out.println(buffer.get());
        System.out.println("mark的pos=" + buffer.position());
        buffer.mark();
        //读完剩下的内容
        while (buffer.hasRemaining()) {
            System.out.println(buffer.get());
        }
        System.out.println("reset前的pos=" + buffer.position());
        //重置标识
        buffer.reset();
        System.out.println("reset后的pos=" + buffer.position());
    }
}
